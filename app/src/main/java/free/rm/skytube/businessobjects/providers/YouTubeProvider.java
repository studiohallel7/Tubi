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

import com.google.api.services.youtube.model.Video;
import com.google.api.client.util.DateTime;

import java.util.Date;

/**
 * YouTube implementation of the VideoProvider interface.
 * 
 * This class adapts the existing YouTube functionality to the new provider architecture,
 * allowing YouTube videos to work seamlessly alongside other platforms.
 */
public class YouTubeProvider extends VideoProvider {
    
    private static final String PROVIDER_NAME = "YouTube";
    private static final String BASE_URL = "https://www.youtube.com/watch?v=";
    
    @Override
    public java.util.List<VideoInfo> search(String query) throws java.io.IOException {
        // TODO: Implement YouTube search using existing YouTube API infrastructure
        // This would integrate with existing GetYouTubeVideoBySearch or similar classes
        throw new UnsupportedOperationException("Search not yet implemented for YouTube");
    }
    
    @Override
    public VideoInfo getVideo(String videoId) throws java.io.IOException {
        // TODO: Implement getting video details using existing GetVideoDetailsTask
        // For now, return a basic VideoInfo object
        
        VideoInfo videoInfo = new VideoInfo(videoId, "", PROVIDER_NAME);
        videoInfo.setVideoUrl(BASE_URL + videoId);
        videoInfo.setThumbnailUrl("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");
        videoInfo.setThumbnailMaxResUrl("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg");
        
        return videoInfo;
    }
    
    @Override
    public String getStreamUrl(String videoId) throws java.io.IOException {
        // TODO: Integrate with existing GetVideoStreamTask to get actual stream URL
        // This is a placeholder that returns the standard YouTube watch URL
        return BASE_URL + videoId;
    }
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
    
    @Override
    public boolean supportsLiveStreams() {
        return true;
    }
    
    /**
     * Convert an existing YouTubeVideo object to VideoInfo.
     * 
     * @param youTubeVideo The YouTubeVideo object from existing code
     * @return VideoInfo object with equivalent data
     */
    public static VideoInfo convertToVideoInfo(free.rm.skytube.businessobjects.YouTube.POJOs.YouTubeVideo youTubeVideo) {
        if (youTubeVideo == null) {
            return null;
        }
        
        VideoInfo videoInfo = new VideoInfo(
            youTubeVideo.getId(),
            youTubeVideo.getTitle(),
            PROVIDER_NAME
        );
        
        videoInfo.setDescription(youTubeVideo.getDescription());
        videoInfo.setChannelName(youTubeVideo.getChannelName());
        videoInfo.setChannelId(youTubeVideo.getChannelId());
        videoInfo.setThumbnailUrl(youTubeVideo.getThumbnailUrl());
        videoInfo.setThumbnailMaxResUrl(youTubeVideo.getThumbnailMaxResUrl());
        videoInfo.setDurationInSeconds(youTubeVideo.getDurationInSeconds());
        videoInfo.setDuration(youTubeVideo.getDuration());
        
        if (youTubeVideo.getViewsCountInt() != null) {
            videoInfo.setViewCount(youTubeVideo.getViewsCountInt().longValue());
        }
        
        if (youTubeVideo.getPublishDate() != null) {
            DateTime publishDate = youTubeVideo.getPublishDate();
            videoInfo.setPublishDate(new Date(publishDate.getValue()));
        }
        
        videoInfo.setIsLiveStream(youTubeVideo.isLiveStream());
        videoInfo.setVideoUrl(youTubeVideo.getVideoUrl());
        
        return videoInfo;
    }
}
