package com.TheatreTracker.utility;

public class PlayerDidAttack
{
    public String player;
    public String animation;
    public int tick;
    public String weapon;
    public String projectile;
    public String spotAnims;
    public int targetedIndex;
    public int targetedID;

    public PlayerDidAttack(String player, String animation, int tick, String weapon, String projectile, String spotAnims, int targetedIndex, int targetedID)
    {
        this.player = player;
        this.animation = animation;
        this.tick = tick;
        this.weapon = weapon;
        this.projectile = projectile;
        this.spotAnims = spotAnims;
        this.targetedIndex = targetedIndex;
        this.targetedID = targetedID;
    }
}
