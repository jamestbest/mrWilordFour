package com.mygdx.game.Game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Screens.MainMenu;

public class MyGdxGame extends Game {
	public static Vector2 initialRes;

	public static String fontName = "Arial";
	
	@Override
	public void create () {
		Gdx.graphics.setWindowedMode(1600, 900);
		initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		setScreen(new MainMenu(this));
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
}
