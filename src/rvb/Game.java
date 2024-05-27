package rvb;

import java.util.ArrayList;
import java.util.Random;
import managers.PopupManager;
import managers.TextManager.Text;
import rvb.RvB.Difficulty;
import static rvb.RvB.nbTileX;
import static rvb.RvB.nbTileY;
import static rvb.RvB.ref;
import ui.Button;
import ui.Overlay;

public class Game extends AppCore{
    
    public Game(String lvlPath, Difficulty diff){
        super();
        init(diff);
        initOverlays();
        initMap(lvlPath);
    }
    
    public Game(ArrayList<Tile> path, Difficulty diff){
        super();
        init(diff);
        initOverlays();
        initMap(path);
    }
    
    public Game(Difficulty diff){
        super();
        init(diff);
        initOverlays();
        
        int i = 0;
        ArrayList<Tile> path;
        do{
            System.out.println("generate map attempt : "+(++i));
            path = generateRandomPath(diff);
            if(path.size() > diff.getNbRoad()*1.2){
                System.out.println("failure : path too long");
                path.clear();
            }
        }while(path.isEmpty() && i < 100);
        if(path.isEmpty())
            PopupManager.Instance.popup(Text.ERROR.getText());
        
        initMap(path);
    }
    
    @Override
    public void initOverlays(){
        super.initOverlays();
        Button b = new Button(OvMain.getW()-(int)(500*ref), OvMain.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("questionMark"));
        b.setFunction(__ -> {
            PopupManager.Instance.help();
        });
        OvMain.addButton(b);
    }
    
    private static ArrayList<Tile> generateRandomPath(Difficulty diff){
        Random rand = new Random();
        ArrayList<ArrayList<Tile>> map = new ArrayList<>();
        ArrayList<Tile> row, path = new ArrayList<>(), neighbors = new ArrayList<>();
        int x, y, oppositeX, oppositeY, centerX = nbTileX/2, centerY = nbTileY/2;
        Tile road, previous;
        String dir;
        // nombre de road de la map restante
        int nbRoad =  diff.getNbRoad();
        int nbRoadLeft = nbRoad;
        // remplissage de la map par du vide
        for(int i = 0 ; i < nbTileY ; i++){
            row = new ArrayList<>();
            for(int j = 0 ; j < nbTileX ; j++)
                row.add(null);
            map.add(row);
        }
        // spawn random sur le bord gauche
        x = 0;
        y = 1+rand.nextInt(nbTileY-2);
        road = new Tile(RvB.textures.get("roadStraight"), "road", x*RvB.unite, y*RvB.unite);
        road.setRotateIndex(0);
        road.previousRoad = new Tile((x-1)*RvB.unite, y*RvB.unite);
        oppositeX = nbTileX-1-x;
        oppositeY = nbTileY-1-y;
        map.get(y).set(x, road);
        path.add(road);
        nbRoadLeft--;
        // Construction de la route
        int i = 0, r;
        int[][] check;
        Tile up, down, left, right, accross, temp;
        while(nbRoadLeft > 0 || path.get(i).getIndexX()+1 <= nbTileX-1){ // Tant qu'il n'est pas à côté du bord droit
            neighbors.clear();
            up = new Tile(x, y-1);
            down = new Tile(x, y+1);
            left = new Tile(x-1, y);
            right = new Tile(x+1, y);
            accross = null;
            previous = path.get(i).previousRoad;
            previous.setDirectionWithPos();
            // ajout des positions voisines si c'est ni un bord ni une road
            if(y-1 >= 1 && map.get(y-1).get(x) == null)
                neighbors.add(up);
            if(y+1 <= nbTileY-2 && map.get(y+1).get(x) == null)
                neighbors.add(down);
            if(x-1 >= 0 && map.get(y).get(x-1) == null)
                neighbors.add(left);
            if(x+1 <= nbTileX-1 && map.get(y).get(x+1) == null)
                neighbors.add(right);
            // remove les voisins qui nous font entrer dans une boucle
            // (on remove ceux qui vont dans le même sens que la tuile sur laquelle on s'est cogné, en checkant les 3 tuiles en face)
            if(neighbors.size() > 1 && previous.getDirection() != null){
                switch (previous.getDirection()) {
                    case "up":
                        accross = up;
                        check = new int[][]{{x-1, y-1}, {x, y-1}, {x+1, y-1}};
                        break;
                    case "down":
                        accross = down;
                        check = new int[][]{{x-1, y+1}, {x, y+1}, {x+1, y+1}};
                        break;
                    case "left":
                        accross = left;
                        check = new int[][]{{x-1, y-1}, {x-1, y}, {x-1, y+1}};
                        break;
                    default:
                        accross = right;
                        check = new int[][]{{x+1, y-1}, {x+1, y}, {x+1, y+1}};
                        break;
                }
                for(int[] toCheck : check){
                    // Quand on su heurte à un bord
                    if(toCheck[0] < 0 || toCheck[0] > nbTileX-1 || toCheck[1] < 1 || toCheck[1] > nbTileY-2){ //Si c'est un bord
                        if(toCheck[0] != x && toCheck[1] != y) // Si ce n'est pas la tuile d'en face
                            continue;
                        neighbors.clear();
                        if(toCheck[0] < 0)
                            neighbors.add(path.get(0).getY() > y ? down : up);
                        else if(toCheck[0] > nbTileX-1)
                            neighbors.add(up); // Risque de se mordre la queue si en haut il y a plus bcp de place
                        else
                            neighbors.add(right);
                        break;
                    }    
                    // Quand on se heurte à une route
                    temp = map.get(toCheck[1]).get(toCheck[0]);
                    if(temp != null){
                        temp.setDirectionWithPos();
                        dir = temp.getDirection();
                        if(dir.equals(previous.getDirection()))
                            dir = temp.previousRoad.getDirection();
                        if(dir == null)
                            dir = "right"; // previous of spawn
                        switch (dir) {
                            case "up":
                                neighbors.remove(up);
                                break;
                            case "down":
                                neighbors.remove(down);
                                break;
                            case "left":
                                neighbors.remove(left);
                                break;
                            case "right":
                                neighbors.remove(right);
                                break;
                        }
                        if(neighbors.contains(accross) && (dir == "left" && toCheck[0] > x || dir == "right" && toCheck[0] < x || dir == "up" && toCheck[1] > y || dir == "down" && toCheck[1] < y))
                            neighbors.remove(accross);
                        break;
                    }
                }
            }
            
            if(neighbors.isEmpty()){
                System.out.println("failure : path eating itself");// Parce que quand la road se heurte au bord droit, on ne regarde pas la place qu'il y a en haut et en bas, tjrs en haut. Donc quand y'a pas bcp de place en haut ça se mord forcément
                path.clear();
                break;
            }
            
            if(nbRoadLeft <= 0 && neighbors.contains(right)){
                x = (int) right.getX();
                y = (int) right.getY();
            }
            else if(nbRoadLeft > 0 || map.get(y).get(x) != null){ // On continue le pathing
                r = rand.nextInt(2+diff.probabilityRange); // proba tout droit
                if(r != 0 && neighbors.contains(accross)){ // tout droit
                    x = (int) accross.getX();
                    y = (int) accross.getY();
                }
                else{ // tourne
                    if(neighbors.size() > 1 && neighbors.contains(accross))
                        neighbors.remove(accross);
                    if(neighbors.size() == 1){
                        x = (int) neighbors.get(0).getX();
                        y = (int) neighbors.get(0).getY();
                    }
                    else{ // ajoute de la proba pour le virage qui va en direction du centre de la map
                        int aimX = oppositeX, aimY = oppositeY;
                        if(nbRoadLeft <= nbRoad/2){
                            aimX = centerX;
                            aimY = centerY;
                        }
                        if((aimX > neighbors.get(1).getX() && neighbors.get(1).getX()-previous.getX() > 0) || (aimX < neighbors.get(1).getX() && neighbors.get(1).getX()-previous.getX() < 0) || (aimY > neighbors.get(1).getY() && neighbors.get(1).getY()-previous.getY() > 0) || (aimY < neighbors.get(1).getY() && neighbors.get(1).getY()-previous.getY() < 0)){
                            temp = neighbors.get(0);
                            neighbors.set(0, neighbors.get(1));
                            neighbors.set(1, temp);
                        }
                        r = rand.nextInt(3+diff.probabilityRange); // proba direction vers le centre
                        if(r > 0){
                            x = (int) neighbors.get(0).getX();
                            y = (int) neighbors.get(0).getY();
                        }
                        else{
                            x = (int) neighbors.get(1).getX();
                            y = (int) neighbors.get(1).getY();
                        }
                    }    
                }
            }

            // Ajout de la route
            road = new Tile(RvB.textures.get("roadStraight"), "road", x*RvB.unite, y*RvB.unite);
            road.setRotateIndex(0);
            road.setPreviousRoad(path.get(i));
            path.get(i).setNextRoad(road);

            map.get(y).set(x, road);
            path.add(road);
            nbRoadLeft--;
            i++;
        }

        return path;
    }
    
    public Text calculatePath(Difficulty diff){
        Text error = Text.PATH_NOT_VALID;
        if(path.size() == 0)
            return error;
        ArrayList<Tile> roads = (ArrayList<Tile>) path.clone();
        spawn = null;
        base = null;
        for(Tile road : roads){
            if(road.arrowAngle == -1)
                return error;
            if(road.previousRoad != null && road.previousRoad.type == "nothing"){
                if(spawn == null){
                    if(!(road.getIndexX() == 0 || road.getIndexX() == nbTileX-1))
                        return error;
                    spawn = road;
                }
                else
                    return error;
            } 
            if(road.nextRoad != null && road.nextRoad.type == "nothing"){
                if(!(road.getIndexX() == 0 || road.getIndexX() == nbTileX-1))
                    return error;
                base = road;
            } 
        }
        if(spawn == null || base == null)
            return error;
        Tile road = spawn;
        int n = 0;
        while(road != null){
            road = road.nextRoad;
            n++;
        }
        if(n < roads.size())
            return error;
        if(roads.size() > diff.getNbRoad())
            return Text.PATH_TOO_LONG;
        return null;
    }
}
