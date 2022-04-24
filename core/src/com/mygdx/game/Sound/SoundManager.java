package com.mygdx.game.Sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import io.socket.client.Socket;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class SoundManager {
    public ArrayList<Sound> soundEffects;
    HashMap<String, Float> radii = new HashMap<>();

    HashMap<String, com.badlogic.gdx.audio.Sound> soundMap;

    public void addSound(String soundName, int x, int y, Socket socket, boolean isHost) {
        com.badlogic.gdx.audio.Sound s = soundMap.get(soundName);
        if (s != null) {
            long id = s.play();
            Sound s2 = new Sound(id, soundName, x, y, radii.get(soundName));
            soundEffects.add(s2);
            if (socket != null && isHost) {
                socket.emit("playSound", soundName, x, y);
            }
        }
    }

    public void removeSound(String name, int x, int y, Socket socket, boolean isHost) {
        for (Sound s : soundEffects) {
            assert s != null;
            if (s.getName().equals(name) && s.getX() == x && s.getY() == y) {
                com.badlogic.gdx.audio.Sound sound = soundMap.get(name);
                if (sound != null) {
                    sound.stop(s.id);
                    if (socket != null && isHost) {
                        socket.emit("stopSound", name, x, y);
                    }
                }
            }
        }
    }

    public SoundManager() {
        soundEffects = new ArrayList<>();
        setupTemplates();

        setupSoundMap();
        System.out.println(radii.toString());
    }

    public void setupTemplates(){
        Json json = new Json();
        ArrayList<Sound> soundTemplates;
        soundTemplates = json.fromJson(ArrayList.class, com.mygdx.game.Sound.Sound.class, Gdx.files.internal("core/assets/info/soundInfo/SoundInfo"));
        for (Sound s : soundTemplates) {
            radii.put(s.getSoundName(), s.getRadius());
        }
    }

    public void updateSounds(float camX, float camY) {
        ArrayList<Sound> toRemove = new ArrayList<>();
        for (Sound s : soundEffects) {
            if (s.shouldEnd()){
                toRemove.add(s);
                continue;
            }
            float volume = s.calculateVolume(s.calculateDistance(camX, camY));
            long id = s.id;
            String name = s.name;
            com.badlogic.gdx.audio.Sound sound = soundMap.get(name);
            if (sound != null) {
                System.out.println(id + " new vol: " + volume);
                sound.setVolume(id, volume);
            }
        }
        soundEffects.removeAll(toRemove);
    }

    public void setupSoundMap(){
        File f = new File("core/assets/Sounds");
        String[] sounds = f.list();
        soundMap = new HashMap<>();
        assert sounds != null;
        for (String s: sounds) {
            soundMap.put(s.split("\\.")[0], Gdx.audio.newSound(Gdx.files.internal("core/assets/Sounds/" + s)));
        }
    }

    public void dispose(){
        for (com.badlogic.gdx.audio.Sound s : soundMap.values()) {
            s.dispose();
        }
    }
}
