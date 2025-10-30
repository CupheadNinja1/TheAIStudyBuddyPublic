package org.example;

//import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ShimejiWindow extends JWindow {
    private JLabel label;
    private Timer actionTimer;
    private boolean dragging = false;
    private State state = State.WALKING;
    private int sitCounter = 0;
    private int dx = 3; // horizontal speed

    // Animations
    private Animation walkAnim;
    private Animation sitAnim;
    private Animation idleAnim;
    private Animation currentAnim;

    private static final int TIMER_DELAY = 50;
    private static final int SIT_CHANCE = 200; // 1 in 200 ticks
    private static final int MAX_SIT_TIME = 100; // ticks (~5s)

    public ImageIcon flipImage(ImageIcon icon) {
        Image img = icon.getImage();
        BufferedImage flipped = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        g.drawImage(img, img.getWidth(null), 0, -img.getWidth(null), img.getHeight(null), null);
        g.dispose();
        return new ImageIcon(flipped);
    }

    public void close() {
        actionTimer.stop();
        dispose();
    }

    private enum State {
        WALKING,
        SITTING,
        IDLE
    }

    public ShimejiWindow(Icon defaultIcon) {
        // Initialize UI
        label = new JLabel(defaultIcon);
        getContentPane().add(label);
        pack();

        // Load animation frames (replace these paths with yours)
        walkAnim = new Animation(Arrays.asList(
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Walk.png"),
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Walk2.png"),
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Walk3.png"),
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Walk4.png")
        ), 500);

        sitAnim = new Animation(Arrays.asList(
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Sit.png")
        ), 500);

        idleAnim = new Animation(Arrays.asList(
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Idle.png"),
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Idle2.png"),
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Idle3.png"),
                new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Idle4.png")
        ), 500);

        currentAnim = walkAnim;

        // Start position bottom of screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBackground(new Color(0,0,0,0));
        int startX = (int) (Math.random() * (screen.width - getWidth()));
        int startY = screen.height - getHeight() - 40;
        setLocation(startX, startY);

        // Enable dragging
        enableDragging();

        // Start timer
        actionTimer = new Timer(TIMER_DELAY, e -> updateBehavior());
        actionTimer.start();

        setAlwaysOnTop(true);
        setVisible(true);
    }

    private void enableDragging() {
        final Point[] mouseOffset = {null};

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragging = true;
                mouseOffset[0] = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    Point p = e.getLocationOnScreen();
                    label.setIcon(new ImageIcon("src/main/java/org/example/IsaacShimejiAnimation/Isaac_Clicked.png"));
                    setLocation(p.x - mouseOffset[0].x, p.y - mouseOffset[0].y);
                }
            }
        });
    }

    private void updateBehavior() {
        if (dragging) return;

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int bottomY = screen.height - getHeight() - 40;
        Point pos = getLocation();

        switch (state) {
            case WALKING:
                if (Math.random() < 1.0 / SIT_CHANCE) {
                    state = State.SITTING;
                    sitCounter = 0;
                    currentAnim = sitAnim;
                    currentAnim.reset();
                    break;
                }

                int newX = pos.x + dx;

                // Bounce at edges
                if (newX < 0) {
                    newX = 0;
                    dx = -dx;
                } else if (newX > screen.width - getWidth()) {
                    newX = screen.width - getWidth();
                    dx = -dx;
                }

                setLocation(newX, bottomY);
                currentAnim = walkAnim;
                break;

            case SITTING:
                sitCounter++;
                setLocation(pos.x, bottomY + 100);
                if (sitCounter > MAX_SIT_TIME) {
                    setLocation(pos.x, bottomY);
                    state = State.WALKING;
                    currentAnim = walkAnim;
                }
                break;

            case IDLE:
                currentAnim = idleAnim;
                break;
        }

        // Update sprite frame
        if(dx > 0){
            label.setIcon(flipImage(currentAnim.getCurrentFrame()));
        }
        else {
            label.setIcon(currentAnim.getCurrentFrame());
        }
        }
    }