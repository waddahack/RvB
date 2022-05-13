package towser;

import java.util.ArrayList;

public class Game extends AppCore{
    
    public Game(String lvlName){
        super();
        initMap(lvlName);
    }
    
    public Game(ArrayList<Tile> path){
        super();
        initMap(path);
    }
}
