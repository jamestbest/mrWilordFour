package com.mygdx.game.Generation;

public class MapSettings {
    public int width = 250;
    public int height = 250;
    public float tileDims = 20;

    public String seed;

    public int perlinFrequency;

    public int treeFreq;
    public int riverBend;

    public MapSettings( int perlinFrequency, int treeFreq, int riverBend, String seed) {
        this.perlinFrequency = perlinFrequency;
        this.treeFreq = treeFreq;
        this.riverBend = riverBend;
        this.seed = seed;
    }

    public MapSettings(){

    }

    public MapSettings(String seed) {
        this(3,1,10000, seed);
    }
}
