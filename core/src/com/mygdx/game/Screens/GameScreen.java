package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Generation.Map;

public class GameScreen implements Screen {
    public static final int TILES_ON_X = 250;
    public static final int TILES_ON_Y = 250;
    public static final float TILE_DIMS = 20;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    CameraTwo camera;

    Vector2 moveOrigin = new Vector2(0, 0);

    String seed = "testSeed1"; //testSeed1
    int addition;



    Map map;

    InputProcessor gameInputProcessor = new InputProcessor() {
        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            moveOrigin = camera.unproject(new Vector2(screenX, screenY));
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            Vector2 temp = camera.unproject(new Vector2(screenX, screenY));
            camera.move(moveOrigin.x - temp.x, moveOrigin.y - temp.y);
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            //this moves the camera making sure that the tile that the mouse is over is always under the mouse when scrolling
            //allow you to scroll into a position - see ref/cameraZoom.png
            Vector2 startPos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            camera.handleZoom(amountY);
            Vector2 endPos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            camera.move(startPos.x - endPos.x, startPos.y - endPos.y);
            return false;
        }
    };

    @Override
    public void show() {
        map = new Map(TILES_ON_X, TILES_ON_Y, TILE_DIMS);
        addition = map.getAdditionFromSeed(seed);
        map.generateMap(addition);
        map.generateRiver(addition);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new CameraTwo();
        camera.setMinMax(new Vector2(0,0), new Vector2(map.width * TILE_DIMS, map.height * TILE_DIMS));

        Gdx.input.setInputProcessor(gameInputProcessor);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        map.drawMap(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
