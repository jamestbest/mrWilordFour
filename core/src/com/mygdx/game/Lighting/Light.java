package com.mygdx.game.Lighting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class Light {
    private String textureName;

    FrameBuffer lightBuffer;
    Texture lightTexture;
    FrameBuffer combinedBuffer;

    Texture combinedTexture;

    private int x;
    private int y;

    ShapeRenderer shapeRenderer;
    SpriteBatch batch;

    boolean shouldUpdate = true;

    float alphaOffset = 0.05f;
    float currentAlpha = 1f;

    private static final Random random = new Random();

    ArrayList<ArrayList<Float>> arrayOfVisiblePolygonPoints = new ArrayList<>();

    public Light(int x, int y, int dims, String textureName){
        lightBuffer = new FrameBuffer(Pixmap.Format.Alpha, dims, dims, false);
        lightTexture = new Texture("Textures/lightSources/" + textureName + ".png");
        combinedBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, dims, dims, false);
        this.x = x;
        this.y = y;

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
    }

    public Light(int x, int y, int dims){
        this(x, y, dims, "default");
    }

    public void update(EdgeController ec, float cellDims){
        if (shouldUpdate) {
            shouldUpdate = false;
            calculateInLightPoly(x, y, 1000, ec.edges, cellDims);
            Texture lBT = drawToLightBuffer(x, y);
            combinedTexture = getCombinedTexture(lBT);
        }
    }

    public void calculateForPoint(float x, float y, float radius, float dy, float dx, ArrayList<Edge> edges, float cellDims){
        //calculate the angle given the dy and dx using atan2() which returns the theta value of the equivalent polar coord
        float baseAngle = (float) Math.atan2(dy, dx);
        float adjustedAngle;

        for (int j = 0; j < 3; j++) {
            adjustedAngle = -0.001f + (j * 0.001f) + baseAngle;

            //for each of the rays that have been launched from the x and y to the edges x and y +- 0.0001f
            //use the radius of the ray to find the dy and dx by SOHCAHTOA

            float rayDx = (float) (Math.cos(adjustedAngle) * radius);
            float rayDy = (float) (Math.sin(adjustedAngle) * radius);

            float min_t1 = Float.POSITIVE_INFINITY;
            float minx, miny, minAngle;
            minx = miny = minAngle = 0;
            boolean doesIntersect = false;

            for (Edge e2 : edges) {
                float edx = ((e2.startX - e2.endX) * cellDims);
                float edy = ((e2.startY - e2.endY) * cellDims);

                boolean isParallel = ((dy/dx) / (edy/edx) == 1);
                if (!isParallel){
                    float t2 = (rayDx * ((e2.endY * cellDims) - y) + (rayDy * (x - (e2.endX * cellDims)))) / ((edx * rayDy) - (edy * rayDx));
                    float t1 = -1;
                    if (Math.abs(rayDx) >= 0.001) {
                        t1 = ((e2.endX * cellDims) + (edx * t2) - x) / rayDx;
                    }

                    if (t1 > 0f && t2 >= 0f && t2 <= 1f)
                    {
                        if (t1 < min_t1)
                        {
                            min_t1 = t1;
                            minx = x + (rayDx * t1);
                            miny = y + (rayDy * t1);
                            minAngle = (float) Math.atan2(miny - y, minx - x);
                            doesIntersect = true;
                        }
                    }
                }
            }
            if (doesIntersect){
                arrayOfVisiblePolygonPoints.add(new ArrayList<>(Arrays.asList(minAngle, minx, miny)));
            }
        }
        arrayOfVisiblePolygonPoints.sort(Comparator.comparing(list -> list.get(0)));
    }

    public void calculateInLightPoly(float x, float y, float radius, ArrayList<Edge> edges, float cellDims){
        arrayOfVisiblePolygonPoints.clear();
        for (Edge e : edges) {
            calculateForPoint(x, y, radius, (e.endY * cellDims - y), (e.endX * cellDims - x), edges, cellDims);
            calculateForPoint(x, y, radius, (e.startY * cellDims - y), (e.startX * cellDims - x), edges, cellDims);
        }
    }

    public void draw(SpriteBatch batch){
        int shouldOffset = random.nextInt(10);
        if (shouldOffset < 5){
            if (shouldOffset % 2 == 0){
                currentAlpha += alphaOffset;
            }
            else{
                currentAlpha -= alphaOffset;
            }
            currentAlpha = Math.min(1f, currentAlpha);
            currentAlpha = Math.max(0.8f, currentAlpha);
        }
        batch.enableBlending();
        batch.begin();
        Color c = batch.getColor();
        batch.setColor(c.r, c.g, c.b, currentAlpha);
        batch.draw(combinedTexture, 0,0, combinedTexture.getWidth(), combinedTexture.getHeight(),
                                0,0, combinedTexture.getWidth(), combinedTexture.getHeight(),
                                false, true);
        batch.setColor(c.r, c.g, c.b, 1);
        batch.end();
    }

    public Texture drawToLightBuffer(int x, int y){
        lightBuffer.begin();
        Matrix4 p = new Matrix4();
        p.setToOrtho2D(0, 0, lightBuffer.getWidth(), lightBuffer.getHeight());
        shapeRenderer.setProjectionMatrix(p);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1,1,1,1);
        for (int i = 0; i < arrayOfVisiblePolygonPoints.size() - 1; i++) {
            ArrayList<Float> point1 = arrayOfVisiblePolygonPoints.get(i);
            ArrayList<Float> point2 = arrayOfVisiblePolygonPoints.get(i + 1);
            shapeRenderer.triangle(point1.get(1), point1.get(2), point2.get(1), point2.get(2), x, y);
        }
        if (arrayOfVisiblePolygonPoints.size() > 0) {
            ArrayList<Float> point1 = arrayOfVisiblePolygonPoints.get(0);
            ArrayList<Float> point2 = arrayOfVisiblePolygonPoints.get(arrayOfVisiblePolygonPoints.size() - 1);
            shapeRenderer.triangle(point1.get(1), point1.get(2), point2.get(1), point2.get(2), x, y);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
        lightBuffer.end();
        return lightBuffer.getColorBufferTexture();
    }

    public Texture getCombinedTexture(Texture lBT){
        combinedBuffer.begin();
        Matrix4 p = new Matrix4();
        p.setToOrtho2D(0, 0, combinedBuffer.getWidth(), combinedBuffer.getHeight());
        batch.setProjectionMatrix(p);

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        Color c = batch.getColor();
        batch.setColor(c.r, c.g, c.b, 0.35f);
        batch.draw(lightTexture, x - lightTexture.getWidth() / 2f, y - lightTexture.getHeight() / 2f);
        batch.end();
        batch.setColor(c.r, c.g, c.b, 1f);

        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();

        batch.enableBlending();
        batch.begin();
        batch.setBlendFunction(GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA);
        batch.draw(lBT, 0, 0, lBT.getWidth(), lBT.getHeight(), 0,0, lBT.getWidth(), lBT.getHeight(), false, true);
        batch.end();
        batch.setBlendFunction(src, dst);
        batch.disableBlending();

        combinedBuffer.end();

        return combinedBuffer.getColorBufferTexture();
    }

    public void setPosition(float x, float y){
        this.x = (int) x;
        this.y = (int) y;
    }

    public String getTextureName() {
        return textureName;
    }

    public void setTextureName(String textureName) {
        this.textureName = textureName;
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
}