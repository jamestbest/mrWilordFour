package com.mygdx.game.DataStructures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.util.AbstractList;

public class Queue<E> extends AbstractList<E> {
    Array<Object> array;

    int front;
    int rear;

    int size;

    public Queue(int size){
        array = new Array<>(size);
        front = 0;
        rear = 0;
        this.size = size;
    }

    public void enQueue(Object item){
        if (rear == size){
            queueMaintenance();
        }

        if (rear != size){
            rear++;
            array.add(item);
        }
        else {
            Gdx.app.log("Queue", "Queue is full");
        }
    }

    public Object deQueue(){
        if (rear != front){
            front++;
            return array.get(front - 1);
        }
        else{
            Gdx.app.log("Queue", "Queue is empty");
            return null;
        }
    }

    public Object peak(){
        if (rear != front){
            return array.get(front);
        }
        else{
            Gdx.app.log("Queue", "Queue is empty");
            return null;
        }
    }

    public void queueMaintenance(){
        if (front == rear){
            array.clear();
            front = 0;
            rear = 0;
        }
        else {
            Array<Object> tempArray = new Array<>();
            for (int i = front; i < rear; i++) {
                tempArray.add(array.get(i));
            }
            front = 0;
            rear = tempArray.size;
            array = tempArray;
        }
    }

    public void displayQueue(){
        System.out.println("displaying array: ");
        for (int i = front; i < rear; i++) {
            System.out.println(array.get(i));
        }
    }

    @Override
    public E get(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
