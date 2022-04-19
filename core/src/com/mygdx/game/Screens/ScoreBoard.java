package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Math.sortingAlgorithms.QuickSort;
import com.mygdx.game.ui.items.Score;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static java.util.Comparator.comparing;

public class ScoreBoard implements Screen {
    int start;
    int height = 10;

    ArrayList<Score> scores = new ArrayList<>();

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    BitmapFont font;
    GlyphLayout glyphLayout;

    MyGdxGame game;

    Json json;
    private static final Random random = new Random();
    Date date = new Date();
    int day;
    int month;
    int year;

    InputAdapter inputListener = new InputAdapter() {
        @Override
        public boolean scrolled(float amountX, float amountY) {
            if (amountY > 0) {
                start++;
                int minAmountShown = Math.min(height, scores.size());
                int endMin = scores.size() - minAmountShown;
                if (start > endMin) {
                    start = endMin;
                }
            }
            if (amountY < 0) {
                start--;
                if (start < 0) {
                    start = 0;
                }
            }
            return true;
        }
    };

    public ScoreBoard(MyGdxGame game){
        setup();
        this.game = game;
    }

    public ScoreBoard(MyGdxGame game, Score score){
        setup();
        this.game = game;
        addScore(score);
    }

    public void setup(){
        start = 0;
        scores = new ArrayList<>();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout(font, "");

        json = new Json();
        getScores();

        Gdx.input.setInputProcessor(inputListener);

        day = date.getDate();
        month = date.getMonth();
        year = date.getYear();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(49/255f, 53/255f, 61/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)){
            date = new Date();
            addScore(new Score("test", random.nextInt(100) + 1, date.getTime()));
        }

        batch.begin();
        font.getData().setScale(4);
        glyphLayout.setText(font, "Score Board");
        font.draw(batch, glyphLayout, (MyGdxGame.initialRes.x / 2f) - (glyphLayout.width / 2f), MyGdxGame.initialRes.y - glyphLayout.height / 4f);

        int y = (int) (MyGdxGame.initialRes.y - (glyphLayout.height * 3f));
        font.getData().setScale(1);
        int end = Math.min(start + height, scores.size());
        drawAll(end, y);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
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

    public void drawAll(int end, int startY){
        Float[] positions = new Float[]{0.2f, 0.3f, 0.45f, 0.6f};
        String[] texts = new String[]{"Position", "Score", "Name", "Date"};
        for (int i = 0; i < 4; i++) {
            drawInfo(end, startY, positions[i], texts[i]);
        }
    }

    public void drawInfo(int end, int startY, float position, String text){
        glyphLayout.setText(font, text);
        int x = (int) (MyGdxGame.initialRes.x * position);
        font.draw(batch, glyphLayout, x, startY);

        for (int i = start; i < end; i++) {
            Score s = scores.get(i);
            int i2 = i - start;
            switch (text) {
                case "Position" -> glyphLayout.setText(font, i + 1 + "");
                case "Score" -> glyphLayout.setText(font, s.getScore() + "");
                case "Name" -> glyphLayout.setText(font, s.getName());
                case "Date" -> {
                    Date d = new Date(s.getDate());
                    if (d.getDate() == day && d.getMonth() == month && d.getYear() == year) {
                        glyphLayout.setText(font, "Today");
                    }
                    else if (d.getDate() == day - 1 && d.getMonth() == month && d.getYear() == year) {
                        glyphLayout.setText(font, "Yesterday");
                    }
                    else if (d.getDate() == day + 1 && d.getMonth() == month && d.getYear() == year) {
                        glyphLayout.setText(font, "Tomorrow");
                    }
                    else {
                        glyphLayout.setText(font, d.toString());
                    }
                }
            }
            font.draw(batch, glyphLayout, x, startY - ((glyphLayout.height * 3f) * (i2 + 1)));
        }
    }

    public void getScores(){
        FileReader fileReader;
        try {
            fileReader = new FileReader("core/assets/Scores/Scores.txt");
            BufferedReader br = new BufferedReader(fileReader);

            String line;
            while((line = br.readLine()) != null){
                System.out.println(line);
                Score s = json.fromJson(Score.class, line);
                scores.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(scores.toString());
    }

    public void addScore(Score score){
        scores.add(score);
        sortScores();
        writeScores();
    }

    public void sortScores(){
        scores = QuickSort.reverse(QuickSort.sortScores(scores));
    }

    public void writeScores(){
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter("core/assets/Scores/Scores.txt");
            BufferedWriter bw = new BufferedWriter(fileWriter);

            for (Score score : scores){
                bw.write(json.toJson(score, Score.class));
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}