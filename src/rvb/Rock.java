package rvb;

import java.util.Random;

public class Rock extends Tile{
    
    public int life;
    public int maxLife;
    private static Random random = new Random();
    
    public Rock(float x, float y, int size){ // Size pour différente taille (donc sprite) et life
        super(x, y);
        texture = RvB.textures.get("rock"+(random.nextInt(2)+1));
        type = "rock";
        life = 1000;
        maxLife = life;
        this.size = RvB.unite;/*(int) (RvB.unite*0.9+0.1*RvB.unite*size);*/
        angle = Math.floorDiv(random.nextInt(360), 90)*90;
    }
}
