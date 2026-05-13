package com.example.bijlibachat.ui;

import com.example.bijlibachat.dao.ApplianceDAO;
import com.example.bijlibachat.model.Appliance;
import com.example.bijlibachat.model.User;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ApplianceScreen {

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
    private final ApplianceDAO applianceDAO = new ApplianceDAO();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");

    public ApplianceScreen(String userEmail) {
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void start(Stage stage) {
        ApplianceViewModel data = loadData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildContent(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Appliances", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(280), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, ApplianceViewModel data) {
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
        brandText.getChildren().addAll(label("Bijli Bachat", 18, TEXT, true), label("Appliances", 11, TEXT_SOFT, false));
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
                navButton("Appliances", true, null),
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

    private ScrollPane buildContent(Stage stage, ApplianceViewModel data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24, 26, 30, 26));

        VBox hero = cardBox(20, CARD);
        hero.setSpacing(18);

        HBox heroTop = new HBox();
        heroTop.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(6);
        copy.getChildren().addAll(
                label("Appliances", 12, TEXT_SOFT, false),
                label("Track devices", 26, TEXT, true),
                label("Usage list", 13, TEXT_SOFT, false)
        );
        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);
        VBox highlight = cardBox(16, CARD_SUBTLE);
        highlight.setEffect(null);
        highlight.getChildren().addAll(
                statusPill("Tracked", ACCENT),
                label(String.valueOf(data.appliances.size()), 24, TEXT, true),
                label("Devices", 12, TEXT_SOFT, false)
        );
        heroTop.getChildren().addAll(copy, heroSpacer, highlight);

        HBox heroBands = new HBox(12);
        heroBands.getChildren().addAll(
                heroBand("Total kWh", data.totalUsageLabel, BLUE),
                heroBand("Highest load", data.topApplianceLabel, SUCCESS),
                heroBand("Daily hours", data.totalHoursLabel, PURPLE)
        );
        for (Node node : heroBands.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        hero.getChildren().addAll(heroTop, heroBands);

        HBox lower = new HBox(14);
        VBox listCard = buildListCard(data);
        VBox formCard = buildFormCard(stage, data);
        HBox.setHgrow(listCard, Priority.ALWAYS);
        HBox.setHgrow(formCard, Priority.ALWAYS);
        lower.getChildren().addAll(listCard, formCard);

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

    private VBox buildListCard(ApplianceViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(14);

        HBox heading = new HBox();
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getChildren().addAll(
                label("Appliance list", 16, TEXT, true),
                spacer(),
                statusPill("Live kWh", SUCCESS)
        );

        if (data.appliances.isEmpty()) {
            VBox empty = cardBox(16, CARD_SUBTLE);
            empty.setEffect(null);
            Label title = label("No appliances yet", 14, TEXT, true);
            Label body = label("Add your first appliance on the right to start seeing a breakdown.", 12, TEXT_SOFT, false);
            body.setWrapText(true);
            empty.getChildren().addAll(statusPill("Start here", ACCENT), title, body);
            card.getChildren().addAll(heading, empty);
            return card;
        }

        VBox rows = new VBox(10);
        for (Appliance appliance : data.appliances) {
            rows.getChildren().add(applianceRow(appliance));
        }

        card.getChildren().addAll(heading, rows);
        return card;
    }

    private VBox buildFormCard(Stage stage, ApplianceViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(16);

        Label title = label("Add appliance", 16, TEXT, true);
        Label sub = label("Save name, wattage, and hours used to estimate kWh automatically.", 12, TEXT_SOFT, false);
        sub.setWrapText(true);

        TextField nameField = styledField("Appliance name");
        TextField wattageField = styledField("Wattage (e.g. 1500)");
        TextField hoursField = styledField("Hours used (e.g. 6)");

        VBox preview = cardBox(14, CARD_SUBTLE);
        preview.setEffect(null);
        Label previewTitle = label("Estimated usage", 13, TEXT, true);
        Label previewValue = label("0 kWh", 20, ACCENT, true);
        Label previewSub = label("Usage updates when wattage and hours are entered.", 12, TEXT_SOFT, false);
        previewSub.setWrapText(true);
        preview.getChildren().addAll(previewTitle, previewValue, previewSub);

        Runnable updatePreview = () -> {
            try {
                float wattage = wattageField.getText().trim().isEmpty() ? 0 : Float.parseFloat(wattageField.getText().trim());
                float hours = hoursField.getText().trim().isEmpty() ? 0 : Float.parseFloat(hoursField.getText().trim());
                float usage = (wattage / 1000f) * hours;
                previewValue.setText(numberFormat.format(usage) + " kWh");
                previewSub.setText(usage > 0 ? "Based on the values entered above." : "Usage updates when wattage and hours are entered.");
            } catch (Exception ignored) {
                previewValue.setText("0 kWh");
                previewSub.setText("Enter valid numeric wattage and hours.");
            }
        };

        wattageField.textProperty().addListener((obs, o, n) -> updatePreview.run());
        hoursField.textProperty().addListener((obs, o, n) -> updatePreview.run());

        Label message = label("", 12, TEXT_SOFT, false);
        message.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = secondaryButton("Reset");
        cancel.setOnAction(e -> {
            nameField.clear();
            wattageField.clear();
            hoursField.clear();
            message.setText("");
            updatePreview.run();
        });
        Button billHistory = secondaryButton("Bills & History");
        billHistory.setOnAction(e -> goToBillView(stage));
        Button save = primaryButton("Save appliance");
        actions.getChildren().addAll(cancel, billHistory, save);

        save.setOnAction(e -> {
            String name = nameField.getText().trim();
            String wattageText = wattageField.getText().trim();
            String hoursText = hoursField.getText().trim();

            if (name.isEmpty() || wattageText.isEmpty() || hoursText.isEmpty()) {
                setMessage(message, "Complete name, wattage, and hours before saving.", DANGER);
                return;
            }

            float wattage;
            float hours;
            try {
                wattage = Float.parseFloat(wattageText);
                hours = Float.parseFloat(hoursText);
            } catch (NumberFormatException ex) {
                setMessage(message, "Wattage and hours must both be numeric.", DANGER);
                return;
            }

            if (wattage <= 0 || hours < 0) {
                setMessage(message, "Wattage must be positive and hours cannot be negative.", DANGER);
                return;
            }

            if (data.userId <= 0) {
                setMessage(message, "Your user session is missing. Please log in again before saving appliances.", DANGER);
                return;
            }

            try {
                Appliance appliance = new Appliance();
                appliance.setUserID(data.userId);
                appliance.setName(name);
                appliance.setWattage(wattage);
                appliance.setHoursUsed(hours);
                boolean saved = applianceDAO.saveAppliance(appliance);
                if (!saved) {
                    setMessage(message, "Appliance could not be saved. Please try again.", DANGER);
                    return;
                }
                setMessage(message, "Appliance saved successfully. The list will refresh now.", SUCCESS);
                switchScene(stage, () -> {
                    try {
                        new ApplianceScreen(userEmail).start(stage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (Exception ex) {
                String reason = ex.getMessage() == null || ex.getMessage().isBlank()
                        ? "Check the database connection and try again."
                        : ex.getMessage();
                setMessage(message, reason, DANGER);
            }
        });

        card.getChildren().addAll(
                title,
                sub,
                fieldGroup("Appliance name", nameField),
                fieldGroup("Wattage", wattageField),
                fieldGroup("Hours used", hoursField),
                preview,
                message,
                actions
        );
        return card;
    }

    private ApplianceViewModel loadData() {
        ApplianceViewModel vm = new ApplianceViewModel();
        vm.displayName = "User";
        vm.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        vm.accountTypeLabel = "Grid User";
        vm.totalUsageLabel = "0 kWh";
        vm.topApplianceLabel = "No data";
        vm.totalHoursLabel = "0 h";

        try {
            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                return vm;
            }

            vm.userId = user.getUserID();
            vm.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
            vm.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());
            vm.accountTypeLabel = vm.isSolarUser ? "Solar User" : "Grid User";

            vm.appliances = applianceDAO.getAppliancesByUser(user.getUserID());
            vm.appliances.sort(Comparator.comparingDouble(a -> -a.getLiveReading(a.getApplianceID())));

            float totalUsage = 0;
            float totalHours = 0;
            for (Appliance appliance : vm.appliances) {
                totalUsage += appliance.getLiveReading(appliance.getApplianceID());
                totalHours += appliance.getHoursUsed();
            }

            vm.totalUsageLabel = numberFormat.format(totalUsage) + " kWh";
            vm.totalHoursLabel = numberFormat.format(totalHours) + " h";
            if (!vm.appliances.isEmpty()) {
                Appliance top = vm.appliances.get(0);
                vm.topApplianceLabel = top.getName() + " - " + numberFormat.format(top.getLiveReading(top.getApplianceID())) + " kWh";
            }
        } catch (Exception ignored) {
            // Keep fallback UI if data is unavailable.
        }
        return vm;
    }

    private HBox applianceRow(Appliance appliance) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setBackground(new Background(new BackgroundFill(Color.web(CARD_SUBTLE), new CornerRadii(8), Insets.EMPTY)));
        row.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        Circle dot = new Circle(5, Color.web(ACCENT));

        VBox copy = new VBox(3);
        copy.getChildren().addAll(
                label(appliance.getName(), 13, TEXT, true),
                label(numberFormat.format(appliance.getWattage()) + " W • " + numberFormat.format(appliance.getHoursUsed()) + " h", 11, TEXT_SOFT, false)
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox value = new VBox(2);
        value.setAlignment(Pos.CENTER_RIGHT);
        value.getChildren().addAll(
                label(numberFormat.format(appliance.getLiveReading(appliance.getApplianceID())) + " kWh", 13, ACCENT, true),
                label("Estimated", 10, TEXT_FAINT, false)
        );

        row.getChildren().addAll(dot, copy, spacer, value);
        return row;
    }

    private VBox heroBand(String title, String value, String accent) {
        VBox band = cardBox(14, CARD_SUBTLE);
        band.setEffect(null);
        Label valueLabel = label(value, 15, TEXT, true);
        valueLabel.setWrapText(true);
        band.getChildren().addAll(statusPill(title, accent), valueLabel);
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

    private VBox fieldGroup(String title, Node field) {
        VBox box = new VBox(8);
        box.getChildren().addAll(label(title, 13, TEXT, true), field);
        return box;
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
                        "-fx-font-weight: " + (active ? "bold" : "normal") + ";" +
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

    private void goToBillView(Stage stage) {
        switchScene(stage, () -> {
            try {
                new BillViewScreen(userEmail).start(stage);
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

    private static class ApplianceViewModel {
        int userId;
        boolean isSolarUser;
        String displayName;
        String userEmailLabel;
        String accountTypeLabel;
        String totalUsageLabel;
        String topApplianceLabel;
        String totalHoursLabel;
        List<Appliance> appliances = new ArrayList<>();
    }
}
