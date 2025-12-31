package ennemies;

import static ennemies.Enemy.Type.SNIPER;
import java.util.ArrayList;
import java.util.Random;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import rvb.Shootable;
import towers.Tower;


public class SniperEnemy extends Enemy{
    
    public static int balance = 50;
    private Evolution shield;
    private int snipingIndex;
    private float waitBeforeAttack, startTimeWaitBeforeAttack;
    
    public SniperEnemy(){
        super(SNIPER);
        textures.add(RvB.textures.get("sniperEnemy"));
        texturesBright.add(RvB.textures.get("sniperEnemyBright"));
        rotateIndex = 0;
        rotateIndexShoot = 0;
        textureStatic = RvB.textures.get("sniperEnemy");
        name = Text.ENEMY_SNIPER;
        reward = 3;
        commitPower = 2f;
        
        canShoot = true;
        follow = true;
        power = 3f;
        bulletSprite = RvB.textures.get("roundBullet");
        bulletSizeBonus = (int) (-7*ref);
        clip = SoundManager.Instance.getClip("sniper");
        SoundManager.Instance.setClipVolume(clip, volume);
        snipingIndex = -1;
        waitBeforeAttack = 5000f;
        startTimeWaitBeforeAttack = 0f;
        bulletSpeed = 30;
        shootRate = 0.5f;
        range = 10*RvB.unite;
        focusIndex = 2;

        moveSpeed = 4.5f;
        life = 30f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        volumeWalk = SoundManager.Volume.VERY_LOW;
        clipWalk = SoundManager.Instance.getClip("quad_elec");
        stepEveryMilli = 0;
        eBalance = balance;
        
        shield = new Evolution(this, 150, RvB.textures.get("sniperEnemyVehicle"), RvB.textures.get("sniperEnemyVehicleBright"), RvB.colors.get("life1"), null);
        evolutions.add(shield);
        
        initBack();
    }
    
    @Override
    public void move(){
        if((enemyAimed == null && snipingIndex > 0) || snipingIndex == 3)
            stopSnipe();
        if(shield != null && shield.life <= 0){
            moveSpeed *= 0.6f;
            clipWalk.close();
            clipWalk = SoundManager.Instance.getClip("walking");
            SoundManager.Instance.setClipVolume(clipWalk, volumeWalk);
            stepEveryMilli = 800;
            shield = null;
        }
        super.move();
    }
    
    @Override
    protected Shootable searchForStrongest(ArrayList<Shootable> enemies){
        // Pareil que dans shootable sauf qu'il regarde le prix investi dans la tourelle plutot que ses pv max
        Tower aim = null;
        Tower enemyTower;
        for(int i = 0 ; i < enemies.size() ; i++){
            enemyTower = (Tower)enemies.get(i);
            if(enemyTower.hasStarted() && enemyTower.isInRangeOf(this)){
                if(aim == null || enemyTower.totalMoneySpent > aim.totalMoneySpent)
                    aim = enemyTower;
            }
        }
        if(aim != null && aim.life <= 0)
            aim = null; 
        
        if(aim != null){
            boolean safe = true;
            if(moveSpeed > 0){
                for(Shootable tower : game.towers){
                    Tower t = (Tower) tower;
                    if(!t.isPlaced())
                        continue;
                    if(tower.canShoot && this.isInRangeOf(tower)){
                        safe = false;
                        break;
                    }
                }
            }
            if(safe && snipingIndex < 0){
                // Check si un autre Znooper est à côté. Ne pas s'arrêter le cas échéant
                int sep = (int)(20*ref);
                for(Shootable enemy : game.enemies){
                    if(enemy.getName() != Text.ENEMY_SNIPER)
                        continue;
                    SniperEnemy se = (SniperEnemy) enemy;
                    if(se.snipingIndex < 0)
                        continue;
                    if(x < enemy.getX()+sep && x > enemy.getX()-sep && y < enemy.getY()+sep && y > enemy.getY()-sep){
                        safe = false;
                        break;
                    }
                }
                if(safe && game.timeInGamePassed - startTimeWaitBeforeAttack >= waitBeforeAttack)
                    snipe();
            }
            if(snipingIndex < 0)
                aim = null;
        }

        return aim;
    }
    
    @Override
    public void attack(Shootable shootable){
        if(!shootable.hasStarted())
            return;
        snipingIndex++;
        super.attack(shootable);
    }
    
    private void snipe(){
        oldMoveSpeed = moveSpeed;
        moveSpeed = 0;
        snipingIndex = 0;
        clipWalk.close();
        
        if(!evolutions.isEmpty())
            shield = evolutions.peek();
        else
            shield = null;
        evolutions.clear();
        
        textures.clear();
        texturesBright.clear();
        textures.add(RvB.textures.get("sniperEnemySniping"));
        texturesBright.add(RvB.textures.get("sniperEnemySnipingBright"));
        textureStatic = RvB.textures.get("sniperEnemySniping");
    }
    
    private void stopSnipe(){
        moveSpeed = oldMoveSpeed;
        snipingIndex = -1;
        startTimeWaitBeforeAttack = game.timeInGamePassed;
        if(shield != null){
            clipWalk.close();
            clipWalk = SoundManager.Instance.getClip("quad_elec");
            SoundManager.Instance.setClipVolume(clipWalk, volumeWalk);
            SoundManager.Instance.playLoop(clipWalk);
        }
        else{
            clipWalk.close();
            clipWalk = SoundManager.Instance.getClip("walking");
            SoundManager.Instance.setClipVolume(clipWalk, volumeWalk);
        }
        
        if(shield != null)
            evolutions.add(shield);
        
        textures.clear();
        texturesBright.clear();
        textures.add(RvB.textures.get("sniperEnemy"));
        texturesBright.add(RvB.textures.get("sniperEnemyBright"));
        textureStatic = RvB.textures.get("sniperEnemy");
    }
}