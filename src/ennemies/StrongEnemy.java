package ennemies;

import static ennemies.Enemy.Type.STRONG;
import managers.SoundManager;
import managers.TextManager.Text;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;

public class StrongEnemy extends Enemy{
    
    public static int balance = 120;
    
    public StrongEnemy(){
        super(STRONG);
        textures.add(RvB.textures.get("strongEnemyBase"));
        textures.add(RvB.textures.get("strongEnemyCannon"));
        texturesBright.add(RvB.textures.get("strongEnemyBaseBright"));
        texturesBright.add(RvB.textures.get("strongEnemyCannonBright"));
        rotateIndex = 0;
        rotateIndexShoot = 1;
        textureStatic = RvB.textures.get("strongEnemy");
        name = Text.ENEMY_STRONG;
        reward = 4;
        commitPower = 10f;
        
        shootRate = 1f;
        range = 3*RvB.unite;
        
        canShoot = true;
        follow = true;
        power = 5f;
        bulletSprite = RvB.textures.get("shell");
        bulletSizeBonus = 0;
        clip = SoundManager.Instance.getClip("fatCannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSpeed = 10;
        shootRate = 0.2f;
        range = 3*RvB.unite;
        focusIndex = 4;
        
        moveSpeed = 2.6f;
        life = 270f;
        size = RvB.unite;
        hitboxWidth = size;
        eBalance = balance;
        clipWalk = SoundManager.Instance.getClip("tank");
        volumeWalk = SoundManager.Volume.SEMI_LOW;
        stepEveryMilli = 0;

        initBack();
    }
    
    @Override
    public void drawSprite(Texture sprite, int angle){
        RvB.drawFilledRectangle(x, y, size, size, sprite, angle, 1, 0, (int) (3.5*4)*size/128);
    }
}
