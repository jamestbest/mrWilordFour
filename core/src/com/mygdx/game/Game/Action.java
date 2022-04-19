package com.mygdx.game.Game;

import java.util.ArrayList;

public class Action {
    private int x;
    private int y;

    private int width;
    private int height;

    private String actionType;

    private ArrayList<Object> data;

    public Action(int x, int y, String actionType) {
        this.x = x;
        this.y = y;
        this.actionType = actionType;
    }

    public Action(int x, int y, int width, int height, String actionType) {
        this.x = x;
        this.y = y;
        this.actionType = actionType;
    }

    public Action(ArrayList<Object> data, String actionType) {
        this.data = data;
        this.actionType = actionType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
