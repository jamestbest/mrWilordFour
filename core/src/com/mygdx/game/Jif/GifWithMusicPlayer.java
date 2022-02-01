package com.mygdx.game.Jif;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.File;

public class GifWithMusicPlayer {
    int pointer = 0;
    int numberOfImages = 0;
    String gDA;
    String musicExtension;
    String musicName;

    int x = 0;
    int y = 0;
    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();

    int fps = 12;
    float deltaTime = 0;

    public boolean ended = false;

    Music music;

    SpriteBatch batch;

    Texture drawing;

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

    public void getMusic(){
        music = Gdx.audio.newMusic(Gdx.files.internal("Music/" + musicName + "." + musicExtension));
    }

    public void getNumberOfImages(){
        File directory = new File("core/assets/GifResources/" + gDA);
        String[] temp = directory.list();
        assert temp != null;
        this.numberOfImages = temp.length;
    }

    public void start(){

    }

    public void render(){
        if (!ended) {
            deltaTime += Gdx.graphics.getDeltaTime();
            if (deltaTime > (1f / fps)) {
                deltaTime = 0;
                String tempStr = "";
                if (pointer < 10) {
                    tempStr += "00";
                    tempStr += pointer;
                } else if (pointer < 100) {
                    tempStr += "0";
                    tempStr += pointer;
                } else {
                    tempStr += pointer;
                }

                drawing = new Texture(Gdx.files.internal("core/assets/GifResources/" + gDA + "/" + gDA + "_" + tempStr + ".jpg"));
                if (pointer < numberOfImages - 1) {
                    pointer++;
                } else {
                    ended = true;
                }
            }
            batch.begin();
            batch.draw(drawing, x, y, width, height);
            batch.end();
        }
        else {
            music.stop();
        }
    }

    public void setDims(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void setFPS(int fps){
        this.fps = fps;
    }

    public void dispose(){
        batch.dispose();
    }

}
