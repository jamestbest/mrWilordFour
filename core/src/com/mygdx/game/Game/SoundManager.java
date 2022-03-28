package com.mygdx.game.Game;

import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;

public class SoundManager {
    ArrayList<Sound> soundEffects;

    public void addSound(Sound sound) {
        soundEffects.add(sound);
    }

    public void removeSound(Sound sound) {
        soundEffects.remove(sound);
    }

    public void removeALlSounds() {
        soundEffects.clear();
    }

    public SoundManager() {
        soundEffects = new ArrayList<>();
    }
}
