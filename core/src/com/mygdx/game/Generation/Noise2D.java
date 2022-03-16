package com.mygdx.game.Generation;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Math.Math;

public class Noise2D {

    public static double noise(float x, float y, int numberOfLargeSquares){

        int largeSquareX = (int) Math.ffloor(x) % numberOfLargeSquares;
        int largeSquareY = (int) Math.ffloor(y) % numberOfLargeSquares;

        x -= Math.ffloor(x);
        y -= Math.ffloor(y);

        int[][] mults = new int[][]{{0,0},{1,0},{0,1},{1,1}};
        Vector2[] distVecs = new Vector2[4];
        for (int i = 0; i < 4; i++) {
            distVecs[i] = new Vector2(x - mults[i][0], y - mults[i][1]);
        }

        int[] gradVecs = new int[4];
        for (int i = 0; i < 4; i++) {
            gradVecs[i] = p[p[largeSquareX + mults[i][0]] + largeSquareY + mults[i][1]];
        }

        double[] dots = new double[4];
        for (int i = 0; i < 4; i++) {
            dots[i] = dotProduct(gradVecs[i], distVecs[i].x, distVecs[i].y);
        }

        double fadedX = fade(x);
        double fadedY = fade(y);

        double interp1 = linearInterpolation(fadedX, dots[0], dots[1]);
        double interp2 = linearInterpolation(fadedX, dots[2], dots[3]);
        return (linearInterpolation(fadedY, interp1, interp2) + 1) / 2;
    }

    public static double linearInterpolation(double x, double left, double right){
        return (left + (x * (right - left)));
    }

    public static double fade(double a){
        return a * a * a * (a * (a * 6 - 15) + 10);
    }

    public static double dotProduct(int gradVector, double x, double y){
        new Vector2();
        Vector2 temp = switch (gradVector % 4) {
            case 0 -> new Vector2(1, 1);
            case 1 -> new Vector2(-1, 1);
            case 2 -> new Vector2(1, -1);
            case 3 -> new Vector2(-1, -1);
            default -> new Vector2();
        };
        return ((temp.x * x) + (temp.y * y));
    }

    static final int[] p = new int[512], permutation = { 151,160,137,91,90,15,
            131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
            190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
            88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
            77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
            102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
            135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
            5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
            223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
            129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
            251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
            49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
            138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
    };
    static {
        for (int i=0; i < 256 ; i++){
            p[256+i] = p[i] = permutation[i];
        }
    }
}
