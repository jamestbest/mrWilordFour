package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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

    int startPos = 3;
    int endPos = 8;
    int numBoxesShown = 5;

    int hoverPos = -1;

    int drawLayer = 2;

    public boolean newItemSelected;

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
            setHoverPos();
            if (isToggled){
                for (int i = startPos; i < endPos; i++){
                    if ((i - startPos) == hoverPos){
                        batch.draw(pressedTexture, x, y - (((i - startPos)) * height), width, height);
                    }
                    else{
                        batch.draw(unpressedTexture, x, y - (((i - startPos)) * height), width, height);
                    }
                    glyphLayout.setText(font, dropDowns.get(i));
                    int xPos = (int) (x + (width / 2) - (glyphLayout.width / 2));
                    int yPos = (int) ((y + height) - (height * ((i - startPos))) - (height / 2) + glyphLayout.height / 2f);
                    font.draw(batch, glyphLayout, xPos, yPos);
                }
            }
            else {
                batch.draw(unpressedTexture, x, y, width, height);
                glyphLayout.setText(font, dropDowns.get(selectedPosition));
                int xPos = (int) (x + (width / 2) - (glyphLayout.width / 2));
                int yPos = (int) ((y + (height / 2)) + glyphLayout.height / 2f);
                font.draw(batch, glyphLayout, xPos, yPos);
            }
        }
    }

    public void setup(){
        unpressedTexture = new Texture("Textures/ui/buttons/dropDownButtonGrey.png");
        pressedTexture = new Texture("Textures/ui/buttons/dropDownButtonPressedGrey.png");
        super.setup();
        dropDowns = new ArrayList<>();
        dropDowns.add("asdf");
        dropDowns.add("2");
        dropDowns.add("3");
        dropDowns.add("4");
        dropDowns.add("5");
        dropDowns.add("6");
        dropDowns.add("7");
        dropDowns.add("8");
        dropDowns.add("9");
    }

    public boolean checkIfPressed(int x, int y){
        if (visible){
            if (isToggled){
                if (x > this.x && x < this.x + this.width && y > (this.y - (height * (numBoxesShown - 1))) && y < this.y + this.height){
                    if (Gdx.input.isButtonJustPressed(0)){
                        int temp = numBoxesShown - (y - (this.y - (height * (numBoxesShown - 1)))) / height + startPos - 1;
                        if (temp != selectedPosition){
                            selectedPosition = temp;
                            newItemSelected = true;
                        }
                        isToggled = false;
                    }
                    return true;
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

        if (x > this.x && x < this.x + this.width && y > (this.y - (height * (numBoxesShown - 1))) && y < this.y + this.height){
            hoverPos = numBoxesShown - (y - (this.y - (height * (numBoxesShown - 1)))) / height - 1;
        }
        else{
            hoverPos = -1;
        }
    }

    public String getSelectedItem(){
        return dropDowns.get(selectedPosition);
    }

    public void setDropDowns(ArrayList<String> dropDowns){
        this.dropDowns = dropDowns;
    }

    public void setSize(int width, int height){
        this.width = (int) (width * 0.9);
        this.height = (int) (height * 0.4f);
    }
}
