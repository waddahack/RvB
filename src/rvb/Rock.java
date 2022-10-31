package rvb;

public class Rock extends Tile{
    
    public int life;
    public int maxLife;
    
    public Rock(float x, float y, int size){ // Size pour différente taille (donc sprite) et life
        super(x, y);
        texture = RvB.textures.get("rock");
        type = "rock";
        life = 1000;
        maxLife = life;
    }
}
