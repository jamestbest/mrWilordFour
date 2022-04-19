package com.mygdx.game.ui.items;

import java.util.Date;

public class Score {
    public int score;
    private String name;
    private long date;

    public Score(String name, int score, long date) {
        this.name = name;
        this.score = score;
        this.date = date;
    }

    public Score(){

    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
