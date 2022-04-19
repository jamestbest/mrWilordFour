package com.mygdx.game.Weapons;

import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Entity.Entity;
import com.mygdx.game.Screens.GameScreen;
import io.socket.client.Socket;

import java.util.Random;

public class Weapon {
    protected String name;
    protected int damage;
    protected int damageRange;
    protected int damageLevelEffector;
    protected float range;
    protected float coolDown;
    protected float currentCoolDown;
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
        this.damageRange = copy.damageRange;
        this.damageLevelEffector = copy.damageLevelEffector;
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

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public float getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(float coolDown) {
        this.coolDown = coolDown;
    }

    public float getCurrentCoolDown() {
        return currentCoolDown;
    }

    public void setCurrentCoolDown(float currentCoolDown) {
        this.currentCoolDown = currentCoolDown;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
    
    public boolean attack(Entity defender, Entity attacker, Socket socket, boolean isHost) {
        if (currentCoolDown <= 0) {
            currentCoolDown = coolDown;
            int temp = random.nextInt(100);
            if (temp <= accuracy && isInRange(attacker, defender)) {
                int delta = accuracy - temp;
                int totalDamage = damage + (attacker.getLevel() * damageLevelEffector / 2);
                if (random.nextInt(100) <= (50 + delta)){
                    totalDamage += damageRange;
                }
                else {
                    totalDamage -= damageRange;
                }
                GameScreen.soundManager.addSound(name, attacker.getX(), attacker.getY(), socket, isHost);
                defender.setHealth(defender.getHealth() - totalDamage);
                if (isHost && socket != null) {
                    socket.emit("updateHealth", defender.getEntityID(), defender.getHealth());
                }
                if (random.nextInt(10) <= 1 && attacker instanceof Colonist) {
                    attacker.setXp(attacker.getXp() + random.nextInt(2) + 1);
                    GameScreen.score += random.nextInt(2) + 1;
                }
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
