package com.Bureau.Achivki;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String userName;
    private int score;
    private Map<String, Integer> achievements;

    public User() {

    }

    public User(String userName) {
        this.userName = userName;
        this.score = 0;
        this.achievements = new HashMap<>();
    }
    public User(String userName, int score) {
        this.userName = userName;
        this.score = score;
    }

    public void setName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public int getScore() {
        return score;
    }

    public Map<String, Integer> getAchievements() {
        return achievements;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void addAchievement(String achievementName, int achievementScore) {
        achievements.put(achievementName, achievementScore);
    }

    public boolean hasAchievement(String achievementName) {
        return achievements.containsKey(achievementName);
    }

    public int getAchievementScore(String achievementName) {
        return achievements.getOrDefault(achievementName, 0);
    }
}
