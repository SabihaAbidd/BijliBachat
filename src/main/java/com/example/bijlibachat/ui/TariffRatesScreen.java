package com.example.bijlibachat.ui;

import com.example.bijlibachat.model.TariffRate;
import com.example.bijlibachat.model.User;
import com.example.bijlibachat.service.TariffCatalog;
import com.example.bijlibachat.service.TariffEngine;
import com.example.bijlibachat.service.UserService;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TariffRatesScreen {

    private static final String SHELL = "#0D0D0D";
    private static final String SIDEBAR = "#111111";
    private static final String CARD = "#161616";
    private static final String CARD_SUBTLE = "#141414";
    private static final String BORDER = "#252525";
    private static final String TEXT = "#FFFFFF";
    private static final String TEXT_SOFT = "#9CA3AF";
    private static final String TEXT_FAINT = "#6B7280";
    private static final String ACCENT = "#F5A623";
    private static final String SUCCESS = "#23B26D";
    private static final String BLUE = "#5E7BF9";
    private static final String PURPLE = "#8C5CF6";
    private static final String DANGER = "#F46D6D";

    private final String userEmail;
    private final String messageOverride;
    private final String messageColorOverride;
    private final UserService userService = new UserService();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");
    private final TariffEngine gridTariffEngine = new TariffEngine("grid");

    public TariffRatesScreen(String userEmail) {
        this(userEmail, null, null);
    }

    private TariffRatesScreen(String userEmail, String message, String messageColor) {
        this.userEmail = userEmail == null ? "" : userEmail;
        this.messageOverride = message;
        this.messageColorOverride = messageColor;
    }

    public void start(Stage stage) {
        TariffViewModel data = loadData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildContent(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Tariff Rates", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(280), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, TariffViewModel data) {
        VBox sidebar = new VBox(22);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(22, 18, 18, 18));
        sidebar.setBackground(new Background(new BackgroundFill(Color.web(SIDEBAR), CornerRadii.EMPTY, Insets.EMPTY)));
        sidebar.setStyle("-fx-border-color: transparent " + BORDER + " transparent transparent;");

        HBox brandRow = new HBox(10);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        StackPane brandMark = new StackPane();
        Circle halo = new Circle(22, Color.web("#1B1407"));
        Text bolt = new Text("*");
        bolt.setFill(Color.web(ACCENT));
        bolt.setFont(Font.font("System", FontWeight.BOLD, 20));
        brandMark.getChildren().addAll(halo, bolt);

        VBox brandText = new VBox(2);
        brandText.getChildren().addAll(label("Bijli Bachat", 18, TEXT, true), label("Tariff rates", 11, TEXT_SOFT, false));
        brandRow.getChildren().addAll(brandMark, brandText);

        VBox profile = cardBox(16, CARD_SUBTLE);
        profile.getChildren().addAll(
                label(data.displayName, 18, TEXT, true),
                label(data.userEmailLabel, 12, TEXT_SOFT, false),
                statusPill(data.accountTypeLabel, data.isSolarUser ? PURPLE : ACCENT)
        );

        VBox nav = new VBox(4);
        nav.getChildren().addAll(
                navSection("OVERVIEW"),
                navButton("Dashboard", false, () -> goToDashboard(stage)),
                navButton("Meter Reading", false, () -> goToMeterReading(stage)),
                navButton("Bills & History", false, () -> goToBillView(stage)),
                navButton("Appliances", false, () -> goToAppliances(stage)),
                navButton("Monthly Ceiling", false, () -> goToMonthlyCeiling(stage)),
                navButton("Reports", false, () -> goToReports(stage)),
                navButton("Tariff Rates", true, null),
                navButton("Settings", false, () -> goToSettings(stage))
        );
        if (data.isSolarUser) {
            nav.getChildren().add(navButton("Solar Net Metering", false, () -> goToSolar(stage)));
        }

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);
        Button back = secondaryButton("<-  Back to Dashboard");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(e -> goToDashboard(stage));

        sidebar.getChildren().addAll(brandRow, profile, nav, push, back);
        animateEntrance(profile, 80);
        animateEntrance(nav, 150);
        return sidebar;
    }

    private ScrollPane buildContent(Stage stage, TariffViewModel data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24, 26, 30, 26));

        VBox hero = cardBox(20, CARD);
        hero.setSpacing(18);
        HBox heroTop = new HBox(18);
        heroTop.setAlignment(Pos.TOP_LEFT);

        VBox copy = new VBox(6);
        Label eyebrow = label("Tariff sync", 12, TEXT_SOFT, false);
        Label title = label("NEPRA-backed billing slabs", 26, TEXT, true);
        Label subtitle = label("Current residential slabs, sync status, and the rates used by bill estimates.", 13, TEXT_SOFT, false);
        subtitle.setWrapText(true);
        copy.setMaxWidth(520);
        copy.getChildren().addAll(eyebrow, title, subtitle);

        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);

        VBox highlight = cardBox(16, CARD_SUBTLE);
        highlight.setEffect(null);
        highlight.setMinWidth(320);
        highlight.setMaxWidth(420);
        Label sourceLabel = label(data.sourceLabel, 15, TEXT, true);
        sourceLabel.setWrapText(true);
        Label effectiveLabel = label("Effective " + data.effectiveDate, 12, TEXT_SOFT, false);
        Label syncedLabel = label("Last sync " + data.lastSyncedLabel, 12, TEXT_SOFT, false);
        highlight.getChildren().addAll(statusPill("Source", ACCENT), sourceLabel, effectiveLabel, syncedLabel);

        heroTop.getChildren().addAll(copy, heroSpacer, highlight);

        HBox bands = new HBox(12);
        bands.getChildren().addAll(
                heroBand("1-100", data.tariffs.size() > 0 ? rateLabel(data.tariffs.get(0)) : "Unavailable", BLUE),
                heroBand("101-300", data.tariffs.size() > 2 ? rateRangeLabel(data.tariffs.get(1), data.tariffs.get(2)) : "Unavailable", SUCCESS),
                heroBand("Above 300", data.tariffs.size() > 3 ? rateLabel(data.tariffs.get(3)) : "Unavailable", PURPLE)
        );
        for (Node node : bands.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        hero.getChildren().addAll(heroTop, bands);

        VBox slabCard = buildSlabCard(stage, data);

        page.getChildren().addAll(hero, slabCard);
        animateEntrance(hero, 40);
        animateEntrance(slabCard, 120);
        return wrap(page);
    }

    private VBox buildSlabCard(Stage stage, TariffViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(16);

        Label title = label("Current slabs", 16, TEXT, true);
        Label sub = label("These slabs are used by bill estimation and solar-credit preview.", 12, TEXT_SOFT, false);
        sub.setWrapText(true);

        Label message = label(data.message, 12, data.messageColor, false);
        message.setWrapText(true);

        GridPane slabGrid = new GridPane();
        slabGrid.setHgap(14);
        slabGrid.setVgap(14);

        int columnCount = 2;
        for (int i = 0; i < data.tariffs.size(); i++) {
            TariffRate tariff = data.tariffs.get(i);
            VBox row = slabTile(tariff);
            row.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(row, Priority.ALWAYS);
            slabGrid.add(row, i % columnCount, i / columnCount);
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button back = secondaryButton("Back");
        back.setOnAction(e -> goToDashboard(stage));
        Button refresh = primaryButton("Refresh NEPRA rates");
        refresh.setOnAction(e -> triggerRefresh(stage, refresh));
        actions.getChildren().addAll(back, refresh);

        card.getChildren().addAll(title, sub, message, slabGrid, actions);
        return card;
    }

    private void triggerRefresh(Stage stage, Button refreshButton) {
        refreshButton.setDisable(true);
        refreshButton.setText("Refreshing...");

        Task<TariffCatalog> refreshTask = new Task<>() {
            @Override
            protected TariffCatalog call() {
                return gridTariffEngine.refreshCatalog();
            }
        };

        refreshTask.setOnSucceeded(event -> switchScene(stage, () ->
                new TariffRatesScreen(
                        userEmail,
                        "NEPRA rates refreshed successfully. Effective date: " +
                                DEFAULT_STRING(refreshTask.getValue().getEffectiveDate()) + ".",
                        SUCCESS
                ).start(stage)));

        refreshTask.setOnFailed(event -> {
            Throwable error = refreshTask.getException();
            String detail = error == null || error.getMessage() == null || error.getMessage().isBlank()
                    ? "The app kept the last available tariff snapshot."
                    : error.getMessage();
            switchScene(stage, () -> new TariffRatesScreen(
                    userEmail,
                    "Refresh failed. " + detail,
                    DANGER
            ).start(stage));
        });

        Thread refreshThread = new Thread(refreshTask, "nepra-refresh");
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    private VBox slabTile(TariffRate tariff) {
        VBox row = cardBox(16, CARD_SUBTLE);
        row.setEffect(null);
        row.setSpacing(8);

        String unitLabel = tariff.getMaxUnits() <= 0
                ? tariff.getMinUnits() + "+ units"
                : tariff.getMinUnits() + "-" + tariff.getMaxUnits() + " units";
        String fixedLabel = tariff.getFixedCharge() <= 0
                ? "No fixed charge"
                : "Fixed charge Rs. " + numberFormat.format(tariff.getFixedCharge());

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label slabName = label(tariff.getTierName(), 15, TEXT, true);
        Label slabUnits = label(unitLabel, 12, TEXT_SOFT, false);
        HBox.setHgrow(slabName, Priority.ALWAYS);
        top.getChildren().addAll(slabName, slabUnits);

        Label rate = label("Rs. " + numberFormat.format(tariff.getRatePerUnit()) + "/unit", 19, ACCENT, true);
        Label fixed = label(fixedLabel, 12, TEXT_SOFT, false);

        row.getChildren().addAll(top, rate, fixed);
        return row;
    }

    private TariffViewModel loadData() {
        TariffViewModel vm = new TariffViewModel();
        vm.displayName = "User";
        vm.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        vm.accountTypeLabel = "Grid User";
        vm.accountTypeRaw = "grid";
        vm.message = messageOverride == null
                ? "Using NEPRA residential unprotected slabs dated " + DEFAULT_STRING(gridTariffEngine.getCatalog().getEffectiveDate()) + "."
                : messageOverride;
        vm.messageColor = messageColorOverride == null ? TEXT_SOFT : messageColorOverride;

        try {
            User user = userService.getUserByEmail(userEmail);
            if (user != null) {
                vm.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
                vm.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());
                vm.accountTypeLabel = vm.isSolarUser ? "Solar User" : "Grid User";
                vm.accountTypeRaw = vm.isSolarUser ? "solar" : "grid";
            }

            TariffCatalog catalog = gridTariffEngine.getCatalog();
            vm.sourceLabel = DEFAULT_STRING(catalog.getSourceLabel());
            vm.sourceUrl = DEFAULT_STRING(catalog.getSourceUrl());
            vm.effectiveDate = DEFAULT_STRING(catalog.getEffectiveDate());
            vm.lastSyncedLabel = formatTimestamp(catalog.getLastSyncedAt());
            vm.tariffs = catalog.getTariffs();
        } catch (Exception ignored) {
            vm.sourceLabel = "Bundled snapshot";
            vm.sourceUrl = "";
            vm.effectiveDate = "Unavailable";
            vm.lastSyncedLabel = "Unavailable";
        }
        return vm;
    }

    private String formatTimestamp(String value) {
        try {
            Instant instant = Instant.parse(value);
            return DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        } catch (Exception ignored) {
            return "Unavailable";
        }
    }

    private String DEFAULT_STRING(String value) {
        return value == null || value.isBlank() ? "Unavailable" : value;
    }

    private ScrollPane wrap(VBox page) {
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setStyle("-fx-background: " + SHELL + "; -fx-background-color: " + SHELL + "; -fx-border-color: transparent;");
        return scrollPane;
    }

    private String rateLabel(TariffRate tariff) {
        return "Rs. " + numberFormat.format(tariff.getRatePerUnit()) + "/unit";
    }

    private String rateRangeLabel(TariffRate start, TariffRate end) {
        return "Rs. " + numberFormat.format(start.getRatePerUnit()) + " to " + numberFormat.format(end.getRatePerUnit());
    }

    private VBox heroBand(String title, String value, String accentColor) {
        VBox band = cardBox(14, CARD_SUBTLE);
        band.setEffect(null);
        Rectangle accentBar = new Rectangle(40, 4, Color.web(accentColor));
        accentBar.setArcWidth(4);
        accentBar.setArcHeight(4);
        Label valueLabel = label(value, 16, TEXT, true);
        valueLabel.setWrapText(true);
        band.getChildren().addAll(accentBar, label(title, 12, TEXT_FAINT, false), valueLabel);
        return band;
    }

    private VBox cardBox(double padding, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(padding));
        card.setBackground(new Background(new BackgroundFill(Color.web(color), new CornerRadii(8), Insets.EMPTY)));
        card.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setEffect(new DropShadow(10, 0, 3, Color.web("#00000088")));
        return card;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(40);
        button.setMinHeight(40);
        button.setFocusTraversable(false);
        button.setWrapText(false);
        String normalStyle =
                "-fx-background-color: " + ACCENT + ";" +
                        "-fx-text-fill: #0D0D0D;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-insets: 0;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-padding: 0 14 0 14;";
        String hoverStyle =
                "-fx-background-color: #E7A11F;" +
                        "-fx-text-fill: #0D0D0D;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-insets: 0;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-padding: 0 14 0 14;";
        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(40);
        button.setMinHeight(40);
        button.setFocusTraversable(false);
        button.setWrapText(false);
        String normalStyle =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT_SOFT + ";" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-insets: 0;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-padding: 0 14 0 14;";
        String hoverStyle =
                "-fx-background-color: #141414;" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-insets: 0;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-padding: 0 14 0 14;";
        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
        return button;
    }

    private Button navButton(String text, boolean active, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(38);
        button.setStyle(
                "-fx-background-color: " + (active ? "#1A1A1A" : "transparent") + ";" +
                        "-fx-text-fill: " + (active ? ACCENT : TEXT_SOFT) + ";" +
                        "-fx-font-size: 13;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: " + (active ? "bold" : "normal") + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 0 12 0 12;"
        );
        if (action != null) {
            button.setOnAction(e -> action.run());
        }
        return button;
    }

    private VBox navSection(String text) {
        VBox box = new VBox();
        box.setPadding(new Insets(8, 10, 4, 10));
        box.getChildren().add(label(text, 10, TEXT_FAINT, true));
        return box;
    }

    private Label label(String text, int size, String color, boolean bold) {
        Label label = new Label(text);
        label.setTextFill(Color.web(color));
        label.setFont(Font.font("Verdana", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        return label;
    }

    private Label statusPill(String text, String color) {
        Label pill = new Label(text);
        pill.setStyle(
                "-fx-background-color: #1B1407;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: 10;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 4 8 4 8;"
        );
        return pill;
    }

    private void goToDashboard(Stage stage) {
        switchScene(stage, () -> new DashboardScreen(userEmail).start(stage));
    }

    private void goToMeterReading(Stage stage) {
        switchScene(stage, () -> new MeterReadingScreen(userEmail).start(stage));
    }

    private void goToBillView(Stage stage) {
        switchScene(stage, () -> new BillViewScreen(userEmail).start(stage));
    }

    private void goToAppliances(Stage stage) {
        switchScene(stage, () -> new ApplianceScreen(userEmail).start(stage));
    }

    private void goToMonthlyCeiling(Stage stage) {
        switchScene(stage, () -> new MonthlyCeilingScreen(userEmail).start(stage));
    }

    private void goToReports(Stage stage) {
        switchScene(stage, () -> new ReportsScreen(userEmail).start(stage));
    }

    private void goToSettings(Stage stage) {
        switchScene(stage, () -> new SettingsScreen(userEmail).start(stage));
    }

    private void goToSolar(Stage stage) {
        switchScene(stage, () -> new SolarNetMeteringScreen(userEmail).start(stage));
    }

    private void switchScene(Stage stage, Runnable navigation) {
        navigation.run();
    }

    private void animateEntrance(Node node, int delayMs) {
        node.setOpacity(0);
        double originalY = node.getTranslateY();
        node.setTranslateY(originalY + 14);

        FadeTransition fade = new FadeTransition(Duration.millis(420), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));

        TranslateTransition slide = new TranslateTransition(Duration.millis(420), node);
        slide.setFromY(originalY + 14);
        slide.setToY(originalY);
        slide.setDelay(Duration.millis(delayMs));
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();
    }

    private static class TariffViewModel {
        boolean isSolarUser;
        String displayName;
        String userEmailLabel;
        String accountTypeLabel;
        String accountTypeRaw;
        String sourceLabel = "Unavailable";
        String sourceUrl = "";
        String effectiveDate = "Unavailable";
        String lastSyncedLabel = "Unavailable";
        String message = "";
        String messageColor = TEXT_SOFT;
        List<TariffRate> tariffs = new ArrayList<>();
    }
}
