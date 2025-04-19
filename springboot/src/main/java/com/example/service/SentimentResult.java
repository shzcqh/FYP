package com.example.service;

public class SentimentResult {
    private int sentiment;
    private double positiveProb;
    private double negativeProb;

    public SentimentResult(int sentiment, double positiveProb, double negativeProb) {
        this.sentiment = sentiment;
        this.positiveProb = positiveProb;
        this.negativeProb = negativeProb;
    }
    public int getSentiment() { return sentiment; }
    public double getPositiveProb() { return positiveProb; }
    public double getNegativeProb() { return negativeProb; }
}
