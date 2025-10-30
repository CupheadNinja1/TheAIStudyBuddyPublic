package org.example;

import javax.swing.*;
import java.util.List;

public class Animation {
    private List<ImageIcon> frames;
    private int currentFrame = 0;
    private int frameDelay = 100; // milliseconds between frames
    private long lastFrameTime = 0;

    public Animation(List<ImageIcon> frames, int frameDelay) {
        this.frames = frames;
        this.frameDelay = frameDelay;
    }

    public ImageIcon getCurrentFrame() {
        long now = System.currentTimeMillis();
        if (now - lastFrameTime > frameDelay) {
            currentFrame = (currentFrame + 1) % frames.size();
            lastFrameTime = now;
        }
        return frames.get(currentFrame);
    }

    public void reset() {
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }
}