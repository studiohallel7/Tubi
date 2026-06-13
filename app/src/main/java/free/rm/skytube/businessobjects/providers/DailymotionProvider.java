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
 * Dailymotion implementation of the VideoProvider interface.
 * 
 * Supports Dailymotion videos and basic search functionality.
 */
public class DailymotionProvider extends VideoProvider {
    
    private static final String PROVIDER_NAME = "Dailymotion";
    private static final String BASE_URL = "https://www.dailymotion.com/video/";
    private static final String API_BASE = "https://api.dailymotion.com";
    
    @Override
    public List<VideoInfo> search(String query) throws IOException {
        List<VideoInfo> results = new ArrayList<>();
        
        String searchUrl = API_BASE + "/videos?search=" + URLEncoder.encode(query, "UTF-8") 
            + "&fields=id,title,description,duration,views_total,created_time,owner.name,owner.id,thumbnail_url";
        
        JSONObject response = makeApiRequest(searchUrl);
        
        if (response != null && response.has("list")) {
            JSONArray videos = response.getJSONArray("list");
            for (int i = 0; i < videos.length(); i++) {
                JSONObject video = videos.getJSONObject(i);
                
                VideoInfo videoInfo = new VideoInfo(
                    video.getString("id"),
                    video.getString("title"),
                    PROVIDER_NAME
                );
                
                videoInfo.setDescription(video.optString("description", ""));
                
                if (video.has("owner")) {
                    JSONObject owner = video.getJSONObject("owner");
                    videoInfo.setChannelName(owner.optString("name", ""));
                    videoInfo.setChannelId(owner.optString("id", ""));
                }
                
                videoInfo.setDurationInSeconds(video.optInt("duration", -1));
                videoInfo.setViewCount(video.optLong("views_total", 0));
                
                if (video.has("created_time")) {
                    long timestamp = video.getLong("created_time") * 1000; // Convert to milliseconds
                    videoInfo.setPublishDate(new Date(timestamp));
                }
                
                if (video.has("thumbnail_url")) {
                    videoInfo.setThumbnailUrl(video.getString("thumbnail_url"));
                }
                
                videoInfo.setVideoUrl(BASE_URL + videoInfo.getId());
                
                results.add(videoInfo);
            }
        }
        
        return results;
    }
    
    @Override
    public VideoInfo getVideo(String videoId) throws IOException {
        String videoUrl = API_BASE + "/video/" + videoId 
            + "?fields=id,title,description,duration,views_total,created_time,owner.name,owner.id,thumbnail_url";
        
        JSONObject response = makeApiRequest(videoUrl);
        
        if (response != null && !response.has("error")) {
            VideoInfo videoInfo = new VideoInfo(
                response.getString("id"),
                response.getString("title"),
                PROVIDER_NAME
            );
            
            videoInfo.setDescription(response.optString("description", ""));
            
            if (response.has("owner")) {
                JSONObject owner = response.getJSONObject("owner");
                videoInfo.setChannelName(owner.optString("name", ""));
                videoInfo.setChannelId(owner.optString("id", ""));
            }
            
            videoInfo.setDurationInSeconds(response.optInt("duration", -1));
            videoInfo.setViewCount(response.optLong("views_total", 0));
            
            if (response.has("created_time")) {
                long timestamp = response.getLong("created_time") * 1000;
                videoInfo.setPublishDate(new Date(timestamp));
            }
            
            if (response.has("thumbnail_url")) {
                videoInfo.setThumbnailUrl(response.getString("thumbnail_url"));
            }
            
            videoInfo.setVideoUrl(BASE_URL + videoInfo.getId());
            
            return videoInfo;
        }
        
        // Fallback: create basic video info
        VideoInfo videoInfo = new VideoInfo(videoId, "", PROVIDER_NAME);
        videoInfo.setVideoUrl(BASE_URL + videoId);
        return videoInfo;
    }
    
    @Override
    public String getStreamUrl(String videoId) throws IOException {
        // Get video information
        VideoInfo videoInfo = getVideo(videoId);
        return videoInfo.getVideoUrl();
        
        // Note: Actual HLS stream extraction would require additional API calls
        // to get the manifest URL
    }
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
    
    /**
     * Make an API request to Dailymotion.
     * 
     * @param urlString The API URL to call
     * @return JSONObject response or null if failed
     * @throws IOException if request fails
     */
    private JSONObject makeApiRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
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
}
