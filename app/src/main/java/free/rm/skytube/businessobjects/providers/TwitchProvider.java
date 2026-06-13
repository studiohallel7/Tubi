/*
 * SkyTube - Multiplatform Video Hub
 * Copyright (C) 2018  Ramon Mifsud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package free.rm.skytube.businessobjects.providers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Twitch implementation of the VideoProvider interface.
 * 
 * Supports Twitch live streams and past broadcasts (VODs).
 * Requires Twitch API credentials for full functionality.
 */
public class TwitchProvider extends VideoProvider {
    
    private static final String PROVIDER_NAME = "Twitch";
    private static final String BASE_URL = "https://www.twitch.tv/";
    private static final String API_BASE = "https://api.twitch.tv/helix";
    
    private String clientId;
    private String accessToken;
    
    /**
     * Constructor without credentials (limited functionality).
     */
    public TwitchProvider() {
        this.clientId = null;
        this.accessToken = null;
    }
    
    /**
     * Constructor with API credentials.
     * 
     * @param clientId Twitch API Client ID
     * @param accessToken Twitch API Access Token
     */
    public TwitchProvider(String clientId, String accessToken) {
        this.clientId = clientId;
        this.accessToken = accessToken;
    }
    
    @Override
    public List<VideoInfo> search(String query) throws IOException {
        if (clientId == null || accessToken == null) {
            throw new IOException("Twitch API credentials required for search");
        }
        
        List<VideoInfo> results = new ArrayList<>();
        
        // Search for channels/streams
        String searchUrl = API_BASE + "/search/channels?query=" + URLEncoder.encode(query, "UTF-8");
        JSONObject response = makeApiRequest(searchUrl);
        
        if (response != null && response.has("data")) {
            JSONArray channels = response.getJSONArray("data");
            for (int i = 0; i < channels.length(); i++) {
                JSONObject channel = channels.getJSONObject(i);
                
                VideoInfo videoInfo = new VideoInfo(
                    channel.getString("id"),
                    channel.getString("display_name"),
                    PROVIDER_NAME
                );
                
                videoInfo.setChannelName(channel.getString("display_name"));
                videoInfo.setChannelId(channel.getString("id"));
                videoInfo.setDescription(channel.optString("description", ""));
                
                // Check if currently live
                boolean isLive = channel.optBoolean("broadcaster_type") || 
                                channel.has("is_live");
                videoInfo.setIsLiveStream(isLive);
                
                if (isLive) {
                    videoInfo.setDuration("LIVE");
                }
                
                results.add(videoInfo);
            }
        }
        
        return results;
    }
    
    @Override
    public VideoInfo getVideo(String videoId) throws IOException {
        // For Twitch, videoId can be a channel name or video ID
        String apiUrl;
        
        if (videoId.startsWith("v")) {
            // This is a VOD ID
            apiUrl = API_BASE + "/videos?id=" + videoId;
        } else {
            // This is a channel name, check if live
            apiUrl = API_BASE + "/streams?user_login=" + videoId.toLowerCase();
        }
        
        JSONObject response = makeApiRequest(apiUrl);
        
        if (response != null && response.has("data")) {
            JSONArray data = response.getJSONArray("data");
            if (data.length() > 0) {
                JSONObject item = data.getJSONObject(0);
                
                boolean isStream = item.has("game_id"); // Streams have game_id
                
                VideoInfo videoInfo = new VideoInfo(
                    item.getString("id"),
                    item.optString("title", "Unknown"),
                    PROVIDER_NAME
                );
                
                if (isStream) {
                    // Live stream
                    videoInfo.setChannelId(item.getString("user_id"));
                    videoInfo.setChannelName(item.getString("user_name"));
                    videoInfo.setIsLiveStream(true);
                    videoInfo.setDuration("LIVE");
                    
                    // Get viewer count
                    videoInfo.setViewCount(item.optLong("viewer_count", 0));
                    
                    // Thumbnail URL with placeholders
                    String thumbnailUrl = item.optJSONObject("thumbnail_url").toString();
                    if (thumbnailUrl != null) {
                        thumbnailUrl = thumbnailUrl.replace("{width}", "640")
                                                  .replace("{height}", "480");
                        videoInfo.setThumbnailUrl(thumbnailUrl);
                    }
                } else {
                    // VOD
                    videoInfo.setChannelId(item.getString("user_id"));
                    videoInfo.setChannelName(item.getString("user_name"));
                    videoInfo.setIsLiveStream(false);
                    
                    // Duration in seconds
                    String duration = item.getString("duration");
                    videoInfo.setDuration(duration);
                    videoInfo.setDurationInSeconds(parseTwitchDuration(duration));
                    
                    videoInfo.setViewCount(item.optLong("view_count", 0));
                }
                
                videoInfo.setVideoUrl(BASE_URL + (isStream ? videoInfo.getChannelName() : "videos/" + videoInfo.getId()));
                
                return videoInfo;
            }
        }
        
        // Fallback: create basic video info
        VideoInfo videoInfo = new VideoInfo(videoId, "", PROVIDER_NAME);
        videoInfo.setVideoUrl(BASE_URL + videoId);
        return videoInfo;
    }
    
    @Override
    public String getStreamUrl(String videoId) throws IOException {
        // For live streams, return the channel URL
        // For VODs, return the VOD URL
        VideoInfo videoInfo = getVideo(videoId);
        return videoInfo.getVideoUrl();
        
        // Note: Actual HLS stream extraction would require additional API calls
        // and potentially client-id authentication headers
    }
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
    
    @Override
    public boolean supportsLiveStreams() {
        return true;
    }
    
    @Override
    public boolean requiresAuthentication() {
        return true; // Twitch API requires authentication for most endpoints
    }
    
    /**
     * Make an authenticated API request to Twitch.
     * 
     * @param urlString The API URL to call
     * @return JSONObject response or null if failed
     * @throws IOException if request fails
     */
    private JSONObject makeApiRequest(String urlString) throws IOException {
        if (clientId == null) {
            return null;
        }
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Client-ID", clientId);
        
        if (accessToken != null) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            return null;
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return new JSONObject(response.toString());
    }
    
    /**
     * Parse Twitch duration string (e.g., "1h30m15s", "45m30s") to seconds.
     * 
     * @param duration Twitch duration string
     * @return Duration in seconds
     */
    private int parseTwitchDuration(String duration) {
        int totalSeconds = 0;
        
        try {
            duration = duration.toLowerCase();
            
            if (duration.contains("h")) {
                String[] parts = duration.split("h");
                totalSeconds += Integer.parseInt(parts[0].trim()) * 3600;
                duration = parts[1];
            }
            
            if (duration.contains("m")) {
                String[] parts = duration.split("m");
                totalSeconds += Integer.parseInt(parts[0].trim()) * 60;
                duration = parts[1];
            }
            
            if (duration.contains("s")) {
                String[] parts = duration.split("s");
                totalSeconds += Integer.parseInt(parts[0].trim());
            }
        } catch (Exception e) {
            // Return -1 if parsing fails
            return -1;
        }
        
        return totalSeconds;
    }
}
