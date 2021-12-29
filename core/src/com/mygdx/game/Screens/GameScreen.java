package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Generation.Noise2D;
import com.mygdx.game.Math.AStar.AStar;
import com.mygdx.game.Math.CStar.CStar;
import com.mygdx.game.Math.DStar.DStar;
import com.mygdx.game.Terrain.Tile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class GameScreen implements Screen {
    ArrayList<ArrayList<Tile>> map;
    public static final int TILES_ON_X = 250;
    public static final int TILES_ON_Y = 250;
    public static final float TILE_DIMS = 20;

    HashMap<String, Texture> textures = new HashMap<>();

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    CameraTwo camera;

    Vector2 moveOrigin = new Vector2(0, 0);

    String seed = "testSeed12"; //testSeed1
    int addition;

    ArrayList<Vector2> path;

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
        initialiseTextures();

        generateMap();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new CameraTwo();
        camera.setMinMax(new Vector2(0,0), new Vector2(map.size() * TILE_DIMS, map.get(0).size() * TILE_DIMS));

        Gdx.input.setInputProcessor(gameInputProcessor);


        DStar dStar = new DStar();
        path = dStar.pathFind(new Vector2(0,0), new Vector2(154, 249), map);

//        AStar aStar = new AStar();
//        aStar.setupWithPerlin(addition, addition, new Vector2(154, 249), new Vector2(0,0));
//        path = aStar.pathFind(true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        drawMap(batch);
        batch.end();


        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setProjectionMatrix(camera.projViewMatrix);
        shapeRenderer.setColor(Color.BLUE);
        for (Vector2 v: path) {
            shapeRenderer.rect(v.x * TILE_DIMS, v.y * TILE_DIMS, TILE_DIMS, TILE_DIMS);
        }
        shapeRenderer.end();

//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.setProjectionMatrix(camera.projViewMatrix);
//        ArrayList<ArrayList<Float>> infoOnSearch = AStar.infoOnSearch;
//        for (int i = 0; i < infoOnSearch.size(); i++) {
//            for (int j = 0; j < infoOnSearch.get(i).size(); j++) {
//                shapeRenderer.setColor(infoOnSearch.get(i).get(j) * 10, 0, 0, 1);
//                shapeRenderer.rect(i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
//            }
//        }
//        shapeRenderer.end();

//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.setProjectionMatrix(camera.projViewMatrix);
//        shapeRenderer.setColor(Color.PURPLE);
//        for (int i = 0; i < 250; i++) {
//            for (int j = 0; j < 250; j++) {
//                if (DStar.nodes.get(i).get(j).visited){
//                    shapeRenderer.rect(i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
//                }
//            }
//        }
//        shapeRenderer.end();
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

    public void generateMap(){
        map = new ArrayList<>();
        for (char c: seed.toCharArray()
             ) {
            addition += c * 1000;
        }
        System.out.println(addition);
        for(int i = 0; i < TILES_ON_X; i++){
            map.add(new ArrayList<Tile>());
            for(int j = 0; j < TILES_ON_Y; j++){
                float temp = (float) Noise2D.noise((((float) i / TILES_ON_X) * 3) + addition, (((float) j / TILES_ON_Y) * 3) + addition, 255);
                if(temp > 0.6f){
                    map.get(i).add(new Tile(i, j, "stone"));
                }
                else{
                    map.get(i).add(new Tile(i, j, "grass"));
                }
            }
        }
    }

    public void drawMap(SpriteBatch batch){
        for(int i = 0; i < map.size(); i++){
            for(int j = 0; j < map.get(i).size(); j++){
                switch (map.get(i).get(j).type){
                    case "grass":
                        batch.draw(textures.get("grass"), i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                        break;
                    case "stone":
                        batch.draw(textures.get("stone"), i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void initialiseTextures(){
        //loads every texture in the textures map
        File directory= new File("core/assets/Textures");
        String[] files = directory.list();
        assert files != null;
        for ( String fileName : files) {
            String[] temp = fileName.split("\\.");
            textures.put(temp[0], new Texture(Gdx.files.internal("core/assets/Textures/" + fileName)));
        }
    }
}
