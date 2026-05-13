package com.example.bijlibachat.service;

import com.example.bijlibachat.model.TariffRate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TariffSyncService {
    private static final String SNAPSHOT_RESOURCE = "/com/example/bijlibachat/tariffs/nepra-domestic-unprotected.properties";
    private static final Path CACHE_PATH = Paths.get(System.getProperty("user.home"), ".bijli-bachat", "nepra-domestic-unprotected.properties");
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final Duration MEMORY_TTL = Duration.ofMinutes(10);
    private static final String DEFAULT_SOURCE_URL =
            "https://www.nepra.org.pk/tariff/Tariff/Ex-WAPDA%20DISCOS/2026/TRF-100%20XWDISCOS%20AND%20KE%20RATIONALIZATION%20OF%20TARIFF%2011-02-2026%202935-57.pdf";
    private static final String DEFAULT_SOURCE_LABEL =
            "NEPRA residential unprotected tariff (XWDISCOs & K-Electric)";
    private static final String DEFAULT_EFFECTIVE_DATE = "2026-02-11";
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_INSTANT;
    private static final Pattern HREF_PATTERN = Pattern.compile("href\\s*=\\s*\"([^\"]+\\.pdf)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2})-(\\d{2})-(\\d{4})");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("\\d+\\.\\d+");
    private static final String[] SLAB_LABELS = {
            "01-100 Units",
            "101-200 Units",
            "201-300 Units",
            "301-400 Units",
            "401-500 Units",
            "501-600 Units",
            "601-700 Units",
            "Above 700 Units"
    };
    private static final int[] MIN_UNITS = {1, 101, 201, 301, 401, 501, 601, 701};
    private static final int[] MAX_UNITS = {100, 200, 300, 400, 500, 600, 700, -1};
    private static final float[] DEFAULT_FIXED_CHARGES = {0f, 0f, 0f, 200f, 400f, 600f, 800f, 1000f};

    private static final Object LOCK = new Object();
    private static volatile TariffCatalog memoryCatalog;
    private static volatile Instant memoryLoadedAt;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public TariffCatalog getCatalog() {
        Instant now = Instant.now();
        TariffCatalog cached = memoryCatalog;
        if (cached != null && memoryLoadedAt != null && now.isBefore(memoryLoadedAt.plus(MEMORY_TTL))) {
            return cached;
        }

        synchronized (LOCK) {
            cached = memoryCatalog;
            if (cached != null && memoryLoadedAt != null && now.isBefore(memoryLoadedAt.plus(MEMORY_TTL))) {
                return cached;
            }

            TariffCatalog catalog = loadBestAvailable(false);
            memoryCatalog = catalog;
            memoryLoadedAt = now;
            return catalog;
        }
    }

    public TariffCatalog forceRefresh() {
        synchronized (LOCK) {
            TariffCatalog catalog = loadBestAvailable(true);
            memoryCatalog = catalog;
            memoryLoadedAt = Instant.now();
            return catalog;
        }
    }

    private TariffCatalog loadBestAvailable(boolean forceRefresh) {
        try {
            if (!forceRefresh && Files.exists(CACHE_PATH)) {
                TariffCatalog cached = loadFromPath(CACHE_PATH);
                if (!isStale(cached)) {
                    return cached;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            TariffCatalog refreshed = refreshFromRemote();
            saveToCache(refreshed);
            return refreshed;
        } catch (Exception ignored) {
        }

        try {
            if (Files.exists(CACHE_PATH)) {
                return loadFromPath(CACHE_PATH);
            }
        } catch (Exception ignored) {
        }

        return loadBundledSnapshot();
    }

    private boolean isStale(TariffCatalog catalog) {
        try {
            Instant lastSynced = Instant.parse(catalog.getLastSyncedAt());
            return Instant.now().isAfter(lastSynced.plus(CACHE_TTL));
        } catch (Exception ignored) {
            return true;
        }
    }

    private TariffCatalog refreshFromRemote() throws Exception {
        String sourceUrl = discoverLatestSourceUrl().orElse(DEFAULT_SOURCE_URL);
        byte[] pdfBytes = download(sourceUrl);
        String text = extractPdfText(pdfBytes);
        TariffCatalog parsed = parseCatalog(text, sourceUrl);
        return new TariffCatalog(
                parsed.getTariffs(),
                parsed.getCategory(),
                parsed.getSourceLabel(),
                sourceUrl,
                parsed.getEffectiveDate(),
                ISO_FORMAT.format(Instant.now())
        );
    }

    private Optional<String> discoverLatestSourceUrl() {
        List<String> candidates = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear; year >= currentYear - 2; year--) {
            try {
                String html = downloadText("https://www.nepra.org.pk/tariff/Tariff/Ex-WAPDA%20DISCOS/" + year + "/");
                candidates.addAll(extractCandidateUrls(html, year));
            } catch (Exception ignored) {
            }
        }

        return candidates.stream()
                .distinct()
                .map(this::absolutePdfUrl)
                .max(Comparator.comparing(this::extractDateScore));
    }

    private List<String> extractCandidateUrls(String html, int year) {
        List<String> candidates = new ArrayList<>();
        Matcher matcher = HREF_PATTERN.matcher(html);
        while (matcher.find()) {
            String href = matcher.group(1);
            String upper = href.toUpperCase(Locale.ROOT);
            if (upper.contains("RATIONALIZATION") && upper.contains("TARIFF")) {
                candidates.add(href);
            } else if (upper.contains("MOTION") && upper.contains("TARIFF")) {
                candidates.add(href);
            } else if (upper.contains("TRF-100") && upper.contains(String.valueOf(year))) {
                candidates.add(href);
            }
        }
        return candidates;
    }

    private String absolutePdfUrl(String href) {
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }
        if (href.startsWith("/")) {
            return "https://www.nepra.org.pk" + href;
        }
        return "https://www.nepra.org.pk/tariff/Tariff/Ex-WAPDA%20DISCOS/" + LocalDate.now().getYear() + "/" + href;
    }

    private long extractDateScore(String url) {
        Matcher matcher = DATE_PATTERN.matcher(url);
        if (!matcher.find()) {
            return Long.MIN_VALUE;
        }
        int day = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int year = Integer.parseInt(matcher.group(3));
        return LocalDate.of(year, month, day).toEpochDay();
    }

    private byte[] download(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0 Safari/537.36")
                .header("Accept", "text/html,application/pdf,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 400) {
            throw new IOException("Remote request failed with status " + response.statusCode());
        }
        return response.body();
    }

    private String downloadText(String url) throws Exception {
        return new String(download(url), StandardCharsets.UTF_8);
    }

    private String extractPdfText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private TariffCatalog parseCatalog(String pdfText, String sourceUrl) {
        String normalized = pdfText
                .replace('\u00A0', ' ')
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();

        String unprotectedSection = extractUnprotectedSection(normalized);
        List<Float> rates = extractRates(unprotectedSection);
        if (rates.size() < SLAB_LABELS.length) {
            throw new IllegalStateException("Could not parse all NEPRA slab rates from source.");
        }

        List<TariffRate> tariffs = new ArrayList<>();
        for (int i = 0; i < SLAB_LABELS.length; i++) {
            TariffRate tariff = new TariffRate();
            tariff.setTariffID(i + 1);
            tariff.setTierName(SLAB_LABELS[i]);
            tariff.setMinUnits(MIN_UNITS[i]);
            tariff.setMaxUnits(MAX_UNITS[i]);
            tariff.setRatePerUnit(rates.get(i));
            tariff.setFixedCharge(DEFAULT_FIXED_CHARGES[i]);
            tariffs.add(tariff);
        }

        String effectiveDate = extractEffectiveDate(sourceUrl).orElse(DEFAULT_EFFECTIVE_DATE);
        return new TariffCatalog(
                tariffs,
                "residential-unprotected",
                DEFAULT_SOURCE_LABEL,
                sourceUrl,
                effectiveDate,
                ISO_FORMAT.format(Instant.now())
        );
    }

    private String extractUnprotectedSection(String normalized) {
        Pattern pattern = Pattern.compile("Un-?Protected(.*?)(Commercial|General Services|Industrial|Bulk|Temporary Supply)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(normalized);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return normalized;
    }

    private List<Float> extractRates(String section) {
        Pattern explicitPattern = Pattern.compile("Uniform Applicable\\s+((?:\\d+\\.\\d+\\s+){7}\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher explicitMatcher = explicitPattern.matcher(section);
        String valuesBlock = null;
        while (explicitMatcher.find()) {
            valuesBlock = explicitMatcher.group(1);
        }

        List<Float> rates = new ArrayList<>();
        if (valuesBlock != null) {
            Matcher valueMatcher = FLOAT_PATTERN.matcher(valuesBlock);
            while (valueMatcher.find()) {
                rates.add(Float.parseFloat(valueMatcher.group()));
            }
            return rates;
        }

        List<Float> allFloats = new ArrayList<>();
        Matcher matcher = FLOAT_PATTERN.matcher(section);
        while (matcher.find()) {
            allFloats.add(Float.parseFloat(matcher.group()));
        }
        if (allFloats.size() >= SLAB_LABELS.length) {
            return new ArrayList<>(allFloats.subList(allFloats.size() - SLAB_LABELS.length, allFloats.size()));
        }
        return rates;
    }

    private Optional<String> extractEffectiveDate(String sourceUrl) {
        Matcher matcher = DATE_PATTERN.matcher(sourceUrl);
        if (!matcher.find()) {
            return Optional.empty();
        }
        int day = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int year = Integer.parseInt(matcher.group(3));
        return Optional.of(LocalDate.of(year, month, day).toString());
    }

    private TariffCatalog loadBundledSnapshot() {
        try (InputStream stream = TariffSyncService.class.getResourceAsStream(SNAPSHOT_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Bundled tariff snapshot is missing.");
            }
            return loadFromProperties(stream);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load bundled tariff snapshot.", ex);
        }
    }

    private TariffCatalog loadFromPath(Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            return loadFromProperties(stream);
        }
    }

    private TariffCatalog loadFromProperties(InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);

        int count = Integer.parseInt(properties.getProperty("slab.count", "0"));
        List<TariffRate> tariffs = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            TariffRate tariff = new TariffRate();
            tariff.setTariffID(i);
            tariff.setTierName(properties.getProperty("slab." + i + ".name"));
            tariff.setMinUnits(Integer.parseInt(properties.getProperty("slab." + i + ".min", "0")));
            tariff.setMaxUnits(Integer.parseInt(properties.getProperty("slab." + i + ".max", "-1")));
            tariff.setRatePerUnit(Float.parseFloat(properties.getProperty("slab." + i + ".rate", "0")));
            tariff.setFixedCharge(Float.parseFloat(properties.getProperty("slab." + i + ".fixed", "0")));
            tariffs.add(tariff);
        }

        return new TariffCatalog(
                tariffs,
                properties.getProperty("category", "residential-unprotected"),
                properties.getProperty("source.label", DEFAULT_SOURCE_LABEL),
                properties.getProperty("source.url", DEFAULT_SOURCE_URL),
                properties.getProperty("effective.date", DEFAULT_EFFECTIVE_DATE),
                properties.getProperty("last.synced.at", ISO_FORMAT.format(Instant.ofEpochMilli(0).atOffset(ZoneOffset.UTC).toInstant()))
        );
    }

    private void saveToCache(TariffCatalog catalog) throws IOException {
        Files.createDirectories(CACHE_PATH.getParent());
        Properties properties = new Properties();
        properties.setProperty("category", catalog.getCategory());
        properties.setProperty("source.label", catalog.getSourceLabel());
        properties.setProperty("source.url", catalog.getSourceUrl());
        properties.setProperty("effective.date", catalog.getEffectiveDate());
        properties.setProperty("last.synced.at", catalog.getLastSyncedAt());
        properties.setProperty("slab.count", String.valueOf(catalog.getTariffs().size()));

        for (int i = 0; i < catalog.getTariffs().size(); i++) {
            TariffRate tariff = catalog.getTariffs().get(i);
            int index = i + 1;
            properties.setProperty("slab." + index + ".name", tariff.getTierName());
            properties.setProperty("slab." + index + ".min", String.valueOf(tariff.getMinUnits()));
            properties.setProperty("slab." + index + ".max", String.valueOf(tariff.getMaxUnits()));
            properties.setProperty("slab." + index + ".rate", String.valueOf(tariff.getRatePerUnit()));
            properties.setProperty("slab." + index + ".fixed", String.valueOf(tariff.getFixedCharge()));
        }

        try (OutputStream stream = Files.newOutputStream(CACHE_PATH)) {
            properties.store(stream, "Auto-synced NEPRA residential unprotected tariff snapshot");
        }
    }
}
