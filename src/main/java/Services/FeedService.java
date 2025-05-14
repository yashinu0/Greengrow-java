package Services;

import Interfaces.FeedInterface;
import Entities.Feed;
import Utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeedService implements FeedInterface {
    private static final Logger LOGGER = Logger.getLogger(FeedService.class.getName());
    private final Connection connection;

    public FeedService() {
        this.connection = MyDB.getInstance().getCon();
    }

    @Override
    public void addFeed(Feed feed) {
        String query = "INSERT INTO feed (email_feed, commentaire_feed, subject_feed, date_feed, name_feed, is_processed, sentiment) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, feed.getEmail_feed());
            pst.setString(2, feed.getCommentaire_feed());
            pst.setString(3, feed.getSubject_feed());
            pst.setTimestamp(4, new Timestamp(feed.getDate_feed().getTime()));
            pst.setString(5, feed.getName_feed());
            pst.setBoolean(6, feed.isIs_processed());
            pst.setString(7, feed.getSentiment());
            
            pst.executeUpdate();
            LOGGER.info("Feed added successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding feed", e);
            throw new RuntimeException("Error adding feed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Feed> getAllFeeds() {
        return getFeedsByStatus(null);
    }

    @Override
    public Feed getFeedById(int id) {
        String query = "SELECT * FROM feed WHERE id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFeed(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting feed by ID: " + id, e);
            throw new RuntimeException("Error getting feed: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void updateFeed(Feed feed) {
        String query = "UPDATE feed SET email_feed = ?, commentaire_feed = ?, subject_feed = ?, " +
                      "date_feed = ?, name_feed = ?, is_processed = ?, sentiment = ? WHERE id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, feed.getEmail_feed());
            pst.setString(2, feed.getCommentaire_feed());
            pst.setString(3, feed.getSubject_feed());
            pst.setTimestamp(4, new Timestamp(feed.getDate_feed().getTime()));
            pst.setString(5, feed.getName_feed());
            pst.setBoolean(6, feed.isIs_processed());
            pst.setString(7, feed.getSentiment());
            pst.setInt(8, feed.getId());
            
            pst.executeUpdate();
            LOGGER.info("Feed updated successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating feed", e);
            throw new RuntimeException("Error updating feed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFeed(int id) {
        String query = "DELETE FROM feed WHERE id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting feed failed, no rows affected.");
            }
            LOGGER.info("Feed deleted successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting feed", e);
            throw new RuntimeException("Error deleting feed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Feed> getUnprocessedFeeds() {
        return getFeedsByStatus(false);
    }

    @Override
    public List<Feed> getProcessedFeeds() {
        return getFeedsByStatus(true);
    }

    @Override
    public void updateFeedProcessedStatus(int id, boolean processed) {
        String query = "UPDATE feed SET is_processed = ? WHERE id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setBoolean(1, processed);
            pst.setInt(2, id);
            
            pst.executeUpdate();
            LOGGER.info("Feed status updated successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating feed status", e);
            throw new RuntimeException("Error updating feed status: " + e.getMessage(), e);
        }
    }

    private List<Feed> getFeedsByStatus(Boolean processed) {
        List<Feed> feeds = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM feed");
        
        if (processed != null) {
            query.append(" WHERE is_processed = ").append(processed);
        }
        query.append(" ORDER BY date_feed DESC");
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query.toString())) {
            while (rs.next()) {
                feeds.add(mapResultSetToFeed(rs));
            }
        } catch (SQLException e) {
            String status = processed == null ? "all" : (processed ? "processed" : "unprocessed");
            LOGGER.log(Level.SEVERE, "Error getting " + status + " feeds", e);
            throw new RuntimeException("Error getting " + status + " feeds: " + e.getMessage(), e);
        }
        return feeds;
    }

    private Feed mapResultSetToFeed(ResultSet rs) throws SQLException {
        Feed feed = new Feed();
        feed.setId(rs.getInt("id"));
        feed.setEmail_feed(rs.getString("email_feed"));
        feed.setCommentaire_feed(rs.getString("commentaire_feed"));
        feed.setSubject_feed(rs.getString("subject_feed"));
        feed.setDate_feed(rs.getTimestamp("date_feed"));
        feed.setName_feed(rs.getString("name_feed"));
        feed.setIs_processed(rs.getBoolean("is_processed"));
        feed.setSentiment(rs.getString("sentiment"));
        return feed;
    }
} 