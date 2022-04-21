package com.mygdx.game.Sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Sound.Sound;
import io.socket.client.Socket;

import java.io.File;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

public class SoundManager {
    public ArrayList<Sound> soundEffects;
    ArrayList<Sound> soundTemplates;
    HashMap<String, Float> radii = new HashMap<>();

    int idCounter;

    HashMap<String, com.badlogic.gdx.audio.Sound> soundMap;

    public void addSound(Sound sound) {
        soundEffects.add(sound);
    }

    public void addSound(String soundName, int x, int y, Socket socket, boolean isHost) {
//        for (Sound s: soundTemplates) {
//            if (s.getSoundName().equals(soundName)) {
//                soundEffects.add(new Sound(x, y, getNextID(), s));
//                if (isHost && socket != null) {
//                    socket.emit("playSound", soundName, x, y);
//                }
//                break;
//            }
//        }
        com.badlogic.gdx.audio.Sound s = soundMap.get(soundName);
        if (s != null) {
            long id = s.play();
            Sound s2 = new Sound(id, soundName, x, y, radii.get(soundName));
            soundEffects.add(s2);
        }
    }

    public void removeSound(Sound sound) {
        soundEffects.remove(sound);
    }

    public void removeSound(String name, int x, int y){
//        for (Sound s: soundEffects) {
//            if (s.getSoundName().equals(soundName) && s.getX() == x && s.getY() == y) {
//                s.stop();
//                soundEffects.remove(s);
//                break;
//            }
//        }
        for (Sound s : soundEffects) {
            assert s != null;
            if (s.getName().equals(name) && s.getX() == x && s.getY() == y) {
                com.badlogic.gdx.audio.Sound sound = soundMap.get(name);
                if (sound != null) {
                    sound.stop(s.id);
                }
            }
        }
    }

    public void removeALlSounds() {
        soundEffects.clear();
    }

    public SoundManager() {
        soundEffects = new ArrayList<>();
        setupTemplates();

        setupSoundMap();
        System.out.println(radii.toString());
    }

    public void setupTemplates(){
        Json json = new Json();
        soundTemplates = new ArrayList<>();
        soundTemplates = json.fromJson(ArrayList.class, com.mygdx.game.Sound.Sound.class, Gdx.files.internal("core/assets/info/soundInfo/SoundInfo"));
        for (Sound s : soundTemplates) {
            radii.put(s.getSoundName(), s.getRadius());
        }
    }

    public void updateSounds(float camX, float camY) {
//        ArrayList<Sound> toRemove = new ArrayList<>();
//        try {
//            for (Sound s : soundEffects) {
//                if (!s.update(camX, camY)) {
//                    toRemove.add(s);
//                }
//            }
//        }catch (ConcurrentModificationException e){
//            System.out.println("ConcurrentModificationException on updating sounds");
//        }
//        soundEffects.removeAll(toRemove);

        for (Sound s : soundEffects) {
            float volume = s.calculateVolume(s.calculateDistance(camX, camY));
            long id = s.id;
            String name = s.name;
            com.badlogic.gdx.audio.Sound sound = soundMap.get(name);
            if (sound != null) {
                sound.setVolume(id, volume);
            }
        }
    }

    public void dispose(){
//        for (Sound s: soundEffects) {
//            s.dispose();
//        }
        for (Sound s : soundTemplates) {
            s.dispose();
        }
    }

    public int getNextID() {
        return idCounter++;
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
}
