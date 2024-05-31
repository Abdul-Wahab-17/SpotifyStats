package org.example;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SpotifyStats {

    private static final Map<String, Object> statistics = new HashMap<>();

    public static void main(String[] args) {
        try {
            JSONArray combinedHistory = combineHistories(
                    "d:\\Downloads\\my_spotify_data (1)\\Spotify Extended Streaming History\\Streaming_History_Audio_2021-2022_0.json",
                    "d:\\Downloads\\my_spotify_data (1)\\Spotify Extended Streaming History\\Streaming_History_Audio_2022-2023_1.json",
                    "d:\\Downloads\\my_spotify_data (1)\\Spotify Extended Streaming History\\Streaming_History_Audio_2023-2024_2.json",
                    "d:\\Downloads\\my_spotify_data (1)\\Spotify Extended Streaming History\\Streaming_History_Audio_2024_3.json"
            );
            parseHistory(combinedHistory);
            displayStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONArray combineHistories(String... filePaths) throws Exception {
        JSONArray combinedHistory = new JSONArray();
        for (String filePath : filePaths) {
            combinedHistory.addAll(readHistory(filePath));
        }
        return combinedHistory;
    }

    private static JSONArray readHistory(String filePath) throws Exception {
        FileReader fileReader = new FileReader(filePath);
        JSONParser parser = new JSONParser();
        return (JSONArray) parser.parse(fileReader);
    }

    private static void parseHistory(JSONArray history) {
        int totalSongs = 0;
        Map<String, Integer> artistCounts = new HashMap<>();
        Map<String, Integer> songCounts = new HashMap<>();

        for (Object entry : history) {
            JSONObject item = (JSONObject) entry;

            String songName = (String) item.get("master_metadata_track_name");
            String artistName = (String) item.get("master_metadata_album_artist_name");

            if (songName == null || artistName == null) {
                continue;
            }

            totalSongs++;

            artistCounts.put(artistName, artistCounts.getOrDefault(artistName, 0) + 1);

            String songKey = songName + " - " + artistName;
            songCounts.put(songKey, songCounts.getOrDefault(songKey, 0) + 1);
        }

        statistics.put("totalSongs", totalSongs);
        statistics.put("artistCounts", artistCounts);
        statistics.put("songCounts", songCounts);
    }

    private static void displayStatistics() {
        System.out.println("Statistics:");
        System.out.println("Total songs listened: " + statistics.get("totalSongs"));
        displayTopFiftyArtists();
        displayTopFiftySongs();
    }

    private static void displayTopFiftyArtists() {
        System.out.println("\nTop Fifty Artists:");
        Map<String, Integer> artistCounts = (Map<String, Integer>) statistics.get("artistCounts");
        artistCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(50)
                .forEach(entry -> System.out.printf("%-30s %d\n", entry.getKey(), entry.getValue()));
    }

    private static void displayTopFiftySongs() {
        System.out.println("\nTop Fifty Songs:");
        Map<String, Integer> songCounts = (Map<String, Integer>) statistics.get("songCounts");
        songCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(50)
                .forEach(entry -> System.out.printf("%-50s %d\n", entry.getKey(), entry.getValue()));
    }
}
