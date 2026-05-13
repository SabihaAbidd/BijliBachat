package com.example.bijlibachat.ui;

import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

final class ResponsiveStageHelper {
    private static final String TRANSITION_NEW_ROOT = "transition.newRoot";

    private ResponsiveStageHelper() {
    }

    static Scene createScene(Stage stage, Parent root, double defaultWidth, double defaultHeight, Paint fill) {
        double width = defaultWidth;
        double height = defaultHeight;

        if (stage.getScene() != null) {
            width = stage.getScene().getWidth() > 0 ? stage.getScene().getWidth() : defaultWidth;
            height = stage.getScene().getHeight() > 0 ? stage.getScene().getHeight() : defaultHeight;
        }

        if (stage.getScene() != null) {
            Scene existingScene = stage.getScene();
            Parent currentRoot = existingScene.getRoot();
            StackPane transitionRoot = new StackPane(currentRoot, root);
            existingScene.setRoot(transitionRoot);
            existingScene.setFill(fill);
            existingScene.getProperties().put(TRANSITION_NEW_ROOT, root);
            return existingScene;
        }

        Scene scene = new Scene(root, width, height);
        scene.setFill(fill);
        return scene;
    }

    static void apply(Stage stage, Scene scene, String title, double minWidth, double minHeight) {
        stage.setTitle(title);
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setScene(scene);

        if (!stage.isShowing()) {
            stage.show();
        } else {
            stage.toFront();
        }

        Object pendingRoot = scene.getProperties().remove(TRANSITION_NEW_ROOT);
        if (pendingRoot instanceof Parent newRoot) {
            PauseTransition cleanup = new PauseTransition(Duration.millis(380));
            cleanup.setOnFinished(event -> {
                if (scene.getRoot() != newRoot) {
                    scene.setRoot(newRoot);
                }
            });
            cleanup.play();
        }
    }
}
