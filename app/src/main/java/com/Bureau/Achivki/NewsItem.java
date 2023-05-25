package com.Bureau.Achivki;

public class NewsItem {
    private String name;
    private Long likes;
    private String time;
    private String photoUrl;

    public NewsItem() {
    }

    public NewsItem(String name, Long likes, String time, String photoUrl) {
        this.name = name;
        this.likes = likes;
        this.time = time;
        this.photoUrl = photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
