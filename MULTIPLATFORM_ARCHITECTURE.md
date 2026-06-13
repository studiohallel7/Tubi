# SkyTube Multiplatform Video Hub - Architecture Documentation

## Overview

This document describes the new multiplatform architecture for SkyTube, transforming it from a YouTube-only player into a **Multiplatform Video Hub** that supports:

- **YouTube** (existing functionality - maintained and enhanced)
- **Twitch** (live streams and VODs)
- **Dailymotion** (videos)
- **Vimeo** (optional/future implementation)

---

## 🏗️ Architecture Layers

### 1. Provider Layer (`free.rm.skytube.businessobjects.providers`)

The core of the new architecture. This layer abstracts video platform differences behind a common interface.

#### Core Classes

```
VideoProvider (abstract base class)
├── YouTubeProvider
├── TwitchProvider  
├── DailymotionProvider
└── VimeoProvider (future)

VideoInfo (common data model)
ProviderManager (singleton registry)
```

#### VideoProvider Interface

All providers must implement these methods:

```java
public abstract class VideoProvider {
    // Search for videos by query
    public abstract List<VideoInfo> search(String query) throws IOException;
    
    // Get video details by ID
    public abstract VideoInfo getVideo(String videoId) throws IOException;
    
    // Get stream URL for playback
    public abstract String getStreamUrl(String videoId) throws IOException;
    
    // Get provider display name
    public abstract String getProviderName();
    
    // Optional capabilities
    public boolean supportsLiveStreams() { return false; }
    public boolean requiresAuthentication() { return false; }
}
```

#### VideoInfo Data Model

Common data structure for all videos regardless of source:

```java
public class VideoInfo {
    private String id;              // Platform-specific video ID
    private String title;           // Video title
    private String description;     // Video description
    private String channelName;     // Creator/channel name
    private String channelId;       // Creator/channel ID
    private String thumbnailUrl;    // Thumbnail URL
    private int durationInSeconds;  // Duration (-1 for live/unknown)
    private String duration;        // Human-readable duration
    private long viewCount;         // View count
    private Date publishDate;       // Publication date
    private String providerName;    // Source platform name
    private boolean isLiveStream;   // Live stream flag
    private String videoUrl;        // Original platform URL
}
```

---

## 📦 File Structure

```
app/src/main/java/free/rm/skytube/businessobjects/providers/
├── VideoProvider.java      # Base abstract class
├── VideoInfo.java          # Common video data model
├── ProviderManager.java    # Provider registry & URL detection
├── YouTubeProvider.java    # YouTube implementation
├── TwitchProvider.java     # Twitch implementation
└── DailymotionProvider.java # Dailymotion implementation
```

---

## 🔌 Provider Implementations

### YouTubeProvider

- **Status**: ✅ Implemented (adapter for existing code)
- **Features**:
  - Integrates with existing `YouTubeVideo` class
  - Static method `convertToVideoInfo()` for backward compatibility
  - Supports live streams
  - Uses existing YouTube API infrastructure

### TwitchProvider

- **Status**: ✅ Implemented
- **Features**:
  - Live stream detection
  - VOD (past broadcast) support
  - Requires API credentials for full functionality
  - Supports channel search
  - HLS stream ready (URL-based playback)

**Configuration Required**:
```java
TwitchProvider provider = new TwitchProvider(
    "your-client-id",
    "your-access-token"
);
```

### DailymotionProvider

- **Status**: ✅ Implemented
- **Features**:
  - Video search
  - Video metadata retrieval
  - No authentication required for basic features
  - RESTful API integration

---

## 🎯 ProviderManager

Central singleton for managing providers:

```java
ProviderManager manager = ProviderManager.getInstance();

// Get specific provider
VideoProvider youtube = manager.getProvider("YouTube");

// Detect provider from URL
VideoProvider provider = manager.detectProviderFromUrl(url);

// Extract video ID from any supported URL
String videoId = manager.extractVideoIdFromUrl(url);

// Register custom provider
manager.registerProvider(new CustomProvider());

// List all available providers
VideoProvider[] allProviders = manager.getAllProviders();
```

### URL Detection

The ProviderManager can automatically detect the source platform:

| URL Pattern | Detected Provider |
|-------------|------------------|
| `youtube.com/*`, `youtu.be/*` | YouTube |
| `twitch.tv/*` | Twitch |
| `dailymotion.com/*` | Dailymotion |
| `vimeo.com/*` | Vimeo (future) |

---

## 🔄 Migration Strategy

### Backward Compatibility

Existing YouTube code remains functional:

```java
// Old way (still works)
YouTubeVideo video = new YouTubeVideo(apiVideo);
video.getDesiredStream(listener);

// New way (unified across platforms)
VideoInfo videoInfo = YouTubeProvider.convertToVideoInfo(youTubeVideo);
String streamUrl = youtubeProvider.getStreamUrl(videoInfo.getId());
```

### Integration Points

1. **Search**: Use `VideoProvider.search(query)` instead of platform-specific search classes
2. **Video Details**: Use `VideoProvider.getVideo(id)` for unified video info
3. **Playback**: Use `VideoProvider.getStreamUrl(id)` to get playable URLs
4. **UI Display**: Bind UI components to `VideoInfo` objects

---

## 🚀 Future Enhancements

### Recommended Next Steps

1. **Player Adapter Layer**
   - Create `UnifiedPlayerAdapter` that handles different stream types
   - Support HLS (Twitch), progressive (YouTube), and DASH formats

2. **UI Components**
   - Multi-platform video grid adapter
   - Provider selection/filtering UI
   - Unified search across all providers

3. **Additional Providers**
   - Vimeo provider implementation
   - PeerTube support (decentralized)
   - Custom RTMP/stream URLs

4. **Advanced Features**
   - Cross-platform watch history
   - Unified subscriptions/favorites
   - Provider-specific settings

---

## 📝 Code Examples

### Basic Usage

```java
// Initialize provider manager
ProviderManager pm = ProviderManager.getInstance();

// Search YouTube
VideoProvider youtube = pm.getProvider("YouTube");
List<VideoInfo> results = youtube.search("android tutorial");

// Get video details
VideoInfo video = youtube.getVideo(results.get(0).getId());

// Play video
String streamUrl = youtube.getStreamUrl(video.getId());
// Pass streamUrl to your video player component
```

### Multi-Provider Search

```java
// Search across all providers
ProviderManager pm = ProviderManager.getInstance();
List<VideoInfo> allResults = new ArrayList<>();

for (VideoProvider provider : pm.getAllProviders()) {
    try {
        List<VideoInfo> providerResults = provider.search("query");
        allResults.addAll(providerResults);
    } catch (IOException e) {
        // Handle provider-specific errors
    }
}

// Sort by relevance/date as needed
```

### URL Handling

```java
// User pastes any video URL
String url = userInput.getText().toString();

// Automatically detect and play
ProviderManager pm = ProviderManager.getInstance();
VideoProvider provider = pm.detectProviderFromUrl(url);

if (provider != null) {
    String videoId = pm.extractVideoIdFromUrl(url);
    String streamUrl = provider.getStreamUrl(videoId);
    playVideo(streamUrl);
} else {
    showError("Unsupported video URL");
}
```

---

## ⚠️ Important Notes

1. **API Credentials**: Some providers (Twitch) require API keys. Store these securely.
2. **Threading**: All provider methods should be called from background threads.
3. **Error Handling**: Always wrap provider calls in try-catch blocks.
4. **Network**: Ensure proper network permissions and connectivity checks.
5. **Licensing**: Verify each provider's API terms of service.

---

## 📄 License

This architecture is part of SkyTube, licensed under GPLv3.
