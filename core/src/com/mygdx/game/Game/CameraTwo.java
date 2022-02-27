package com.mygdx.game.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Screens.GameScreen;

public class CameraTwo {

    public Matrix4 projectionMatrix, viewMatrix, projViewMatrix;
    public Vector3 position;

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
        this((int)MyGdxGame.initialRes.x, (int)MyGdxGame.initialRes.y, new Vector3(MyGdxGame.initialRes.x / 2f, MyGdxGame.initialRes.y / 2f, 0));
    }

    public void initialise(){
        this.projectionMatrix = new Matrix4();
        this.viewMatrix = new Matrix4();
        this.projViewMatrix = new Matrix4();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.idt();
        projectionMatrix.setToOrtho(-width/2f, width/2f, -height/2f, height/2f, 0, 100);
    }

    public void updateZoom(){
        projectionMatrix.setToOrtho((-width * zoom)/2f, (width*zoom)/2f, (-height*zoom)/2f, (height*zoom)/2f, 0, 100);
    }

    public void update() {
        viewMatrix.idt();
        viewMatrix.setToLookAt(position, new Vector3(position).add(new Vector3(0,0,-1)),  new Vector3(0.0f, 1.0f, 0.0f));
        projViewMatrix.set(projectionMatrix).mul(viewMatrix);
        counter -= Gdx.graphics.getDeltaTime();
        if (allowMovement && counter <= 0) {
            counter = counterMax;
            updatePosition();
        }
    }

    public void translate(Vector3 vector3){
        this.position.add(vector3);
    }

    public void translate(float a, float b, float c){
        this.position.add(new Vector3(a,b,c));
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

    public Vector2 project(Vector3 coords){
        coords.prj(projViewMatrix);
        coords.x = (coords.x + 1) * width;
        coords.y = (coords.y + 1) * height;
        coords.x /= 2f;
        coords.y /= 2f;

        return new Vector2(coords.x, coords.y);
    }

    public static Vector2 testProj(Vector2 coords, float screenWidth, float screenHeight, float viewportWidth, float viewportHeight){
        coords.y = screenHeight - coords.y;
        coords.x *= (viewportWidth/screenWidth);
        coords.y *= (viewportHeight/screenHeight);
        return coords;
    }

    public void updatePosition(){
        float temp = GameScreen.TILE_DIMS / 2;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            temp = GameScreen.TILE_DIMS;
        }
        temp *= zoom;
        temp /= 5;

        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) && position.y < maxPoint.y) {
            position.y += temp;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) && position.y > minPoint.y) {
            position.y -= temp;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) && position.x > minPoint.x) {
            position.x -= temp;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && position.x < maxPoint.x) {
            position.x += temp;
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
