package ennemies;

import java.util.ArrayList;
import java.util.Collections;
import static rvb.RvB.game;
import static rvb.RvB.ref;


public class Wave{
    
    private int index;
    private ArrayList<Enemy> enemies;
    private double startTime;
    private double waitBetweenType = 900,  waitBetween = 450;
    
    public Wave(){
        enemies = new ArrayList<>();
        index = 0;
        startTime = System.currentTimeMillis();
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
        enemies.add(e);
    }
    
    public ArrayList<Enemy> getEnnemies(){
        return enemies;
    }
    
    public void shuffleEnemies(){
        Collections.shuffle(enemies);
    }
    
    public void update(){
        if(index == enemies.size())
            return;
        Enemy nextEnemy = enemies.get(index);
        Enemy previousEnemy = null;
        if(index > 0)
            previousEnemy = enemies.get(index-1);
        double time = System.currentTimeMillis();
        if((time - startTime >= waitBetweenType/game.gameSpeed && index < enemies.size()) || previousEnemy == null || (!previousEnemy.name.equals(nextEnemy.name) && time - startTime >= waitBetween/game.gameSpeed)){
            if(!nextEnemy.name.equals("Bazoo") || time - startTime >= 4000/game.gameSpeed){
                nextEnemy.setStarted(true);
                startTime = System.currentTimeMillis();
                index++;
            }
        }
    }
    
    public boolean isDone(){
        for(Enemy e : enemies)
            if(!e.isDead())
                return false;
        return true;
    }
}
