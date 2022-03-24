package com.mygdx.game.Weapons;

import com.mygdx.game.Entity.Entity;

import java.util.Random;

public class Weapon {
    protected String name;
    protected int damage;
    protected int damageRange;
    protected int range;
    protected int coolDown;
    protected int currentCoolDown;
    protected int accuracy; // 0 - 100
    
    protected static Random random = new Random();

    public Weapon(String weaponType) {
        this.name = weaponType;
    }

    public Weapon(Weapon copy){
        this.accuracy = copy.accuracy;
        this.coolDown = copy.coolDown;
        this.currentCoolDown = copy.coolDown;
        this.damage = copy.damage;
        this.name = copy.name;
        this.range = copy.range;
    }

    public Weapon(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
    }

    public int getCurrentCoolDown() {
        return currentCoolDown;
    }

    public void setCurrentCoolDown(int currentCoolDown) {
        this.currentCoolDown = currentCoolDown;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
    
    public boolean attack(Entity defender, Entity attacker) {
        if (currentCoolDown <= 0) {
            currentCoolDown = coolDown;
            int temp = random.nextInt(100);
            if (temp <= accuracy && isInRange(attacker, defender)) {
                int delta = accuracy - temp;
                int totalDamage = damage;
                if (random.nextInt(100) <= (50 + delta)){
                    totalDamage += damageRange;
                }
                else {
                    totalDamage -= damageRange;
                }
                defender.setHealth(defender.getHealth() - totalDamage);
                return true;
            }
        }
        return false;
    }

    public boolean isInRange(Entity attacker, Entity defender){
        return (Math.pow(attacker.getX() - defender.getX(), 2) +
                Math.pow(attacker.getY() - defender.getY(), 2)) <= Math.pow(range, 2);
    }

    public void updateTimers(float deltaTime){
        currentCoolDown -= deltaTime;
    }
}
