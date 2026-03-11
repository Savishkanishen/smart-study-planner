package com.studyplanner.smartstudyplannerfx;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class AIService 
{
private static final String API_KEY = System.getenv("GEMINI_API_KEY");
   
private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + API_KEY;

    public String generateStudyMaterial(String subject, double score, String topics) throws Exception {
        // This creates the instruction for the AI based on your project data
        String prompt = "As an expert tutor, for the subject " + subject + " where I scored " + score + "%, " +
                        "generate a 7-day study plan, summary notes for " + topics + ", and 3 practice questions.";

        // Construct the JSON request body
        String jsonRequest = "{ \"contents\": [{ \"parts\":[{ \"text\": \"" + prompt + "\" }] }] }";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // Returns the AI's response in JSON format
    }

   public String extractTextFromResponse(String jsonResponse) {
    try {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        
        // Check if Gemini returned an error block
        if (jsonObject.has("error")) {
            return "API Error: " + jsonObject.getAsJsonObject("error").get("message").getAsString();
        }

        // Check if candidates exists
        if (jsonObject.has("candidates")) {
            return jsonObject.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        }
        
        return "Unexpected Response Format: " + jsonResponse;
    } catch (Exception e) {
        return "Parsing Error: " + e.getMessage() + "\nRaw Response: " + jsonResponse;
    }
}



}