package com.mygdx.game.Sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Sound.Sound;
import io.socket.client.Socket;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class SoundManager {
    public ArrayList<Sound> soundEffects;
    ArrayList<Sound> soundTemplates;

    int idCounter;

    public void addSound(Sound sound) {
        soundEffects.add(sound);
    }

    public void addSound(String soundName, int x, int y, Socket socket, boolean isHost) {
        for (Sound s: soundTemplates) {
            if (s.getSoundName().equals(soundName)) {
                soundEffects.add(new Sound(x, y, getNextID(), s));
                if (isHost && socket != null) {
                    socket.emit("playSound", soundName, x, y);
                }
                break;
            }
        }
    }

    public void removeSound(Sound sound) {
        soundEffects.remove(sound);
    }

    public void removeSound(String soundName, int x, int y){
        for (Sound s: soundEffects) {
            if (s.getSoundName().equals(soundName) && s.getX() == x && s.getY() == y) {
                s.stop();
                soundEffects.remove(s);
                break;
            }
        }
    }

    public void removeALlSounds() {
        soundEffects.clear();
    }

    public SoundManager() {
        soundEffects = new ArrayList<>();
        setupTemplates();
    }

    public void setupTemplates(){
        Json json = new Json();
        soundTemplates = new ArrayList<>();
        soundTemplates = json.fromJson(ArrayList.class, com.mygdx.game.Sound.Sound.class, Gdx.files.internal("core/assets/info/soundInfo/SoundInfo"));
    }

    public void updateSounds(float camX, float camY) {
        ArrayList<Sound> toRemove = new ArrayList<>();
        try {
            for (Sound s : soundEffects) {
                if (!s.update(camX, camY)) {
                    toRemove.add(s);
                }
            }
        }catch (ConcurrentModificationException e){
            System.out.println("ConcurrentModificationException on updating sounds");
        }
        soundEffects.removeAll(toRemove);
    }

    public void dispose(){
        for (Sound s: soundEffects) {
            s.dispose();
        }
    }

    public int getNextID() {
        return idCounter++;
    }
}
