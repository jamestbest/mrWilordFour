package com.mygdx.game.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.Colonist;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.MapSettings;
import com.mygdx.game.ui.elements.BoxedTextButton;
import com.mygdx.game.ui.elements.Button;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.ButtonCollection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class LoadSaveScreen2 implements Screen {
    MyGdxGame game;

    ArrayList<String> saveNames;

    ButtonCollection buttonCollectionForSaves;
    ButtonCollection buttonCollectionForUI;
    CameraTwo camera;

    SpriteBatch batch;

    int startIndex = 0;
    int numberShown = 8;

    int selectedIndex = 0;

    int offset = 5;

    boolean hasLoadedAMap;

    ArrayList<Colonist> colonists = new ArrayList<>();
    Map map;
    HashMap<String, Texture> textures;
    HashMap<String, TextureAtlas> thingTextures;

    InputProcessor inputProcessor = new InputAdapter(){
        @Override
        public boolean scrolled(float amountX, float amountY) {
            System.out.println(amountY);
            System.out.println(saveNames);
            if(amountY > 0){
                System.out.println("attempting to increase start index");
                if (startIndex + numberShown < saveNames.size()){
                    System.out.println("increasing start index");
                    startIndex++;
                    updateSaveButtons();
//                    updateSelected(true);
                }
            }
            else if(amountY < 0){
                System.out.println("attempting to decrease start index");
                if (startIndex > 0){
                    System.out.println("decreasing start index");
                    startIndex--;
                    updateSaveButtons();
//                    updateSelected(false);
                }
            }
            return false;
        }
    };

    public LoadSaveScreen2(MyGdxGame game){
        this.game = game;
        setup();
    }

    public void setup(){
        batch = new SpriteBatch();
        camera = new CameraTwo();
        buttonCollectionForSaves = new ButtonCollection();
        buttonCollectionForUI = new ButtonCollection();
        setupSaveNames();
        setupAllSaveButtons();
        setupUI();
        setupMap();

        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        buttonCollectionForSaves.drawButtons(batch);
        buttonCollectionForUI.drawButtons(batch);

        map.drawMiniMap(batch, textures, thingTextures);
        batch.end();

        if (Gdx.input.isButtonPressed(0)) {
            buttonCollectionForSaves.updateButtons(camera, Gdx.input.isButtonJustPressed(0));
            buttonCollectionForUI.updateButtons(camera, Gdx.input.isButtonJustPressed(0));

            if (Gdx.input.isButtonJustPressed(0)){
                if (buttonCollectionForUI.pressedButtonName.equals("loadButton")){
                    hasLoadedAMap = loadMap();
                }
                else if (buttonCollectionForUI.pressedButtonName.equals("continueButton")){
                    if (hasLoadedAMap){
                        game.setScreen(new GameScreen(game, colonists, map));
                    }
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(game.mainMenu);
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

    public void setupAllSaveButtons(){
        int x = (int) (MyGdxGame.initialRes.x / 10);
        int y = (int) (MyGdxGame.initialRes.y / 10);

        int buttonWidth = (int) (MyGdxGame.initialRes.x / 5);
        int buttonHeight = (int) (MyGdxGame.initialRes.y / 10);

        for (int i = startIndex; i < startIndex + numberShown; i++) {
            String text = "";
            if (i < saveNames.size()) {
                text = saveNames.get(i);
            }
            int height = (int) (MyGdxGame.initialRes.y / 10 * 8) + (offset * (numberShown / 2));
            BoxedTextButton b = new BoxedTextButton(x, height - (buttonHeight * i) - (offset * i),
                    buttonWidth, buttonHeight, i + "", text);
            buttonCollectionForSaves.add(b);

        }
        //No one can grow if he does not accept his smallness
    }

    public void updateSaveButtons(){
        for (int i = startIndex; i < startIndex + numberShown; i++) {
            if (i < saveNames.size()) {
                System.out.println(i);
                BoxedTextButton b = (BoxedTextButton) buttonCollectionForSaves.buttons.get(i - startIndex);
                b.setText(saveNames.get(i));
            }
        }
//        buttonCollection.setAllToUnpressed();
//        buttonCollection.setAllToUnSelected();
    }

//    public void updateSelected(boolean movedUp){
//        if (movedUp){
//            for (int i = 0; i < buttonCollection.buttons.size(); i++) {
//                BoxedTextButton b = (BoxedTextButton) buttonCollection.buttons.get(i);
//                if (b.selected){
//                    b.selected = (false);
//                    if (i > 0){
//                        BoxedTextButton b2 = (BoxedTextButton) buttonCollection.buttons.get(i - 1);
//                        b2.selected = (true);
//                        break;
//                    }
//                }
//            }
//        }
//        else{
//            for (int i = buttonCollection.buttons.size() - 1; i >= 0; i--) {
//                BoxedTextButton b = (BoxedTextButton) buttonCollection.buttons.get(i);
//                if (b.selected){
//                    b.selected = (false);
//                    if (i < buttonCollection.buttons.size() - 1){
//                        BoxedTextButton b2 = (BoxedTextButton) buttonCollection.buttons.get(i + 1);
//                        b2.selected = (true);
//                        break;
//                    }
//                }
//            }
//        }
//    }

    public void setupSaveNames(){
        saveNames = new ArrayList<String>();
        File dir = new File("core/assets/Saves");
        saveNames.addAll(Arrays.asList(Objects.requireNonNull(dir.list())));
    }

    public void setupUI(){
        TextButton loadButton = new TextButton("Load", "loadButton");
        TextButton continueButton = new TextButton("Continue", "continueButton");

        loadButton.setSize(MyGdxGame.initialRes.x / 10f * 2, MyGdxGame.initialRes.y / 10f);
        continueButton.setSize(MyGdxGame.initialRes.x / 10f * 2, MyGdxGame.initialRes.y / 10f);

        float y = (MyGdxGame.initialRes.y / 10 * 8) + (offset * (numberShown / 2f))
                - ((MyGdxGame.initialRes.y / 10) * (numberShown - 1)) - (offset * numberShown);

        loadButton.setPos(MyGdxGame.initialRes.x / 2 - (loadButton.width * 0.85f), y);
        continueButton.setPos(MyGdxGame.initialRes.x - MyGdxGame.initialRes.y * 0.1f - continueButton.width
                , y);

        buttonCollectionForUI.add(loadButton, continueButton);
    }

    public void setupMap(){
        map = new Map((int) (MyGdxGame.initialRes.y / 10 * 7),
                (int) (MyGdxGame.initialRes.x - MyGdxGame.initialRes.y / 10 * 8),
                (int) (MyGdxGame.initialRes.y / 10 * 2f), "sed");
        map.generateBlank();
        setupMapResources();
    }

    public void setupMapResources(){
        textures = new HashMap<>();
        thingTextures = new HashMap<>();
        GameScreen.getAllMapTextures(textures, thingTextures);
    }

    public boolean loadMap(){
        Button b = getSelectedButton();
        if (b != null){
            BoxedTextButton b2 = (BoxedTextButton) b;
            colonists = new ArrayList<>();
            return Map.loadMap(b2.text, map, colonists);
        }
        return false;
    }

    public Button getSelectedButton(){
        for (Button b : buttonCollectionForSaves.buttons) {
            if (b.selected){
                return b;
            }
        }
        return null;
    }
}
