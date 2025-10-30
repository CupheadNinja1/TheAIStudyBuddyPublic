package org.example;


public class Expressions {
    public String emoteTranslate(String emotion) {
        // Translate emotions into corresponding image file paths
        if ("neutral".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/01-Idle.png";
        }
        else if ("happy".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/02-Happy.png";
        }
        else if ("humorous".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/03-Laugh.png";
        }
        else if ("mad".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/04-Mad.png";
        }
        else if ("sad".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/05-Sad.png";
        }
        else if ("thinking".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/06-Thinking.png";
        }
        else if ("realization".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/07-Idea.png";
        }
        else if ("concerned".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/08-Concerned.png";
        }
        else if ("shocked".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/09-Shocked.png";
        }
        else if ("fearful".equalsIgnoreCase(emotion)) {
            return "src/main/java/org/example/Images/10-Fear.png";
        }
        else {
            return "src/main/java/org/example/Images/01-Idle.png";
        }
    }
}