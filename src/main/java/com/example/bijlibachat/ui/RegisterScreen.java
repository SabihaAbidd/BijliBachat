package com.example.bijlibachat.ui;
import javafx.application.Platform;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RegisterScreen extends Application {

    private static final String AMBER      = "#F59E0B";
    private static final String AMBER_DEEP = "#D97706";
    private static final String DARK_BG    = "#0D0D0D";
    private static final String PANEL_BG   = "#111111";
    private static final String CARD_BG    = "#161616";
    private static final String INPUT_BG   = "#181818";
    private static final String BORDER     = "#2A2A2A";
    private static final String TEXT_MUTED = "#6B7280";
    private static final String TEXT_DIM   = "#3D4450";

    @Override
    public void start(Stage stage) {
        VBox leftPanel = buildLeftPanel(stage);
        leftPanel.setPrefWidth(460);
        leftPanel.setMinWidth(340);

        StackPane rightPanel = buildRightPanel();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        HBox root = new HBox(leftPanel, rightPanel);
        root.setStyle("-fx-background-color: " + DARK_BG + ";");
        root.setOpacity(0);

        Scene scene = ResponsiveStageHelper.createScene(stage, root, 980, 660, Color.web(DARK_BG));

        leftPanel.prefWidthProperty().bind(
                scene.widthProperty().multiply(0.46));
        rightPanel.layoutBoundsProperty().addListener((obs, old, b) ->
                rightPanel.setClip(
                        new Rectangle(b.getWidth(), b.getHeight())));

        stage.setResizable(true);
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Register", 700, 500);

        Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(350), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }
    // ════════════════════════════════════════════════════════
    //  LEFT PANEL
    // ════════════════════════════════════════════════════════
    private VBox buildLeftPanel(Stage stage) {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: " + PANEL_BG + ";");
        panel.setPadding(new Insets(36, 55, 36, 55));

        // Logo row
        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        BijliLogo logo = new BijliLogo(0.50);
        Text brand = new Text("Bijli Bachat");
        brand.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        brand.setFill(Color.web(AMBER));
        logoRow.getChildren().addAll(logo, brand);

        // Hero headline
        VBox heroBox = new VBox(0);
        heroBox.setPadding(new Insets(24, 0, 20, 0));
        Text headline1 = new Text("Create Account");
        headline1.setFont(Font.font("Verdana", FontWeight.BOLD, 36));
        headline1.setFill(Color.WHITE);
        Text headline2 = new Text("Start saving today");
        headline2.setFont(Font.font("Verdana", FontWeight.NORMAL, 14));
        headline2.setFill(Color.web(TEXT_MUTED));
        Rectangle accentBar = new Rectangle(180, 4);
        accentBar.setArcWidth(4);
        accentBar.setArcHeight(4);
        accentBar.setFill(Color.web(AMBER));
        heroBox.getChildren().addAll(
                headline1,
                createSpacer(4),
                headline2,
                createSpacer(10),
                accentBar
        );

        // Form fields
        VBox form = new VBox(12);

        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        nameField.setPrefHeight(46);
        styleTextField(nameField);
        VBox nameGroup = createInputGroup("Full Name", nameField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        emailField.setPrefHeight(46);
        styleTextField(emailField);
        VBox emailGroup = createInputGroup("Email", emailField);

        TextField phoneField = new TextField();
        phoneField.setPromptText("03xx-xxxxxxx");
        phoneField.setPrefHeight(46);
        styleTextField(phoneField);
        VBox phoneGroup = createInputGroup("Phone Number", phoneField);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Create password (min 6 chars)");
        passField.setPrefHeight(46);
        styleTextField(passField);
        VBox passGroup = createInputGroup("Password", passField);

        // Account type toggle
        VBox typeGroup = new VBox(7);
        Label typeLabel = new Label("Account Type");
        typeLabel.setStyle(
                "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + TEXT_DIM + ";" +
                        "-fx-font-size: 11;"
        );
        HBox typeRow = new HBox(10);
        ToggleGroup typeToggle = new ToggleGroup();

        RadioButton gridBtn = new RadioButton("Grid User");
        gridBtn.setToggleGroup(typeToggle);
        gridBtn.setSelected(true);
        gridBtn.setStyle(
                "-fx-text-fill: #9CA3AF;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 12;"
        );
        RadioButton solarBtn = new RadioButton("Solar User");
        solarBtn.setToggleGroup(typeToggle);
        solarBtn.setStyle(
                "-fx-text-fill: #9CA3AF;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 12;"
        );
        typeRow.getChildren().addAll(gridBtn, solarBtn);
        typeGroup.getChildren().addAll(typeLabel, typeRow);

        // Message label
        Label messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.setStyle(
                "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';"
        );

        form.getChildren().addAll(
                nameGroup,
                emailGroup,
                phoneGroup,
                passGroup,
                typeGroup,
                messageLabel
        );

        // Register button
        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(50);
        registerBtn.setCursor(Cursor.HAND);

        String btnNormal =
                "-fx-background-color: " + AMBER + ";" +
                        "-fx-text-fill: #0D0D0D;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-background-radius: 32;";

        String btnHover =
                "-fx-background-color: " + AMBER_DEEP + ";" +
                        "-fx-text-fill: #0D0D0D;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-background-radius: 32;";

        registerBtn.setStyle(btnNormal);
        DropShadow glow = new DropShadow(24, Color.web("#F59E0B66"));
        registerBtn.setOnMouseEntered(e -> {
            registerBtn.setStyle(btnHover);
            registerBtn.setEffect(glow);
        });
        registerBtn.setOnMouseExited(e -> {
            registerBtn.setStyle(btnNormal);
            registerBtn.setEffect(null);
        });

        // Register action
        registerBtn.setOnAction(e -> {
            String name     = nameField.getText().trim();
            String email    = emailField.getText().trim();
            String phone    = phoneField.getText().trim();
            String password = passField.getText().trim();
            String accType  = gridBtn.isSelected() ? "grid" : "solar";

            // 1. Empty fields check
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                setMessage(messageLabel, "⚠ Name, email and password are required.", false);
                return;
            }

// 2. Name must not contain numbers
            if (!name.matches("[a-zA-Z ]+")) {
                setMessage(messageLabel, "⚠ Name must contain letters only.", false);
                return;
            }

// 3. Email proper format validation
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                setMessage(messageLabel, "⚠ Please enter a valid email address (e.g. name@gmail.com).", false);
                return;
            }

// 4. Phone must be 11 digits and start with 03s
            if (!phone.isEmpty()) {
                String cleanPhone = phone.replaceAll("[\\s-]", "");
                if (!cleanPhone.matches("0[0-9]{10}")) {
                    setMessage(messageLabel, "⚠ Phone must be 11 digits and start with 0 (e.g. 03001234567).", false);
                    return;
                }
            }

// 5. Password length
            if (password.length() < 6) {
                setMessage(messageLabel, "⚠ Password must be at least 6 characters.", false);
                return;
            }

// 6. Password must contain a number
            if (!password.matches(".*[0-9].*")) {
                setMessage(messageLabel, "⚠ Password must contain at least one number.", false);
                return;
            }

            if (registerUser(name, email, phone, password, accType)) {
                setMessage(messageLabel,
                        "✅ Account created! Redirecting to login...", true);

                PauseTransition pause = new PauseTransition(
                        Duration.millis(1500));
                pause.setOnFinished(ev -> {
                    try {
                        new LoginScreen().start(stage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                pause.play();

            } else {
                setMessage(messageLabel,
                        "❌ Email already exists. Please use another.", false);
                shakeNode(form);
            }
        });

        // Back to login
        HBox loginRow = new HBox(4);
        loginRow.setAlignment(Pos.CENTER);
        loginRow.setPadding(new Insets(6, 0, 0, 0));

        Label alreadyLabel = new Label("Already have an account?");
        alreadyLabel.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';"
        );

        Hyperlink loginLink = new Hyperlink("Sign In");
        loginLink.setStyle(
                "-fx-text-fill: " + AMBER + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 12;" +
                        "-fx-padding: 0;"
        );
        loginLink.setOnAction(e -> {
            try {
                new LoginScreen().start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        loginRow.getChildren().addAll(alreadyLabel, loginLink);

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        panel.getChildren().addAll(
                logoRow,
                heroBox,
                form,
                createSpacer(10),
                registerBtn,
                loginRow,
                push
        );

        animateEntrance(heroBox,     150);
        animateEntrance(form,        280);
        animateEntrance(registerBtn, 400);
        animateEntrance(loginRow,    460);

        return panel;
    }

    // ════════════════════════════════════════════════════════
    //  RIGHT PANEL
    // ════════════════════════════════════════════════════════
    private StackPane buildRightPanel() {
        StackPane panel = new StackPane();
        panel.setStyle("-fx-background-color: #0D0D0D;");
        panel.setAlignment(Pos.CENTER);

        // Dot grid
        Canvas grid = new Canvas(520, 660);
        GraphicsContext gc = grid.getGraphicsContext2D();
        gc.setFill(Color.web("#F59E0B12"));
        double spacing = 30;
        for (double x = 4; x < 520; x += spacing)
            for (double y = 4; y < 660; y += spacing)
                gc.fillOval(x - 1, y - 1, 2.5, 2.5);

        // Ambient glow
        Circle glow = new Circle(220);
        glow.setFill(new RadialGradient(
                0, 0, 0.5, 0.5, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#F59E0B12")),
                new Stop(1, Color.TRANSPARENT)
        ));
        glow.setTranslateY(200);

        // Rings
        Circle ring1 = new Circle(110);
        ring1.setFill(Color.TRANSPARENT);
        ring1.setStroke(Color.web("#F59E0B"));
        ring1.setStrokeWidth(1.0);
        ring1.setOpacity(0.13);
        ring1.setTranslateX(-160);
        ring1.setTranslateY(230);

        Circle ring2 = new Circle(70);
        ring2.setFill(Color.TRANSPARENT);
        ring2.setStroke(Color.web("#F59E0B"));
        ring2.setStrokeWidth(1.0);
        ring2.setOpacity(0.10);
        ring2.setTranslateX(190);
        ring2.setTranslateY(-220);

        // Steps cards
        VBox stepsBox = new VBox(16);
        stepsBox.setAlignment(Pos.CENTER_LEFT);
        stepsBox.setMaxWidth(300);

        Text stepsTitle = new Text("Get started in 3 steps");
        stepsTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        stepsTitle.setFill(Color.WHITE);

        stepsBox.getChildren().addAll(
                stepsTitle,
                createSpacer(8),
                buildStepCard("01", "Create Account",
                        "Fill in your details and\nchoose your account type"),
                buildStepCard("02", "Add Your Meter",
                        "Enter your electricity meter\nreading to get started"),
                buildStepCard("03", "Track & Save",
                        "Monitor usage, get alerts\nand reduce your bill")
        );

        // Float animation
        TranslateTransition floatAnim = new TranslateTransition(
                Duration.millis(3400), stepsBox);
        floatAnim.setFromY(0);
        floatAnim.setToY(-10);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.setInterpolator(Interpolator.EASE_BOTH);
        floatAnim.play();

        VBox layout = new VBox(0);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(28, 24, 24, 24));
        layout.setMaxWidth(340);
        layout.getChildren().add(stepsBox);

        panel.getChildren().addAll(grid, glow, ring1, ring2, layout);

        animateEntrance(stepsBox, 300);

        return panel;
    }

    private HBox buildStepCard(String number, String title, String desc) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #252525;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );
        card.setEffect(new DropShadow(12, Color.web("#00000066")));

        Label numLbl = new Label(number);
        numLbl.setStyle(
                "-fx-background-color: " + AMBER + ";" +
                        "-fx-text-fill: #0D0D0D;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 13;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 10 6 10;"
        );

        VBox textBox = new VBox(3);
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 12;"
        );
        Label descLbl = new Label(desc);
        descLbl.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 10;"
        );
        textBox.getChildren().addAll(titleLbl, descLbl);
        card.getChildren().addAll(numLbl, textBox);
        return card;
    }

    // ════════════════════════════════════════════════════════
    //  DATABASE
    // ════════════════════════════════════════════════════════
    private boolean registerUser(String name, String email,
                                 String phone, String password,
                                 String accountType) {
        com.example.bijlibachat.service.UserService userService =
                new com.example.bijlibachat.service.UserService();
        return userService.registerUser(name, email, password, phone, accountType);
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    private void setMessage(Label label, String msg, boolean success) {
        label.setStyle(
                "-fx-text-fill: " + (success ? "#4ADE80" : "#FF4444") + ";" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';"
        );
        label.setText(msg);
    }

    private VBox createInputGroup(String labelText, Control input) {
        Label lbl = new Label(labelText);
        lbl.setStyle(
                "-fx-font-family: 'Verdana';" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + TEXT_DIM + ";" +
                        "-fx-font-size: 11;"
        );
        return new VBox(7, lbl, input);
    }

    private void styleTextField(TextField tf) {
        String base =
                "-fx-background-color: " + INPUT_BG + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 0 15 0 15;" +
                        "-fx-prompt-text-fill: #3A3A3A;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 13;";

        String focused =
                "-fx-background-color: " + INPUT_BG + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: " + AMBER + ";" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 0 15 0 15;" +
                        "-fx-prompt-text-fill: #3A3A3A;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 13;";

        tf.setStyle(base);
        tf.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) tf.setStyle(focused);
            else           tf.setStyle(base);
        });
    }

    private Region createSpacer(double height) {
        Region r = new Region();
        r.setMinHeight(height);
        r.setMaxHeight(height);
        return r;
    }

    private void shakeNode(Node node) {
        TranslateTransition shake = new TranslateTransition(
                Duration.millis(60), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }

    private void animateEntrance(Node node, int delayMs) {
        node.setOpacity(0);
        double origY = node.getTranslateY();
        node.setTranslateY(origY + 20);
        FadeTransition ft = new FadeTransition(Duration.millis(520), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));
        TranslateTransition tt = new TranslateTransition(
                Duration.millis(520), node);
        tt.setFromY(origY + 20);
        tt.setToY(origY);
        tt.setDelay(Duration.millis(delayMs));
        tt.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(ft, tt).play();
    }
}
