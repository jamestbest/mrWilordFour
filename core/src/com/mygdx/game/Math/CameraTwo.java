package com.mygdx.game.Math;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Screens.GameScreen;

public class CameraTwo {
    public Vector3 position;

    public com.mygdx.game.Math.Matrix4 projectionMatrix2, viewMatrix2, projViewMatrix;

    public int width, height;

    public float zoom = 1;

    public Vector2 minPoint;
    public Vector2 maxPoint;

    float maxZoom = 5.4f;
    float minZoom = 0.4f;

    public boolean allowMovement = true;

    float counter = 0.01f;
    float counterMax = 0.01f;

    public CameraTwo(int width, int height, Vector3 position) {
        this.position = position;

        this.width = width;
        this.height = height;
        initialise();
    }

    public CameraTwo(){
        this((int) MyGdxGame.initialRes.x, (int)MyGdxGame.initialRes.y, new Vector3(MyGdxGame.initialRes.x / 2f, MyGdxGame.initialRes.y / 2f, 0));
    }

    public void initialise(){
        this.projectionMatrix2 = new com.mygdx.game.Math.Matrix4();
        this.viewMatrix2 = new com.mygdx.game.Math.Matrix4();


        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix2.identityMatrix();
        projectionMatrix2.setToOrthographic(-width/2f, width/2f, -height/2f, height/2f, 0, 100);
    }

    public void updateZoom(){
        projectionMatrix2.setToOrthographic((-width * zoom)/2f, (width*zoom)/2f, (-height*zoom)/2f, (height*zoom)/2f, 0, 100);
    }

    public void update() {
        viewMatrix2.identityMatrix();
        viewMatrix2.lookAt(position, new Vector3(position).add(new Vector3(0,0,-1)), new Vector3(0,1,0));

        projViewMatrix = new com.mygdx.game.Math.Matrix4(projectionMatrix2);
        projViewMatrix.multiply(viewMatrix2);

        counter -= Gdx.graphics.getDeltaTime();
        if (allowMovement && counter <= 0) {
            counter = counterMax;
            updatePosition();
        }
    }

    public Vector2 unproject(Vector2 coords){
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 viewportDims = MyGdxGame.initialRes;

        coords.y = screenHeight - coords.y;
        coords.x *= (viewportDims.x/screenWidth);
        coords.y *= (viewportDims.y/screenHeight);

        coords.x -= (viewportDims.x/2);
        coords.y -= (viewportDims.y/2);

        coords.x *= zoom;
        coords.y *= zoom;

        coords.x += position.x;
        coords.y += position.y;

        return coords;
    }

    public static Vector2 testProj(Vector2 coords, float screenWidth, float screenHeight, float viewportWidth, float viewportHeight){
        coords.y = screenHeight - coords.y;
        coords.x *= (viewportWidth/screenWidth);
        coords.y *= (viewportHeight/screenHeight);
        return coords;
    }

    public void updatePosition(){
        float movement = GameScreen.TILE_DIMS / 2;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            movement *= 2;
        }
        movement *= zoom;
        movement /= 5;

        if (GameScreen.followingSelected){
            if (GameScreen.selectedColonist != null) {
                position.x = GameScreen.selectedColonist.getFullX();
                position.y = GameScreen.selectedColonist.getFullY();
            }
        }

        boolean hasMoved = false;

        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) && position.y < maxPoint.y) {
            position.y += movement;
            hasMoved = true;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) && position.y > minPoint.y) {
            position.y -= movement;
            hasMoved = true;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) && position.x > minPoint.x) {
            position.x -= movement;
            hasMoved = true;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && position.x < maxPoint.x) {
            position.x += movement;
            hasMoved = true;
        }

        if (hasMoved) {
            GameScreen.followingSelected = false;
        }
    }

    public void setMinMax(Vector2 min, Vector2 max){
        minPoint = min;
        maxPoint = max;
    }

    public void move(float x, float y){
        if (position.x + x < maxPoint.x && position.x + x > minPoint.x) {
            position.x += x;
        }
        if (position.y + y < maxPoint.y && position.y + y > minPoint.y) {
            position.y += y;
        }
    }

    public void moveTo(float x, float y){
        position.x = x;
        position.y = y;
    }

    public void handleZoom(float amountY){
        if (zoom > minZoom && amountY < 0){
            zoom += amountY / 6f;
        }
        else if (zoom < maxZoom && amountY > 0){
            zoom += amountY / 6f;
        }
        updateZoom();
    }
}
