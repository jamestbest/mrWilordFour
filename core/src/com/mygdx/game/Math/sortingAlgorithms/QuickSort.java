package com.mygdx.game.Math.sortingAlgorithms;

import com.mygdx.game.Math.AStar.Node;
import com.mygdx.game.Math.CStar.NodeC;
import com.mygdx.game.Math.DStar.NodeD;

import java.util.ArrayList;

public class QuickSort {
    public static ArrayList<NodeD> sortNodes(ArrayList<NodeD> list){
        int pointerLoc = list.size() - 1;
        ArrayList<NodeD> left = new ArrayList<>();
        ArrayList<NodeD> right = new ArrayList<>();
        ArrayList<NodeD> middle = new ArrayList<>();
        float PointerOverall = list.get(pointerLoc).global;
        for (int i = 0; i < list.size() - 1; i++) {
            NodeD NodeD = list.get(i);
            float overall = NodeD.global;
            if (overall < PointerOverall) {
                left.add(NodeD);
            } else if (overall > PointerOverall) {
                right.add(NodeD);
            } else {
                middle.add(NodeD);
            }
        }
        if (left.size() > 1) {
            left = sortNodes(left);
        }
        if (right.size() > 1) {
            right = sortNodes(right);
        }
        ArrayList<NodeD> sorted = new ArrayList<NodeD>(left);
        sorted.add(list.get(pointerLoc));
        sorted.addAll(middle);
        sorted.addAll(right);
        return sorted;
    }
}
