package com.example.bijlibachat.ui;

import com.example.bijlibachat.model.ElectricityBill;
import com.example.bijlibachat.model.MeterReading;
import com.example.bijlibachat.model.SolarMeter;
import com.example.bijlibachat.model.User;
import com.example.bijlibachat.service.BillingService;
import com.example.bijlibachat.service.SolarService;
import com.example.bijlibachat.service.TariffEngine;
import com.example.bijlibachat.service.UserService;
import com.example.bijlibachat.dao.MeterReadingDAO;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BillViewScreen {

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

    private static final float GST_RATE = 0.17f;

    private final String userEmail;
    private final UserService userService = new UserService();
    private final BillingService billingService = new BillingService();
    private final MeterReadingDAO meterReadingDAO = new MeterReadingDAO();
    private final SolarService solarService = new SolarService();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    public BillViewScreen(String userEmail) {
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void start(Stage stage) {
        BillViewModel data = loadData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildContent(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Bill View", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(280), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, BillViewModel data) {
        VBox sidebar = new VBox(22);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(22, 18, 18, 18));
        sidebar.setBackground(new Background(new BackgroundFill(Color.web(SIDEBAR), CornerRadii.EMPTY, Insets.EMPTY)));
        sidebar.setStyle("-fx-border-color: transparent " + BORDER + " transparent transparent;");

        HBox brandRow = new HBox(10);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        StackPane brandMark = new StackPane();
        Circle halo = new Circle(22, Color.web("#1B1407"));
        Text bolt = new Text("⚡");
        bolt.setFill(Color.web(ACCENT));
        bolt.setFont(Font.font("System", FontWeight.BOLD, 20));
        brandMark.getChildren().addAll(halo, bolt);

        VBox brandText = new VBox(2);
        brandText.getChildren().addAll(label("Bijli Bachat", 18, TEXT, true), label("Bill view", 11, TEXT_SOFT, false));
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
                navButton("Bills & History", true, null),
                navButton("Appliances", false, () -> goToAppliances(stage)),
                navButton("Monthly Ceiling", false, () -> goToMonthlyCeiling(stage)),
                navButton("Reports", false, () -> goToReports(stage)),
                navButton("Tariff Rates", false, () -> goToTariffRates(stage)),
                navButton("Settings", false, () -> goToSettings(stage))
        );
        if (data.isSolarUser) {
            nav.getChildren().add(navButton("Solar Net Metering", false, () -> goToSolar(stage)));
        }

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        Button back = secondaryButton("←  Back to Dashboard");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(e -> goToDashboard(stage));

        sidebar.getChildren().addAll(brandRow, profile, nav, push, back);
        animateEntrance(profile, 80);
        animateEntrance(nav, 150);
        return sidebar;
    }

    private ScrollPane buildContent(Stage stage, BillViewModel data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24, 26, 30, 26));

        VBox titleBlock = buildHeroHeader(data);

        HBox summaryRow = new HBox(14);
        VBox estimateCard = metricCard("Estimate", data.totalEstimate, data.billingCycle, ACCENT);
        VBox unitsCard = metricCard("Units", data.unitsValue, data.unitsSubline, BLUE);
        VBox tierCard = metricCard("Tier", data.tierValue, data.tierSubline, SUCCESS);
        HBox.setHgrow(estimateCard, Priority.ALWAYS);
        HBox.setHgrow(unitsCard, Priority.ALWAYS);
        HBox.setHgrow(tierCard, Priority.ALWAYS);
        estimateCard.setMaxWidth(Double.MAX_VALUE);
        unitsCard.setMaxWidth(Double.MAX_VALUE);
        tierCard.setMaxWidth(Double.MAX_VALUE);
        estimateCard.setMinWidth(0);
        unitsCard.setMinWidth(0);
        tierCard.setMinWidth(0);
        summaryRow.getChildren().addAll(estimateCard, unitsCard, tierCard);

        HBox lower = new HBox(14);
        VBox breakdown = buildBreakdownCard(data);
        VBox history = buildHistoryCard(data);
        HBox.setHgrow(breakdown, Priority.ALWAYS);
        HBox.setHgrow(history, Priority.ALWAYS);
        breakdown.setMaxWidth(Double.MAX_VALUE);
        history.setMaxWidth(Double.MAX_VALUE);
        breakdown.setMinWidth(0);
        history.setMinWidth(0);
        lower.getChildren().addAll(breakdown, history);

        page.getChildren().addAll(titleBlock, summaryRow, lower);
        animateEntrance(titleBlock, 40);
        animateEntrance(summaryRow, 110);
        animateEntrance(lower, 180);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setStyle("-fx-background: " + SHELL + "; -fx-background-color: " + SHELL + "; -fx-border-color: transparent;");
        return scrollPane;
    }

    private VBox buildBreakdownCard(BillViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(16);

        Label title = label("Bill breakdown", 16, TEXT, true);

        VBox rows = new VBox(10);
        rows.getChildren().addAll(
                breakdownRow("Base tariff", data.baseAmount),
                breakdownRow("Fixed charge", data.fixedCharge),
                breakdownRow("GST (17%)", data.gstAmount)
        );
        if (data.isSolarUser) {
            rows.getChildren().add(breakdownRow("Solar credit", data.solarCredit));
        }

        Rectangle divider = new Rectangle();
        divider.setHeight(1);
        divider.setFill(Color.web(BORDER));
        divider.widthProperty().bind(card.widthProperty().subtract(36));

        HBox total = new HBox(12);
        total.setAlignment(Pos.CENTER_LEFT);
        Label totalLabel = label("Total estimate", 15, TEXT, true);
        Label totalValue = label(data.totalEstimate, 18, ACCENT, true);
        totalValue.setMinWidth(0);
        totalValue.setAlignment(Pos.CENTER_RIGHT);
        totalValue.setWrapText(true);
        Region totalSpacer = new Region();
        HBox.setHgrow(totalLabel, Priority.ALWAYS);
        HBox.setHgrow(totalSpacer, Priority.ALWAYS);
        HBox.setHgrow(totalValue, Priority.ALWAYS);
        total.getChildren().addAll(totalLabel, totalSpacer, totalValue);

        VBox notes = cardBox(14, CARD_SUBTLE);
        notes.setEffect(null);
        Label noteText = label(data.note, 12, TEXT_SOFT, false);
        noteText.setWrapText(true);
        notes.getChildren().addAll(
                label("Current cycle note", 13, TEXT, true),
                noteText
        );

        card.getChildren().addAll(title, rows, divider, total, notes);
        return card;
    }

    private VBox buildHistoryCard(BillViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(12);

        HBox heading = new HBox();
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getChildren().addAll(
                label("Recent billing history", 16, TEXT, true),
                spacer(),
                statusPill("Saved cycles", SUCCESS)
        );

        if (data.historyRows.isEmpty()) {
            VBox emptyState = cardBox(16, CARD_SUBTLE);
            emptyState.setEffect(null);
            Label emptyTitle = label("No bill records yet", 14, TEXT, true);
            Label emptyBody = label("No history yet.", 12, TEXT_SOFT, false);
            emptyBody.setWrapText(true);
            emptyState.getChildren().addAll(emptyTitle, emptyBody);
            card.getChildren().addAll(heading, emptyState);
            return card;
        }

        VBox rows = new VBox(8);
        Label selectedCycle = label(data.historyRows.get(0).cycle, 14, TEXT, true);
        Label selectedStatus = label("Status: " + data.historyRows.get(0).status, 12, TEXT_SOFT, false);
        Label selectedAmount = label(data.historyRows.get(0).amount, 18, ACCENT, true);
        VBox detailCard = cardBox(14, CARD_SUBTLE);
        detailCard.setEffect(null);
        detailCard.getChildren().addAll(
                label("Cycle details", 13, TEXT, true),
                selectedCycle,
                selectedStatus,
                selectedAmount
        );
        for (HistoryRow row : data.historyRows) {
            rows.getChildren().add(historyRow(row, selectedCycle, selectedStatus, selectedAmount));
        }

        card.getChildren().addAll(heading, rows, detailCard);
        return card;
    }

    private BillViewModel loadData() {
        BillViewModel vm = new BillViewModel();
        vm.displayName = "User";
        vm.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        vm.accountTypeLabel = "Grid User";
        vm.totalEstimate = "Unavailable";
        vm.billingCycle = "No active cycle";
        vm.unitsValue = "0 kWh";
        vm.unitsSubline = "Awaiting meter data";
        vm.tierValue = "Not available";
        vm.tierSubline = "Tariff data missing";
        vm.baseAmount = "Rs. 0.00";
        vm.fixedCharge = "Rs. 0.00";
        vm.gstAmount = "Rs. 0.00";
        vm.solarCredit = "Rs. 0.00";
        vm.note = "Record a meter reading first so the estimate can be generated from live units.";

        try {
            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                return vm;
            }

            vm.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
            vm.accountTypeLabel = "solar".equalsIgnoreCase(user.getAccountType()) ? "Solar User" : "Grid User";
            vm.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());

            float units = meterReadingDAO.getTotalUnitsThisCycle(user.getUserID());
            MeterReading lastReading = meterReadingDAO.getLastReading(user.getUserID());
            TariffEngine engine = new TariffEngine(user.getAccountType());

            float solarCreditAmount = 0;
            if (vm.isSolarUser) {
                SolarMeter solar = solarService.getLatestSolarReading(user.getUserID());
                if (solar != null) {
                    solarCreditAmount = solar.getNetCredit();
                }
            }

            float base = engine.applyTariffStructure(units);
            float fixedCharge = engine.getFixedCharge(units);
            float withFixed = base + fixedCharge;
            float gst = withFixed * GST_RATE;
            float total = Math.max(0, withFixed + gst - solarCreditAmount);

            if (units > 0) {
                billingService.calculateAndRecordBill(user.getUserID(), units, user.getAccountType());
            }
            ElectricityBill latest = billingService.getLatestBill(user.getUserID());

            vm.totalEstimate = units <= 0 ? "Unavailable" : "Rs. " + currencyFormat.format(total);
            vm.billingCycle = latest == null ? "Needs current cycle data" : latest.getBillingCycle();
            vm.unitsValue = numberFormat.format(units) + " kWh";
            vm.unitsSubline = lastReading == null ? "No recorded entry yet" : "Last reading captured at " + numberFormat.format(lastReading.getValue());
            vm.tierValue = units <= 0 ? "Tier pending" : activeTierLabel(engine, units);
            vm.tierSubline = units <= 0 ? "Add readings to determine the slab" : "Calculated using current tariff structure";
            vm.baseAmount = "Rs. " + currencyFormat.format(base);
            vm.fixedCharge = "Rs. " + currencyFormat.format(fixedCharge);
            vm.gstAmount = "Rs. " + currencyFormat.format(gst);
            vm.solarCredit = "Rs. " + currencyFormat.format(solarCreditAmount);
            vm.note = units <= 0
                    ? "No consumption is available yet, so this screen is showing the bill structure with zero usage."
                    : "This estimate uses the current cycle units, fixed charge, and tax rules already present in your service layer.";

            List<ElectricityBill> history = billingService.getBillingHistory(user.getUserID());
            for (int i = 0; i < Math.min(5, history.size()); i++) {
                ElectricityBill bill = history.get(i);
                vm.historyRows.add(new HistoryRow(
                        bill.getBillingCycle() == null ? "Cycle " + (i + 1) : bill.getBillingCycle(),
                        bill.getStatus() == null ? "estimated" : bill.getStatus(),
                        "Rs. " + currencyFormat.format(bill.getEstimatedAmount())
                ));
            }

            return vm;
        } catch (Exception ignored) {
            return vm;
        }
    }

    private String activeTierLabel(TariffEngine engine, float units) {
        if (units <= 0) {
            return "Tier pending";
        }
        var tier = engine.getTariffForUnits(units);
        return tier == null ? "Unknown tier" : tier.getTierName();
    }

    private VBox metricCard(String titleText, String valueText, String subText, String accent) {
        VBox card = cardBox(16, CARD);
        card.setSpacing(10);
        card.setMinHeight(112);
        card.setPrefWidth(0);
        card.setMinWidth(0);
        Rectangle accentBar = new Rectangle(44, 4, Color.web(accent));
        accentBar.setArcWidth(4);
        accentBar.setArcHeight(4);
        Label subLabel = label(subText, 12, accent, true);
        subLabel.setWrapText(true);
        card.getChildren().addAll(
                accentBar,
                label(titleText, 12, TEXT_FAINT, false),
                label(valueText, 18, TEXT, true),
                subLabel
        );
        return card;
    }

    private HBox breakdownRow(String left, String right) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label leftLabel = label(left, 13, TEXT_SOFT, false);
        Label rightLabel = label(right, 13, TEXT, true);
        rightLabel.setMinWidth(0);
        rightLabel.setWrapText(true);
        rightLabel.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(rightLabel, Priority.ALWAYS);
        row.getChildren().addAll(leftLabel, spacer, rightLabel);
        return row;
    }

    private HBox historyRow(HistoryRow row, Label selectedCycle, Label selectedStatus, Label selectedAmount) {
        HBox line = new HBox();
        line.setAlignment(Pos.CENTER_LEFT);
        line.setPadding(new Insets(10, 12, 10, 12));
        line.setBackground(new Background(new BackgroundFill(Color.web(CARD_SUBTLE), new CornerRadii(8), Insets.EMPTY)));
        line.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        line.setCursor(Cursor.HAND);

        VBox left = new VBox(3);
        left.getChildren().addAll(
                label(row.cycle, 12, TEXT, true),
                label(row.status, 11, TEXT_SOFT, false)
        );

        line.getChildren().addAll(left, spacer(), label(row.amount, 12, ACCENT, true));
        line.setOnMouseClicked(e -> {
            selectedCycle.setText(row.cycle);
            selectedStatus.setText("Status: " + row.status);
            selectedAmount.setText(row.amount);
        });
        return line;
    }

    private VBox buildHeroHeader(BillViewModel data) {
        VBox hero = cardBox(20, CARD);
        hero.setSpacing(18);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        VBox copy = new VBox(6);
        copy.getChildren().addAll(
                label("Bill summary", 12, TEXT_SOFT, false),
                label("Bill details", 26, TEXT, true),
                label("Current bill", 13, TEXT_SOFT, false)
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox highlight = cardBox(16, CARD_SUBTLE);
        highlight.setEffect(null);
        highlight.setMinWidth(260);
        highlight.getChildren().addAll(
                statusPill("Estimate", ACCENT),
                label(data.totalEstimate, 24, TEXT, true),
                label(data.billingCycle, 12, TEXT_SOFT, false)
        );

        top.getChildren().addAll(copy, spacer, highlight);

        HBox bands = new HBox(12);
        bands.getChildren().addAll(
                heroBand("Base tariff", data.baseAmount, BLUE),
                heroBand("Fixed + GST", "Rs. " + currencyFormat.format(parseMoney(data.fixedCharge) + parseMoney(data.gstAmount)), SUCCESS),
                heroBand("Solar credit", data.isSolarUser ? data.solarCredit : "Rs. 0.00", PURPLE)
        );
        for (Node node : bands.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        hero.getChildren().addAll(top, bands);
        return hero;
    }

    private VBox heroBand(String title, String value, String accent) {
        VBox band = cardBox(14, CARD_SUBTLE);
        band.setEffect(null);
        band.getChildren().addAll(
                statusPill(title, accent),
                label(value, 16, TEXT, true)
        );
        return band;
    }

    private VBox cardBox(double padding, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(padding));
        card.setBackground(new Background(new BackgroundFill(Color.web(color), new CornerRadii(8), Insets.EMPTY)));
        card.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        DropShadow shadow = new DropShadow(10, 0, 3, Color.web("#00000088"));
        shadow.setSpread(0);
        card.setEffect(shadow);
        return card;
    }

    private float parseMoney(String text) {
        try {
            return Float.parseFloat(text.replace("Rs.", "").replace(",", "").trim());
        } catch (Exception ignored) {
            return 0f;
        }
    }

    private VBox navSection(String text) {
        VBox box = new VBox();
        box.setPadding(new Insets(8, 10, 4, 10));
        box.getChildren().add(label(text, 10, TEXT_FAINT, true));
        return box;
    }

    private Button navButton(String text, boolean active, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(38);
        button.setMinHeight(38);
        button.setFocusTraversable(false);
        button.setWrapText(false);
        button.setStyle(
                "-fx-background-color: " + (active ? "#1A1A1A" : "transparent") + ";" +
                        "-fx-text-fill: " + (active ? ACCENT : TEXT_SOFT) + ";" +
                        "-fx-font-size: 13;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: normal;" +
                        "-fx-background-radius: 8;" +
                        "-fx-background-insets: 0;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-padding: 0 12 0 12;"
        );
        if (action != null) {
            button.setOnAction(e -> action.run());
        }
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(40);
        button.setMinHeight(40);
        button.setFocusTraversable(false);
        button.setWrapText(false);
        button.setStyle(
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
                        "-fx-padding: 0 14 0 14;"
        );
        return button;
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

    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void goToDashboard(Stage stage) {
        switchScene(stage, () -> {
            try {
                new DashboardScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void goToMeterReading(Stage stage) {
        switchScene(stage, () -> {
            try {
                new MeterReadingScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void goToAppliances(Stage stage) {
        switchScene(stage, () -> {
            try {
                new ApplianceScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void goToMonthlyCeiling(Stage stage) {
        switchScene(stage, () -> {
            try {
                new MonthlyCeilingScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void goToReports(Stage stage) {
        switchScene(stage, () -> {
            try {
                new ReportsScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void goToSettings(Stage stage) {
        switchScene(stage, () -> {
            try {
                new SettingsScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void goToSolar(Stage stage) {
        switchScene(stage, () -> {
            try {
                new SolarNetMeteringScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void goToTariffRates(Stage stage) {
        switchScene(stage, () -> {
            try {
                new TariffRatesScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
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

    private static class BillViewModel {
        boolean isSolarUser;
        String displayName;
        String userEmailLabel;
        String accountTypeLabel;
        String totalEstimate;
        String billingCycle;
        String unitsValue;
        String unitsSubline;
        String tierValue;
        String tierSubline;
        String baseAmount;
        String fixedCharge;
        String gstAmount;
        String solarCredit;
        String note;
        List<HistoryRow> historyRows = new ArrayList<>();
    }

    private record HistoryRow(String cycle, String status, String amount) { }
}
