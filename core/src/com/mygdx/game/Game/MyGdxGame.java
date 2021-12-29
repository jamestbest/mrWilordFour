package com.mygdx.game.Game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;

public class MyGdxGame extends Game {
	public static Vector2 initialRes;
	
	@Override
	public void create () {
		Gdx.graphics.setWindowedMode(1600, 900);
		initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		setScreen(new GameScreen());

	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
}
