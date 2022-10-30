package rvb;

import java.util.ArrayList;
import java.util.Random;
import managers.PopupManager;
import managers.TextManager.Text;
import rvb.RvB.Difficulty;
import static rvb.RvB.nbTileX;
import static rvb.RvB.nbTileY;

public class Game extends AppCore{
    
    public Game(String lvlName, Difficulty diff){
        init(diff);
        initOverlays();
        initMap(lvlName);
    }
    
    public Game(Difficulty diff){
        init(diff);
        initOverlays();
        
        int i = -1;
        ArrayList<Tile> path;
        do{
            System.out.println("generate map attempt : "+(++i));
            path = generateRandomPath(diff);
            if(path.size() > diff.getNbRoad()*1.2){
                System.out.println("failure : path too long");
                path.clear();
            }
        }while(path.isEmpty() && i < 10);
        if(path.isEmpty())
            PopupManager.Instance.popup(Text.SECRET_REVEAL.getLines(), Text.WHAT.getText());
        
        initMap(path);
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
        road = new Tile(x, y);
        road.previousRoad = new Tile(x-1, y);
        oppositeX = nbTileX-1-x;
        oppositeY = nbTileY-1-y;
        map.get(y).set(x, road);
        path.add(road);
        nbRoadLeft--;
        // Construction de la route
        int i = 0, r;
        int[][] check;
        Tile up, down, left, right, accross, temp;
        while(nbRoadLeft > 0 || path.get(i).getX()+1 <= nbTileX-1){ // Tant qu'il n'est pas à côté d'un bord
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
            // remove les voisins qui nous fait entrer dans une boucle
            // (on remove ceux qui vont dans le même sens quela tuile sur laquelle on s'est cogné, en checkant les 3 tuiles en face)
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
                        if(toCheck[0] < 0)
                            dir = "left";
                        else if(toCheck[0] > nbTileX-1)
                            dir = "right";
                        else if(toCheck[1] < 1)
                            dir = "up";
                        else
                            dir = "down";
                        
                        neighbors.clear();
                        switch(dir){
                            case "right":
                                neighbors.add(up); // Risque de se mordre la queue si en haut il y a plus bcp de place
                                break;
                            case "left":
                                neighbors.add(path.get(0).getY() > y ? down : up);
                                break;
                            default:
                                neighbors.add(right);
                                break;
                        }
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
                        if(neighbors.contains(accross) && (dir == "left" && toCheck[0] > x || dir == "right" && toCheck[0] < x))
                            neighbors.remove(accross);
                        break;
                    }
                }
            }
            
            if(neighbors.isEmpty()){
                System.out.println("failure : path eating itself");
                path.clear();
                break;
            }
            
            if(nbRoadLeft <= 0 && neighbors.contains(right)){
                x = (int) right.getX();
                y = (int) right.getY();
            }
            else if(nbRoadLeft > 0 || map.get(y).get(x) != null){ // On continue le pathing
                r = rand.nextInt(2+diff.value); // proba tout droit
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
                        r = rand.nextInt(3+diff.value); // proba direction vers le centre
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
            road = new Tile(x, y);
            road.setPreviousRoad(path.get(i));
            path.get(i).setNextRoad(road);

            map.get(y).set(x, road);
            path.add(road);
            nbRoadLeft--;
            i++;
        }

        return path;
    }
}
