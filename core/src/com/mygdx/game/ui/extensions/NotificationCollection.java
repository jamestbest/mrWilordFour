package com.mygdx.game.ui.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Entity.Barbarian;
import com.mygdx.game.Entity.Entity;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Things.Fire;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.ui.elements.Notification;
import io.socket.client.Socket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.mygdx.game.Screens.GameScreen.json;

public class NotificationCollection {
    public ArrayList<Notification> notifications;

    String displayType;
    public boolean displaying;

    int nextId;

    float x;
    float y;
    float dims;

    int count;

    GlyphLayout glyphLayout;
    BitmapFont font;

    HashMap<String, Texture> notificationTextures;

    public NotificationCollection(float x, float y, float dims) {
        this.notifications = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.dims = dims;

        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout();

        setupNotificationTextures();
    }

    public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, Map map, ArrayList<Entity> entities){
        for (int i = 0; i < 3; i++) {
            for (Notification n : notifications) {
                n.draw(batch, i, glyphLayout, font, notificationTextures.get(n.getType()));
            }
        }
        batch.end();
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (displaying){
            display(shapeRenderer, map, entities);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
        batch.begin();

        makeSureNotificationsShouldStay(map, entities);
    }

    public boolean updateNotis(boolean leftClick, boolean rightClick, int x, int y, CameraTwo cameraTwo, Map map, ArrayList<Entity> entities){
        boolean updated = false;
        for (Notification n : notifications) {
            if (n.update(leftClick, rightClick, x, y)){
                displayType = n.getType();
                displaying = true;
                moveToType(displayType, map, entities, cameraTwo);
                updated = true;
            }
        }
        ArrayList<Notification> toRemove = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.toBeRemoved){
                toRemove.add(n);
                displaying = false;
            }
        }
        notifications.removeAll(toRemove);

        if (updated){
            repositionAll();
        }
        return updated;
    }

    public void addAll(Socket socket, boolean isHost, Notification... n){
        for (Notification noti : n) {
            add(socket, isHost, noti);
        }
    }

    public void add(Socket socket, boolean isHost, Notification n){
        if (isHost && socket != null){
            socket.emit("addNoti", json.toJson(n));
        }

        Notification toRemove = null;
        for (Notification noti : notifications) {
            if (noti.getType().equals(n.getType())){
                toRemove = noti;
            }
        }
        int numberOfNotis = notifications.size();
        n.setSize(dims, dims);
        n.setPos(x, y - (numberOfNotis * dims));
        notifications.add(n);

        if (toRemove != null){
            notifications.remove(toRemove);
            repositionAll();
        }
        GameScreen.soundManager.addSound("ping", 0,0, socket, isHost);
    }

    public void remove(Notification n, Socket socket, boolean isHost){
        if (isHost && socket != null){
            socket.emit("removeNoti", json.toJson(n));
        }
        notifications.remove(n);
    }

    public void display(ShapeRenderer shapeRenderer, Map map, ArrayList<Entity> entities){
        switch (displayType) {
            case "fire" -> {
                shapeRenderer.setColor(1, 0, 0, 0.45f);
                for (Fire f : map.fire) {
                    shapeRenderer.rect(f.getX() * GameScreen.TILE_DIMS, f.getY() * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
                }
            }
            case "raid" -> {
                shapeRenderer.setColor(0, 0, 0.45f, 0.45f);
                for (Entity e : entities) {
                    if (e instanceof Barbarian) {
                        shapeRenderer.rect(e.getFullX(), e.getFullY(), GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
                    }
                }
            }
        }
    }

    public void moveToType(String displayType, Map map, ArrayList<Entity> entities, CameraTwo cameraTwo){
        switch (displayType) {
            case "fire" -> {
                if (map.fire.size() > 0) {
                    if (count >= map.fire.size()) {
                        count = 0;
                    }
                    Fire f = map.fire.get(count);
                    count++;
                    cameraTwo.moveTo(f.getX() * GameScreen.TILE_DIMS, f.getY() * GameScreen.TILE_DIMS);
                }
            }
            case "raid" -> {
                ArrayList<Barbarian> raid = new ArrayList<>();
                for (Entity e : entities) {
                    if (e instanceof Barbarian && e.isAlive()) {
                        raid.add((Barbarian) e);
                    }
                }
                if (raid.size() > 0) {
                    if (count >= raid.size()) {
                        count = 0;
                    }
                    Barbarian b = raid.get(count);
                    count++;
                    cameraTwo.moveTo(b.getFullX(), b.getFullY());
                }
            }
        }
    }

    public void repositionAll(){
        for (int i = 0; i < notifications.size(); i++) {
            Notification n = notifications.get(i);
            n.setPos(x, y - (i * dims));
        }
    }

    public int getNextId(){
        return nextId++;
    }

    public void makeSureNotificationsShouldStay(Map map, ArrayList<Entity> entities){
        ArrayList<Notification> toRemove = new ArrayList<>();
        for (Notification n : notifications) {
            String type = n.getType();
            switch (type) {
                case "fire" -> {
                    if (map.fire.size() == 0) {
                        toRemove.add(n);
                    }
                }
                case "raid" -> {
                    ArrayList<Barbarian> raid = new ArrayList<>();
                    for (Entity e : entities) {
                        if (e instanceof Barbarian && e.isAlive()) {
                            raid.add((Barbarian) e);
                        }
                    }
                    if (raid.size() == 0) {
                        toRemove.add(n);
                    }
                }
            }
        }
        if (toRemove.size() > 0){
            notifications.removeAll(toRemove);
            repositionAll();
        }
    }

    public void setupNotificationTextures(){
        notificationTextures = new HashMap<>();
        String[] textureNames = {"fire", "raid"};
        for (String s : textureNames) {
            Texture t = new Texture("Textures/ui/notifications/" + s + ".png");
            notificationTextures.put(s, t);
        }
    }
}
