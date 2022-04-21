package com.mygdx.game.Sound;

import com.badlogic.gdx.Gdx;

public class Sound {
    private int x;
    private int y;

    private float radius;
    private float volume;

    private boolean isPlaying;
    private int soundID;
    private String soundName;
    private float count;

    private com.badlogic.gdx.audio.Sound sound;

    long id;
    String name;

    public Sound(int x, int y, float radius, float volume, int soundID, String soundName) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.volume = volume;
        this.soundID = soundID;
        this.soundName = soundName;
    }

    public Sound(float radius, String soundName) {
        this.radius = radius;
        this.soundName = soundName;
    }

    public Sound(int x, int y, int id, Sound s) {
        this.soundID = id;
        this.soundName = s.soundName;
        this.x = x;
        this.y = y;
        this.radius = s.radius;
        this.volume = s.volume;
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

    public boolean update(float camX, float camY) {
        count += Gdx.graphics.getDeltaTime();
        if (!isPlaying) {
            sound = Gdx.audio.newSound(Gdx.files.internal("core/assets/Sounds/" + soundName + ".mp3"));
            sound.play(calculateVolume(calculateDistance(camX, camY)));
            isPlaying = true;
        }
        return !(count > 100);
    }

    public void stop(){
        sound.stop();
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

    public void dispose() {
        sound.dispose();
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

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getSoundID() {
        return soundID;
    }

    public void setSoundID(int soundID) {
        this.soundID = soundID;
    }

    public String getSoundName() {
        return soundName;
    }

    public String getName(){
        return name;
    }

    public void setSoundName(String soundName) {
        this.soundName = soundName;
    }
}
