package com.example.bijlibachat.ui;

import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.Pos;

public class BijliLogo extends StackPane {
    private final String AMBER = "#F59E0B";

    public BijliLogo(double scale) {
        this.setAlignment(Pos.CENTER);
        this.setMaxSize(100 * scale, 100 * scale);

        Circle bulbOutline = new Circle(45 * scale);
        bulbOutline.setFill(Color.TRANSPARENT);
        bulbOutline.setStroke(Color.web(AMBER));
        bulbOutline.setStrokeWidth(3 * scale);

        SVGPath bBolt = new SVGPath();
        bBolt.setContent("M 20 10 L 10 35 L 18 35 L 12 60 L 35 25 L 25 25 L 35 10 Z");
        bBolt.setFill(Color.web(AMBER));
        bBolt.setScaleX(2.0 * scale);
        bBolt.setScaleY(2.0 * scale);

        VBox base = new VBox(2 * scale);
        base.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Rectangle seg = new Rectangle((18 - (i * 4)) * scale, 4 * scale);
            seg.setArcWidth(4); seg.setArcHeight(4);
            seg.setFill(Color.web(AMBER));
            base.getChildren().add(seg);
        }
        base.setTranslateY(48 * scale);

        this.getChildren().addAll(bulbOutline, bBolt, base);
    }
}