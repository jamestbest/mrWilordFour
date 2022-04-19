package com.mygdx.game.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Screens.GameScreen;
import io.socket.client.Socket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class EntityGroup {
    public ArrayList<Entity> entities;

    int x = 50;
    int y = 50;
    int radius = 10;

    String entityType;

    float chanceToMove;

    private static final Random random = new Random();

    private int id;

    public EntityGroup() {

    }

    public EntityGroup(String entityType, int x, int y, int radius, int id) {
        entities = new ArrayList<>();
        this.entityType = entityType;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.id = id;
    }

    public void add(Entity entity) {
        entities.add(entity);
    }

    public void add(Entity... entities) {
        this.entities.addAll(Arrays.asList(entities));
    }

    public void remove(Entity entity) {
        entities.remove(entity);
    }

    public void removeAll(Entity... entities){
        this.entities.removeAll(Arrays.asList(entities));
    }

    public void removeAll(ArrayList<Entity> entities) {
        this.entities.removeAll(entities);
    }

    public void moveGroup(Map map, ArrayList<Entity> allEntities, Socket socket, boolean isHost) {
        for (Entity entity : entities){
            entity.updateMovement(this, map, allEntities, socket, isHost);
        }
        chanceToMove += random.nextFloat() / 60;
        if (chanceToMove > 1){
            chanceToMove = 0;

        }
    }

    public void draw(SpriteBatch batch, HashMap<String, TextureAtlas> TAs, ShapeRenderer shapeRenderer) {
        for (Entity entity : entities) {
            entity.draw(batch, GameScreen.TILE_DIMS, TAs);
        }
        batch.end();

        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0.2f, 1f, 0.5f);
        for (Entity entity : entities) {
            entity.drawPathOutline(shapeRenderer);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);

        batch.begin();
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
