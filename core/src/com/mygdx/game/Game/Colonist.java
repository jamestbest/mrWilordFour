package com.mygdx.game.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Colonist {

    public int x;
    public int y;

    public int health;
    public HashMap<String, Integer> skills;

    public String profession;
    public String backstory;

    public String clotheName;
    public TextureAtlas textureAtlas;

    public String firstName;
    public String lastName;

    static Random random = new Random();

    public Colonist() {
        this.health = 100;
    }

    public void setup(){
        generateTextureAtlas();
        getRandomName();
        System.out.println(firstName + " " + lastName + " is a " + profession);
    }

    public void copyTemplate(Colonist template){
        this.skills = template.skills;
        this.profession = template.profession;
        this.backstory = template.backstory;
    }

    public void generateTextureAtlas(){
        textureAtlas = new TextureAtlas("Textures/TAResources/" + clotheName + ".atlas");
    }

    public void getRandomName() {
        ArrayList<String> firstNames = makeArrayOfNames("ColonistFirstNames");
        ArrayList<String> lastNames = makeArrayOfNames("ColonistLastNames");
        firstName = firstNames.get(random.nextInt(firstNames.size()));
        lastName = lastNames.get(random.nextInt(lastNames.size()));
    }

    public ArrayList<String> makeArrayOfNames(String nameOfFile) {
        try {
            FileReader fileReader = new FileReader("core/assets/ColonistInformation/" + nameOfFile);

            BufferedReader br = new BufferedReader(fileReader);

            ArrayList<String> temp = new ArrayList<>();

            String line = br.readLine();
            while(line != null) {
                temp.add(line);
                line = br.readLine();
            }

            fileReader.close();
            br.close();
            return temp;
        }
        catch (IOException e) {
            Gdx.app.log("Colonist", "Error reading file");
        }

        return null;
    }

}
