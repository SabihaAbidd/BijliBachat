package com.example.bijlibachat.ui;

import com.example.bijlibachat.model.MonthlyBudget;
import com.example.bijlibachat.model.User;
import com.example.bijlibachat.service.AlertService;
import com.example.bijlibachat.service.ConsumptionObserver;
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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class MonthlyCeilingScreen {

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
    private final AlertService alertService = new AlertService();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");

    public MonthlyCeilingScreen(String userEmail) {
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void start(Stage stage) {
        CeilingViewModel data = loadData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildContent(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Monthly Ceiling", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(280), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, CeilingViewModel data) {
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
        brandText.getChildren().addAll(label("Bijli Bachat", 18, TEXT, true), label("Monthly ceiling", 11, TEXT_SOFT, false));
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
                navButton("Monthly Ceiling", true, null),
                navButton("Reports", false, () -> goToReports(stage)),
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

    private ScrollPane buildContent(Stage stage, CeilingViewModel data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24, 26, 30, 26));

        VBox hero = cardBox(20, CARD);
        hero.setSpacing(18);
        HBox heroTop = new HBox();
        heroTop.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(6);
        copy.getChildren().addAll(
                label("Monthly ceiling", 12, TEXT_SOFT, false),
                label("Track budget", 26, TEXT, true),
                label("Usage limit", 13, TEXT_SOFT, false)
        );
        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);
        VBox highlight = cardBox(16, CARD_SUBTLE);
        highlight.setEffect(null);
        highlight.getChildren().addAll(
                statusPill("Ceiling", ACCENT),
                label(data.progressValue, 24, TEXT, true),
                label("Current use", 12, TEXT_SOFT, false)
        );
        heroTop.getChildren().addAll(copy, heroSpacer, highlight);

        HBox bands = new HBox(12);
        bands.getChildren().addAll(
                heroBand("Current units", data.currentUnitsValue, BLUE),
                heroBand("Ceiling", data.ceilingValue, SUCCESS),
                heroBand("Alert state", data.alertHeadline, PURPLE)
        );
        for (Node node : bands.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        hero.getChildren().addAll(heroTop, bands);

        HBox lower = new HBox(14);
        VBox formCard = buildFormCard(stage, data);
        VBox alertCard = buildAlertCard(data);
        HBox.setHgrow(formCard, Priority.ALWAYS);
        HBox.setHgrow(alertCard, Priority.ALWAYS);
        lower.getChildren().addAll(formCard, alertCard);

        page.getChildren().addAll(hero, lower);
        animateEntrance(hero, 40);
        animateEntrance(lower, 120);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setStyle("-fx-background: " + SHELL + "; -fx-background-color: " + SHELL + "; -fx-border-color: transparent;");
        return scrollPane;
    }

    private VBox buildFormCard(Stage stage, CeilingViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(16);

        Label title = label("Set limit", 16, TEXT, true);
        Label sub = label(data.monthLabel + " budget", 12, TEXT_SOFT, false);
        sub.setWrapText(true);

        TextField ceilingField = styledField("Ceiling in units");
        if (data.currentBudget != null) {
            ceilingField.setText(numberFormat.format(data.currentBudget.getCeilingAmount()));
        }

        ProgressBar progressBar = new ProgressBar(Math.min(1, data.progressRatio));
        progressBar.setPrefHeight(10);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: " + (data.progressRatio >= 1 ? DANGER : data.progressRatio >= 0.8 ? ACCENT : SUCCESS) + ";");

        Label progressCopy = label(data.progressValue + " of ceiling used", 12, TEXT_SOFT, false);
        Label message = label("", 12, TEXT_SOFT, false);
        message.setWrapText(true);
        alertService.addObserver(new com.example.bijlibachat.service.EmailObserver(userEmail));
        alertService.addObserver(new ConsumptionObserver() {
            @Override
            public void onThresholdReached(float consumption, float threshold, float costRisk) {
                // existing code
                setMessage(message, "...", ACCENT);
                // ADD THIS — popup alert
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle("Consumption Warning");
                    alert.setHeaderText("You are approaching your ceiling!");
                    alert.setContentText("You have used " + numberFormat.format(consumption) +
                            " out of " + numberFormat.format(threshold) +
                            "  (80%+). Reduce usage to avoid higher bills.");
                    alert.showAndWait();
                });
            }

            public void onCeilingExceeded(float consumption, float ceiling) {
                // existing code
                setMessage(message, "...", DANGER);

                // ADD THIS — popup alert
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Ceiling Exceeded!");
                    alert.setHeaderText("Monthly ceiling exceeded!");
                    alert.setContentText("You have used " + numberFormat.format(consumption) +
                            " units against your ceiling of " + numberFormat.format(ceiling) +
                            " units. You are now in the most expensive tariff tier.");
                    alert.showAndWait();
                });
            }
        });

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button reset = secondaryButton("Reset");
        reset.setOnAction(e -> {
            ceilingField.setText(data.currentBudget == null ? "" : numberFormat.format(data.currentBudget.getCeilingAmount()));
            message.setText("");
        });
        Button save = primaryButton("Save ceiling");
        save.setOnAction(e -> {
            String text = ceilingField.getText().trim();
            if (text.isEmpty()) {
                setMessage(message, "Enter a monthly ceiling before saving.", DANGER);
                return;
            }

            float ceilingUnits;
            try {
                ceilingUnits = Float.parseFloat(text);
            } catch (NumberFormatException ex) {
                setMessage(message, "Ceiling must be numeric.", DANGER);
                return;
            }

            if (ceilingUnits <= 0) {
                setMessage(message, "Ceiling must be greater than zero.", DANGER);
                return;
            }

            try {
                reportService.saveCeiling(data.userId, ceilingUnits, data.monthLabel);
                alertService.monitorConsumption(data.currentUnits, ceilingUnits);
                if (message.getText().isEmpty()) {
                    setMessage(message, "Monthly ceiling saved. Dashboard progress now maps to the same " + data.monthLabel + " budget.", SUCCESS);
                }
                switchScene(stage, () -> new MonthlyCeilingScreen(userEmail).start(stage));
            } catch (Exception ex) {
                setMessage(message, "Could not save the monthly ceiling. Check the database connection and try again.", DANGER);
            }
        });
        actions.getChildren().addAll(reset, save);

        card.getChildren().addAll(title, sub, fieldGroup("Ceiling units", ceilingField), progressBar, progressCopy, message, actions);
        return card;
    }

    private VBox buildAlertCard(CeilingViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(14);
        ProgressBar progressBar = new ProgressBar(Math.min(1, data.progressRatio));
        progressBar.setPrefHeight(12);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: " + (data.progressRatio >= 1 ? DANGER : data.progressRatio >= 0.8 ? ACCENT : SUCCESS) + ";");

        card.getChildren().addAll(
                label("Live pressure", 16, TEXT, true),
                label(data.alertDetail, 12, TEXT_SOFT, false),
                progressBar,
                checklistRow(data.currentBudget == null ? "No ceiling saved yet for this month." : "Ceiling is stored for " + data.monthLabel + "."),
                checklistRow("Dashboard progress and this module read the same monthly budget."),
                checklistRow("Threshold alert starts at 80% usage and exceeded alert starts at 100%.")
        );
        return card;
    }

    private CeilingViewModel loadData() {
        CeilingViewModel vm = new CeilingViewModel();
        vm.displayName = "User";
        vm.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        vm.accountTypeLabel = "Grid User";
        vm.monthLabel = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        vm.currentUnitsValue = "0 kWh";
        vm.ceilingValue = "Not set";
        vm.progressValue = "0%";
        vm.progressNarrative = "Add a ceiling to start tracking";
        vm.alertHeadline = "No alert";
        vm.alertDetail = "Save a monthly ceiling to start threshold monitoring.";

        try {
            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                return vm;
            }

            vm.userId = user.getUserID();
            vm.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
            vm.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());
            vm.accountTypeLabel = vm.isSolarUser ? "Solar User" : "Grid User";
            vm.currentUnits = reportService.getCurrentCycleUnits(user.getUserID());
            vm.currentBudget = reportService.getCurrentBudget(user.getUserID(), vm.monthLabel);
            vm.currentUnitsValue = numberFormat.format(vm.currentUnits) + " kWh";

            if (vm.currentBudget != null && vm.currentBudget.getCeilingAmount() > 0) {
                vm.ceilingValue = numberFormat.format(vm.currentBudget.getCeilingAmount()) + " kWh";
                vm.progressRatio = vm.currentUnits / vm.currentBudget.getCeilingAmount();
                vm.progressValue = numberFormat.format(Math.max(0, vm.progressRatio) * 100) + "%";
                vm.progressNarrative = vm.progressRatio >= 1
                        ? "Ceiling exceeded this cycle"
                        : numberFormat.format(Math.max(0, vm.currentBudget.getCeilingAmount() - vm.currentUnits)) + " units still available";

                if (vm.progressRatio >= 1) {
                    vm.alertHeadline = "Exceeded";
                    vm.alertDetail = "Consumption is above the saved ceiling. Review appliance load and solar offset immediately.";
                } else if (vm.progressRatio >= 0.8) {
                    vm.alertHeadline = "Threshold reached";
                    vm.alertDetail = "Usage is above 80% of the monthly ceiling. The user should reduce load before the next tier pressure increases.";
                } else {
                    vm.alertHeadline = "Healthy";
                    vm.alertDetail = "Usage is below the threshold and the monthly target is still under control.";
                }
            }
        } catch (Exception ignored) {
            // Keep fallback values
        }

        return vm;
    }

    private VBox fieldGroup(String title, Node field) {
        VBox box = new VBox(8);
        box.getChildren().addAll(label(title, 13, TEXT, true), field);
        return box;
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

    private HBox checklistRow(String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web(ACCENT));
        Label copy = label(text, 12, TEXT_SOFT, false);
        copy.setWrapText(true);
        row.getChildren().addAll(dot, copy);
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

    private TextField styledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setStyle(
                "-fx-background-color: " + CARD_SUBTLE + ";" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-prompt-text-fill: " + TEXT_FAINT + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13;" +
                        "-fx-font-family: 'Verdana';"
        );
        return field;
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

    private void goToReports(Stage stage) {
        switchScene(stage, () -> new ReportsScreen(userEmail).start(stage));
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

    private static class CeilingViewModel {
        int userId;
        boolean isSolarUser;
        float currentUnits;
        float progressRatio;
        MonthlyBudget currentBudget;
        String displayName;
        String userEmailLabel;
        String accountTypeLabel;
        String monthLabel;
        String currentUnitsValue;
        String ceilingValue;
        String progressValue;
        String progressNarrative;
        String alertHeadline;
        String alertDetail;
    }
}
