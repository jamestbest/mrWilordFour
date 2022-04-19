package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.ui.elements.Button;
import com.mygdx.game.ui.elements.ImgOnlyButton;
import com.mygdx.game.ui.elements.InputButtonTwo;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.ButtonCollection;
import com.mygdx.game.ui.items.Score;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class EndScreen implements Screen {
    MyGdxGame myGdxGame;

    GameScreen gameScreen;
    Map map;
    ArrayList<Colonist> colonists;
    int score;

    ButtonCollection choiceButtons;
    ButtonCollection scoreButtons;

    CameraTwo camera;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    HashMap<String, Texture> textureHashMap;
    HashMap<String, TextureAtlas> textureAtlasHashMap;
    HashMap<String, TextureAtlas> colonistClothes;

    BitmapFont font;
    GlyphLayout glyphLayout;

    InputMultiplexer inputMultiplexer;

    boolean scoreHasBeenAdded;

    public EndScreen(MyGdxGame myGdxGame, GameScreen gameScreen){
        inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);
        this.myGdxGame = myGdxGame;
        this.gameScreen = gameScreen;
        this.map = gameScreen.map;
        this.colonists = gameScreen.colonists;
        this.score = GameScreen.score;
        setup();
    }

    public void setup(){
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout(font, "");

        setupChoiceButtons();
        setupScoreButtons();
        setupHashMaps();
        setupMiniMap();
    }

    @Override
    public void show() {
        camera = new CameraTwo();
        camera.allowMovement = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(79/255f, 109/255f, 158/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.projViewMatrix.getGdxMatrix());

        batch.begin();
        font.getData().setScale(4f);
        glyphLayout.setText(font, "Game Over");
        font.draw(batch, glyphLayout, MyGdxGame.initialRes.x / 2f - glyphLayout.width/2, MyGdxGame.initialRes.y);

        font.getData().setScale(1f);
        choiceButtons.drawButtons(batch);
        scoreButtons.drawButtons(batch);

        map.drawMiniMap(batch, textureHashMap, textureAtlasHashMap);

        drawColonists(batch);

        drawScoreInfo(batch);
        batch.end();

        if (Gdx.input.isButtonPressed(0)) {
            choiceButtons.updateButtons(camera, Gdx.input.isButtonJustPressed(0));
            scoreButtons.updateButtons(camera, Gdx.input.isButtonJustPressed(0));
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void drawScoreInfo(SpriteBatch batch){
        int x = 20;
        int startY = (int) (MyGdxGame.initialRes.y / 4 * 3);
        int height = (int) (MyGdxGame.initialRes.y / 8);
        glyphLayout.setText(font, "Score: " + score);
        font.draw(batch, glyphLayout, x, startY);

        glyphLayout.setText(font, "To add your score to the leaderboard, enter your name and press add.");
        font.draw(batch, glyphLayout, x, startY - glyphLayout.height * 2);
    }

    public void setupChoiceButtons(){
        choiceButtons = new ButtonCollection();

        TextButton continueButton = new TextButton("Continue on", "continueButton", this::continueOn);
        TextButton newGameButton = new TextButton("New Game", "newGameButton", () -> myGdxGame.setScreen(new MapGeneration(myGdxGame)));
        TextButton loadGame = new TextButton("Load Game", "loadGameButton", () -> myGdxGame.setScreen(new LoadSaveScreen2(myGdxGame)));
        TextButton mainMenuButton = new TextButton("Main Menu", "mainMenuButton", () -> myGdxGame.setScreen(new MainMenu(myGdxGame)));

        float width = MyGdxGame.initialRes.x/4f;
        choiceButtons.add(continueButton, newGameButton, loadGame, mainMenuButton);

        for (int i = 0; i < choiceButtons.buttons.size(); i++) {
            Button b = choiceButtons.buttons.get(i);
            b.setPos(width * (i), 0);
            b.setSize(width, MyGdxGame.initialRes.y/10f);
        }
    }

    public void setupScoreButtons(){
        scoreButtons = new ButtonCollection();

        InputButtonTwo nameButton = new InputButtonTwo("Name", "nameButton", inputMultiplexer);
        ImgOnlyButton addButton = new ImgOnlyButton("addButton", "add", () -> {
            if (!scoreHasBeenAdded) {
                scoreHasBeenAdded = true;
                Date d = new Date();
                myGdxGame.setScreen(new ScoreBoard(myGdxGame, new Score(nameButton.text, score, d.getTime())));
            }
            else {
                myGdxGame.setScreen(new ScoreBoard(myGdxGame));
            }
        });
        TextButton viewScoreBoardButton = new TextButton("View Scoreboard", "viewScoreBoardButton", () -> myGdxGame.setScreen(new ScoreBoard(myGdxGame)));

        int x = 20;
        int startY = (int) (MyGdxGame.initialRes.y / 4 * 3);
        int height = (int) (MyGdxGame.initialRes.y / 12f);
        nameButton.setPos(x, startY - height * 2.25f);
        nameButton.setSize(MyGdxGame.initialRes.x/7f, height);
        addButton.setPos(x + nameButton.width * 1.1f, startY - height * 2.25f + nameButton.height/4f);
        addButton.setSize((float)height /2f, height / 2f);
        viewScoreBoardButton.setPos(x, startY - height * 3.5f);
        viewScoreBoardButton.setSize(MyGdxGame.initialRes.x/10f, height);
        viewScoreBoardButton.autoSize();

        scoreButtons.add(nameButton, addButton, viewScoreBoardButton);
    }

    public void continueOn(){
        myGdxGame.setScreen(gameScreen);
    }

    public void setupHashMaps(){
        textureHashMap = new HashMap<>();
        textureAtlasHashMap = new HashMap<>();
        colonistClothes = new HashMap<>();
        GameScreen.getTAResources(colonistClothes, "TAResources");
        GameScreen.getAllMapTextures(textureHashMap, textureAtlasHashMap);
    }

    public void setupMiniMap(){
        int offset = 10;
        map.drawHeight = (int) (MyGdxGame.initialRes.y/2f);
        map.setY((int) (MyGdxGame.initialRes.y / 2) - offset);
        map.setX((int) (MyGdxGame.initialRes.x - map.getY()) - 2 * offset);
    }

    public void drawColonists(SpriteBatch batch){
        int dims = (int) (MyGdxGame.initialRes.y/20f);
        if (dims * colonists.size() > map.drawHeight){
            dims = map.drawHeight/colonists.size();
        }
        int x = map.getX() - 10;
        int y = map.getY() - dims;
        for (int i = 0; i < colonists.size(); i++) {
            Colonist c = colonists.get(i);
            c.drawMini(batch, x + dims * i, y, dims, colonistClothes);
        }
    }
}
