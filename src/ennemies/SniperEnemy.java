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
    private boolean sniping;
    private Random rand = new Random();
    private int snipingTile;
    
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
        sniping = false;
        follow = true;
        power = 4f;
        bulletSprite = RvB.textures.get("roundBullet");
        bulletSizeBonus = (int) (-7*ref);
        clip = SoundManager.Instance.getClip("sniper");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSpeed = 30;
        shootRate = 0.5f;
        range = 10*RvB.unite;
        focusIndex = 2;

        moveSpeed = 4.5f;
        life = 40f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        volumeWalk = SoundManager.Volume.VERY_LOW;
        clipWalk = SoundManager.Instance.getClip("quad_elec");
        stepEveryMilli = 0;
        eBalance = balance;
        
        shield = new Evolution(this, 160, RvB.textures.get("sniperEnemyVehicle"), RvB.textures.get("sniperEnemyVehicleBright"), RvB.colors.get("life1"), null);
        evolutions.add(shield);
        
        initBack();
    }
    
    @Override
    public void setStarted(boolean v){
        super.setStarted(v);
        snipingTile = rand.nextInt(4, game.path.size()-6);
    }
    
    @Override
    public void move(){
        if(enemyAimed == null && sniping)
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
        if(aim != null && getIndiceTuile() >= snipingTile){
            // Check si un autre Znooper est à côté. Ne pas s'arrêter le cas échéant
            boolean safe = true;
            if(safe && !sniping){
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
        }
        if(!sniping)
            aim = null;
        // Pour snipe que quand il est safe (trop dure, activer en version hardcore ?)
        /*if(aim != null){
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
        }*/

        return aim;
    }
    
    private void snipe(){
        oldMoveSpeed = moveSpeed;
        moveSpeed = 0;
        sniping = true;
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
        sniping = false;
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