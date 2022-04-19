package com.mygdx.game.Lighting;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Things.Thing;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;
import java.util.Arrays;

public class LightManager {
    public ArrayList<Light> lights;
    public boolean once = true;

    public LightManager() {
        lights = new ArrayList<>();
    }

    public void addLight(Light light) {
        lights.add(light);
    }

    public void removeLight(Light light) {
        lights.remove(light);
    }

    public void removeLight(int x, int y){
        int x1 = (int) (x * GameScreen.TILE_DIMS + GameScreen.TILE_DIMS / 2);
        int y1 = (int) (y * GameScreen.TILE_DIMS + GameScreen.TILE_DIMS / 2);
        lights.removeIf(light -> light.getX() == x1 && light.getY() == y1);
    }

    public void addLight(Light... lights) {
        this.lights.addAll(Arrays.asList(lights));
    }

    public void updateLights(EdgeController ec, float tileDims) {
        for (Light light : lights) {
            light.update(ec, tileDims);
        }
    }

    public void drawLights(SpriteBatch batch) {
        for (Light light : lights) {
            light.draw(batch);
        }
    }

    public void setAllToShouldUpdate() {
        for (Light light : lights) {
            light.shouldUpdate = (true);
        }
    }
}
