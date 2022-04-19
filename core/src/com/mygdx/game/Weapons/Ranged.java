package com.mygdx.game.Weapons;

import com.mygdx.game.Entity.Entity;
import com.mygdx.game.Screens.GameScreen;
import io.socket.client.Socket;

public class Ranged extends Weapon {
    public Ranged(String name) {
        super(name);
    }

    public Ranged(Ranged ranged) {
        super(ranged);
        this.ammo = ranged.ammo;
        this.maxAmmo = ranged.maxAmmo;
        this.reloadTime = ranged.reloadTime;
        this.reloadTimer = 0;
    }

    public Ranged(){

    }

    private float reloadTime;
    private float reloadTimer;

    private int ammo;
    private int maxAmmo;

    public float getReloadTime() {
        return reloadTime;
    }

    public void setReloadTime(float reloadTime) {
        this.reloadTime = reloadTime;
    }

    public float getReloadTimer() {
        return reloadTimer;
    }

    public void setReloadTimer(float reloadTimer) {
        this.reloadTimer = reloadTimer;
    }

    public int getAmmo() {
        return ammo;
    }

    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public void setMaxAmmo(int maxAmmo) {
        this.maxAmmo = maxAmmo;
    }

    public boolean attack(Entity defender, Entity attacker, Socket socket, boolean isHost){
        if(ammo > 0 && reloadTimer <= 0){
            super.attack(defender, attacker, socket, isHost);
        }
        else {
            reload();
        }
        return false;
    }

    public void reload(){
        ammo = maxAmmo;
        reloadTimer = reloadTime;
    }

    public void updateTimers(float deltaTime){
        super.updateTimers(deltaTime);
        reloadTimer -= deltaTime;
    }
}
