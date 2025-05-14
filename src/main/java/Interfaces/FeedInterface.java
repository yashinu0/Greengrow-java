package Interfaces;

import Entities.Feed;
import java.util.List;

public interface FeedInterface {
    void addFeed(Feed feed);
    List<Feed> getAllFeeds();
    Feed getFeedById(int id);
    void updateFeed(Feed feed);
    void deleteFeed(int id);
    void updateFeedProcessedStatus(int id, boolean processed);
    List<Feed> getUnprocessedFeeds();
    List<Feed> getProcessedFeeds();
} 