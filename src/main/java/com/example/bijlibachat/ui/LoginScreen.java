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

public class LoginScreen extends Application {

    private static final String AMBER      = "#F59E0B";
    private static final String AMBER_DEEP = "#D97706";
    private static final String DARK_BG    = "#0D0D0D";
    private static final String PANEL_BG   = "#111111";
    private static final String CARD_BG    = "#161616";
    private static final String INPUT_BG   = "#181818";
    private static final String BORDER     = "#2A2A2A";
    private static final String TEXT_MUTED = "#6B7280";
    private static final String TEXT_DIM   = "#3D4450";
    private final com.example.bijlibachat.service.UserService userService =
            new com.example.bijlibachat.service.UserService();

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
        ResponsiveStageHelper.apply(stage, scene, "Bijli Bachat | Login", 700, 500);

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
        panel.setPadding(new Insets(40, 55, 44, 55));

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
        heroBox.setPadding(new Insets(36, 0, 28, 0));
        Text headline1 = new Text("Salam,");
        headline1.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        headline1.setFill(Color.WHITE);
        Text headline2 = new Text("Welcome Back");
        headline2.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        headline2.setFill(Color.WHITE);
        Rectangle accentBar = new Rectangle(220, 4);
        accentBar.setArcWidth(4);
        accentBar.setArcHeight(4);
        accentBar.setFill(Color.web(AMBER));
        heroBox.getChildren().addAll(
                headline1,
                headline2,
                createSpacer(8),
                accentBar
        );

        // Form fields
        VBox form = new VBox(16);

        TextField emailField = new TextField();
        emailField.setPromptText("Your email");
        emailField.setPrefHeight(50);
        styleTextField(emailField);
        VBox emailGroup = createInputGroup("Email", emailField);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Your password");
        passField.setPrefHeight(50);
        styleTextField(passField);
        VBox passGroup = createInputGroup("Password", passField);

        // Remember me + Forgot password
        HBox rememberRow = new HBox(8);
        rememberRow.setAlignment(Pos.CENTER_LEFT);
        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle(
                "-fx-text-fill: #9CA3AF;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';"
        );
        Region rowSpacer = new Region();
        HBox.setHgrow(rowSpacer, Priority.ALWAYS);
        Hyperlink forgotLink = new Hyperlink("Forgot Password?");
        forgotLink.setStyle(
                "-fx-text-fill: " + AMBER + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 11;" +
                        "-fx-padding: 0;"
        );
        rememberRow.getChildren().addAll(rememberMe, rowSpacer, forgotLink);

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setStyle(
                "-fx-text-fill: #FF4444;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';"
        );

        form.getChildren().addAll(
                emailGroup,
                passGroup,
                rememberRow,
                errorLabel
        );

        // Sign In button
        Button signInBtn = new Button("Sign In");
        signInBtn.setMaxWidth(Double.MAX_VALUE);
        signInBtn.setPrefHeight(54);
        signInBtn.setCursor(Cursor.HAND);

        String btnNormal =
                "-fx-background-color: " + AMBER + ";" +
                        "-fx-text-fill: #0D0D0D;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-background-radius: 32;";

        String btnHover =
                "-fx-background-color: " + AMBER_DEEP + ";" +
                        "-fx-text-fill: #0D0D0D;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-background-radius: 32;";

        signInBtn.setStyle(btnNormal);
        DropShadow glow = new DropShadow(24, Color.web("#F59E0B66"));
        glow.setSpread(0.1);
        signInBtn.setOnMouseEntered(e -> {
            signInBtn.setStyle(btnHover);
            signInBtn.setEffect(glow);
        });
        signInBtn.setOnMouseExited(e -> {
            signInBtn.setStyle(btnNormal);
            signInBtn.setEffect(null);
        });

        // Sign in action
        signInBtn.setOnAction(e -> {
            String email    = emailField.getText().trim();
            String password = passField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("⚠ Please enter email and password.");
                shakeNode(form);
                return;
            }
            com.example.bijlibachat.service.UserService.AuthenticationResult authResult =
                    authenticateUser(email, password);
            if (authResult.isAuthenticated()) {
                try {
                    new DashboardScreen(email).start(stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (authResult.isLockedOut()) {
                errorLabel.setText("Too many failed attempts. Sign in is disabled for this session.");
                signInBtn.setDisable(true);
                shakeNode(form);
            } else {
                errorLabel.setText("Invalid email or password. Attempts left: " +
                        authResult.getRemainingAttempts());
                shakeNode(form);
            }
        });

        // Enter key triggers login
        passField.setOnAction(e -> signInBtn.fire());

        // Sign up row
        HBox signupRow = new HBox(4);
        signupRow.setAlignment(Pos.CENTER);
        signupRow.setPadding(new Insets(8, 0, 0, 0));

        Label noAccLabel = new Label("Don't have an account?");
        noAccLabel.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 12;" +
                        "-fx-font-family: 'Verdana';"
        );

        Hyperlink signupLink = new Hyperlink("Sign Up");
        signupLink.setStyle(
                "-fx-text-fill: " + AMBER + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 12;" +
                        "-fx-padding: 0;"
        );
        signupLink.setOnAction(e -> {
            try {
                new RegisterScreen().start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        signupRow.getChildren().addAll(noAccLabel, signupLink);

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        panel.getChildren().addAll(
                logoRow,
                heroBox,
                form,
                createSpacer(14),
                signInBtn,
                signupRow,
                push
        );

        animateEntrance(heroBox,   150);
        animateEntrance(form,      300);
        animateEntrance(signInBtn, 420);
        animateEntrance(signupRow, 490);

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

        // Soft ambient glow
        Circle glow = new Circle(220);
        glow.setFill(new RadialGradient(
                0, 0, 0.5, 0.5, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#F59E0B12")),
                new Stop(1, Color.TRANSPARENT)
        ));
        glow.setTranslateY(200);

        // Two clean stroke rings
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

        // Slogan
        VBox sloganBox = new VBox(2);
        sloganBox.setAlignment(Pos.CENTER);
        Text sloganLine1 = new Text("Bijli Bachao,");
        sloganLine1.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        sloganLine1.setFill(Color.web("#D1D5DB"));
        sloganLine1.setTextAlignment(TextAlignment.CENTER);
        Text sloganLine2 = new Text("Paisay Bachao ⚡");
        sloganLine2.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        sloganLine2.setFill(Color.web(AMBER));
        sloganLine2.setEffect(new DropShadow(14, Color.web("#F59E0B55")));
        sloganLine2.setTextAlignment(TextAlignment.CENTER);
        sloganBox.getChildren().addAll(sloganLine1, sloganLine2);

        // Dashboard cards
        VBox cardStack = buildDashboardPreview();
        TranslateTransition floatAnim = new TranslateTransition(
                Duration.millis(3400), cardStack);
        floatAnim.setFromY(0);
        floatAnim.setToY(-10);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.setInterpolator(Interpolator.EASE_BOTH);
        floatAnim.play();

        // Main vertical layout
        VBox layout = new VBox(16);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(28, 24, 24, 24));
        layout.setMaxWidth(340);
        layout.getChildren().addAll(sloganBox, cardStack);

        panel.getChildren().addAll(grid, glow, ring1, ring2, layout);

        animateEntrance(sloganBox, 250);
        animateEntrance(cardStack,  380);

        return panel;
    }

    // ════════════════════════════════════════════════════════
    //  DASHBOARD PREVIEW CARDS
    // ════════════════════════════════════════════════════════
    private VBox buildDashboardPreview() {
        VBox stack = new VBox(10);
        stack.setAlignment(Pos.CENTER);

        // Card 1 — Bill Summary
        VBox billCard = buildCard();
        Label billTitle = new Label("Monthly Bill");
        billTitle.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 10;" +
                        "-fx-font-family: 'Verdana';"
        );
        HBox billRow = new HBox();
        billRow.setAlignment(Pos.CENTER_LEFT);
        Label billVal = new Label("Rs. 2,840");
        billVal.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Verdana';"
        );
        Region bs = new Region();
        HBox.setHgrow(bs, Priority.ALWAYS);
        Label badge = new Label("↓ 27.5%");
        badge.setStyle(
                "-fx-background-color: #162310;" +
                        "-fx-text-fill: #4ADE80;" +
                        "-fx-font-size: 10;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Verdana';" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 3 8 3 8;"
        );
        billRow.getChildren().addAll(billVal, bs, badge);
        Label billSub = new Label("vs Rs. 3,920 last month");
        billSub.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 9;" +
                        "-fx-font-family: 'Verdana';"
        );

        // Mini bar chart
        HBox bars = new HBox(5);
        bars.setAlignment(Pos.BOTTOM_LEFT);
        bars.setPadding(new Insets(8, 0, 0, 0));
        int[] heights = {22, 32, 26, 38, 24, 18, 34};
        String[] months = {"Oct","Nov","Dec","Jan","Feb","Mar","Apr"};
        for (int i = 0; i < heights.length; i++) {
            VBox col = new VBox(3);
            col.setAlignment(Pos.BOTTOM_CENTER);
            Rectangle bar = new Rectangle(22, heights[i]);
            bar.setArcWidth(4);
            bar.setArcHeight(4);
            bar.setFill(i == 6 ? Color.web(AMBER) : Color.web("#252525"));
            if (i == 6) bar.setEffect(
                    new DropShadow(8, Color.web("#F59E0B66")));
            Label mLbl = new Label(months[i]);
            mLbl.setStyle(
                    "-fx-text-fill: " + TEXT_MUTED + ";" +
                            "-fx-font-size: 7;" +
                            "-fx-font-family: 'Verdana';"
            );
            col.getChildren().addAll(bar, mLbl);
            bars.getChildren().add(col);
        }
        billCard.getChildren().addAll(billTitle, billRow, billSub, bars);

        // Card Row 2 — two mini cards
        HBox miniRow = new HBox(10);

        VBox unitsCard = buildCard();
        unitsCard.setPrefWidth(140);
        Label unitsTitle = new Label("Units This Month");
        unitsTitle.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 9;" +
                        "-fx-font-family: 'Verdana';"
        );
        Label unitsVal = new Label("180 kWh");
        unitsVal.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 17;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Verdana';"
        );
        Label unitsDelta = new Label("↓ 12% vs last month");
        unitsDelta.setStyle(
                "-fx-text-fill: #4ADE80;" +
                        "-fx-font-size: 9;" +
                        "-fx-font-family: 'Verdana';"
        );
        unitsCard.getChildren().addAll(unitsTitle, unitsVal, unitsDelta);

        VBox peakCard = buildCard();
        peakCard.setPrefWidth(140);
        Label peakTitle = new Label("Peak Hours");
        peakTitle.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 9;" +
                        "-fx-font-family: 'Verdana';"
        );
        Label peakVal = new Label("6 PM – 10 PM");
        peakVal.setStyle(
                "-fx-text-fill: " + AMBER + ";" +
                        "-fx-font-size: 12;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Verdana';"
        );
        Label peakSub = new Label("Avoid high usage\nduring this window");
        peakSub.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 9;" +
                        "-fx-font-family: 'Verdana';"
        );
        peakCard.getChildren().addAll(peakTitle, peakVal, peakSub);

        HBox.setHgrow(unitsCard, Priority.ALWAYS);
        HBox.setHgrow(peakCard,  Priority.ALWAYS);
        miniRow.getChildren().addAll(unitsCard, peakCard);

        // Card 3 — Appliance Breakdown
        VBox breakCard = buildCard();
        Label breakTitle = new Label("Appliance Breakdown");
        breakTitle.setStyle(
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 10;" +
                        "-fx-font-family: 'Verdana';"
        );
        VBox appRows = new VBox(7);
        appRows.setPadding(new Insets(4, 0, 0, 0));
        String[][] appliances = {
                {"AC",           "45", "#F59E0B"},
                {"Water Heater", "28", "#60A5FA"},
                {"Lighting",     "15", "#4ADE80"},
                {"Other",        "12", "#A78BFA"}
        };
        for (String[] app : appliances) {
            HBox appRow = new HBox(8);
            appRow.setAlignment(Pos.CENTER_LEFT);
            Circle dot = new Circle(4, Color.web(app[2]));
            Label appName = new Label(app[0]);
            appName.setStyle(
                    "-fx-text-fill: #9CA3AF;" +
                            "-fx-font-size: 10;" +
                            "-fx-font-family: 'Verdana';"
            );
            appName.setPrefWidth(80);
            double pct = Double.parseDouble(app[1]) / 100.0;
            StackPane prog = new StackPane();
            prog.setAlignment(Pos.CENTER_LEFT);
            Rectangle track = new Rectangle(90, 4);
            track.setArcWidth(4);
            track.setArcHeight(4);
            track.setFill(Color.web("#222222"));
            Rectangle fill = new Rectangle(90 * pct, 4);
            fill.setArcWidth(4);
            fill.setArcHeight(4);
            fill.setFill(Color.web(app[2]));
            prog.getChildren().addAll(track, fill);
            Region appSpacer = new Region();
            HBox.setHgrow(appSpacer, Priority.ALWAYS);
            Label pctLbl = new Label(app[1] + "%");
            pctLbl.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 10;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-family: 'Verdana';"
            );
            pctLbl.setPrefWidth(32);
            pctLbl.setAlignment(Pos.CENTER_RIGHT);
            appRow.getChildren().addAll(
                    dot, appName, prog, appSpacer, pctLbl);
            appRows.getChildren().add(appRow);
        }
        breakCard.getChildren().addAll(breakTitle, appRows);

        stack.getChildren().addAll(billCard, miniRow, breakCard);
        return stack;
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    private VBox buildCard() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #252525;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );
        card.setEffect(new DropShadow(16, Color.web("#00000088")));
        return card;
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
        tf.focusedProperty().addListener((obs, oldVal, isFocused) -> {
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

    private com.example.bijlibachat.service.UserService.AuthenticationResult authenticateUser(
            String email, String password) {
        return userService.authenticateLogin(email, password);
    }
}
