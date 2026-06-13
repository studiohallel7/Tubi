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

import java.io.Serializable;
import java.util.Date;

/**
 * Generic video information class that represents a video from any provider.
 * 
 * This class serves as a common data model across all video providers, allowing
 * the UI and player components to work with videos regardless of their source.
 */
public class VideoInfo implements Serializable {
    
    /**
     * Unique identifier for the video (provider-specific format).
     */
    private String id;
    
    /**
     * Video title.
     */
    private String title;
    
    /**
     * Video description.
     */
    private String description;
    
    /**
     * Channel/creator name.
     */
    private String channelName;
    
    /**
     * Channel/creator ID (provider-specific).
     */
    private String channelId;
    
    /**
     * Thumbnail URL (high quality).
     */
    private String thumbnailUrl;
    
    /**
     * Thumbnail URL (maximum resolution).
     */
    private String thumbnailMaxResUrl;
    
    /**
     * Video duration in seconds (-1 for live streams or unknown).
     */
    private int durationInSeconds;
    
    /**
     * Human-readable duration string (e.g., "5:15", "LIVE").
     */
    private String duration;
    
    /**
     * Total view count.
     */
    private long viewCount;
    
    /**
     * Publish date/time.
     */
    private Date publishDate;
    
    /**
     * Provider name (e.g., "YouTube", "Twitch", "Dailymotion").
     */
    private String providerName;
    
    /**
     * Set to true if the video is a current live stream.
     */
    private boolean isLiveStream;
    
    /**
     * Original platform URL for sharing.
     */
    private String videoUrl;
    
    /**
     * Constructor.
     * 
     * @param id Video ID
     * @param title Video title
     * @param providerName Name of the provider
     */
    public VideoInfo(String id, String title, String providerName) {
        this.id = id;
        this.title = title;
        this.providerName = providerName;
        this.durationInSeconds = -1;
        this.isLiveStream = false;
        this.viewCount = 0;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getChannelName() {
        return channelName;
    }
    
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
    
    public String getChannelId() {
        return channelId;
    }
    
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public String getThumbnailMaxResUrl() {
        return thumbnailMaxResUrl;
    }
    
    public void setThumbnailMaxResUrl(String thumbnailMaxResUrl) {
        this.thumbnailMaxResUrl = thumbnailMaxResUrl;
    }
    
    public int getDurationInSeconds() {
        return durationInSeconds;
    }
    
    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public long getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }
    
    public Date getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
    
    public boolean isLiveStream() {
        return isLiveStream;
    }
    
    public void setIsLiveStream(boolean isLiveStream) {
        this.isLiveStream = isLiveStream;
    }
    
    public String getVideoUrl() {
        return videoUrl;
    }
    
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    
    /**
     * Generate a platform-specific video URL.
     * Override this method in subclasses if needed.
     * 
     * @return The video URL
     */
    public String generateVideoUrl() {
        return videoUrl;
    }
}
