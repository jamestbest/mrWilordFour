package com.mygdx.game.Generation;

public class MapSettings {
    public String seed;

    public int perlinFrequency;

    public int treeFreq;
    public int riverBend;

    public boolean riverToggle;

    public MapSettings(int perlinFrequency, int treeFreq, int riverBend, String seed, boolean riverToggle) {
        this.perlinFrequency = perlinFrequency;
        this.treeFreq = treeFreq;
        this.riverBend = riverBend;
        this.seed = seed;
        this.riverToggle = riverToggle;
    }

    public MapSettings(){

    }

    public MapSettings(String seed) {
        this(4,1,10000, seed, true);
    }
}
