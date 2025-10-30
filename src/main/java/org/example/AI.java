package org.example;

import org.json.JSONObject;
import static org.example.Main.*;


public class AI {
    // Subclass: Word_Blacklist
    public static class Word_Blacklist {
        private int count = 0;
        private final int maxCount;

        // Creates List of blacklisted words
        private static final String[] BAD_WORDS = {
                "skibidi toilet","crap","stupid", "tralalero tralala", "bombardiro crocodilo","fuck","shit","ass", "bitch", "nigger", "clancker", "motherfucker"
        };
        // Adds 1 to the counter if a blacklisted word is found
        public Word_Blacklist(int maxCount) {
            super();
            this.maxCount = maxCount;
        }
        // Checks if the input contains a blacklisted word
        public boolean check(String text) {
            String lower = text.toLowerCase();
            for (String word : BAD_WORDS) {
                if (lower.contains(word)) {
                    count++;
                    return true;
                }
            }
            return false;
        }

        public int getCount() {
            return count;
        }

        public boolean isExceeded() {
            return count >= maxCount;
        }
    }

    // Subclass: Backend_Code
    public static class Backend_Code {
        private final Word_Blacklist filter;
        private java.util.List<String> conversationHistory;
        private Expressions expressions;

        public Backend_Code(int maxProfanity) {
            this.filter = new Word_Blacklist(maxProfanity);
            this.conversationHistory = new java.util.ArrayList<>();
            this.expressions = new Expressions();
        }


        /**
         * Process user input and return Buddy's response.
         * Returns null if profanity limit exceeded.
         */
        public String processMessage(String input) {
            if (filter.check(input)) {
                if (filter.isExceeded()) {
                    return null; // Signal shutdown
                }
                return "[Warning] Please avoid profanity. ("
                        + filter.getCount() + "/" + 10 + ")";
            }

            // Add user input to conversation history
            conversationHistory.add("User: " + input);
            String response = getAIResponse(input);

            // Add AI response to conversation history
            if (response != null && !response.startsWith("Buddy [error]")) {
                conversationHistory.add(response + "Please try again. :3");
            }
            return response;
        }


        // AI connection code, establishes conversation with 1st API Key and establishes emotion translation to 2nd API Key.
        private String getAIResponse(String input) {
            try {
                String apiKey = System.getenv("Buddy_Text_Responder");
                if (apiKey == null || apiKey.isEmpty()) {
                    return "Buddy [error]: Missing API key. Set Buddy_Text_Responder in your environment.";
                }

                String response = callAI(input, apiKey);
                // Get emotion for the response
                String emotion = getEmotionForResponse(input, response);
                String imagePath = expressions.emoteTranslate(emotion);

                //Return Response with the corresponding image file.
                return "Buddy: " + response + "|EMOTION:" + imagePath;


            } catch (Exception e) {
                return "Buddy [error]: Could not connect to AI service. (" + e.getMessage() + ")";
            }
        }


        private String callAI(String userMessage, String apiKey) throws Exception {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

            JSONObject requestBody = getJsonObject(userMessage);

            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    requestBody.toString(),
                    okhttp3.MediaType.parse("application/json")
            );

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();


            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new Exception("OpenAI API error: " + response.code());
                }

                if (response.body() == null) {
                    throw new Exception("Empty response body from OpenAI.");
                }

                String responseBody = response.body().string();
                org.json.JSONObject jsonResponse = new org.json.JSONObject(responseBody);


                return jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim();
            }

        }
        // Prompt manipulation for personality of the text AI will respond with, this gets sent to the AI with the intial prompt from the user, this personality can also be changed by the user by accessing the settings
        private JSONObject getJsonObject(String userMessage) {
            org.json.JSONArray messages = new org.json.JSONArray();
            String prompt = "Your name is Buddy. ";
            prompt+="You are an " + personality;
            if (mentoring){
                prompt+=", academically responsible study buddy. ";
                prompt+="Format responses in one or two sentences with no emojis while not exceeding 150 tokens. ";
                prompt+="Help people figure out answers by guiding them to solutions instead of giving direct answers.";
            }
            else {
                prompt+="Format responses in one or two sentences with no emojis while not exceeding 150 tokens. ";
            }
            prompt+="The user's prefered language is: " + prefLang;
            if (hasUser){
                prompt+="please referer to the user as: " + userName;
            }

            // Add a system message with personality
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", prompt);
            messages.put(systemMessage);

            // Add conversation history
            for (String historyItem : conversationHistory) {
                JSONObject historyMessage = new JSONObject();
                if (historyItem.startsWith("User: ")) {
                    historyMessage.put("role", "user");
                    historyMessage.put("content", historyItem.substring(6));
                } else if (historyItem.startsWith("Buddy: ")) {
                    historyMessage.put("role", "assistant");
                    historyMessage.put("content", historyItem.substring(7));
                }
                messages.put(historyMessage);
            }

            // Add current user message
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", userMessage);
            messages.put(message);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-5-nano-2025-08-07");
            requestBody.put("messages", messages);
            requestBody.put("verbosity", "low");
            requestBody.put("reasoning_effort", "minimal");
            return requestBody;
        }


        private String getEmotionForResponse(String userInput, String aiResponse) {
            try {
                String apiKey = System.getenv("Buddy_Emotion_Responder");
                if (apiKey == null || apiKey.isEmpty()) {
                    return "Buddy [error]: Missing API key. Set Buddy_Emotion_Responder in your environment.";
                }
                return getEmotionFromAI(userInput, aiResponse, apiKey);
            } catch (Exception e) {
                return "neutral"; // Default emotion if detection fails
            }
        }
        //Recives the emotion from the second AI to be parsed into the visual change with the buddy.
        private String getEmotionFromAI(String userInput, String aiResponse, String apiKey) throws Exception {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            JSONObject requestBody = AI.getJsonObject(userInput, aiResponse);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    requestBody.toString(),
                    okhttp3.MediaType.parse("application/json")
            );
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "neutral";
                }
                String responseBody = response.body().string();
                org.json.JSONObject jsonResponse = new org.json.JSONObject(responseBody);
                String emotion = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                        .toLowerCase();
                return emotion;
            }
        }
    }

    //Prompt for the second AI
    private static JSONObject getJsonObject(String userInput, String aiResponse) {
        org.json.JSONArray messages = new org.json.JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an emotion classifier. Based on the conversation context, determine Buddy's emotional state. Respond with ONLY one word from the following list: neutral, happy, humorous, mad, sad, thinking, realization, concerned, shocked, fearful");
        messages.put(systemMessage);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", "User said: '" + userInput + "' and Buddy responded: '" + aiResponse + "'. What emotion should Buddy display?");
        messages.put(message);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-5-nano-2025-08-07");
        requestBody.put("messages", messages);
        requestBody.put("verbosity", "low");
        requestBody.put("reasoning_effort", "minimal");
        return requestBody;
    }
}