package com.mygdx.game.ui.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Screens.GameScreen;

import java.io.File;

public class Clock {
    private int x;
    private int y;

    private int radius;

    private int day;

    private float hour = 17;
    private float minute = 32;
    private float second;

    private float angleHour;
    private float angleMinute;

    Texture face;
    TextureRegion hourHand;
    TextureRegion minuteHand;

    String gDA;

    float drawHeight = 25f;
    float centreCircleRadius = 6;

    public boolean newDay;

    public Clock(int x, int y, int radius, String gDA) {
        this.x = x;
        this.y = y;
        this.radius = radius;

        this.gDA = gDA;
        setupTexturesFromGda();
        updateTime(0);
    }

    public void updateTime(float delta){
        second += delta * 100 * GameScreen.gameSpeed;
        if(second >= 60){
            second = 0;
            minute += 1;
        }
//        System.out.println(minute);
        if(minute >= 60){
            minute = 0;
            hour += 1;
        }
        if(hour >= 24){
            hour = 0;
        }
        angleMinute = - (minute * 6 + second * 6 / 60);
        angleHour = - ((hour % 12) * 30 + minute * 30f / 60f);

        if (hour == 0 && minute == 0 && second == 0) {
            day++;
            newDay = true;
        }
    }

    public void draw(float delta, SpriteBatch batch, ShapeRenderer shapeRenderer){
        updateTime(delta);
        batch.draw(face, x - radius, y - radius, radius * 2, radius * 2);

        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.circle(x, y, centreCircleRadius);
        shapeRenderer.end();
        batch.begin();

        batch.draw(hourHand, x + centreCircleRadius / 2f, y - drawHeight / 2f,
                        -centreCircleRadius / 2f,drawHeight / 2f,
                            radius, 25, 1, 1, angleHour + (90));

        batch.draw(minuteHand, x + centreCircleRadius / 2f, y - drawHeight / 2f,
                            -centreCircleRadius / 2f,drawHeight / 2f,
                            radius, 25, 1, 1, angleMinute + 90);
    }

    public void setTime(int hour, int minute, int second){
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public void setupTexturesFromGda(){
        File dir = new File("core/assets/Textures/Clocks/" + gDA);
        String[] files = dir.list();
        assert files != null;
        for(String f : files){
            String name = f.split("\\.")[0];
            if(name.equals("face")){
                face = new Texture("core/assets/Textures/Clocks/" + gDA + "/" + f);
            }
            if(name.equals("hour")){
                Texture tmp = new Texture("core/assets/Textures/Clocks/" + gDA + "/" + f);
                hourHand = new TextureRegion(tmp);
            }
            if(name.equals("minute")){
                Texture tmp = new Texture("core/assets/Textures/Clocks/" + gDA + "/" + f);
                minuteHand = new TextureRegion(tmp);
            }
        }
    }

    public boolean isPressed(int x, int y) {
        float dy = y - this.y;
        float dx = x - this.x;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < radius;
    }

    public String getTime(){
        return hour + ":" + minute + ":" + second;
    }

    public void setTime(String time){
        String[] parts = time.split(":");
        hour = Float.parseFloat(parts[0]);
        minute = Float.parseFloat(parts[1]);
        second = Float.parseFloat(parts[2]);
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public float getHourAngle(){
        return angleHour;
    }

    public float getHour() {
        return hour;
    }

    public void setHour(float hour) {
        this.hour = hour;
    }

    public float getMinute() {
        return minute;
    }

    public void setMinute(float minute) {
        this.minute = minute;
    }

    public float getSecond() {
        return second;
    }

    public void setSecond(float second) {
        this.second = second;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
