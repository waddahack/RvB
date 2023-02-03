package ennemies;

import java.util.ArrayList;
import java.util.Collections;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import rvb.Shootable;


public class Wave{
    
    private int index;
    private ArrayList<Shootable> enemies;
    private int startTime;
    private int waitBetweenType = 900,  waitBetween = 450;
    
    public Wave(){
        enemies = new ArrayList<>();
        index = 0;
        startTime = game.timeInGamePassed;
        waitBetweenType -= game.waveNumber*10;
        if(waitBetweenType < 250)
            waitBetweenType = 250;
        waitBetween -= game.waveNumber*10;
        if(waitBetween < 250)
            waitBetween = 250;
        waitBetweenType *= ref;
        waitBetween *= ref;
    }
    
    public void addEnemy(Enemy e){
        e.addBonusLife(game.enemiesBonusLife);
        e.addBonusMS(game.enemiesBonusMS);
        enemies.add(e);
    }
    
    public ArrayList<Shootable> getEnnemies(){
        return enemies;
    }
    
    public void shuffleEnemies(){
        Collections.shuffle(enemies);
    }
    
    public void update(){
        if(index == enemies.size() || game.gameSpeed == 0)
            return;
        Shootable nextEnemy = enemies.get(index);
        Shootable previousEnemy = null;
        if(index > 0)
            previousEnemy = enemies.get(index-1);
        double time = game.timeInGamePassed;
        if((time - startTime >= waitBetweenType && index < enemies.size()) || previousEnemy == null || (!previousEnemy.name.equals(nextEnemy.name) && time - startTime >= waitBetween)){
            if(!nextEnemy.name.equals("Bazoo") || time - startTime >= 2000){
                nextEnemy.setStarted(true);
                startTime = game.timeInGamePassed;
                index++;
            }
        }
    }
    
    public boolean isDone(){
        for(Shootable e : enemies)
            if(!e.isDead())
                return false;
        return true;
    }
}
