package com.mygdx.game.Math.sortingAlgorithms;

import com.mygdx.game.AStar.Node;


import java.util.ArrayList;

public class QuickSort {
    public static ArrayList<Node> sortNodes(ArrayList<Node> list){
        int pointerLoc = list.size() - 1;
        ArrayList<Node> left = new ArrayList<>();
        ArrayList<Node> right = new ArrayList<>();
        ArrayList<Node> middle = new ArrayList<>();
        float PointerOverall = list.get(pointerLoc).global;
        for (int i = 0; i < list.size() - 1; i++) {
            Node Node = list.get(i);
            float overall = Node.global;
            if (overall < PointerOverall) {
                left.add(Node);
            } else if (overall > PointerOverall) {
                right.add(Node);
            } else {
                middle.add(Node);
            }
        }
        if (left.size() > 1) {
            left = sortNodes(left);
        }
        if (right.size() > 1) {
            right = sortNodes(right);
        }
        ArrayList<Node> sorted = new ArrayList<Node>(left);
        sorted.add(list.get(pointerLoc));
        sorted.addAll(middle);
        sorted.addAll(right);
        return sorted;
    }
}
