package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.Colonist;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.ui.elements.Label;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.ButtonCollection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import static com.badlogic.gdx.math.MathUtils.random;

public class ColonistSelectionScreen implements Screen {
    MyGdxGame game;

    ArrayList<Colonist> colonistTemplates;
    ArrayList<Colonist> colonistsToSelectFrom;
    ArrayList<Colonist> colonistsSelected;

    int numberOfColonistsToSelectFrom = 10;
    int numberOfColonistsToSelect = 3;
    boolean ignoreSelectedRequirement = false;

    int selectedIndex = numberOfColonistsToSelectFrom - 1;

    ButtonCollection buttonCollection;
    Label title;
    TextButton selectRemoveButton;
    TextButton continueButton;
    TextButton randomiseButton;

    ShapeRenderer shapeRenderer;
    SpriteBatch batch;

    BitmapFont font;
    GlyphLayout glyphLayout;

    CameraTwo camera;

    float offsetY = MyGdxGame.initialRes.y / 12f;
    float offsetX = MyGdxGame.initialRes.x / 38f;
    int width = (int) (MyGdxGame.initialRes.x / 38 * 7);
    float height = (MyGdxGame.initialRes.y - (3 * offsetY)) / (float) numberOfColonistsToSelectFrom;

    HashMap<String, TextureAtlas> colonistClothes = new HashMap<>();

    float inputTimer = 0.5f;

    Random random = new Random();
    String[] clothes;

    Map map;

    public ColonistSelectionScreen(MyGdxGame game, Map map) {
        this.game = game;
        this.map = map;
        Json json = new Json();
        colonistTemplates = json.fromJson(ArrayList.class, Colonist.class, Gdx.files.internal("ColonistInformation/Backstories"));

        generateColonists();

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        buttonCollection = new ButtonCollection();

        title = new Label(0,0,0,0, "title", "select your colonists");
        title.centre((int) (MyGdxGame.initialRes.y / 100 * 47));

        selectRemoveButton = new TextButton(0,0, (int) (MyGdxGame.initialRes.x / 16 * 3), (int) (MyGdxGame.initialRes.y / 9), "Select/Remove colonist", "selectButton");
        selectRemoveButton.resizeFontToCorrectProportionByWidth();
        selectRemoveButton.setPos((int) (offsetX * 3 + width), (int) offsetY);

        continueButton = new TextButton(0,0,(int) (MyGdxGame.initialRes.x / 32 * 5), (int) (MyGdxGame.initialRes.y / 9), "Continue 0/3", "continueButton");
        continueButton.resizeFontToCorrectProportionByWidth();
        continueButton.setPos((int) MyGdxGame.initialRes.x / 23 * 16, (int) offsetY);

        randomiseButton = new TextButton(0,0, (int) (MyGdxGame.initialRes.x / 16 * 3), (int) (MyGdxGame.initialRes.y / 9), "Randomise", "randomiseButton");
        randomiseButton.resizeFontToCorrectProportionByWidth();
        randomiseButton.setPos((int) (offsetX * 4 + 2 * width), (int) offsetY);

        buttonCollection.add(title, selectRemoveButton, continueButton, randomiseButton);

        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout();
        setupFont();

        setupColonistClothes();

        camera = new CameraTwo();
        camera.allowMovement = false;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(49/255f, 53/255f, 61/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        setupFont();
        drawColonistMiniCards();
        drawOutlines();

        drawSelected();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        buttonCollection.drawButtons(batch);
        batch.end();

        if (Gdx.input.isButtonPressed(0)){
            buttonCollection.updateButtons(camera);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.C)){
            setupLoadsOfColonists();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            game.setScreen(game.mainMenu);
        }

        continueButton.setText("Continue " + colonistsSelected.size() + "/" + numberOfColonistsToSelect);

        handleInput();
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

    public void generateColonists(){
        colonistsToSelectFrom = new ArrayList<>();
        colonistsSelected = new ArrayList<>();

        clothes = getListOfClothes();

        for (int i = 0; i < numberOfColonistsToSelectFrom; i++) {
            colonistsToSelectFrom.add(generateColonist());
        }
    }

    public Colonist generateColonist(){
        int j = random.nextInt(colonistTemplates.size());
        int k = random.nextInt(clothes.length);
        Colonist c = new Colonist();
        c.copyTemplate(colonistTemplates.get(j));
        c.clotheName = clothes[k];
        c.setup();

        return c;
    }

    public String[] getListOfClothes(){
        File dir = new File("core/assets/Textures/TAResources");
        String[] files = dir.list();
        assert files != null;
        String[] output = new String[files.length / 2];
        for (int i = 0; i < files.length; i++) {
            if (!files[i].split("\\.")[1].equals("png")){
                output[i / 2] = files[i].split("\\.")[0];
            }
        }
        return output;
    }

    public void drawColonistMiniCards(){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setProjectionMatrix(camera.projViewMatrix);

        for (int i = 0; i < numberOfColonistsToSelectFrom; i++) {
            shapeRenderer.rect(offsetX, offsetY + (height * i) + (offsetY / numberOfColonistsToSelectFrom * i), width, height);
        }
        shapeRenderer.end();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        for (int i = 0; i < numberOfColonistsToSelectFrom; i++) {
            batch.draw(colonistClothes.get(colonistsToSelectFrom.get(i).clotheName)
                    .findRegion("front"),
                    offsetX, offsetY + (height * i) + (offsetY / numberOfColonistsToSelectFrom * i), height - 2, height - 2);
        }

        for (int i = 0; i < numberOfColonistsToSelectFrom; i++) {
            if (colonistsSelected.contains(colonistsToSelectFrom.get(i))){
                font.setColor(Color.GOLD);
            }
            else {
                font.setColor(Color.WHITE);
            }
            glyphLayout.setText(font, colonistsToSelectFrom.get(i).firstName + " " + colonistsToSelectFrom.get(i).lastName);
            font.draw(batch, glyphLayout, offsetX + height + 10, offsetY + (height * i) + (offsetY / numberOfColonistsToSelectFrom * i) + (height / 2) + (glyphLayout.height / 2));
            font.setColor(Color.WHITE);
        }
        batch.end();
    }

    public void setupFont(){
        glyphLayout.setText(font, "James Coward");
        float factor = (glyphLayout.height) / (height / 3);
        font.getData().setScale(font.getData().scaleX / factor);
    }

    public void handleInput(){
        if (Gdx.input.isButtonPressed(0)){
            Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            mousePos = camera.unproject(mousePos);
            for (int i = 0; i < numberOfColonistsToSelectFrom; i++) {
                if (mousePos.x > offsetX && mousePos.x < offsetX + width && mousePos.y > offsetY + (height * i) + (offsetY / numberOfColonistsToSelectFrom * i) && mousePos.y < offsetY + (height * i) + (offsetY / numberOfColonistsToSelectFrom * i) + height){
                    selectedIndex = i;
                }
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            inputTimer -= Gdx.graphics.getDeltaTime();
            if (inputTimer < 0 || Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
                selectedIndex -= 1;
                if (selectedIndex < 0){
                    selectedIndex = numberOfColonistsToSelectFrom - 1;
                }
                inputTimer = 0.5f;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)){
            inputTimer -= Gdx.graphics.getDeltaTime();
            if (inputTimer < 0 || Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)){
                selectedIndex += 1;
                selectedIndex %= numberOfColonistsToSelectFrom;
                inputTimer = 0.5f;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            addToColonistsSelected();
        }


        if (Gdx.input.isButtonJustPressed(0)){
            if (buttonCollection.pressedButtonName.equals("selectButton")){
                addToColonistsSelected();
            }
            if (buttonCollection.pressedButtonName.equals("continueButton")){
                if (colonistsSelected.size() == numberOfColonistsToSelect || ignoreSelectedRequirement){
                    game.setScreen(new GameScreen(game, colonistsSelected, map));
                }
            }
            if (buttonCollection.pressedButtonName.equals("randomiseButton")){
                colonistsSelected.remove(colonistsToSelectFrom.get(selectedIndex));
                colonistsToSelectFrom.set(selectedIndex, generateColonist());
            }
        }
    }

    public void addToColonistsSelected(){
        if (!colonistsSelected.contains(colonistsToSelectFrom.get(selectedIndex))){
            if (colonistsSelected.size() < numberOfColonistsToSelect){
                colonistsSelected.add(colonistsToSelectFrom.get(selectedIndex));
            }
        }
        else{
            colonistsSelected.remove(colonistsToSelectFrom.get(selectedIndex));
        }
    }

    public void drawOutlines(){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setProjectionMatrix(camera.projViewMatrix);

        shapeRenderer.setColor(Color.GOLD);
        for (int i = 0; i < numberOfColonistsToSelectFrom; i++) {
            if (colonistsSelected.contains(colonistsToSelectFrom.get(i))){
                shapeRenderer.rect(offsetX, offsetY + (height * i) + (offsetY / numberOfColonistsToSelectFrom * i), width, height);
            }
        }

        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(offsetX, offsetY + (height * selectedIndex) + (offsetY / numberOfColonistsToSelectFrom * selectedIndex), width, height);

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.end();
    }

    public void drawSelected(){
        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        Colonist c = colonistsToSelectFrom.get(selectedIndex);

        glyphLayout.setText(font, "James Coward");
        float factor = (glyphLayout.height) / (height / 100 * 44);
        font.getData().setScale(factor);

        float x = offsetX * 3 + width;

        batch.draw(colonistClothes.get(c.clotheName)
                .findRegion("front"),
                x, MyGdxGame.initialRes.y / 11 * 6, MyGdxGame.initialRes.x / 5, MyGdxGame.initialRes.x / 5);

        int gapY = 20;

        glyphLayout.setText(font, "Name: " + c.firstName + " " + c.lastName);
        font.draw(batch, glyphLayout, x, MyGdxGame.initialRes.y / 23 * 12 - gapY);
        glyphLayout.setText(font, "Profession: " + c.profession);
        font.draw(batch, glyphLayout, x, MyGdxGame.initialRes.y / 23 * 10 - gapY);
        glyphLayout.setText(font, "Backstory: " + c.backstory);
        font.draw(batch, glyphLayout, x, MyGdxGame.initialRes.y / 23 * 8 - gapY);

        Object[] skills = c.skills.keySet().toArray();
        Object[] skillValues = c.skills.values().toArray();

        float height = MyGdxGame.initialRes.y / 23 * 11 / skills.length;

        glyphLayout.setText(font, "Skills");
        font.draw(batch, glyphLayout, MyGdxGame.initialRes.x / 23 * 16, MyGdxGame.initialRes.y / 23 * 19.5f + gapY);
        for (int i = 0; i < skills.length; i++) {
            glyphLayout.setText(font, skills[i] + ": " + skillValues[i]);
            font.draw(batch, glyphLayout, MyGdxGame.initialRes.x / 23 * 16, MyGdxGame.initialRes.y / 23 * 18 - (height * i) + gapY);
        }

        batch.end();
    }

    public void setupLoadsOfColonists(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("How many colonists do you want to generate?");
        numberOfColonistsToSelect = scanner.nextInt();
        for (int i = 0; i < numberOfColonistsToSelect; i++){
            colonistsSelected.add(generateColonist());
        }
        ignoreSelectedRequirement = true;
    }

    public void setupColonistClothes(){
        GameScreen.getTAResources(colonistClothes);
    }
}
