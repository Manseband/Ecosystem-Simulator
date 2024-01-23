/* Name: Andrew Manga
 ** Date: June 14, 2023
 ** Class: ICS4U1 - J. Radulovic
 ** Assignment: Culminating
 ** Purpose: Create a JavaFX program to simulate and illustrate the changes in population in an ecosystem.
 */

package com.program.sim;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public abstract class Sim {

    protected final double UPDATE_RATE; // The initial update rate cannot be changed, only the speed multiplier
    protected final Timeline TIMELINE;
    protected final double MIN_RATE_MULTIPLIER;
    protected final double MAX_RATE_MULTIPLIER;

    public Sim(double updateRate) {
        UPDATE_RATE = updateRate;
        TIMELINE = new Timeline(new KeyFrame(Duration.seconds(updateRate), (t) -> simulate()));
        TIMELINE.setCycleCount(Timeline.INDEFINITE);

        // Timeline's rate property is not a KeyFrame (in seconds), but a fractional multiplier
        // The exponent represents how far the speed can multiply in one direction
        MIN_RATE_MULTIPLIER = 1.0 / Math.pow(2, 3);
        MAX_RATE_MULTIPLIER = Math.pow(2, 3);
    }

    public abstract void simulate();

    public void play() {
        TIMELINE.play();
    }

    public void pause() {
        TIMELINE.pause();
    }

    public void halveSpeed() {
        if (TIMELINE.getRate() > MIN_RATE_MULTIPLIER)
            TIMELINE.setRate(TIMELINE.getRate() / 2);
    }

    public void doubleSpeed() {
        if (TIMELINE.getRate() < MAX_RATE_MULTIPLIER)
            TIMELINE.setRate(TIMELINE.getRate() * 2);
    }

    public boolean isPaused() {
        return TIMELINE.getStatus().equals(Animation.Status.PAUSED);
    }

    public double getRate() {
        return UPDATE_RATE / TIMELINE.getRate();
    }

    public double getUpdateRate() {
        return UPDATE_RATE;
    }
}
