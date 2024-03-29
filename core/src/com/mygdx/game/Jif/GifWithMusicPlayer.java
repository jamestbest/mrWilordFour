package com.mygdx.game.Jif;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.File;

public class GifWithMusicPlayer {
    int pointer = 1;
    int numberOfImages = 0;
    String gDA;
    String musicExtension;
    String musicName;

    private int x = 0;
    private int y = 0;
    private int width = Gdx.graphics.getWidth();
    private int height = Gdx.graphics.getHeight();

    private int fps = 12;
    float deltaTime = 0;

    public boolean ended = false;

    Music music;

    SpriteBatch batch;

    Texture drawing;

    boolean hasMusic = true;
    public boolean loop = false;

    public GifWithMusicPlayer(String generalDirectoryAddition, String musicName, String musicExtension){
        this.gDA = generalDirectoryAddition;
        this.musicExtension = musicExtension;
        this.musicName = musicName;
        getNumberOfImages();
        getMusic();

        music.play();

        drawing = new Texture(Gdx.files.internal("core/assets/GifResources/" + gDA + "/" + gDA + "_" + "000" + ".jpg"));

        batch = new SpriteBatch();
    }

    public GifWithMusicPlayer(String generalDirectoryAddition){
        this.gDA = generalDirectoryAddition;
        hasMusic = false;
        getNumberOfImages();

        drawing = new Texture(Gdx.files.internal("core/assets/GifResources/" + gDA + "/" + gDA + "_" + "000" + ".jpg"));

        batch = new SpriteBatch();
    }

    public void getMusic(){
        music = Gdx.audio.newMusic(Gdx.files.internal("Music/" + musicName + "." + musicExtension));
    }

    public void getNumberOfImages(){
        File directory = new File("core/assets/GifResources/" + gDA);
        String[] temp = directory.list();
        assert temp != null;
        this.numberOfImages = temp.length;
    }

    public void render(){
        if (!ended) {
            deltaTime += Gdx.graphics.getDeltaTime();
            if (deltaTime > (1f / fps)) {
                deltaTime = 0;
                String tempStr =  String.format("%03d", pointer);

                drawing = new Texture(Gdx.files.internal("core/assets/GifResources/" + gDA + "/" + gDA + "_" + tempStr + ".jpg"));
                if (pointer < numberOfImages - 1) {
                    pointer++;
                } else {
                    if (!loop) {
                        ended = true;
                    } else {
                        pointer = 1;
                    }
                }
            }
            batch.begin();
            batch.draw(drawing, x, y, width, height);
            batch.end();
        }
        else {
            if (hasMusic) {
                music.stop();
            }
        }
    }

    public void setDims(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void setDims(float width, float height){
        this.width = (int) width;
        this.height = (int) height;
    }

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void setPosition(float x, float y){
        this.x = (int) x;
        this.y = (int) y;
    }

    public void setFPS(int fps){
        this.fps = fps;
    }

    public void dispose(){
        batch.dispose();
    }

}
