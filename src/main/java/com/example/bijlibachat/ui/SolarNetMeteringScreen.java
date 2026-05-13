package com.example.bijlibachat.ui;

import com.example.bijlibachat.model.SolarMeter;
import com.example.bijlibachat.model.User;
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
import javafx.scene.control.Label;
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
import java.text.SimpleDateFormat;

public class SolarNetMeteringScreen {

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
    private final SolarService solarService = new SolarService();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    public SolarNetMeteringScreen(String userEmail) {
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void start(Stage stage) {
        SolarViewModel data = loadData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildContent(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Solar Net Metering", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(280), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, SolarViewModel data) {
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
        brandText.getChildren().addAll(label("Bijli Bachat", 18, TEXT, true), label("Solar net metering", 11, TEXT_SOFT, false));
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
                navSection("CONTROL"),
                navButton("Monthly Ceiling", false, () -> goToMonthlyCeiling(stage)),
                navButton("Reports", false, () -> goToReports(stage)),
                navButton("Tariff Rates", false, () -> goToTariffRates(stage)),
                navButton("Settings", false, () -> goToSettings(stage)),
                navButton("Solar Net Metering", true, null)
        );

        VBox tip = cardBox(16, CARD_SUBTLE);
        Label tipTitle = label("Use case", 14, TEXT, true);
        Label tipBody = label("UC10: manage solar generation, exported units, and net-metering credit through the solar service layer.", 12, TEXT_SOFT, false);
        tipBody.setWrapText(true);
        tip.getChildren().addAll(tipTitle, tipBody);

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);
        Button back = secondaryButton("<-  Back to Dashboard");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(e -> goToDashboard(stage));

        sidebar.getChildren().addAll(brandRow, profile, nav, tip, push, back);
        animateEntrance(profile, 80);
        animateEntrance(nav, 150);
        animateEntrance(tip, 220);
        return sidebar;
    }

    private ScrollPane buildContent(Stage stage, SolarViewModel data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24, 26, 30, 26));

        VBox hero = cardBox(20, CARD);
        hero.setSpacing(18);
        HBox heroTop = new HBox();
        heroTop.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(6);
        copy.getChildren().addAll(
                label("Solar net metering", 12, TEXT_SOFT, false),
                label("Track generation, export, and credit", 26, TEXT, true),
                label("Use this screen for solar accounts to capture generation snapshots and quantify bill offsets.", 13, TEXT_SOFT, false)
        );
        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);
        VBox highlight = cardBox(16, CARD_SUBTLE);
        highlight.setEffect(null);
        highlight.getChildren().addAll(
                statusPill("UC10", ACCENT),
                label(data.creditValue, 24, TEXT, true),
                label(data.creditNarrative, 12, TEXT_SOFT, false)
        );
        heroTop.getChildren().addAll(copy, heroSpacer, highlight);

        HBox bands = new HBox(12);
        bands.getChildren().addAll(
                heroBand("Generation", data.generationValue, BLUE),
                heroBand("Exported", data.exportValue, SUCCESS),
                heroBand("Last reading", data.lastReadingDate, PURPLE)
        );
        for (Node node : bands.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        hero.getChildren().addAll(heroTop, bands);

        HBox lower = new HBox(14);
        VBox formCard = buildFormCard(stage, data);
        VBox noteCard = buildNoteCard(data);
        HBox.setHgrow(formCard, Priority.ALWAYS);
        HBox.setHgrow(noteCard, Priority.ALWAYS);
        lower.getChildren().addAll(formCard, noteCard);

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

    private VBox buildFormCard(Stage stage, SolarViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(16);

        if (!data.isSolarUser) {
            card.getChildren().addAll(
                    label("Solar account required", 16, TEXT, true),
                    label("This module is only active for solar users. Switch the account type in Settings to solar to use UC10.", 12, TEXT_SOFT, false)
            );
            return card;
        }

        TextField gridUnitsField = styledField("Grid units for this snapshot");
        TextField generationField = styledField("Solar generation units");
        Label preview = label("Exported 0 units, credit Rs. 0", 12, TEXT_SOFT, false);
        Label message = label("", 12, TEXT_SOFT, false);
        message.setWrapText(true);

        Runnable updatePreview = () -> {
            try {
                float gridUnits = gridUnitsField.getText().trim().isEmpty() ? 0 : Float.parseFloat(gridUnitsField.getText().trim());
                float generation = generationField.getText().trim().isEmpty() ? 0 : Float.parseFloat(generationField.getText().trim());
                float exported = Math.max(0, generation - gridUnits);
                float creditRate = new TariffEngine("solar").getReferenceRate(gridUnits);
                float credit = solarService.deductNetMeteringCredit(gridUnits, generation, creditRate);
                preview.setText("Exported " + numberFormat.format(exported) + " units, credit Rs. " + numberFormat.format(credit));
            } catch (Exception ignored) {
                preview.setText("Enter valid numeric grid and generation values.");
            }
        };
        gridUnitsField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview.run());
        generationField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview.run());

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button reset = secondaryButton("Reset");
        reset.setOnAction(e -> {
            gridUnitsField.clear();
            generationField.clear();
            preview.setText("Exported 0 units, credit Rs. 0");
            message.setText("");
        });
        Button save = primaryButton("Save solar snapshot");
        save.setOnAction(e -> {
            String gridText = gridUnitsField.getText().trim();
            String generationText = generationField.getText().trim();
            if (gridText.isEmpty() || generationText.isEmpty()) {
                setMessage(message, "Enter both grid units and solar generation.", DANGER);
                return;
            }
            try {
                float gridUnits = Float.parseFloat(gridText);
                float generation = Float.parseFloat(generationText);
                if (gridUnits < 0 || generation < 0) {
                    setMessage(message, "Grid and generation values cannot be negative.", DANGER);
                    return;
                }
                float creditRate = new TariffEngine("solar").getReferenceRate(gridUnits);
                solarService.saveNetMeteringSnapshot(data.userId, gridUnits, generation, creditRate);
                setMessage(message, "Solar snapshot saved. Bills & History and Dashboard now read the same net-metering data.", SUCCESS);
                switchScene(stage, () -> new SolarNetMeteringScreen(userEmail).start(stage));
            } catch (NumberFormatException ex) {
                setMessage(message, "Grid and generation must both be numeric.", DANGER);
            } catch (Exception ex) {
                setMessage(message, "Could not save solar data. Check the database connection and try again.", DANGER);
            }
        });
        actions.getChildren().addAll(reset, save);

        card.getChildren().addAll(
                label("Capture solar snapshot", 16, TEXT, true),
                label("Enter grid units for the current snapshot and the solar generation reading. The service computes export and net credit.", 12, TEXT_SOFT, false),
                fieldGroup("Grid units", gridUnitsField),
                fieldGroup("Solar generation", generationField),
                preview,
                message,
                actions
        );
        return card;
    }

    private VBox buildNoteCard(SolarViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(14);
        card.getChildren().addAll(
                label("Solar mapping", 16, TEXT, true),
                checklistRow("UI: SolarNetMeteringScreen captures the snapshot."),
                checklistRow("Service: SolarService computes exported units and net credit."),
                checklistRow("DAO: SolarDAO persists and retrieves solar meter rows."),
                checklistRow("Model: SolarMeter represents the saved generation snapshot."),
                checklistRow(data.isSolarUser ? "Latest saved credit: " + data.creditValue + "." : "Current account is grid-only.")
        );
        return card;
    }

    private SolarViewModel loadData() {
        SolarViewModel vm = new SolarViewModel();
        vm.displayName = "User";
        vm.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        vm.accountTypeLabel = "Grid User";
        vm.generationValue = "0 units";
        vm.exportValue = "0 units";
        vm.creditValue = "Rs. 0";
        vm.creditNarrative = "No solar reading yet";
        vm.lastReadingDate = "No date";

        try {
            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                return vm;
            }
            vm.userId = user.getUserID();
            vm.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
            vm.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());
            vm.accountTypeLabel = vm.isSolarUser ? "Solar User" : "Grid User";

            SolarMeter latest = solarService.getLatestSolarReading(user.getUserID());
            if (latest != null) {
                vm.generationValue = numberFormat.format(latest.getGenerationReading()) + " units";
                vm.exportValue = numberFormat.format(latest.getExportedUnits()) + " units";
                vm.creditValue = "Rs. " + numberFormat.format(latest.getNetCredit());
                vm.creditNarrative = latest.getNetCredit() > 0 ? "Export credit available" : "Generation offsets on-site usage";
                vm.lastReadingDate = latest.getReadingDate() == null ? "No date" : dateFormat.format(latest.getReadingDate());
            } else if (vm.isSolarUser) {
                vm.creditNarrative = "Save the first solar reading to unlock UC10.";
            }
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

    private void goToMonthlyCeiling(Stage stage) {
        switchScene(stage, () -> new MonthlyCeilingScreen(userEmail).start(stage));
    }

    private void goToReports(Stage stage) {
        switchScene(stage, () -> new ReportsScreen(userEmail).start(stage));
    }

    private void goToSettings(Stage stage) {
        switchScene(stage, () -> new SettingsScreen(userEmail).start(stage));
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

    private static class SolarViewModel {
        int userId;
        boolean isSolarUser;
        String displayName;
        String userEmailLabel;
        String accountTypeLabel;
        String generationValue;
        String exportValue;
        String creditValue;
        String creditNarrative;
        String lastReadingDate;
    }
}
