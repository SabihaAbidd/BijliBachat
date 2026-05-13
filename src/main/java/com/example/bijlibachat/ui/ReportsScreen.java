package com.example.bijlibachat.ui;

import com.example.bijlibachat.model.ConsumptionReport;
import com.example.bijlibachat.model.User;
import com.example.bijlibachat.service.ReportService;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;

public class ReportsScreen {

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
    private final UserService userService = new UserService();
    private final ReportService reportService = new ReportService();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    public ReportsScreen(String userEmail) {
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void start(Stage stage) {
        ReportViewModel data = loadData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildContent(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Reports", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(280), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, ReportViewModel data) {
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
        brandText.getChildren().addAll(label("Bijli Bachat", 18, TEXT, true), label("Reports", 11, TEXT_SOFT, false));
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
                navButton("Reports", true, null),
                navButton("Tariff Rates", false, () -> goToTariffRates(stage)),
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

    private ScrollPane buildContent(Stage stage, ReportViewModel data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24, 26, 30, 26));

        VBox hero = cardBox(20, CARD);
        hero.setSpacing(18);
        HBox heroTop = new HBox();
        heroTop.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(6);
        copy.getChildren().addAll(
                label("Consumption reports", 12, TEXT_SOFT, false),
                label("Generate a clean range-based summary", 26, TEXT, true),
                label("Pick dates to preview total usage and estimated cost for any selected period on this screen.", 13, TEXT_SOFT, false)
        );
        heroTop.getChildren().addAll(copy);

        HBox bands = new HBox(12);
        bands.getChildren().addAll(
                heroBand("Current cycle units", data.currentUnitsValue, BLUE),
                heroBand("Estimated base cost", data.currentCostValue, SUCCESS),
                heroBand("Account", data.accountTypeLabel, PURPLE)
        );
        for (Node node : bands.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        hero.getChildren().addAll(heroTop, bands);

        VBox formCard = buildReportForm(stage, data);
        page.getChildren().addAll(hero, formCard);
        animateEntrance(hero, 40);
        animateEntrance(formCard, 120);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setStyle("-fx-background: " + SHELL + "; -fx-background-color: " + SHELL + "; -fx-border-color: transparent;");
        return scrollPane;
    }

    private VBox buildReportForm(Stage stage, ReportViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(16);

        Label title = label("Generate report", 16, TEXT, true);
        Label sub = label("Select a reporting window to generate an on-screen summary. This version does not export or save a file yet.", 12, TEXT_SOFT, false);
        sub.setWrapText(true);

        DatePicker startPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker endPicker = new DatePicker(LocalDate.now());
        styleDatePicker(startPicker);
        styleDatePicker(endPicker);

        Label periodValue = label(data.generatedPeriod, 14, TEXT, true);
        Label unitsValue = label(data.generatedUnits, 18, ACCENT, true);
        Label costValue = label(data.generatedCost, 16, TEXT, true);
        Label generatedOn = label(data.generatedOn, 12, TEXT_SOFT, false);
        Label exportPaths = label(data.generatedFiles, 12, TEXT_SOFT, false);
        exportPaths.setWrapText(true);
        Label message = label("", 12, TEXT_SOFT, false);
        message.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button reset = secondaryButton("Reset");
        reset.setOnAction(e -> {
            startPicker.setValue(LocalDate.now().withDayOfMonth(1));
            endPicker.setValue(LocalDate.now());
            periodValue.setText(data.generatedPeriod);
            unitsValue.setText(data.generatedUnits);
            costValue.setText(data.generatedCost);
            generatedOn.setText(data.generatedOn);
            exportPaths.setText(data.generatedFiles);
            message.setText("");
        });
        Button generate = primaryButton("Generate report");
        generate.setOnAction(e -> {
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();

            if (start == null || end == null) {
                setMessage(message, "Pick both start and end dates.", DANGER);
                return;
            }
            if (end.isBefore(start)) {
                setMessage(message, "End date cannot be earlier than start date.", DANGER);
                return;
            }

            try {
                ConsumptionReport report = reportService.generateReport(
                        data.userId,
                        Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        data.accountTypeRaw
                );

                report = reportService.exportReportFiles(report, data.accountTypeRaw);
                periodValue.setText(report.getPeriod());
                unitsValue.setText(numberFormat.format(report.getTotalUnits()) + " kWh");
                costValue.setText("Rs. " + numberFormat.format(report.getEstimatedCost()));
                generatedOn.setText("Generated on " + dateFormat.format(report.getGeneratedDate()));
                exportPaths.setText("CSV: " + report.getCsvPath() + "\nPDF: " + report.getPdfPath());
                setMessage(message, "Report exported successfully.", SUCCESS);
            } catch (Exception ex) {
                setMessage(message, "Could not generate the report. Check the database connection and try again.", DANGER);
            }
        });
        actions.getChildren().addAll(reset, generate);

        card.getChildren().addAll(
                title,
                sub,
                fieldGroup("Start date", startPicker),
                fieldGroup("End date", endPicker),
                summaryRow("Selected period", periodValue),
                summaryRow("Total units", unitsValue),
                summaryRow("Estimated cost", costValue),
                generatedOn,
                exportPaths,
                message,
                actions
        );
        return card;
    }

    private ReportViewModel loadData() {
        ReportViewModel vm = new ReportViewModel();
        vm.displayName = "User";
        vm.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        vm.accountTypeLabel = "Grid User";
        vm.accountTypeRaw = "grid";
        vm.currentMonthLabel = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        vm.currentUnitsValue = "0 kWh";
        vm.currentCostValue = "Rs. 0";
        vm.generatedPeriod = "No report generated yet";
        vm.generatedUnits = "0 kWh";
        vm.generatedCost = "Rs. 0";
        vm.generatedOn = "Generate a report to see the timestamp";
        vm.generatedFiles = "Generated files will be saved in the reports folder.";

        try {
            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                return vm;
            }
            vm.userId = user.getUserID();
            vm.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
            vm.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());
            vm.accountTypeRaw = vm.isSolarUser ? "solar" : "grid";
            vm.accountTypeLabel = vm.isSolarUser ? "Solar User" : "Grid User";

            float units = reportService.getCurrentCycleUnits(user.getUserID());
            vm.currentUnitsValue = numberFormat.format(units) + " kWh";
            vm.currentCostValue = "Rs. " + numberFormat.format(reportService.estimateCost(units, vm.accountTypeRaw));
        } catch (Exception ignored) {
            // Keep fallback values
        }
        return vm;
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

    private VBox fieldGroup(String title, Node field) {
        VBox box = new VBox(8);
        box.getChildren().addAll(label(title, 13, TEXT, true), field);
        return box;
    }

    private HBox summaryRow(String left, Node right) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label leftLabel = label(left, 13, TEXT_SOFT, false);
        HBox.setHgrow(leftLabel, Priority.ALWAYS);
        row.getChildren().addAll(leftLabel, right);
        return row;
    }

    private VBox cardBox(double padding, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(padding));
        card.setBackground(new Background(new BackgroundFill(Color.web(color), new CornerRadii(8), Insets.EMPTY)));
        card.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setEffect(new DropShadow(10, 0, 3, Color.web("#00000088")));
        return card;
    }

    private void styleDatePicker(DatePicker picker) {
        picker.setPrefHeight(42);
        picker.setStyle(
                "-fx-background-color: " + CARD_SUBTLE + ";" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13;" +
                        "-fx-font-family: 'Verdana';"
        );
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(40);
        button.setMinHeight(40);
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

    private VBox navSection(String text) {
        VBox box = new VBox();
        box.setPadding(new Insets(8, 10, 4, 10));
        box.setMinHeight(Region.USE_PREF_SIZE);
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

    private void setMessage(Label label, String text, String color) {
        label.setText(text);
        label.setTextFill(Color.web(color));
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

    private void goToSettings(Stage stage) {
        switchScene(stage, () -> new SettingsScreen(userEmail).start(stage));
    }

    private void goToSolar(Stage stage) {
        switchScene(stage, () -> new SolarNetMeteringScreen(userEmail).start(stage));
    }

    private void goToTariffRates(Stage stage) {
        switchScene(stage, () -> new TariffRatesScreen(userEmail).start(stage));
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

    private static class ReportViewModel {
        int userId;
        boolean isSolarUser;
        String displayName;
        String userEmailLabel;
        String accountTypeLabel;
        String accountTypeRaw;
        String currentMonthLabel;
        String currentUnitsValue;
        String currentCostValue;
        String generatedPeriod;
        String generatedUnits;
        String generatedCost;
        String generatedOn;
        String generatedFiles;
    }
}
