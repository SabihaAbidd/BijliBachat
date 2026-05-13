package com.example.bijlibachat.ui;

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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SettingsScreen {

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

    public SettingsScreen(String userEmail) {
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void start(Stage stage) {
        SettingsViewModel data = loadData();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web(SHELL), CornerRadii.EMPTY, Insets.EMPTY)));
        root.setLeft(buildSidebar(stage, data));
        root.setCenter(buildContent(stage, data));
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 1200, 780, Color.web(SHELL));
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Settings", 760, 540);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(280), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private VBox buildSidebar(Stage stage, SettingsViewModel data) {
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
        brandText.getChildren().addAll(label("Bijli Bachat", 18, TEXT, true), label("Settings", 11, TEXT_SOFT, false));
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
                navButton("Tariff Rates", false, () -> goToTariffRates(stage)),
                navButton("Settings", true, null)
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

    private ScrollPane buildContent(Stage stage, SettingsViewModel data) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(24, 26, 30, 26));

        VBox hero = cardBox(20, CARD);
        hero.setSpacing(18);

        HBox heroTop = new HBox();
        heroTop.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(6);
        copy.getChildren().addAll(
                label("Settings", 12, TEXT_SOFT, false),
                label("Manage profile & account", 26, TEXT, true),
                label("Update your personal details and account preferences.", 13, TEXT_SOFT, false)
        );
        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);
        VBox highlight = cardBox(16, CARD_SUBTLE);
        highlight.setEffect(null);
        highlight.getChildren().addAll(
                statusPill("Profile", ACCENT),
                label(data.displayName, 22, TEXT, true),
                label(data.accountTypeLabel, 12, TEXT_SOFT, false)
        );
        heroTop.getChildren().addAll(copy, heroSpacer, highlight);

        HBox bands = new HBox(12);
        bands.getChildren().addAll(
                heroBand("Email", data.userEmailLabel, BLUE),
                heroBand("Phone", data.phoneValue, SUCCESS),
                heroBand("Account", data.accountTypeLabel, PURPLE)
        );
        for (Node node : bands.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        hero.getChildren().addAll(heroTop, bands);

        VBox formCard = buildFormCard(stage, data);
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

    private VBox buildFormCard(Stage stage, SettingsViewModel data) {
        VBox card = cardBox(18, CARD);
        card.setSpacing(16);

        Label title = label("Profile editor", 16, TEXT, true);
        Label sub = label("Edit your profile details and save the changes to your account.", 12, TEXT_SOFT, false);
        sub.setWrapText(true);

        TextField nameField = styledField("Full name");
        nameField.setText(data.displayName);
        TextField emailField = styledField("Email");
        emailField.setText(data.userEmailLabel);
        emailField.setDisable(true);
        TextField phoneField = styledField("Phone number");
        phoneField.setText(data.phoneValue);
        TextField accountTypeField = styledField("Account type");
        accountTypeField.setText(data.accountTypeEditValue);

        Label message = label("", 12, TEXT_SOFT, false);
        message.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button reset = secondaryButton("Reset");
        reset.setOnAction(e -> {
            nameField.setText(data.displayName);
            phoneField.setText(data.phoneValue);
            accountTypeField.setText(data.accountTypeEditValue);
            message.setText("");
        });
        Button save = primaryButton("Save changes");
        actions.getChildren().addAll(reset, save);

        save.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String accountType = accountTypeField.getText().trim().toLowerCase();

            if (name.isEmpty()) {
                setMessage(message, "Name cannot be empty.", DANGER);
                return;
            }
            if (!accountType.equals("grid") && !accountType.equals("solar")) {
                setMessage(message, "Account type should be either grid or solar.", DANGER);
                return;
            }

            try {
                User user = data.user;
                user.setName(name);
                user.setPhone(phone);
                user.setAccountType(accountType);
                boolean updated = userService.updateProfile(user);
                if (updated) {
                    setMessage(message, "Profile updated successfully.", SUCCESS);
                } else {
                    setMessage(message, "Profile could not be updated.", DANGER);
                }
            } catch (Exception ex) {
                setMessage(message, "Could not save changes. Check the database connection and try again.", DANGER);
            }
        });

        card.getChildren().addAll(
                title,
                sub,
                fieldGroup("Name", nameField),
                fieldGroup("Email", emailField),
                fieldGroup("Phone", phoneField),
                fieldGroup("Account type", accountTypeField),
                message,
                actions
        );
        return card;
    }

    private SettingsViewModel loadData() {
        SettingsViewModel vm = new SettingsViewModel();
        vm.displayName = "User";
        vm.userEmailLabel = userEmail.isBlank() ? "No email found" : userEmail;
        vm.phoneValue = "Not available";
        vm.accountTypeLabel = "Grid User";
        vm.accountTypeEditValue = "grid";

        try {
            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                return vm;
            }
            vm.user = user;
            vm.displayName = user.getName() == null || user.getName().isBlank() ? "User" : user.getName();
            vm.phoneValue = user.getPhone() == null || user.getPhone().isBlank() ? "Not available" : user.getPhone();
            vm.isSolarUser = "solar".equalsIgnoreCase(user.getAccountType());
            vm.accountTypeLabel = vm.isSolarUser ? "Solar User" : "Grid User";
            vm.accountTypeEditValue = vm.isSolarUser ? "solar" : "grid";
        } catch (Exception ignored) {
            // Keep fallback values if DB is unavailable.
        }
        return vm;
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

    private static class SettingsViewModel {
        boolean isSolarUser;
        User user;
        String displayName;
        String userEmailLabel;
        String phoneValue;
        String accountTypeLabel;
        String accountTypeEditValue;
    }
}
