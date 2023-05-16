package com.Bureau.Achivki;

public class Leader {
    private String name;
    private int score;
    private String profileImageUrl;

    private String token;

    public Leader() {
        // Пустой конструктор требуется для Firebase Firestore
    }

    public Leader(String name, int score, String profileImageUrl, String token) {
        this.name = name;
        this.score = score;
        this.profileImageUrl = profileImageUrl;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public String getToken() {
        return token;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}