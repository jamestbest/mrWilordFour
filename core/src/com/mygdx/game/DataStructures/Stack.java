package com.mygdx.game.DataStructures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class Stack<T> {

    public int top = 0;
    int bottom = 0;

    int size;

    Array<T> array;

    public boolean eraseOld;

    public Stack(int size){
        this.size = size;
        setupArray();
    }

    public void setupArray(){
        array = new Array<>();
        for (int i = 0; i < size; i++) {
            array.add(null);
        }
    }

    public void enStack(T item){
        if (top == size){
            if (eraseOld){
                bottom++;
            }
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

    public T deStack(){
        if (top != bottom){
            top --;
            return array.get(top);
        }
        else{
            Gdx.app.log("Stack", "Stack is empty");
            return null;
        }
    }

    public T pop(){
        if (top != bottom){
            top--;
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
            Array<T> tempArray = new Array<>(size);
            for (int i = bottom; i < top; i++) {
                tempArray.add(array.get(i));
            }
            int endTop = tempArray.size;
            for (int i = endTop; i < size; i++) {
                tempArray.add(null);
            }
            array = tempArray;
            bottom = 0;
            top = endTop;
        }
    }

    public void displayStack(){
        for (int i = bottom; i < top; i++) {
            System.out.println(array.get(i));
        }
    }

    public void clear(){
        setupArray();
        top = 0;
        bottom = 0;
    }
}
