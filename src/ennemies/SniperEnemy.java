package ennemies;

import static ennemies.Enemy.Type.SNIPER;
import java.util.ArrayList;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import rvb.Shootable;
import towers.Tower;


public class SniperEnemy extends Enemy{
    
    public static int balance = 30;
    private Evolution shield;
    private boolean sniping;
    
    public SniperEnemy(){
        super(SNIPER);
        textures.add(RvB.textures.get("basicEnemy"));
        texturesBright.add(RvB.textures.get("basicEnemyBright"));
        rotateIndex = 0;
        rotateIndexShoot = 0;
        textureStatic = RvB.textures.get("basicEnemy");
        name = Text.ENEMY_SNIPER;
        reward = 3;
        commitPower = 2f;
        
        canShoot = true;
        sniping = false;
        follow = true;
        power = 2f;
        bulletSprite = RvB.textures.get("roundBullet");
        bulletSizeBonus = (int) (-7*ref);
        clip = SoundManager.Instance.getClip("sniper");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSpeed = 20;
        shootRate = 0.5f;
        range = 10*RvB.unite;
        focusIndex = 2;

        moveSpeed = 4f;
        life = 12f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        volumeWalk = SoundManager.Volume.VERY_LOW;
        clipWalk = SoundManager.Instance.getClip("walking");
        stepEveryMilli = 600;
        eBalance = balance;
        
        shield = new Evolution(this, 140, RvB.textures.get("bazooEvo1"), RvB.textures.get("bazooEvo1Bright"), RvB.colors.get("life1"), null);
        evolutions.add(shield);
        
        initBack();
    }
    
    @Override
    public void move(){
        super.move();
        if(enemyAimed == null && sniping)
            stopSnipe();
    }
    
    @Override
    protected Shootable searchForStrongest(ArrayList<Shootable> enemies){
        Shootable aim = super.searchForStrongest(enemies);
        
        if(aim != null){
            boolean safe = true;
            if(moveSpeed > 0){
                for(Shootable tower : game.towers){
                    Tower t = (Tower) tower;
                    if(!t.isPlaced())
                        continue;
                    if(this.isInRangeOf(tower)){
                        safe = false;
                        break;
                    }
                }
            }
            if(safe && !sniping){
                // Check si un autre Znooper est à côté. Ne pas s'arrêter le cas échéant
                int sep = (int)(20*ref);
                for(Shootable enemy : game.enemies){
                    if(enemy.getName() != Text.ENEMY_SNIPER)
                        continue;
                    SniperEnemy se = (SniperEnemy) enemy;
                    if(!se.sniping)
                        continue;
                    if(x < enemy.getX()+sep && x > enemy.getX()-sep && y < enemy.getY()+sep && y > enemy.getY()-sep){
                        safe = false;
                        break;
                    }
                }
                if(safe)
                    snipe();
            }
            if(!sniping)
                aim = null;
        }

        return aim;
    }
    
    private void snipe(){
        moveSpeed = 0;
        sniping = true;
        clipWalk = null;
        if(!evolutions.isEmpty())
            shield = evolutions.peek();
        else
            shield = null;
        evolutions.clear();
    }
    
    private void stopSnipe(){
        moveSpeed = 4f;
        sniping = false;
        clipWalk = SoundManager.Instance.getClip("walking");
        SoundManager.Instance.setClipVolume(clipWalk, volumeWalk);
        if(shield != null)
            evolutions.add(shield);
    }
}