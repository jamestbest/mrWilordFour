package com.mygdx.game.Game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.DataStructures.Stack;
import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Screens.*;

import java.util.ArrayList;

public class MyGdxGame extends Game {
	public static Vector2 initialRes;

	public static String fontName = "Fortnite";
	public static String title = "mR. Wilord";

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

	public static Stack<Screen> screenStack = new Stack<>(5);
	
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
//		setScreen(new TutorialsScreen(this));
		screenStack.eraseOld = true;
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

	@Override
	public void setScreen(Screen s){
		if (this.screen != null) screenStack.enStack(this.screen);
		super.setScreen(s);
	}

	public void setScreenForEscape(Screen s){
		super.setScreen(s);
	}

	public void escapeScreen(){
		Screen s = screenStack.pop();
		if (s != null) setScreenForEscape(s);
	}

	public void clearStack(){
		screenStack.clear();
	}

	public void addToStack(Screen s){
		screenStack.enStack(s);
	}
}
