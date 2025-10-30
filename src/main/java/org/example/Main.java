package org.example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.*;

public class Main extends JFrame {
    // Make the idle timer a class-level static field so resetIdleTimer() can see it.
    private static javax.swing.Timer idleTimer;
    private static final int IDLE_TIMEOUT = 600_000; // 60 seconds

    // Keep references available across methods
    private static JLabel chara;
    private static JFrame jFrame;
    private static boolean isSettingsWindowOpen = false;
    private static ImageIcon imgIsaac;
    private static ShimejiWindow activeShimeji;
    private static JToggleButton spawn;
    private static String curSay;

    //Variables that can be changed in settings
    public static boolean t2S = true;
    public static boolean hasUser = false;
    public static String userName = "No Username";
    public static String personality = "encouraging, talkative, friendly, respectful, comedic";
    public static boolean mentoring = true;
    public static String prefLang = "English";

    public static void main(String[] args) {
        AI.Backend_Code buddy = new AI.Backend_Code(10);
        Buddy_TTS tts = new FreeTTSEngine();

        jFrame = new JFrame("AI Study Buddy");
        jFrame.setLayout(new BorderLayout());
        jFrame.setSize(500, 360);
        // Set window icon
        ImageIcon windowIcon = new ImageIcon("src/main/java/org/example/Images/App_Logo.png");
        jFrame.setIconImage(windowIcon.getImage());
        // Set background color
        jFrame.getContentPane().setBackground(new Color(0, 144, 255)); // blue

        // Position bottom-right
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - jFrame.getWidth();
        int y = screenSize.height - jFrame.getHeight() - 100;
        jFrame.setLocation(x, y);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);



        // Conversation area
        JTextArea outTxt = new JTextArea(3, 40);
        Border b1 = BorderFactory.createLineBorder(Color.BLACK);
        outTxt.setBackground(Color.WHITE);
        outTxt.setBorder(b1);
        outTxt.setText("AI Study Buddy's conversation will display here \nAI responses may take some time, please be patient...");
        outTxt.setFont(new Font("Arial", Font.PLAIN, 10));
        outTxt.setLineWrap(true);
        outTxt.setWrapStyleWord(true);
        outTxt.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outTxt);
        scrollPane.setPreferredSize(new Dimension(500, 80));


        // Input area
        JTextArea input = new JTextArea("Ask me Anything!", 2, 20);
        Border b3 = BorderFactory.createLineBorder(Color.BLACK);
        input.setBorder(b3);
        input.setBackground(Color.WHITE);
        input.setPreferredSize(new Dimension(500, 50));
        input.setFont(new Font("Arial", Font.PLAIN, 16));
        input.setLineWrap(true);
        input.setWrapStyleWord(true);
        input.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (input.getText().equals("Ask me Anything!")) {
                    input.setText("");
                }
                resetIdleTimer(); // reset when user focuses
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                resetIdleTimer();
                if (input.getText().isEmpty()) {
                    input.setText("Ask me Anything!");
                }
            }
        });


        // Main character label (effectively final)
        imgIsaac = new ImageIcon("src/main/java/org/example/Images/01-Idle.png");
        chara = new JLabel(imgIsaac);
        //When the User Right-Clicks the chara, This will open a Settings window
        chara.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && !isSettingsWindowOpen) {
                    JFrame settings = new JFrame("Settings");
                    settings.setLayout(new BorderLayout());
                    settings.setSize(500, 360);
                    isSettingsWindowOpen = false;
                    // Set window icon
                    ImageIcon windowIcon = new ImageIcon("src/main/java/org/example/Images/App_Logo.png");
                    settings.setIconImage(windowIcon.getImage());
                    // Set background color
                    settings.getContentPane().setBackground(new Color(150, 150, 150)); // Grey
                    // Position bottom-right
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    int x = screenSize.width - jFrame.getWidth() - settings.getWidth();
                    int y = screenSize.height - jFrame.getHeight() - 100;
                    settings.setLocation(x, y);
                    settings.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    settings.setResizable(false);

                    //JPanel for Settings to be formated
                    JPanel area = new JPanel();
                    area.setPreferredSize(new Dimension(500, 300));
                    area.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    //Settings Options
                    JToggleButton opt1 = new JToggleButton("Text-to-Speech Feature: ON");
                    opt1.setContentAreaFilled(true); // Prevents the L&F from filling the content area
                    opt1.setOpaque(true); // Makes the component opaque so the background color is visible
                    opt1.setPreferredSize(new Dimension(400, 25));
                    opt1.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (opt1.isSelected()) {
                                t2S = false;
                                opt1.setText("Text-to-Speech Feature: OFF");
                            } else {
                                t2S = true;
                                opt1.setText("Text-to-Speech Feature: ON");
                            }
                        }
                    });


                    //Mentor Feature Button + Toggle
                    JToggleButton opt2 = new JToggleButton("Mentor Feature: Guiding Response");
                    opt2.setContentAreaFilled(true); // Prevents the L&F from filling the content area
                    opt2.setOpaque(true); // Makes the component opaque so the background color is visible
                    opt2.setPreferredSize(new Dimension(400, 25));
                    opt2.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (opt2.isSelected()) {
                                mentoring = false;
                                opt2.setText("Mentor Feature: Direct Response");
                            } else {
                                mentoring = true;
                                opt2.setText("Mentor Feature: Guiding Response");
                            }
                        }
                    });


                    //Controls Shimenji animation
                    spawn = new JToggleButton("Shimeji", (activeShimeji != null));
                    spawn.setContentAreaFilled(true); // Prevents the L&F from filling the content area
                    spawn.setOpaque(true); // Makes the component opaque so the background color is visible
                    spawn.setPreferredSize(new Dimension(130, 50));
                    spawn.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (spawn.isSelected()) {
                                spawnShimeji();
                            }
                            else {
                                returnShimejiToChara();
                            }
                        }
                    });


                    JToggleButton close = new JToggleButton("Back", false);
                    close.setContentAreaFilled(true); // Prevents the L&F from filling the content area
                    close.setOpaque(true); // Makes the component opaque so the background color is visible
                    close.setPreferredSize(new Dimension(130, 50));
                    close.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (close.isSelected()) {
                                settings.dispose();
                            }
                        }
                    });


                    //Manual button to end program without hitting the IntelliJ end button
                    JToggleButton failSafe = new JToggleButton("End Program");
                    failSafe.setContentAreaFilled(true); // Prevents the L&F from filling the content area
                    failSafe.setOpaque(true); // Makes the component opaque so the background color is visible
                    failSafe.setPreferredSize(new Dimension(130, 50));
                    failSafe.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (failSafe.isSelected()) {
                                System.exit(0);
                            }
                        }
                    });


                    //Creates the setting for manual personality edits for the AI by the user
                    JTextPane aiPerson = new JTextPane();
                    aiPerson.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    aiPerson.setText(personality);
                    aiPerson.setEditable(true);
                    aiPerson.setPreferredSize(new Dimension(300, 25));
                    aiPerson.addKeyListener(new java.awt.event.KeyAdapter() {
                        @Override
                        public void keyPressed(java.awt.event.KeyEvent e) {
                            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !e.isShiftDown()) {
                                e.consume(); // prevent newline

                                String enteredText = aiPerson.getText().trim();
                                if (!(enteredText.isEmpty())) {
                                    personality = enteredText;
                                }
                            }
                        }
                    });


                    //Section to enter the user's chosen username which the AI will reference the current user as
                    JTextPane user = new JTextPane();
                    user.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    user.setText(userName);
                    user.setEditable(false);
                    user.setPreferredSize(new Dimension(400, 30));
                    user.addKeyListener(new java.awt.event.KeyAdapter() {
                        @Override
                        public void keyPressed(java.awt.event.KeyEvent e) {
                            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !e.isShiftDown()) {
                                e.consume(); // prevent newline
                                String enteredText = user.getText().trim();
                                if (!(enteredText.isEmpty())) {
                                    userName = enteredText;
                                }
                            }
                        }
                    });


                    //Section to enter the user's preferred language in which the AI will respond in
                    JTextPane lang = new JTextPane();
                    lang.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    lang.setText(prefLang);
                    lang.setEditable(true);
                    lang.setPreferredSize(new Dimension(200, 25));
                    lang.addKeyListener(new java.awt.event.KeyAdapter() {
                        @Override
                        public void keyPressed(java.awt.event.KeyEvent e) {
                            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !e.isShiftDown()) {
                                e.consume(); // prevent newline
                                    String enteredText = lang.getText().trim();
                                if (!(enteredText.isEmpty()) || enteredText.equalsIgnoreCase(prefLang)) {
                                    prefLang = enteredText;
                                }
                            }
                        }
                    });

                    JCheckBox wantName = new JCheckBox("Username (Select to Edit)", false);
                    wantName.setPreferredSize(new Dimension(400, 25));
                    wantName.addItemListener( n -> {
                        hasUser = wantName.isSelected();
                        user.setEditable(wantName.isSelected());
                    });

                    JTextPane sectAI = new JTextPane();
                    sectAI.setText("AI Settings:");
                    sectAI.setOpaque(false);
                    sectAI.setEditable(false);
                    sectAI.setPreferredSize(new Dimension(400, 25));

                    JTextPane sectUser = new JTextPane();
                    sectUser.setText("User Settings:");
                    sectUser.setOpaque(false);
                    sectUser.setEditable(false);
                    sectUser.setPreferredSize(new Dimension(400, 25));

                    JTextPane sectLang = new JTextPane();
                    sectLang.setText("Language Setting:");
                    sectLang.setOpaque(false);
                    sectLang.setEditable(false);
                    sectLang.setPreferredSize(new Dimension(200, 25));

                    JTextPane sectPer = new JTextPane();
                    sectPer.setText("Personality:");
                    sectPer.setOpaque(false);
                    sectPer.setEditable(false);
                    sectPer.setPreferredSize(new Dimension(100, 25));

                    //Add all Settings to be displayed and visible
                    area.add(sectAI);
                    area.add(opt1);
                    area.add(opt2);
                    area.add(sectPer);
                    area.add(aiPerson);
                    area.add(sectLang);
                    area.add(lang);
                    area.add(sectUser);
                    area.add(wantName);
                    area.add(user);
                    area.add(spawn);
                    area.add(close);
                    area.add(failSafe);
                    settings.add(area);
                    settings.setVisible(true);
                }
            }
        });

        // Key listener for Enter
        input.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume(); // prevent newline
                    resetIdleTimer();
                    String enteredText = input.getText().trim();
                    if (enteredText.isEmpty() || enteredText.equalsIgnoreCase("Ask me anything!")) {
                        return;
                    }
                    // show temporary thinking icon (so user sees feedback)
                    chara.setIcon(new ImageIcon("src/main/java/org/example/Images/06-Thinking.png"));
                    input.setText("Loading...");

                    // Run backend processing off the EDT so UI stays responsive
                    SwingWorker<String, Void> worker = new SwingWorker<>() {
                        @Override
                        protected String doInBackground() {
                            return buddy.processMessage(enteredText);
                        }
                        @Override
                        protected void done() {
                            try {
                                String response = get(); // get result of doInBackground()
                                if (response == null) {
                                    outTxt.append("\n[System]: Profanity limit exceeded. Application shutting down.");
                                    System.exit(0);
                                }
                                String displayText = response;
                                // handle emotion token and switch icon if provided
                                if (response.contains("|EMOTION:")) {
                                    String[] parts = response.split("\\|EMOTION:");
                                    displayText = parts[0];
                                    if (parts.length > 1) {
                                        String imagePath = parts[parts.length - 1].trim();
                                        ImageIcon newIcon = new ImageIcon(imagePath);
                                        chara.setIcon(newIcon);
                                    } else {
                                        // fallback to idle icon
                                        chara.setIcon(imgIsaac);
                                    }
                                } else {
                                    // if no emotion, restore idle
                                    chara.setIcon(imgIsaac);
                                }
                                outTxt.append("\nYou: " + enteredText);
                                outTxt.append("\n" + displayText);
                                outTxt.setCaretPosition(outTxt.getDocument().getLength());
                                input.setText("");
                                curSay = displayText;
                                if (curSay.contains("Buddy: ")) {
                                    String[] partSpk = curSay.split("Buddy: ");
                                    curSay = partSpk[1];

                                    SwingUtilities.invokeLater(() -> {
                                        tts.speak(curSay);
                                    });
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                chara.setIcon(imgIsaac);
                                input.setText("");
                                outTxt.append("\n[Error] processing response.");
                            }
                        }
                    };
                    worker.execute();
                }
            }
        });
        // Reset idle timer on any mouse click inside the main frame (keeps it responsive)
        jFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                resetIdleTimer();
            }
        });
        jFrame.add(chara, BorderLayout.CENTER);
        jFrame.add(scrollPane, BorderLayout.NORTH);
        jFrame.add(input, BorderLayout.SOUTH);
        jFrame.setVisible(true);
        // start idle timer AFTER UI is visible
        startIdleTimer();
    }
    // Start idle boredom timer
    private static void startIdleTimer() {
        idleTimer = new javax.swing.Timer(IDLE_TIMEOUT, e -> {
            spawnShimeji();
            spawn.setSelected(activeShimeji != null);
        });
        idleTimer.setRepeats(true);
        idleTimer.start();
    }
    public static void resetIdleTimer() {
        if (idleTimer != null) {
            idleTimer.restart();
        }
        // If the user becomes focused, pull Shimeji back in
        returnShimejiToChara();
    }
    private static void spawnShimeji() {
        if (chara == null || activeShimeji != null) return; // prevent multiple
        Icon currentIcon = chara.getIcon();
        // Hide chara
        chara.setVisible(false);
        jFrame.revalidate();
        jFrame.repaint();

        // Spawn Shimeji
        activeShimeji = new ShimejiWindow(currentIcon);

        // Auto return after 8 seconds if still active
        new javax.swing.Timer(60000, e -> returnShimejiToChara()).start();
    }

    //Returns the Shimeji to the main interactive window
    private static void returnShimejiToChara() {
        if (activeShimeji != null) {
            activeShimeji.close();
            activeShimeji = null;
            chara.setVisible(true);
            jFrame.revalidate();
            jFrame.repaint();
        }
    }
}