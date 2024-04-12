package com.example.pms;

import java.sql.Blob;
import java.time.Duration;
import java.time.LocalDateTime;

public class Post {
    private String postText;
    private Blob postImageBlob; // BLOB field for storing image
    private LocalDateTime dates;
    private int likeCount;

    private int postId;

    public int getPostId() {
        return postId;
    }
    public void setPostId(int postId) {
        this.postId = postId;
    }

    public Blob getPostImageBlob() {
        return postImageBlob;
    }

    public void setPostImageBlob(Blob postImageBlob) {
        this.postImageBlob = postImageBlob;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public LocalDateTime getDates() {
        return dates;
    }

    public void setDates(LocalDateTime dates) {
        this.dates = dates;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getFormattedDate() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dates, now);

        long days = duration.toDays();
        if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        }

        long hours = duration.toHours();
        if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }

        long minutes = duration.toMinutes();
        if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }

        return "Just now";
    }
}
