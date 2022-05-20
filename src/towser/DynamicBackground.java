package towser;

import ennemies.BasicEnemy;
import ennemies.Enemy;
import java.util.ArrayList;

public class DynamicBackground extends AppCore{
    /**
     * NOT USED ATM
     */
    public DynamicBackground(){
        super();
        initMap("background");
    }
    
    @Override
    protected void init(){
        enemies = new ArrayList<>();
        ennemiesDead = new ArrayList<>();
        gameSpeed = 1;
    }
    
    @Override
    public void update(){
        render();
        
        // Update and render enemies
        for(int i = enemies.size()-1 ; i >= 0 ; i--)
            enemies.get(i).update();
        for(Enemy e : enemies)
            e.render();
        // Remove dead enemies
        for(int i = 0 ; i < ennemiesDead.size() ; i++)
            enemies.remove(ennemiesDead.get(i));
        ennemiesDead.clear();
    }
    
    public void addRandomEnemy(){
        Enemy enemy = new BasicEnemy();
        //enemy.setSilent(true);
        enemies.add(enemy);
        enemy.setStarted(true);
    }
    
    @Override
    public void getAttackedBy(int p){
        // Cancel enemies' damage
    }
}
