package com.example.bijlibachat.ui;
import javafx.scene.control.ScrollPane;
import com.example.bijlibachat.dao.MeterReadingDAO;
import com.example.bijlibachat.model.MeterReading;
import com.example.bijlibachat.model.User;
import com.example.bijlibachat.service.BillingService;
import com.example.bijlibachat.service.SolarService;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class MeterReadingScreen {

    private static final String SHELL = "#0D0D0D";
    private static final String SIDEBAR = "#111111";
    private static final String CARD = "#161616";
    private static final String CARD_SUBTLE = "#141414";
    private static final String BORDER = "#252525";
    private static final String TEXT = "#FFFFFF";
    private static final String TEXT_SOFT = "#9CA3AF";
    private static final String TEXT_FAINT = "#6B7280";
    private static final String ACCENT = "#F5A623";
    private static final String BLUE = "#5E7BF9";
    private static final String SUCCESS = "#23B26D";
    private static final String DANGER = "#F46D6D";
    private static final String PURPLE = "#8C5CF6";

    private final String userEmail;
    private final UserService userService = new UserService();
    private final MeterReadingDAO meterReadingDAO = new MeterReadingDAO();
    private final BillingService billingService = new BillingService();
    private final SolarService solarService = new SolarService();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    public MeterReadingScreen(String userEmail) {
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void start(Stage stage) {
        MeterReadingViewModel data = loadData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildContent(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Meter Reading", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(280), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, MeterReadingViewModel data) {
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
        brandText.getChildren().addAll(label("Bijli Bachat", 18, TEXT, true), label("Meter entry", 11, TEXT_SOFT, false));
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
                navButton("Meter Reading", true, null),
                navButton("Bills & History", false, () -> goToBillView(stage)),
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

    private ScrollPane buildContent(Stage stage, MeterReadingViewModel data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24, 26, 50, 26)); // 50 bottom padding for breathing room

        VBox top = buildHeroHeader(data);

        HBox summaryRow = new HBox(14);
        summaryRow.getChildren().addAll(
                metricCard("Last grid reading", data.lastGridValue, data.lastGridDate, BLUE),
                metricCard("Units from last entry", data.lastUnitsValue, data.unitsHint, SUCCESS),
                metricCard("Solar status", data.solarHeadline, data.solarSubline, PURPLE)
        );
        for (Node node : summaryRow.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        VBox formCard = cardBox(18, CARD);
        formCard.setSpacing(16);

        HBox formHeader = new HBox();
        formHeader.setAlignment(Pos.CENTER_LEFT);
        VBox formCopy = new VBox(4);
        formCopy.getChildren().addAll(
                label("New reading entry", 16, TEXT, true),
                label("The new value must be greater than or equal to the previous meter value.", 12, TEXT_SOFT, false)
        );
        formHeader.getChildren().add(formCopy);

        TextField gridReadingField = styledField("Enter current grid meter value");
        DatePicker readingDatePicker = new DatePicker(LocalDate.now());
        styleDatePicker(readingDatePicker);

        VBox gridGroup = fieldGroup("Grid meter reading", gridReadingField);
        VBox dateGroup = fieldGroup("Reading date", readingDatePicker);

        HBox topInputs = new HBox(14, gridGroup, dateGroup);
        HBox.setHgrow(gridGroup, Priority.ALWAYS);
        HBox.setHgrow(dateGroup, Priority.ALWAYS);

        VBox solarSection = new VBox(10);
        TextField solarReadingField = styledField("Enter solar generation units");
        solarSection.getChildren().addAll(
                label("Solar generation", 13, TEXT, true),
                label("Only required for solar accounts. Leave blank to save zero generation.", 12, TEXT_SOFT, false),
                solarReadingField
        );
        solarSection.setVisible(data.isSolarUser);
        solarSection.setManaged(data.isSolarUser);

        VBox infoStrip = cardBox(14, CARD_SUBTLE);
        infoStrip.getChildren().addAll(
                label("Validation rules", 13, TEXT, true),
                label(data.validationHint, 12, TEXT_SOFT, false)
        );

        Label message = label("", 12, TEXT_SOFT, false);
        message.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = secondaryButton("Cancel");
        cancel.setOnAction(e -> goToDashboard(stage));
        Button billHistory = secondaryButton("Bills & History");
        billHistory.setOnAction(e -> goToBillView(stage));
        Button save = primaryButton("Save reading");
        actions.getChildren().addAll(cancel, billHistory, save);

        save.setOnAction(e -> {
            String gridText = gridReadingField.getText().trim();
            String solarText = solarReadingField.getText().trim();

            if (gridText.isEmpty()) {
                setMessage(message, "Enter the current grid meter value before saving.", DANGER);
                return;
            }

            float gridValue;
            try {
                gridValue = Float.parseFloat(gridText);
            } catch (NumberFormatException ex) {
                setMessage(message, "Grid reading must be a numeric value.", DANGER);
                return;
            }

            if (gridValue < data.lastGridReadingValue) {
                setMessage(message, "New reading cannot be lower than the last saved value of " + numberFormat.format(data.lastGridReadingValue) + ".", DANGER);
                return;
            }

            LocalDate selectedDate = readingDatePicker.getValue();
            if (selectedDate == null) {
                setMessage(message, "Choose a reading date.", DANGER);
                return;
            }
            if (selectedDate.isAfter(LocalDate.now())) {
                setMessage(message, "Reading date cannot be in the future.", DANGER);
                return;
            }

            float unitsConsumed = gridValue - data.lastGridReadingValue;
            if (unitsConsumed > 500) {
                setMessage(message, "This jump looks unusually large. Double-check the meter before saving.", DANGER);
                return;
            }

            try {
                MeterReading reading = new MeterReading();
                reading.setUserID(data.userId);
                reading.setValue(gridValue);
                reading.setDate(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reading.setUnitsConsumed(unitsConsumed);
                meterReadingDAO.save(reading);

                if (data.isSolarUser) {
                    float solarGeneration = 0;
                    if (!solarText.isEmpty()) {
                        try {
                            solarGeneration = Float.parseFloat(solarText);
                        } catch (NumberFormatException ex) {
                            setMessage(message, "Solar generation must be numeric if provided.", DANGER);
                            return;
                        }
                    }
                    float exported = Math.max(0, solarGeneration - unitsConsumed);
                    float creditRate = new TariffEngine(data.accountTypeRaw).getReferenceRate(unitsConsumed);
                    float credit = solarService.deductNetMeteringCredit(unitsConsumed, solarGeneration, creditRate);
                    solarService.saveSolarReading(data.userId, solarGeneration, exported, credit);
                }

                float currentCycleUnits = meterReadingDAO.getTotalUnitsThisCycle(data.userId);
                billingService.calculateAndRecordBill(data.userId, currentCycleUnits, data.accountTypeRaw);

                setMessage(message, "Reading saved successfully. You can now open Bills & History to review the updated billing data.", SUCCESS);
                gridReadingField.clear();
                solarReadingField.clear();
                readingDatePicker.setValue(LocalDate.now());
            } catch (Exception ex) {
                setMessage(message, "Could not save reading. Please check the database connection and try again.", DANGER);
            }
        });

        formCard.getChildren().addAll(formHeader, topInputs, solarSection, infoStrip, message, actions);

        page.getChildren().addAll(top, summaryRow, formCard);

        animateEntrance(top, 40);
        animateEntrance(summaryRow, 110);
        animateEntrance(formCard, 180);

        // ✅ Wrap in ScrollPane so content never gets clipped on smaller screens
        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(
                "-fx-background: " + SHELL + ";" +
                        "-fx-background-color: " + SHELL + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 0;"
        );
        return scroll;
    }

    private MeterReadingViewModel loadData() {
        MeterReadingViewModel vm = new MeterReadingViewModel();
        vm.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        vm.displayName = "User";
        vm.accountTypeLabel = "Grid User";
        vm.accountTypeRaw = "grid";
        vm.lastGridValue = "No previous reading";
        vm.lastGridDate = "Save the first reading to begin tracking";
        vm.lastUnitsValue = "0 units";
        vm.unitsHint = "Fresh cycle";
        vm.solarHeadline = "Grid only";
        vm.solarSubline = "Solar entry hidden for non-solar accounts";
        vm.validationHint = "Grid reading must be numeric and never lower than the previous reading.";

        try {
            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                return vm;
            }

            vm.userId = user.getUserID();
            vm.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
            vm.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());
            vm.accountTypeLabel = vm.isSolarUser ? "Solar User" : "Grid User";
            vm.accountTypeRaw = vm.isSolarUser ? "solar" : "grid";

            MeterReading last = meterReadingDAO.getLastReading(user.getUserID());
            if (last != null) {
                vm.lastGridReadingValue = last.getValue();
                vm.lastGridValue = numberFormat.format(last.getValue()) + " units";
                vm.lastGridDate = "Saved on " + formatDate(last.getDate());
                vm.lastUnitsValue = numberFormat.format(last.getUnitsConsumed()) + " units";
                vm.unitsHint = "Consumed since prior entry";
            }

            if (vm.isSolarUser) {
                vm.solarHeadline = "Solar entry enabled";
                vm.solarSubline = "You can save generation units together with the grid meter reading.";
                vm.validationHint = "Grid reading must be numeric and not lower than the previous reading. Solar generation is optional and defaults to zero if blank.";
            }
        } catch (Exception ignored) {
            // Keep fallback values so the UI still loads.
        }
        return vm;
    }

    private VBox metricCard(String titleText, String valueText, String subText, String accent) {
        VBox card = cardBox(16, CARD);
        card.setSpacing(10);
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

    private VBox fieldGroup(String title, Node field) {
        VBox box = new VBox(8);
        box.getChildren().addAll(label(title, 13, TEXT, true), field);
        return box;
    }

    private VBox buildHeroHeader(MeterReadingViewModel data) {
        VBox hero = cardBox(20, CARD);
        hero.setSpacing(18);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        VBox copy = new VBox(6);
        copy.getChildren().addAll(
                label("Meter reading", 12, TEXT_SOFT, false),
                label("Add reading", 26, TEXT, true),
                label("New entry", 13, TEXT_SOFT, false)
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox highlight = cardBox(16, CARD_SUBTLE);
        highlight.setEffect(null);
        highlight.setMinWidth(260);
        highlight.getChildren().addAll(
                statusPill(data.isSolarUser ? "Solar mode" : "Grid mode", data.isSolarUser ? PURPLE : ACCENT),
                label(data.lastGridValue, 22, TEXT, true),
                label("Last saved", 12, TEXT_SOFT, false)
        );

        top.getChildren().addAll(copy, spacer, highlight);

        HBox bands = new HBox(12);
        bands.getChildren().addAll(
                heroBand("Previous usage", data.lastUnitsValue, BLUE),
                heroBand("Entry status", data.unitsHint, SUCCESS),
                heroBand("Solar panel", data.solarHeadline, PURPLE)
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
        Label valueLabel = label(value, 15, TEXT, true);
        valueLabel.setWrapText(true);
        band.getChildren().addAll(statusPill(title, accent), valueLabel);
        return band;
    }

    private TextField styledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(44);
        field.setStyle(
                "-fx-background-color: #101010;" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-prompt-text-fill: " + TEXT_FAINT + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-padding: 0 12 0 12;"
        );
        return field;
    }

    private void styleDatePicker(DatePicker picker) {
        picker.setPrefHeight(44);
        picker.setStyle(
                "-fx-background-color: #101010;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 12;" +
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

    private String formatDate(Date date) {
        return date == null ? "Unknown date" : dateFormat.format(date);
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

    private void goToBillView(Stage stage) {
        switchScene(stage, () -> {
            try {
                new BillViewScreen(userEmail).start(stage);
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

    private static class MeterReadingViewModel {
        int userId;
        boolean isSolarUser;
        float lastGridReadingValue;
        String displayName;
        String userEmailLabel;
        String accountTypeLabel;
        String accountTypeRaw;
        String lastGridValue;
        String lastGridDate;
        String lastUnitsValue;
        String unitsHint;
        String solarHeadline;
        String solarSubline;
        String validationHint;
    }
}
