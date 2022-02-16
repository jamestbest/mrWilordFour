package com.mygdx.game.Game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Screens.MainMenu;

import java.util.ArrayList;

public class MyGdxGame extends Game {
	public static Vector2 initialRes;

	public static String fontName = "Arial";
	public static String title = "mR. Wilord";

	public String songPlaying = "Moving on.mp3";
	public boolean mute = true;
	public boolean loop = false;
	public float volume = 50;

	public int fpsCap = 60;
	public boolean fpsCounter = true;

	public Music music;
	
	@Override
	public void create () {
		Gdx.graphics.setWindowedMode(1920, 1080); //must be 16:9 ratio
		initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		music = Gdx.audio.newMusic(Gdx.files.internal("Music/" + songPlaying));
		music.setLooping(loop);
		music.setVolume(volume / 100f);
		music.play();

		setScreen(new MainMenu(this));
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}

	public void changeSong(){
		music.stop();
		music = Gdx.audio.newMusic(Gdx.files.internal("Music/" + songPlaying));
		music.setLooping(loop);
		if (!mute) music.setVolume(volume / 100f);
		else music.setVolume(0);
		music.play();
	}

	public void updateMusicInfo(){
		music.setLooping(loop);
		if (!mute) music.setVolume(volume / 100f);
		else music.setVolume(0);
	}
}
