package com.mygdx.game.Math.sortingAlgorithms;

import com.mygdx.game.AStar.Node;
import com.mygdx.game.ui.items.Score;


import java.util.ArrayList;

public class QuickSort {
    public static ArrayList<Score> sortScores(ArrayList<Score> list){
        int pointerLoc = list.size() - 1;
        ArrayList<Score> left = new ArrayList<>();
        ArrayList<Score> right = new ArrayList<>();
        ArrayList<Score> middle = new ArrayList<>();
        float PointerOverall = list.get(pointerLoc).score;
        for (int i = 0; i < list.size() - 1; i++) {
            Score score = list.get(i);
            float overall = score.score;
            if (overall < PointerOverall) {
                left.add(score);
            } else if (overall > PointerOverall) {
                right.add(score);
            } else {
                middle.add(score);
            }
        }
        if (left.size() > 1) {
            left = sortScores(left);
        }
        if (right.size() > 1) {
            right = sortScores(right);
        }
        ArrayList<Score> sorted = new ArrayList<>(left);
        sorted.add(list.get(pointerLoc));
        sorted.addAll(middle);
        sorted.addAll(right);
        return sorted;
    }

    public static <T> ArrayList<T> reverse(ArrayList<T> list) {
        ArrayList<T> reversed = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            reversed.add(list.get(i));
        }
        return reversed;
    }
}
