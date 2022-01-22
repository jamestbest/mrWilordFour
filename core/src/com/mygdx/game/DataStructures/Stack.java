package com.mygdx.game.DataStructures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class Stack {

    int top = 0;
    int bottom = 0;

    int size;

    Array<Object> array;

    public Stack(int size){
        this.size = size;
        array = new Array<>();
        for (int i = 0; i < size; i++) {
            array.add(null);
        }
    }

    public void enStack(Object item){
        if (top == size){
            stackMaintenance();
        }

        if (top != size){
            array.set(top, item);
            top ++;
        }
        else{
            Gdx.app.log("Stack", "Stack is full");
        }
    }

    public Object deStack(){
        if (top != bottom){
            top --;
            return array.get(top);
        }
        else{
            Gdx.app.log("Stack", "Stack is empty");
            return null;
        }
    }

    public Object pop(){
        if (top != bottom){
            return array.get(top);
        }
        else{
            Gdx.app.log("Stack", "Stack is empty");
            return null;
        }
    }

    public void stackMaintenance(){
        if (top == bottom){
            array.clear();
            top = 0;
            bottom = 0;
        }
        else{
            Array<Object> tempArray = new Array<>();
            for (int i = bottom; i < top; i++) {
                tempArray.add(array.get(i));
            }
            array = tempArray;
            bottom = 0;
            top = tempArray.size;
        }
    }

    public void displayStack(){
        for (int i = bottom; i < top; i++) {
            System.out.println(array.get(i));
        }
    }
}
