package org.example;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SpotifyStats {

    private static final Map<String, Object> statistics = new HashMap<>();

    public static void main(String[] args) {
        try {
            String folderPath = "../Spotify Extended Streaming History/";
            JSONArray combinedHistory = readAllJsonFiles(folderPath);

            parseHistory(combinedHistory);
            displayStatistics();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read all .json files in a folder
    private static JSONArray readAllJsonFiles(String folderPath) throws Exception {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Folder not found: " + folderPath);
        }

        JSONArray combinedHistory = new JSONArray();

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                combinedHistory.addAll(readHistory(file.getAbsolutePath()));
            }
        }

        return combinedHistory;
    }

    private static JSONArray readHistory(String filePath) throws Exception {
        JSONParser parser = new JSONParser();
        try (Reader reader = new FileReader(filePath)) {
            return (JSONArray) parser.parse(reader);
        }
    }

    private static void parseHistory(JSONArray history) {
        int totalSongs = 0;
        long totalListeningTime = 0;

        Map<String, Integer> artistCounts = new HashMap<>();
        Map<String, Integer> songCounts = new HashMap<>();

        for (Object entry : history) {
            JSONObject item = (JSONObject) entry;

            String songName = (String) item.get("master_metadata_track_name");
            String artistName = (String) item.get("master_metadata_album_artist_name");
            Long msPlayed = (Long) item.get("ms_played");

            if (songName == null || artistName == null || msPlayed == null) continue;

            // 30% rule: approx 3 min song = 180,000ms → 30% = 54,000ms
           // if (msPlayed < 54000) continue;

            totalSongs++;
            totalListeningTime += msPlayed;

            artistCounts.put(artistName, artistCounts.getOrDefault(artistName, 0) + 1);

            String songKey = songName + " - " + artistName;
            songCounts.put(songKey, songCounts.getOrDefault(songKey, 0) + 1);
        }

        statistics.put("totalSongs", totalSongs);
        statistics.put("artistCounts", artistCounts);
        statistics.put("songCounts", songCounts);
        statistics.put("totalListeningTime", totalListeningTime);
    }

    private static void displayStatistics() {
        System.out.println("🎵 Spotify Extended Listening Summary 🎵\n");

        int totalSongs = (int) statistics.get("totalSongs");
        long totalListeningTime = (long) statistics.get("totalListeningTime");

        System.out.println("Total Songs Played (30% rule applied): " + totalSongs);
        System.out.println("Total Listening Time: " + formatMs(totalListeningTime));
        System.out.println("Average Time per Song: " +
                formatMs(totalSongs == 0 ? 0 : totalListeningTime / totalSongs));

        displayTopFiftyArtists();
        displayTopFiftySongs();
    }

    private static void displayTopFiftyArtists() {
        System.out.println("\nTop 50 Artists:");
        Map<String, Integer> artistCounts = (Map<String, Integer>) statistics.get("artistCounts");
        artistCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .forEach(entry ->
                        System.out.printf("%-30s %d plays%n", entry.getKey(), entry.getValue()));
    }

    private static void displayTopFiftySongs() {
        System.out.println("\nTop 50 Songs:");
        Map<String, Integer> songCounts = (Map<String, Integer>) statistics.get("songCounts");
        songCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .forEach(entry ->
                        System.out.printf("%-60s %d plays%n", entry.getKey(), entry.getValue()));
    }

    private static String formatMs(long ms) {
        long seconds = ms / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02dh %02dm %02ds", hours, minutes, secs);
    }
}
