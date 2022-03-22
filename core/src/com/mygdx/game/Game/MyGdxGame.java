package com.mygdx.game.Game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.Screens.MainMenu;
import com.mygdx.game.Screens.SettingsScreen;

import java.util.ArrayList;

public class MyGdxGame extends Game {
	//need to add a goal to the game, giving it an end game and goal
	//need to add enemies that will spawn semi-randomly they will attack the player
	//they can also destroy crops and buildings
	//their spawning and power are determined by how far out the player has expanded
	//
	public static Vector2 initialRes;

	public static String fontName = "Fortnite";
	public static String title = "mR. Wilord owen woz ere";

	public String songPlaying = "Moving on";
	public boolean mute = true;
	public boolean loop = false;
	public float volume = 50;

	public int fpsCap = 60;
	public boolean fpsCounter = true;

	public boolean vsyncEnabled = false;

	public Music music;

	public MainMenu mainMenu;
	public GameScreen currentGameScreen;

	public float clickWaitTimer = 0.2f;
	
	@Override
	public void create () {
		Gdx.graphics.setWindowedMode(1920, 1080); //must be 16:9 ratio
		initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		music = Gdx.audio.newMusic(Gdx.files.internal("Music/" + songPlaying + ".mp3"));
		music.setLooping(loop);
		if (!mute) music.setVolume(volume / 100f);
		else music.setVolume(0);
		music.play();

		mainMenu = new MainMenu(this);

		Gdx.graphics.setTitle(title);

		setScreen(this.mainMenu);
		// owen woz ere
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}

	public void changeSong(){
		System.out.println("changing song to" + songPlaying +  " with mute " + mute + " and volume " + volume);
		music.stop();
		music = Gdx.audio.newMusic(Gdx.files.internal("Music/" + songPlaying + ".mp3"));
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
