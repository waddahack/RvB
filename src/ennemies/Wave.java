package ennemies;

import java.util.ArrayList;
import towser.Game;
import static towser.Towser.game;


public class Wave{
    
    private int index;
    private ArrayList<Enemy> enemies;
    private double startTime;
    
    public Wave(){
        enemies = new ArrayList<Enemy>();
        index = 0;
        startTime = System.currentTimeMillis();
    }
    
    public void addEnemy(Enemy e){
        enemies.add(e);
    }
    
    public ArrayList<Enemy> getEnnemies(){
        return enemies;
    }
    
    public void update(){
        if(index == enemies.size())
            return;
        Enemy nextEnemy = enemies.get(index);
        if(System.currentTimeMillis() - startTime >= 1000*nextEnemy.getSpawnSpeed()/game.gameSpeed && index < enemies.size() || index == 0){
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
