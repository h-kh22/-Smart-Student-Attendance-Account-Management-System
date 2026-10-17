package com.example.studentattendance;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;

/**
 * Utility class providing smooth JavaFX animations and micro-interactions
 * for scene transitions, card entrances, button feedback, and validation cues.
 */
public final class AnimationUtils {

    private AnimationUtils() {
        // Utility class
    }

    /**
     * Smoothly transitions the scene from the current root to a new root
     * using an synchronized cross-fade and subtle vertical translate.
     */
    public static void crossFadeScene(Parent currentRoot, Parent nextRoot, Scene scene) {
        if (scene == null || currentRoot == null) {
            if (scene != null && nextRoot != null) {
                scene.setRoot(nextRoot);
            }
            return;
        }

        // Quick fade-out for the old view
        FadeTransition fadeOut = new FadeTransition(Duration.millis(140), currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_BOTH);

        fadeOut.setOnFinished(event -> {
            scene.setRoot(nextRoot);
            animateEntrance(nextRoot);
        });

        fadeOut.play();
    }

    /**
     * Animates the entrance of a root node or card (fade in + slight slide up).
     */
    public static void animateEntrance(Node node) {
        if (node == null) return;

        node.setOpacity(0.0);
        node.setTranslateY(12.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(240), node);
        slideIn.setFromY(12.0);
        slideIn.setToY(0.0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallel = new ParallelTransition(fadeIn, slideIn);
        parallel.play();
    }

    /**
     * Smoothly fades in a child node (e.g. subview inside AnchorPane).
     */
    public static void fadeInChild(Node node) {
        if (node == null) return;
        node.setOpacity(0.0);
        FadeTransition ft = new FadeTransition(Duration.millis(220), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    /**
     * Subtle horizontal shake animation to indicate validation errors or failed action.
     */
    public static void shake(Node node) {
        if (node == null) return;

        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.LINEAR);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }
}
