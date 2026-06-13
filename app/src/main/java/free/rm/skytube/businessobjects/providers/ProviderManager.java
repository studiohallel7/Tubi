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

import java.util.HashMap;
import java.util.Map;

/**
 * Provider Manager - Central registry for all video providers.
 * 
 * This class manages the registration and retrieval of video providers,
 * allowing the application to dynamically support multiple platforms.
 */
public class ProviderManager {
    
    private static ProviderManager instance;
    private Map<String, VideoProvider> providers;
    
    /**
     * Private constructor for singleton pattern.
     */
    private ProviderManager() {
        providers = new HashMap<>();
        registerDefaultProviders();
    }
    
    /**
     * Get the singleton instance of ProviderManager.
     * 
     * @return The ProviderManager instance
     */
    public static synchronized ProviderManager getInstance() {
        if (instance == null) {
            instance = new ProviderManager();
        }
        return instance;
    }
    
    /**
     * Register default providers (YouTube, Twitch, Dailymotion).
     */
    private void registerDefaultProviders() {
        // Register YouTube provider
        registerProvider(new YouTubeProvider());
        
        // Register Twitch provider (without credentials - limited functionality)
        registerProvider(new TwitchProvider());
        
        // Register Dailymotion provider
        registerProvider(new DailymotionProvider());
        
        // Optional: Register Vimeo provider (if implemented)
        // registerProvider(new VimeoProvider());
    }
    
    /**
     * Register a video provider.
     * 
     * @param provider The VideoProvider to register
     */
    public void registerProvider(VideoProvider provider) {
        if (provider != null) {
            providers.put(provider.getProviderName().toLowerCase(), provider);
        }
    }
    
    /**
     * Get a registered provider by name.
     * 
     * @param providerName The name of the provider (case-insensitive)
     * @return The VideoProvider instance, or null if not found
     */
    public VideoProvider getProvider(String providerName) {
        if (providerName == null) {
            return null;
        }
        return providers.get(providerName.toLowerCase());
    }
    
    /**
     * Get all registered providers.
     * 
     * @return Array of all registered VideoProvider instances
     */
    public VideoProvider[] getAllProviders() {
        return providers.values().toArray(new VideoProvider[0]);
    }
    
    /**
     * Check if a provider is registered.
     * 
     * @param providerName The name of the provider
     * @return true if the provider is registered, false otherwise
     */
    public boolean isProviderRegistered(String providerName) {
        return providers.containsKey(providerName.toLowerCase());
    }
    
    /**
     * Unregister a provider.
     * 
     * @param providerName The name of the provider to unregister
     * @return true if the provider was unregistered, false if not found
     */
    public boolean unregisterProvider(String providerName) {
        return providers.remove(providerName.toLowerCase()) != null;
    }
    
    /**
     * Detect provider from a URL.
     * 
     * @param url The video URL
     * @return The detected VideoProvider, or null if unknown
     */
    public VideoProvider detectProviderFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        String lowerUrl = url.toLowerCase();
        
        if (lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be")) {
            return getProvider("YouTube");
        } else if (lowerUrl.contains("twitch.tv")) {
            return getProvider("Twitch");
        } else if (lowerUrl.contains("dailymotion.com")) {
            return getProvider("Dailymotion");
        } else if (lowerUrl.contains("vimeo.com")) {
            return getProvider("Vimeo");
        }
        
        return null;
    }
    
    /**
     * Extract video ID from a URL based on the provider.
     * 
     * @param url The video URL
     * @return The extracted video ID, or null if extraction failed
     */
    public String extractVideoIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        VideoProvider provider = detectProviderFromUrl(url);
        if (provider == null) {
            return null;
        }
        
        // Provider-specific extraction logic
        String lowerUrl = url.toLowerCase();
        
        try {
            if (provider instanceof YouTubeProvider) {
                // Use existing YouTube ID extraction
                return free.rm.skytube.businessobjects.YouTube.POJOs.YouTubeVideo.getYouTubeIdFromUrl(url);
            } else if (provider instanceof TwitchProvider) {
                // Twitch URL patterns: twitch.tv/channelname or twitch.tv/videos/123456
                if (lowerUrl.contains("/videos/")) {
                    String[] parts = url.split("/videos/");
                    if (parts.length > 1) {
                        return "v" + parts[1].split("[?#]")[0];
                    }
                } else {
                    String[] parts = url.split("twitch.tv/");
                    if (parts.length > 1) {
                        return parts[1].split("[/?#]")[0];
                    }
                }
            } else if (provider instanceof DailymotionProvider) {
                // Dailymotion URL pattern: dailymotion.com/video/xyz123
                if (lowerUrl.contains("/video/")) {
                    String[] parts = url.split("/video/");
                    if (parts.length > 1) {
                        return parts[1].split("[?#]")[0];
                    }
                }
            }
        } catch (Exception e) {
            // Extraction failed
            return null;
        }
        
        return null;
    }
}
