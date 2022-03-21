package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game.MyGdxGame;

import java.util.ArrayList;

public class DropdownButton extends TextButton{
    public DropdownButton(int x, int y, int width, int height, String text, String TextureGda, String name, InputMultiplexer inputMultiplexer) {
        super(x, y, width, height, text, TextureGda, name);
        inputMultiplexer.addProcessor(inputProcessor);
        setup();
    }

    public DropdownButton(int x, int y, int width, int height, String text, String name, InputMultiplexer inputMultiplexer) {
        super(x, y, width, height, text, name);
        inputMultiplexer.addProcessor(inputProcessor);
        setup();
    }

    public DropdownButton(String name, InputMultiplexer inputMultiplexer){
        this(0, 0, 0, 0, "text", name, inputMultiplexer);
    }

    boolean isToggled = false;
    ArrayList<String> dropDowns;
    int selectedPosition = 0;

    int startPos = 0;
    public int numBoxesShown = 5;
    public int endPos = numBoxesShown + startPos;

    public boolean drawDown = true;

    int hoverPos = -1;

    int drawLayer = 2;

    public boolean newItemSelected;

    public boolean isForFont;

    InputProcessor inputProcessor = new InputAdapter() {
        @Override
        public boolean scrolled(float amountX, float amountY) {
            if (amountY < 0) {
                if (startPos > 0) {
                    startPos--;
                    endPos--;
                }
            }
            if (amountY > 0) {
                if (endPos < dropDowns.size()) {
                    startPos++;
                    endPos++;
                }
            }
            return false;
        }
    };

    public void draw(SpriteBatch batch, int drawLayer){
        newItemSelected = false;
        if (drawLayer == this.drawLayer){
            if (isToggled){

            }
            setHoverPos();
            if (isToggled){
                for (int i = startPos; i < endPos; i++){
                    if ((i - startPos) == hoverPos){
                        if (drawDown){
                            batch.draw(pressedTexture, x, y - (((i - startPos)) * height), width, height);
                        }
                        else {
                            batch.draw(pressedTexture, x, y + (((i - startPos)) * height), width, height);
                        }
                    }
                    else{
                        if (drawDown){
                            batch.draw(unpressedTexture, x, y - (((i - startPos)) * height), width, height);
                        }
                        else {
                            batch.draw(unpressedTexture, x, y + (((i - startPos)) * height), width, height);
                        }
                    }

                    setTextAndReloadFont(i);
                    int xPos = (int) (x + (width / 2) - (glyphLayout.width / 2));
                    int yPos = 0;
                    if (drawDown){
                        yPos = (int) ((y + height) - (height * ((i - startPos))) - (height / 2) + glyphLayout.height / 2f);
                    }
                    else {
                        yPos = (int) ((y + height) + (height * ((i - startPos))) - (height / 2) + glyphLayout.height / 2f);
                    }
                    font.draw(batch, glyphLayout, xPos, yPos);
                }
            }
            else {
                batch.draw(unpressedTexture, x, y, width, height);

                setTextAndReloadFont(selectedPosition);
                int xPos = (int) (x + (width / 2) - (glyphLayout.width / 2));
                int yPos = (int) ((y + (height / 2)) + glyphLayout.height / 2f);
                font.draw(batch, glyphLayout, xPos, yPos);
            }
        }
    }

    private void setTextAndReloadFont(int i) {
        if (isForFont){
            String text = dropDowns.get(i);
            font.dispose();
            font = new BitmapFont(Gdx.files.internal("Fonts/" + text + ".fnt"));
        }

        glyphLayout.setText(font, dropDowns.get(i));
    }

    public void setup(){
        unpressedTexture = new Texture("Textures/ui/buttons/dropDownButtonGrey.png");
        pressedTexture = new Texture("Textures/ui/buttons/dropDownButtonPressedGrey.png");
        super.setup();
        dropDowns = new ArrayList<>();
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if (visible){
            if (isToggled){
                float yPos;
                float nextY;
                if (drawDown){
                    yPos = (this.y - (height * (numBoxesShown - 1)));
                    nextY = (this.y + this.height);
                }
                else {
                    yPos = this.y;
                    nextY = (this.y + (height * (numBoxesShown)));
                }
                if (x > this.x && x < this.x + this.width && y > yPos && y < nextY){
                    if (Gdx.input.isButtonJustPressed(0)){
                        int temp;
                        if (drawDown){
                            temp = (numBoxesShown - (int) ((y - yPos) / height)) + startPos - 1;
                        }
                        else {
                            temp = (y - this.y) / height + startPos;
                        }

                        if (temp != selectedPosition){
                            selectedPosition = temp;
                            newItemSelected = true;
                        }
                        isToggled = false;
                    }
                    return true;
                }
                else {
                    isToggled = false;
                    return false;
                }
            }
            else{
                if (x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height){
                    if (Gdx.input.isButtonJustPressed(0)){
                        isToggled = true;
                    }
                    return true;
                }
            }
        }
        isToggled = false;
        return false;
    }

    public void setHoverPos(){
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (drawDown){
            if (x > this.x && x < this.x + this.width && y > (this.y - (height * (numBoxesShown - 1))) && y < this.y + this.height){
                hoverPos = numBoxesShown - (y - (this.y - (height * (numBoxesShown - 1)))) / height - 1;
            }
            else{
                hoverPos = -1;
            }
        }
        else {
            if (x > this.x && x < this.x + this.width && y > this.y && y < this.y + (height * numBoxesShown)){
                hoverPos = (y - this.y) / height;
            }
            else{
                hoverPos = -1;
            }
        }
    }

    public String getSelectedItem(){
        return dropDowns.get(selectedPosition);
    }

    public void setDropDownsForMusic(ArrayList<String> dropDowns, MyGdxGame game){
        this.dropDowns = dropDowns;
        this.selectedPosition = dropDowns.indexOf(game.songPlaying);
    }

    public void setDropDownsForFont(ArrayList<String> dropDowns){
        this.dropDowns = dropDowns;
        this.selectedPosition = dropDowns.indexOf(MyGdxGame.fontName);
    }

    public void setDropDowns(ArrayList<String> dropDowns){
        this.dropDowns = dropDowns;
    }

    public void setSize(int width, int height){
        this.width = (int) (width * 0.9);
        this.height = (int) (height * 0.4f);
    }

    public void setSize(float width, float height){
        this.width = (int) (width * 0.9);
        this.height = (int) (height * 0.4f);
    }

    public void setSelected(boolean selected){
        this.isToggled = selected;
        this.selected = selected;
    }
}
