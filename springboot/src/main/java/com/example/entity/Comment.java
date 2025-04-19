package com.example.entity;

public class Comment {
    private Integer id;
    private Integer filmId;
    private Double score;
    private String comment;
    private Integer userId;
    private String time;
    private String type;

    private String filmName;
    private String userName;
    /** Affective polarity: 0 = negative, 1 = neutral, 2 = positive **/
    private Integer sentiment;
    /** Probability of belonging to the positive category **/
    private Double positiveProb;
    /** The probability of falling into the negative category **/
    private Double negativeProb;

    public Integer getSentiment() {
        return sentiment;
    }

    public void setSentiment(Integer sentiment) {
        this.sentiment = sentiment;
    }

    public Double getPositiveProb() {
        return positiveProb;
    }

    public void setPositiveProb(Double positiveProb) {
        this.positiveProb = positiveProb;
    }

    public Double getNegativeProb() {
        return negativeProb;
    }

    public void setNegativeProb(Double negativeProb) {
        this.negativeProb = negativeProb;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFilmName() {
        return filmName;
    }

    public void setFilmName(String filmName) {
        this.filmName = filmName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFilmId() {
        return filmId;
    }

    public void setFilmId(Integer filmId) {
        this.filmId = filmId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
