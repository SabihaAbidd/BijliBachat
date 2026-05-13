package com.example.bijlibachat.ui;

import com.example.bijlibachat.dao.ApplianceDAO;
import com.example.bijlibachat.dao.BudgetDAO;
import com.example.bijlibachat.dao.MeterReadingDAO;
import com.example.bijlibachat.dao.SolarDAO;
import com.example.bijlibachat.model.Appliance;
import com.example.bijlibachat.model.ElectricityBill;
import com.example.bijlibachat.model.MeterReading;
import com.example.bijlibachat.model.MonthlyBudget;
import com.example.bijlibachat.model.SolarMeter;
import com.example.bijlibachat.model.TariffRate;
import com.example.bijlibachat.model.User;
import com.example.bijlibachat.service.BillingService;
import com.example.bijlibachat.service.TariffEngine;
import com.example.bijlibachat.service.UserService;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardScreen {

    private static final String SHELL = "#0D0D0D";
    private static final String SIDEBAR = "#111111";
    private static final String CARD = "#161616";
    private static final String CARD_SUBTLE = "#141414";
    private static final String BORDER = "#252525";
    private static final String TEXT = "#FFFFFF";
    private static final String TEXT_SOFT = "#9CA3AF";
    private static final String TEXT_FAINT = "#6B7280";
    private static final String SUCCESS = "#23B26D";
    private static final String ACCENT = "#F5A623";
    private static final String BLUE = "#5E7BF9";
    private static final String CYAN = "#5FB6F7";
    private static final String MINT = "#65D9A5";
    private static final String PURPLE = "#8C5CF6";
    private static final String PINK = "#EE6BC9";
    private static final String RED = "#F46D6D";
    private static final float GST_RATE = 0.17f;

    private final String userEmail;
    private final UserService userService = new UserService();
    private final BillingService billingService = new BillingService();
    private final MeterReadingDAO meterReadingDAO = new MeterReadingDAO();
    private final ApplianceDAO applianceDAO = new ApplianceDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final SolarDAO solarDAO = new SolarDAO();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    public DashboardScreen(String userEmail) {
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void start(Stage stage) {
        DashboardData data = loadDashboardData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildWorkspace(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Dashboard", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(300), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, DashboardData data) {
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
        Label appName = label("Bijli Bachat", 18, TEXT, true);
        Label appSub = label("Energy dashboard", 11, TEXT_SOFT, false);
        brandText.getChildren().addAll(appName, appSub);
        brandRow.getChildren().addAll(brandMark, brandText);

        VBox profile = sectionCard(16);
        Label name = label(data.displayName, 18, TEXT, true);
        Label email = label(data.userEmailLabel, 12, TEXT_SOFT, false);
        HBox tierRow = new HBox(8, statusPill(data.accountTypeLabel, "#211608", ACCENT), statusPill(data.currentMonthLabel, "#111927", BLUE));
        profile.getChildren().addAll(name, email, tierRow);

        VBox nav = new VBox(4);
        nav.getChildren().addAll(
                navSection("OVERVIEW"),
                navButton("Dashboard", true, null),
                navButton("Meter Reading", false, () -> openMeterReading(stage)),
                navButton("Bills & History", false, () -> openBillView(stage)),
                navButton("Appliances", false, () -> openAppliances(stage)),
                navButton("Monthly Ceiling", false, () -> openMonthlyCeiling(stage)),
                navButton("Reports", false, () -> openReports(stage)),
                navButton("Tariff Rates", false, () -> openTariffRates(stage)),
                navButton("Settings", false, () -> openSettings(stage))
        );
        if (data.isSolarUser) {
            nav.getChildren().add(navButton("Solar Net Metering", false, () -> openSolarNetMetering(stage)));
        }

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        Button back = secondaryButton("←  Back to Login");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(e -> fadeToLogin(stage));

        sidebar.getChildren().addAll(brandRow, profile, nav, push, back);

        animateEntrance(profile, 80);
        animateEntrance(nav, 150);
        return sidebar;
    }

    private ScrollPane buildWorkspace(Stage stage, DashboardData data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(22, 26, 30, 26));
        page.getChildren().addAll(
                buildTopBar(stage, data),
                buildKpiGrid(data),
                buildChartRow(data),
                buildInsightsRow(data)
        );

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setStyle("-fx-background: " + SHELL + "; -fx-background-color: " + SHELL + "; -fx-border-color: transparent;");
        return scrollPane;
    }

    private HBox buildTopBar(Stage stage, DashboardData data) {
        VBox copy = new VBox(4);
        Label eyebrow = label(data.currentMonthLabel, 12, TEXT_SOFT, false);
        Text title = new Text("Dashboard");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        title.setFill(Color.web(TEXT));
        Label summary = label(data.headerSummary, 13, TEXT_SOFT, false);
        summary.setWrapText(true);
        copy.getChildren().addAll(eyebrow, title, summary);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.TOP_RIGHT);
        Button period = ghostButton(data.currentMonthLabel);
        Button addReading = primaryButton("+  Add Reading");
        addReading.setOnAction(e -> openMeterReading(stage));
        actions.getChildren().addAll(period, addReading);

        HBox row = new HBox(16, copy, spacer, actions);
        row.setAlignment(Pos.CENTER_LEFT);
        animateEntrance(row, 40);
        return row;
    }

    private GridPane buildKpiGrid(DashboardData data) {
        GridPane grid = new GridPane();
        grid.setHgap(14);

        VBox units = metricCard("Units", data.unitsValue, data.unitsDelta, BLUE);
        VBox bill = metricCard("Bill", data.billValue, data.billDelta, SUCCESS);
        VBox tier = metricCard("Tier", data.tierValue, data.tierDelta, ACCENT);
        VBox ceiling = metricCard("Ceiling", data.ceilingValue, data.ceilingDelta, PURPLE);

        grid.add(units, 0, 0);
        grid.add(bill, 1, 0);
        grid.add(tier, 2, 0);
        grid.add(ceiling, 3, 0);

        GridPane.setHgrow(units, Priority.ALWAYS);
        GridPane.setHgrow(bill, Priority.ALWAYS);
        GridPane.setHgrow(tier, Priority.ALWAYS);
        GridPane.setHgrow(ceiling, Priority.ALWAYS);
        units.setMaxWidth(Double.MAX_VALUE);
        bill.setMaxWidth(Double.MAX_VALUE);
        tier.setMaxWidth(Double.MAX_VALUE);
        ceiling.setMaxWidth(Double.MAX_VALUE);
        units.setMinWidth(0);
        bill.setMinWidth(0);
        tier.setMinWidth(0);
        ceiling.setMinWidth(0);

        animateEntrance(grid, 110);
        return grid;
    }

    private HBox buildChartRow(DashboardData data) {
        HBox row = new HBox(14);

        VBox trend = chartCard(data);
        HBox.setHgrow(trend, Priority.ALWAYS);
        trend.setMinWidth(0);
        VBox watch = sideInsightCard(data);
        watch.setPrefWidth(270);
        watch.setMinWidth(0);

        row.getChildren().addAll(trend, watch);
        animateEntrance(row, 180);
        return row;
    }

    private VBox chartCard(DashboardData data) {
        VBox card = whiteCard(18);
        card.setSpacing(16);

        HBox heading = new HBox();
        heading.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(4);
        Label title = label("Consumption trend", 15, TEXT, true);
        Label sub = label("Saved readings and estimated bill trend for the current month", 12, TEXT_SOFT, false);
        titleBlock.getChildren().addAll(title, sub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox legend = new HBox(14,
                legendItem(BLUE, "Units"),
                legendItem(ACCENT, "Bill impact")
        );
        heading.getChildren().addAll(titleBlock, spacer, legend);

        if (data.chartPoints.isEmpty()) {
            VBox empty = sectionCard(16);
            empty.getChildren().addAll(
                    label("No chart data yet", 14, TEXT, true),
                    label("Save at least one meter reading this month to populate the dashboard trend charts.", 12, TEXT_SOFT, false)
            );
            card.getChildren().addAll(heading, empty);
            return card;
        }

        VBox unitsChart = buildSeriesChart(
                "Units consumed per saved reading",
                "kWh",
                BLUE,
                data.chartPoints.stream().mapToDouble(ChartPoint::units).toArray(),
                data.chartPoints.stream().map(ChartPoint::label).toArray(String[]::new),
                false
        );

        VBox billChart = buildSeriesChart(
                "Estimated bill after each saved reading",
                "Rs.",
                ACCENT,
                data.chartPoints.stream().mapToDouble(ChartPoint::bill).toArray(),
                data.chartPoints.stream().map(ChartPoint::label).toArray(String[]::new),
                true
        );

        card.getChildren().addAll(heading, unitsChart, billChart);
        return card;
    }

    private VBox buildSeriesChart(String titleText, String unitLabel, String color, double[] values, String[] labels, boolean currency) {
        VBox section = new VBox(10);

        HBox sectionHeader = new HBox();
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Region sectionSpacer = new Region();
        HBox.setHgrow(sectionSpacer, Priority.ALWAYS);
        sectionHeader.getChildren().addAll(
                label(titleText, 13, TEXT, true),
                sectionSpacer,
                label(unitLabel, 11, TEXT_FAINT, false)
        );

        double maxValue = 1;
        for (double value : values) {
            maxValue = Math.max(maxValue, value);
        }

        HBox plot = new HBox(12);
        plot.setAlignment(Pos.BOTTOM_LEFT);
        plot.setMinHeight(190);
        plot.setPrefHeight(190);

        VBox yAxis = new VBox(0);
        yAxis.setAlignment(Pos.TOP_RIGHT);
        for (String axis : buildAxisLabels(maxValue, currency)) {
            Label axisLabel = axisLabel(axis);
            axisLabel.setPrefHeight(40);
            yAxis.getChildren().add(axisLabel);
        }

        HBox chartArea = new HBox(18);
        chartArea.setAlignment(Pos.BOTTOM_LEFT);
        chartArea.setPadding(new Insets(6, 0, 0, 0));
        HBox.setHgrow(chartArea, Priority.ALWAYS);

        for (int i = 0; i < labels.length; i++) {
            chartArea.getChildren().add(singleBar(labels[i], values[i], maxValue, color, currency));
        }

        plot.getChildren().addAll(yAxis, chartArea);

        VBox axisHolder = new VBox(0);
        axisHolder.getChildren().addAll(horizontalGuide(0.25), horizontalGuide(0.25), horizontalGuide(0.25), horizontalGuide(0.25));

        StackPane graphStack = new StackPane();
        graphStack.setAlignment(Pos.BOTTOM_LEFT);
        graphStack.getChildren().addAll(axisHolder, plot);

        section.getChildren().addAll(sectionHeader, graphStack);
        return section;
    }

    private VBox sideInsightCard(DashboardData data) {
        VBox card = whiteCard(18);
        card.setSpacing(16);

        Label title = label("Status & actions", 15, TEXT, true);

        VBox content = new VBox(14);
        content.getChildren().addAll(
                insightRow(BLUE, "Last reading", data.lastReadingHeadline, data.lastReadingSubline),
                insightRow(SUCCESS, "Bill estimate", data.billHeadline, data.billSubline),
                insightRow(ACCENT, "Tariff watch", data.tariffHeadline, data.tariffSubline)
        );
        if (data.isSolarUser) {
            content.getChildren().add(insightRow(PURPLE, "Solar panel", data.solarHeadline, data.solarSubline));
        }

        Button manage = secondaryButton("Manage modules");
        manage.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(title, content, manage);
        return card;
    }

    private HBox buildInsightsRow(DashboardData data) {
        HBox row = new HBox(14);
        VBox mix = donutCard(data);
        VBox history = historyCard(data);
        HBox.setHgrow(mix, Priority.ALWAYS);
        HBox.setHgrow(history, Priority.ALWAYS);
        mix.setMinWidth(0);
        history.setMinWidth(0);
        row.getChildren().addAll(mix, history);
        animateEntrance(row, 260);
        return row;
    }

    private VBox donutCard(DashboardData data) {
        VBox card = whiteCard(18);
        card.setSpacing(16);

        Label title = label("Appliance mix", 15, TEXT, true);

        HBox layout = new HBox(18);
        layout.setAlignment(Pos.CENTER_LEFT);

        StackPane donut = buildDonut(data.applianceRows);

        FlowPane legend = new FlowPane();
        legend.setHgap(10);
        legend.setVgap(10);
        legend.setPrefWrapLength(300);

        if (data.applianceRows.isEmpty()) {
            Label empty = label("No devices yet.", 12, TEXT_SOFT, false);
            empty.setWrapText(true);
            legend.getChildren().add(empty);
        } else {
            for (ApplianceRow row : data.applianceRows) {
                legend.getChildren().add(legendChip(row.name + " - " + row.usage, row.color));
            }
        }

        layout.getChildren().addAll(donut, legend);
        HBox.setHgrow(legend, Priority.ALWAYS);

        card.getChildren().addAll(title, layout);
        return card;
    }

    private VBox historyCard(DashboardData data) {
        VBox card = whiteCard(18);
        card.setSpacing(12);

        HBox heading = new HBox();
        heading.setAlignment(Pos.CENTER_LEFT);
        Label title = label("Recent billing history", 15, TEXT, true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label live = statusPill("Live records", "#132218", SUCCESS);
        heading.getChildren().addAll(title, spacer, live);

        if (data.historyRows.isEmpty()) {
            Label empty = label("No history yet.", 12, TEXT_SOFT, false);
            empty.setWrapText(true);
            card.getChildren().addAll(heading, empty);
            return card;
        }

        VBox rows = new VBox(8);
        for (HistoryRow row : data.historyRows) {
            rows.getChildren().add(historyRow(row));
        }

        card.getChildren().addAll(heading, rows);
        return card;
    }

    private VBox metricCard(String titleText, String valueText, String deltaText, String accent) {
        VBox card = whiteCard(16);
        card.setSpacing(14);
        card.setMinHeight(114);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        StackPane dot = new StackPane(new Circle(8, Color.web(tint(accent, 0.18))), new Circle(3.5, Color.web(accent)));
        Label title = label(titleText, 12, TEXT_SOFT, false);
        top.getChildren().addAll(dot, title);

        VBox bottom = new VBox(6);
        bottom.setAlignment(Pos.CENTER_LEFT);
        Label value = label(valueText, 18, TEXT, true);
        Label delta = label(deltaText, 12, accent, true);
        value.setWrapText(true);
        delta.setWrapText(true);
        bottom.getChildren().addAll(value, delta);

        card.getChildren().addAll(top, bottom);
        return card;
    }

    private VBox whiteCard(double padding) {
        VBox card = new VBox();
        card.setPadding(new Insets(padding));
        card.setBackground(new Background(new BackgroundFill(Color.web(CARD), new CornerRadii(8), Insets.EMPTY)));
        card.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        DropShadow shadow = new DropShadow(10, 0, 3, Color.web("#00000088"));
        shadow.setSpread(0);
        card.setEffect(shadow);
        return card;
    }

    private VBox sectionCard(double padding) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(padding));
        card.setBackground(new Background(new BackgroundFill(Color.web(CARD_SUBTLE), new CornerRadii(8), Insets.EMPTY)));
        card.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        return card;
    }

    private VBox navSection(String text) {
        VBox box = new VBox();
        box.setPadding(new Insets(8, 10, 4, 10));
        box.setMinHeight(Region.USE_PREF_SIZE);
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

    private void openMeterReading(Stage stage) {
        switchScene(stage, () -> {
            try {
                new MeterReadingScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openBillView(Stage stage) {
        switchScene(stage, () -> {
            try {
                new BillViewScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openAppliances(Stage stage) {
        switchScene(stage, () -> {
            try {
                new ApplianceScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openMonthlyCeiling(Stage stage) {
        switchScene(stage, () -> {
            try {
                new MonthlyCeilingScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openReports(Stage stage) {
        switchScene(stage, () -> {
            try {
                new ReportsScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openSettings(Stage stage) {
        switchScene(stage, () -> {
            try {
                new SettingsScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openSolarNetMetering(Stage stage) {
        switchScene(stage, () -> {
            try {
                new SolarNetMeteringScreen(userEmail).start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openTariffRates(Stage stage) {
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

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(38);
        button.setMinHeight(38);
        button.setFocusTraversable(false);
        button.setWrapText(false);
        button.setStyle(
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
                        "-fx-padding: 0 14 0 14;"
        );
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(38);
        button.setMinHeight(38);
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

    private Button ghostButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(38);
        button.setMinHeight(38);
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
                        "-fx-padding: 0 12 0 12;"
        );
        return button;
    }

    private Label label(String text, int size, String color, boolean bold) {
        Label label = new Label(text);
        label.setTextFill(Color.web(color));
        label.setFont(Font.font("Verdana", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        return label;
    }

    private Label statusPill(String text, String background, String color) {
        Label pill = new Label(text);
        pill.setStyle(
                "-fx-background-color: " + background + ";" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: 10;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 4 8 4 8;"
        );
        return pill;
    }

    private HBox legendItem(String color, String text) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web(color));
        item.getChildren().addAll(dot, label(text, 11, TEXT_SOFT, false));
        return item;
    }

    private Label axisLabel(String text) {
        Label label = label(text, 10, TEXT_FAINT, false);
        label.setMinWidth(28);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }

    private Region horizontalGuide(double opacity) {
        Region line = new Region();
        line.setPrefHeight(56);
        line.setStyle("-fx-border-color: transparent transparent " + tint(TEXT_FAINT, opacity) + " transparent;");
        return line;
    }

    private VBox singleBar(String labelText, double value, double maxValue, String color, boolean currency) {
        VBox group = new VBox(8);
        group.setAlignment(Pos.BOTTOM_CENTER);
        group.setPrefWidth(78);

        double ratio = maxValue <= 0 ? 0 : value / maxValue;
        Rectangle bar = new Rectangle(22, Math.max(4, 150 * ratio));
        bar.setArcWidth(10);
        bar.setArcHeight(10);
        bar.setFill(Color.web(color));

        Label valueLabel = label(currency ? "Rs. " + currencyFormat.format(value) : numberFormat.format(value), 10, TEXT_SOFT, false);
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(72);
        valueLabel.setAlignment(Pos.CENTER);
        Label label = this.label(labelText, 11, TEXT_SOFT, false);
        group.getChildren().addAll(valueLabel, bar, label);
        return group;
    }

    private List<String> buildAxisLabels(double maxValue, boolean currency) {
        List<String> labels = new ArrayList<>();
        for (int step = 4; step >= 0; step--) {
            double value = (maxValue * step) / 4.0;
            labels.add(currency ? currencyFormat.format(value) : numberFormat.format(value));
        }
        return labels;
    }

    private HBox insightRow(String accent, String titleText, String headline, String detail) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Circle dot = new Circle(5, Color.web(accent));
        VBox copy = new VBox(3);
        Label title = label(titleText, 11, TEXT_FAINT, false);
        Label main = label(headline, 13, TEXT, true);
        Label sub = label(detail, 12, TEXT_SOFT, false);
        sub.setWrapText(true);
        copy.getChildren().addAll(title, main, sub);
        row.getChildren().addAll(dot, copy);
        return row;
    }

    private StackPane buildDonut(List<ApplianceRow> rows) {
        StackPane stack = new StackPane();
        stack.setPrefSize(190, 190);

        Circle track = new Circle(66);
        track.setFill(Color.TRANSPARENT);
        track.setStroke(Color.web("#262626"));
        track.setStrokeWidth(20);

        stack.getChildren().add(track);

        double start = 90;
        List<ApplianceRow> source = rows.isEmpty() ? defaultApplianceRows() : rows;
        for (ApplianceRow row : source) {
            Arc arc = new Arc(0, 0, 66, 66, start, -360 * row.fraction);
            arc.setType(ArcType.OPEN);
            arc.setStroke(Color.web(row.color));
            arc.setStrokeWidth(20);
            arc.setFill(Color.TRANSPARENT);
            arc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.BUTT);
            stack.getChildren().add(arc);
            start -= 360 * row.fraction;
        }

        VBox center = new VBox(2);
        center.setAlignment(Pos.CENTER);
        Label value = label(rows.isEmpty() ? "0" : String.valueOf(rows.size()), 22, TEXT, true);
        Label sub = label("sources", 11, TEXT_SOFT, false);
        center.getChildren().addAll(value, sub);
        stack.getChildren().add(center);

        return stack;
    }

    private Label legendChip(String text, String color) {
        Label chip = label("●  " + text, 11, TEXT_SOFT, false);
        chip.setStyle(
                "-fx-background-color: #191919;" +
                        "-fx-text-fill: " + TEXT_SOFT + ";" +
                        "-fx-font-size: 11;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 10 6 10;"
        );
        chip.setTextFill(Color.web(TEXT_SOFT));
        return chip;
    }

    private HBox historyRow(HistoryRow row) {
        HBox line = new HBox();
        line.setAlignment(Pos.CENTER_LEFT);
        line.setPadding(new Insets(10, 12, 10, 12));
        line.setBackground(new Background(new BackgroundFill(Color.web(CARD_SUBTLE), new CornerRadii(8), Insets.EMPTY)));
        line.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        VBox left = new VBox(3);
        left.getChildren().addAll(
                label(row.cycle, 12, TEXT, true),
                label(row.status, 11, TEXT_SOFT, false)
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amount = label(row.amount, 12, TEXT, true);
        line.getChildren().addAll(left, spacer, amount);
        return line;
    }

    private String tint(String color, double opacity) {
        Color base = Color.web(color, opacity);
        return String.format("rgba(%d,%d,%d,%.3f)",
                (int) Math.round(base.getRed() * 255),
                (int) Math.round(base.getGreen() * 255),
                (int) Math.round(base.getBlue() * 255),
                base.getOpacity());
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

    private void fadeToLogin(Stage stage) {
        try {
            new LoginScreen().start(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private DashboardData loadDashboardData() {
        DashboardData data = new DashboardData();
        data.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        data.currentMonthLabel = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        data.displayName = "User";
        data.accountTypeLabel = "Grid User";
        data.headerSummary = "Live overview";
        data.nextActionText = "Add reading";
        populateFallbackMetrics(data);

        User user;
        try {
            user = userService.getUserByEmail(userEmail);
        } catch (Exception ex) {
            data.headerSummary = "Dashboard could not load the signed-in user profile.";
            data.nextActionText = "Check login";
            return data;
        }

        if (user == null) {
            data.headerSummary = "Dashboard could not find the signed-in user profile.";
            data.nextActionText = "Check login";
            return data;
        }

        data.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
        data.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());
        data.accountTypeLabel = data.isSolarUser ? "Solar User" : "Grid User";

        MeterReading lastReading = null;
        float totalUnits = 0;
        ElectricityBill latestBill = null;
        List<ElectricityBill> history = new ArrayList<>();
        List<Appliance> appliances = new ArrayList<>();
        MonthlyBudget budget = null;
        SolarMeter solarMeter = null;

        try {
            lastReading = meterReadingDAO.getLastReading(user.getUserID());
            totalUnits = meterReadingDAO.getTotalUnitsThisCycle(user.getUserID());
            data.hasReadings = totalUnits > 0;
        } catch (Exception ignored) {
        }

        try {
            latestBill = billingService.getLatestBill(user.getUserID());
            if (latestBill == null && totalUnits > 0) {
                float generated = billingService.calculateAndRecordBill(user.getUserID(), totalUnits, user.getAccountType());
                latestBill = new ElectricityBill();
                latestBill.setEstimatedAmount(generated);
                latestBill.setBillingCycle(currentMonthKey());
                latestBill.setStatus("estimated");
            }
        } catch (Exception ignored) {
        }

        try {
            history = billingService.getBillingHistory(user.getUserID());
        } catch (Exception ignored) {
        }

        try {
            appliances = applianceDAO.getAppliancesByUser(user.getUserID());
        } catch (Exception ignored) {
        }

        try {
            budget = budgetDAO.getBudgetByUserAndMonth(user.getUserID(), currentMonthKey());
        } catch (Exception ignored) {
        }

        if (data.isSolarUser) {
            try {
                solarMeter = solarDAO.getLatestSolarReading(user.getUserID());
            } catch (Exception ignored) {
            }
        }

        try {
            List<MeterReading> currentMonthReadings = meterReadingDAO.getReadingsByPeriod(
                    user.getUserID(),
                    java.sql.Date.valueOf(LocalDate.now().withDayOfMonth(1)),
                    java.sql.Date.valueOf(LocalDate.now())
            );
            data.chartPoints = buildChartPoints(currentMonthReadings, user.getAccountType());
        } catch (Exception ignored) {
            data.chartPoints = new ArrayList<>();
        }

        TariffRate activeTier = safeTariff(totalUnits, user.getAccountType());

        data.unitsValue = totalUnits > 0 ? numberFormat.format(totalUnits) + " kWh" : "No readings";
        data.unitsDelta = lastReading == null ? "Awaiting first reading" : "Last reading " + formatDate(lastReading.getDate());

        data.billValue = latestBill == null ? "Unavailable" : "Rs. " + currencyFormat.format(latestBill.getEstimatedAmount());
        data.billDelta = latestBill == null ? "Needs consumption data" : "Cycle " + latestBill.getBillingCycle();

        data.tierValue = activeTier == null ? "Not available" : activeTier.getTierName();
        data.tierDelta = activeTier == null ? "Tariff data missing" : unitsLeftLabel(activeTier, totalUnits);

        if (budget != null && budget.getCeilingAmount() > 0) {
            double progress = Math.min(totalUnits / budget.getCeilingAmount(), 1.25);
            data.ceilingValue = (int) Math.round(progress * 100) + "%";
            data.ceilingDelta = progress >= 1 ? "Ceiling exceeded" : numberFormat.format(Math.max(0, budget.getCeilingAmount() - totalUnits)) + " units left";
        }

        data.lastReadingHeadline = lastReading == null ? "No reading yet" : numberFormat.format(lastReading.getUnitsConsumed()) + " units captured";
        data.lastReadingSubline = lastReading == null
                ? "Use the meter reading module to record the first entry."
                : "Recorded on " + formatDate(lastReading.getDate()) + " from meter value " + numberFormat.format(lastReading.getValue()) + ".";

        data.billHeadline = latestBill == null ? "No stored estimate" : "Estimate ready";
        data.billSubline = latestBill == null
                ? "The bill panel activates once readings are stored."
                : "Latest estimate stands at Rs. " + currencyFormat.format(latestBill.getEstimatedAmount()) + ".";

        data.tariffHeadline = activeTier == null ? "Tier unknown" : activeTier.getTierName() + " active";
        data.tariffSubline = activeTier == null ? "Tariff slabs could not be loaded." : unitsLeftNarrative(activeTier, totalUnits);

        data.solarHeadline = data.isSolarUser
                ? (solarMeter == null ? "No solar reading yet" : numberFormat.format(solarMeter.getGenerationReading()) + " solar units")
                : "";
        data.solarSubline = data.isSolarUser
                ? (solarMeter == null ? "Log generation to unlock net-metering analytics." :
                "Exported " + numberFormat.format(solarMeter.getExportedUnits()) + " units with credit Rs. " + currencyFormat.format(solarMeter.getNetCredit()) + ".")
                : "";

        data.applianceRows = buildApplianceRows(appliances);
        data.historyRows = buildHistoryRows(history);
        data.headerSummary = buildHeaderSummary(totalUnits, latestBill, budget, data.isSolarUser, solarMeter);
        data.nextActionText = inferNextAction(lastReading, budget, appliances, data.isSolarUser, solarMeter);
        return data;
    }

    private void populateFallbackMetrics(DashboardData data) {
        data.unitsValue = "No readings";
        data.unitsDelta = "Awaiting first reading";
        data.billValue = "Unavailable";
        data.billDelta = "Needs consumption data";
        data.tierValue = "Not available";
        data.tierDelta = "Tariff data missing";
        data.ceilingValue = "Not set";
        data.ceilingDelta = "Add monthly limit";

        data.lastReadingHeadline = "No reading yet";
        data.lastReadingSubline = "Use the meter reading module to record the first entry.";
        data.billHeadline = "No stored estimate";
        data.billSubline = "The bill panel activates once readings are stored.";
        data.tariffHeadline = "Tier unknown";
        data.tariffSubline = "Tariff slabs could not be loaded.";
        data.applianceRows = new ArrayList<>();
        data.historyRows = new ArrayList<>();
    }

    private List<ApplianceRow> buildApplianceRows(List<Appliance> appliances) {
        List<ApplianceRow> rows = new ArrayList<>();
        if (appliances == null || appliances.isEmpty()) {
            return rows;
        }

        float total = 0;
        for (Appliance appliance : appliances) {
            total += appliance.getLiveReading(appliance.getApplianceID());
        }
        if (total <= 0) {
            total = 1;
        }

        String[] colors = {PURPLE, BLUE, MINT, PINK, ACCENT, RED};
        appliances.sort(Comparator.comparingDouble(a -> -a.getLiveReading(a.getApplianceID())));
        for (int i = 0; i < Math.min(6, appliances.size()); i++) {
            Appliance appliance = appliances.get(i);
            float usage = appliance.getLiveReading(appliance.getApplianceID());
            rows.add(new ApplianceRow(
                    appliance.getName(),
                    numberFormat.format(usage) + " kWh",
                    Math.max(0.05, usage / total),
                    colors[i % colors.length]
            ));
        }
        return rows;
    }

    private List<ApplianceRow> defaultApplianceRows() {
        List<ApplianceRow> rows = new ArrayList<>();
        rows.add(new ApplianceRow("Cooling", "0 kWh", 0.26, PURPLE));
        rows.add(new ApplianceRow("Water heating", "0 kWh", 0.18, BLUE));
        rows.add(new ApplianceRow("Lighting", "0 kWh", 0.14, MINT));
        rows.add(new ApplianceRow("Kitchen", "0 kWh", 0.12, PINK));
        rows.add(new ApplianceRow("Laundry", "0 kWh", 0.1, ACCENT));
        rows.add(new ApplianceRow("Other", "0 kWh", 0.2, RED));
        return rows;
    }

    private List<HistoryRow> buildHistoryRows(List<ElectricityBill> history) {
        List<HistoryRow> rows = new ArrayList<>();
        if (history == null) {
            return rows;
        }
        for (int i = 0; i < Math.min(5, history.size()); i++) {
            ElectricityBill bill = history.get(i);
            rows.add(new HistoryRow(
                    bill.getBillingCycle() == null ? "Cycle " + (i + 1) : bill.getBillingCycle(),
                    bill.getStatus() == null ? "estimated" : bill.getStatus(),
                    "Rs. " + currencyFormat.format(bill.getEstimatedAmount())
            ));
        }
        return rows;
    }

    private List<ChartPoint> buildChartPoints(List<MeterReading> readings, String accountType) {
        List<ChartPoint> points = new ArrayList<>();
        if (readings == null || readings.isEmpty()) {
            return points;
        }

        TariffEngine engine = new TariffEngine(accountType);
        float cumulativeUnits = 0;
        int startIndex = Math.max(0, readings.size() - 7);
        for (int i = startIndex; i < readings.size(); i++) {
            MeterReading reading = readings.get(i);
            cumulativeUnits += reading.getUnitsConsumed();
            float baseAmount = engine.applyTariffStructure(cumulativeUnits);
            float fixedCharge = engine.getFixedCharge(cumulativeUnits);
            float bill = billingService.includeFixedChargesAndTaxes(baseAmount, fixedCharge);
            String label = new SimpleDateFormat("dd MMM", Locale.ENGLISH).format(reading.getDate());
            points.add(new ChartPoint(label, reading.getUnitsConsumed(), Math.round(bill * 100.0f) / 100.0f));
        }
        return points;
    }

    private TariffRate safeTariff(float totalUnits, String accountType) {
        try {
            return new TariffEngine(accountType).getTariffForUnits(totalUnits);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String buildHeaderSummary(float totalUnits, ElectricityBill bill, MonthlyBudget budget, boolean isSolarUser, SolarMeter solarMeter) {
        StringBuilder summary = new StringBuilder();
        if (totalUnits > 0) {
            summary.append("Current cycle usage is ").append(numberFormat.format(totalUnits)).append(" units");
        } else {
            summary.append("No meter readings have been recorded for this cycle yet");
        }

        if (bill != null) {
            summary.append(", with an estimated bill of Rs. ").append(currencyFormat.format(bill.getEstimatedAmount()));
        }

        if (budget != null && budget.getCeilingAmount() > 0) {
            summary.append(". Ceiling set at ").append(numberFormat.format(budget.getCeilingAmount())).append(" units");
        }

        if (isSolarUser) {
            if (solarMeter != null) {
                summary.append(". Solar generation logged: ").append(numberFormat.format(solarMeter.getGenerationReading())).append(" units");
            } else {
                summary.append(". Solar mode is enabled but no generation has been recorded");
            }
        }
        summary.append(".");
        return summary.toString();
    }

    private String inferNextAction(MeterReading reading, MonthlyBudget budget, List<Appliance> appliances, boolean isSolarUser, SolarMeter solarMeter) {
        if (reading == null) {
            return "Add reading";
        }
        if (budget == null) {
            return "Set ceiling";
        }
        if (appliances == null || appliances.isEmpty()) {
            return "Add devices";
        }
        if (isSolarUser && solarMeter == null) {
            return "Add solar";
        }
        return "View reports";
    }

    private String currentMonthKey() {
        LocalDate now = LocalDate.now();
        return now.getMonth().toString() + " " + now.getYear();
    }

    private String formatDate(java.util.Date date) {
        return date == null ? "Unknown date" : dateFormat.format(date);
    }

    private String unitsLeftLabel(TariffRate tier, float totalUnits) {
        if (tier.getMaxUnits() <= 0) {
            return "Top slab";
        }
        float remaining = Math.max(0, tier.getMaxUnits() - totalUnits);
        return numberFormat.format(remaining) + " left";
    }

    private String unitsLeftNarrative(TariffRate tier, float totalUnits) {
        if (tier.getMaxUnits() <= 0) {
            return "You are currently in the highest configured tariff tier.";
        }
        float remaining = Math.max(0, tier.getMaxUnits() - totalUnits);
        return numberFormat.format(remaining) + " units remain before the next slab boundary.";
    }

    private static class DashboardData {
        String displayName;
        String userEmailLabel;
        String accountTypeLabel;
        String currentMonthLabel;
        String headerSummary;
        String nextActionText;
        boolean isSolarUser;
        boolean hasReadings;

        String unitsValue;
        String unitsDelta;
        String billValue;
        String billDelta;
        String tierValue;
        String tierDelta;
        String ceilingValue;
        String ceilingDelta;

        String lastReadingHeadline;
        String lastReadingSubline;
        String billHeadline;
        String billSubline;
        String tariffHeadline;
        String tariffSubline;
        String solarHeadline;
        String solarSubline;

        List<ApplianceRow> applianceRows = new ArrayList<>();
        List<HistoryRow> historyRows = new ArrayList<>();
        List<ChartPoint> chartPoints = new ArrayList<>();
    }

    private record ApplianceRow(String name, String usage, double fraction, String color) { }

    private record HistoryRow(String cycle, String status, String amount) { }

    private record ChartPoint(String label, double units, double bill) { }
}
