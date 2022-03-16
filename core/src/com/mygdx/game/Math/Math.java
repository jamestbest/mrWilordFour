package com.mygdx.game.Math;

public class Math {

    public static int lowest(int num1, int num2){
        if (num1 < num2){
            return num1;
        }
        return num2;
    }

    public static int highest(int num1, int num2){
        if (num1 > num2){
            return num1;
        }
        return num2;
    }

    public static float lowest(float a, float b){
        if (a > b){
            return b;
        }
        else{
            return a;
        }
    }

    public static float highest(float a, float b){
        if (a > b){
            return a;
        }
        else{
            return b;
        }
    }

    public static float abs(float a){
        if (a < 0){
            return -a;
        }
        return a;
    }

    public static int floor(float a){
        return (int)a;
    }

    public static int ceil(float a){
        return (int)a + 1;
    }

    public static float ffloor(float a){
        return a>0 ? (int)a : (int)a-1;
    }
}
