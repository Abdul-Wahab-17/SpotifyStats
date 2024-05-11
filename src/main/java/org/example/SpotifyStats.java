package org.example;
import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class SpotifyStats {

    private static HashMap<String, Object> statistics = new HashMap<>();

    public static void main(String[] args) {
        try {
            // Step 1: Read Spotify history from files and combine them
            JSONArray combinedHistory = combineHistories(
                    "D:\\Downloads\\my_spotify_data\\MyData\\StreamingHistory1.json",
                    "D:\\Downloads\\my_spotify_data\\MyData\\StreamingHistory0.json");

            // Step 2: Parse combined history and calculate statistics
            parseHistory(combinedHistory);

            // Step 3: Display statistics
            displayStatistics();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONArray combineHistories(String filePath1, String filePath2) throws Exception {
        // Read and parse history from the first file
        JSONArray history1 = readHistory(filePath1);
        // Read and parse history from the second file
        JSONArray history2 = readHistory(filePath2);
        // Combine both histories into a single list
        JSONArray combinedHistory = new JSONArray();
        combinedHistory.addAll(history1);
        combinedHistory.addAll(history2);
        return combinedHistory;
    }

    private static JSONArray readHistory(String filePath) throws Exception {
        FileReader fileReader = new FileReader(filePath);
        JSONParser parser = new JSONParser();
        return (JSONArray) parser.parse(fileReader);
    }

    private static void parseHistory(JSONArray history) {
        int totalSongs = 0;
        HashMap<String, Integer> artistCounts = new HashMap<>();
        HashMap<String, Integer> songCounts = new HashMap<>();

        // Iterate through each entry in the history
        for (Object entry : history) {
            JSONObject item = (JSONObject) entry;
            String songName = (String) item.get("trackName");
            String artistName = (String) item.get("artistName");

            // Increment total song count
            totalSongs++;

            // Update artist counts
            artistCounts.put(artistName, artistCounts.getOrDefault(artistName, 0) + 1);

            // Update song counts
            String songKey = songName + " - " + artistName;
            songCounts.put(songKey, songCounts.getOrDefault(songKey, 0) + 1);
        }

        // Store statistics
        statistics.put("totalSongs", totalSongs);
        statistics.put("artistCounts", artistCounts);
        statistics.put("songCounts", songCounts);
    }

    // Method to display statistics
    private static void displayStatistics() {
        System.out.println("Statistics:");
        System.out.println("Total songs listened: " + statistics.get("totalSongs"));
        displayTopFiftyArtists();
        displayTopFiftySongs();
    }

    // Method to display top fifty artists
    private static void displayTopFiftyArtists() {
        System.out.println("\nTop Fifty Artists:");
        HashMap<String, Integer> artistCounts = (HashMap<String, Integer>) statistics.get("artistCounts");
        artistCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(50)
                .forEach(entry -> System.out.printf("%-30s %d\n", entry.getKey(), entry.getValue()));
    }

    // Method to display top fifty songs
    private static void displayTopFiftySongs() {
        System.out.println("\nTop Fifty Songs:");
        HashMap<String, Integer> songCounts = (HashMap<String, Integer>) statistics.get("songCounts");
        songCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(50)
                .forEach(entry -> System.out.printf("%-50s %d\n", entry.getKey(), entry.getValue()));
    }
}