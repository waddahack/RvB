package ennemies;

import java.util.ArrayList;
import java.util.Collections;
import static towser.Towser.game;
import static towser.Towser.ref;


public class Wave{
    
    private int index;
    private ArrayList<Enemy> enemies;
    private double startTime;
    private double waitBetween = 250;
    
    public Wave(){
        enemies = new ArrayList<>();
        index = 0;
        startTime = System.currentTimeMillis();
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
        if((System.currentTimeMillis() - startTime >= 1000*nextEnemy.getSpawnSpeed()*ref/game.gameSpeed && index < enemies.size()) || previousEnemy == null || (!previousEnemy.name.equals(nextEnemy.name) && System.currentTimeMillis() - startTime >= waitBetween)){
            nextEnemy.setStarted(true);
            startTime = System.currentTimeMillis();
            index++;
        }
    }
    
    public boolean isDone(){
        for(Enemy e : enemies)
            if(!e.isDead())
                return false;
        return true;
    }
}
