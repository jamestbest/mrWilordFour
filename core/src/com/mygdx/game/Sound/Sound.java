package com.mygdx.game.Sound;

import com.badlogic.gdx.Gdx;

public class Sound {
    private int x;
    private int y;

    private float radius;

    private float count;

    long id;
    String name;

    public Sound(int x, int y, float radius, String soundName) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.name = soundName;
    }

    public Sound(float radius, String soundName) {
        this.radius = radius;
        this.name = soundName;
    }

    public Sound(int x, int y, Sound s) {
        this.name = s.name;
        this.x = x;
        this.y = y;
        this.radius = s.radius;
    }

    public Sound(long id, String name, int x, int y, float radius){
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Sound(){

    }

    public boolean shouldEnd () {
        count += Gdx.graphics.getDeltaTime();
        return (count > 10);
    }

    public float calculateDistance(float camX, float camY) {
        return (float) Math.sqrt(Math.pow(camX - x, 2) + Math.pow(camY - y, 2));
    }

    public float calculateVolume(float distance) {
        float volume = radius - distance;
        if (volume > 100) {
            volume = 100;
        }
        if (volume < 0) {
            volume = 0;
        }
        return volume / 100f;
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getName(){
        return name;
    }
}
