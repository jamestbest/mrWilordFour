package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Jif.GifWithMusicPlayer;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.ui.elements.BoxedTextButton;
import com.mygdx.game.ui.elements.Label;
import com.mygdx.game.ui.extensions.ButtonCollection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TutorialsScreen implements Screen {
    MyGdxGame game;

    ButtonCollection tutorialButtons;
    ButtonCollection labels;

    String[] tutorialNames = {"Priorities", "Orders", "Zones", "Building", "Multiplayer", "Saving and loading", "Resources", "Raids", "Health"};
    HashMap<String, String> tutorialTexts = new HashMap<>();

    GifWithMusicPlayer gwmp;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    CameraTwo camera;

    String selectedTutorial = "";
    String tutorialText = "";

    BitmapFont font;
    GlyphLayout glyphLayout;

    int startX = 0;
    int numShown = 6;

    InputAdapter inputAdapter = new InputAdapter(){
        @Override
        public boolean scrolled(float amountX, float amountY) {
            tutorialButtons.setAllToUnSelected();
            if (amountY > 0) {
                if (startX + numShown < tutorialNames.length) {
                    startX++;
                }
            }
            else if (amountY < 0) {
                if (startX > 0) {
                    startX--;
                }
            }
            updateTutorialButtons();
            return false;
        }
    };

    public TutorialsScreen(MyGdxGame game) {
        this.game = game;
        camera = new CameraTwo();
        camera.allowMovement = false;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        setup();
        setupTutorialButtons();
        setupLabels();

        setupTutorialTexts();

        Gdx.input.setInputProcessor(inputAdapter);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(49/255f, 53/255f, 61/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        boolean leftJustClicked = Gdx.input.isButtonJustPressed(0);

        batch.begin();
        tutorialButtons.drawButtons(batch);
        labels.drawButtons(batch);

        drawTutorialText(batch);

        batch.end();

        if(gwmp != null) {
            gwmp.render();
        }

        if (Gdx.input.isButtonPressed(0)){
            tutorialButtons.updateButtons(camera, leftJustClicked);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.escapeScreen();
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

    public void setup(){
        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout(font, "");
    }

    public void setupTutorialButtons() {
        tutorialButtons = new ButtonCollection();
        int maxShown = 6;
//        int numShown = Math.min(tutorialNames.length, maxShown);
        float x = 10;
        float y = MyGdxGame.initialRes.y * 0.65f;
        float height = MyGdxGame.initialRes.y * 0.7f / maxShown;
        float width = MyGdxGame.initialRes.x * 0.2f;
        float offset = MyGdxGame.initialRes.y / 100f;
        for (int i = startX; i < startX + numShown; i++) {
            int finalI = i;
            BoxedTextButton btb = new BoxedTextButton(tutorialNames[i] + "Button", tutorialNames[i], () -> updateTutorialShown(tutorialNames[finalI]));
            btb.setPos(x, y - height * i - offset * i);
            btb.setSize(width, height);
            tutorialButtons.add(btb);
        }
    }

    public void updateTutorialShown(String tutorialName) {
        selectedTutorial = tutorialName;
        System.out.println("Selected tutorial: " + tutorialName);

        tutorialText = tutorialTexts.get(tutorialName);
        System.out.println("Tutorial text: " + tutorialText);

        gwmp = new GifWithMusicPlayer(tutorialName);
        gwmp.loop = true;
        gwmp.setDims(MyGdxGame.initialRes.x * 0.535f, MyGdxGame.initialRes.y * 0.6f);
        gwmp.setPosition(MyGdxGame.initialRes.x * 0.45f, MyGdxGame.initialRes.y * 0.15f);
    }

    public void drawTutorialText(SpriteBatch batch){
        glyphLayout.setText(font, tutorialText, Color.WHITE, MyGdxGame.initialRes.x * 0.22f, Align.bottomLeft, true);
        font.draw(batch, glyphLayout, MyGdxGame.initialRes.x * 0.22f, MyGdxGame.initialRes.y * 0.75f);
    }

    public void updateTutorialButtons(){
        for (int i = startX; i < numShown + startX; i++) {
            int index = i - startX;
            int finalI = i;
            BoxedTextButton btb = (BoxedTextButton) tutorialButtons.buttons.get(index);
            btb.setR(() -> updateTutorialShown(tutorialNames[finalI]));
            btb.setText(tutorialNames[i]);
        }
    }

    public void setupLabels(){
    	labels = new ButtonCollection();

        Label title = new Label("Title", "Tutorials");
        Label tutorialName = new Label("TutorialName", "Tutorial Name");
        tutorialName.setUpdateRunnable(() -> tutorialName.setText(selectedTutorial));

        title.setFontScale(2.5f);
        title.setSize(MyGdxGame.initialRes.x * 0.25f, MyGdxGame.initialRes.y * 0.1f);
        title.setPos(MyGdxGame.initialRes.x / 2f - title.getWidth() / 2f, MyGdxGame.initialRes.y * 0.9f);

        float x = MyGdxGame.initialRes.x * 0.45f + (MyGdxGame.initialRes.x * 0.535f / 2f);
        tutorialName.setFontScale(1.75f);
        tutorialName.setSize(MyGdxGame.initialRes.x * 0.25f, MyGdxGame.initialRes.y * 0.1f);
        tutorialName.setPos(x - tutorialName.getWidth() / 2f, MyGdxGame.initialRes.y * 0.75f);

        labels.add(title, tutorialName);
    }

    public void setupTutorialTexts(){
        try {
            FileReader fileReader = new FileReader("core/assets/info/tutorialInfo/tutorialInfo");

            BufferedReader br = new BufferedReader(fileReader);

            String line = br.readLine();
            while (line != null) {
                ArrayList<String> split = new ArrayList<>(Arrays.asList(line.split("_")));
                line = br.readLine();

                tutorialTexts.put(split.get(0), split.get(1));
            }

            fileReader.close();
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
