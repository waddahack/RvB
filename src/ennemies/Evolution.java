package ennemies;

import org.newdawn.slick.opengl.Texture;
import rvb.RvB;

public class Evolution {
    
    private Texture texture, textureBright;
    public int life, maxLife;
    private Evolution previousEvo;
    private Enemy owner;
    public float[] lifeColor;
    
    
    public Evolution(Enemy owner, int life, Texture texture, Texture textureBright, float[] lifeColor, Evolution previousEvo){
        this.owner = owner;
        this.texture = texture;
        this.textureBright = textureBright;
        this.life = life;
        maxLife = life;
        this.previousEvo = previousEvo;
        this.lifeColor = lifeColor;
    }
    
    public void render(){
        render(true);
    }
    
    private void render(boolean lastOnPile){
        if(previousEvo != null)
            previousEvo.render(false);
        Texture sprite = texture;
        if(lastOnPile){
            if(owner.startTimeWaitFor != 0 && System.currentTimeMillis() - owner.startTimeWaitFor < owner.waitFor)
                sprite = textureBright;
            else if(owner.startTimeWaitFor != 0)
                owner.startTimeWaitFor = 0;
        }
        RvB.drawFilledRectangle(owner.x, owner.y, owner.width, owner.width, sprite, owner.angle);
    }
    
    public void attacked(int power){
        life -= power;
    }
}
