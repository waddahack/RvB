package ennemies;

import managers.SoundManager;
import towser.Towser;
import static towser.Towser.game;
import static towser.Towser.unite;

public class FlyingEnemy extends Enemy{
    
    public static int idCount = 0, balance = 100;
    
    private double xDiffConst, yDiffConst, hyp, tileEveryPixel;
    
    public FlyingEnemy(){
        super(++idCount);
        name = "Bazoopter";
        spawnSpeed = 5f;
        reward = 36;
        power = 12;
        shootRate = 1;
        moveSpeed = 1.6f;
        range = 3*unite;
        life = 100;
        width = (int) (1.25*unite);
        eBalance = balance;
        rgb = new float[]{0.4f, 0.9f, 0.1f};
        sprite = Towser.textures.get("flyingEnemy");
        brightSprite = Towser.textures.get("flyingEnemyBright");
        clip = null;
        volume = SoundManager.Volume.SEMI_LOW;
        stepEveryMilli = 0;
        
        xDiffConst = (game.getBase().getRealX()-game.getSpawn().getRealX());
        yDiffConst = (game.getBase().getRealY()-game.getSpawn().getRealY());
        hyp = Math.sqrt(xDiffConst*xDiffConst + yDiffConst*yDiffConst);
        tileEveryPixel = (hyp/(game.path.size()-1));
        newAngle = 90+(float) Math.toDegrees(Math.atan2(yDiffConst, xDiffConst));
        angle = newAngle;
        
        initBack();
    }
    
    @Override
    protected void move(){
        double xDiff = (game.getBase().getRealX()-x), yDiff = (game.getBase().getRealY()-y);
        indiceTuile = (int) (game.path.size()-1 - Math.floor(Math.sqrt(xDiff*xDiff + yDiff*yDiff)/tileEveryPixel));

        if(isInBase())
            attack();

        double speed = ((moveSpeed*game.gameSpeed) * Towser.deltaTime / 50) * Towser.ref;
        
        x += xDiffConst * (speed/hyp);
        y += yDiffConst * (speed/hyp);
        
        startTimeMove = System.currentTimeMillis();
    }
}
