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

import java.io.IOException;
import java.util.List;

/**
 * Base interface for all video providers (YouTube, Twitch, Dailymotion, Vimeo, etc.).
 * 
 * This interface defines the contract that all video providers must implement to be
 * part of the multiplatform video hub architecture.
 */
public abstract class VideoProvider {
    
    /**
     * Search for videos based on a query string.
     * 
     * @param query The search query string
     * @return List of {@link VideoInfo} objects representing search results
     * @throws IOException if an error occurs during the search
     */
    public abstract List<VideoInfo> search(String query) throws IOException;
    
    /**
     * Get detailed information about a specific video.
     * 
     * @param videoId The unique identifier for the video on this platform
     * @return {@link VideoInfo} object containing detailed video information
     * @throws IOException if an error occurs while fetching video details
     */
    public abstract VideoInfo getVideo(String videoId) throws IOException;
    
    /**
     * Get the stream URL for a specific video.
     * 
     * @param videoId The unique identifier for the video on this platform
     * @return The stream URL that can be used for playback
     * @throws IOException if an error occurs while fetching the stream URL
     */
    public abstract String getStreamUrl(String videoId) throws IOException;
    
    /**
     * Get the provider name (e.g., "YouTube", "Twitch", "Dailymotion").
     * 
     * @return The human-readable name of this provider
     */
    public abstract String getProviderName();
    
    /**
     * Check if this provider supports live streams.
     * 
     * @return true if the provider supports live streaming, false otherwise
     */
    public boolean supportsLiveStreams() {
        return false;
    }
    
    /**
     * Check if this provider requires authentication for certain operations.
     * 
     * @return true if authentication is required, false otherwise
     */
    public boolean requiresAuthentication() {
        return false;
    }
}
