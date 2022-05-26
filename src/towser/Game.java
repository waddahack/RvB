package towser;

import java.util.ArrayList;
import java.util.Random;
import managers.PopupManager;
import towser.Towser.Difficulty;
import static towser.Towser.nbTileX;
import static towser.Towser.nbTileY;

public class Game extends AppCore{
    
    public Game(String lvlName){
        init(Difficulty.MEDIUM);
        initOverlays();
        initMap(lvlName);
    }
    
    public Game(Difficulty diff){
        init(diff);
        initOverlays();
        
        ArrayList<Tile> path = generateRandomPath(diff);
        initMap(path);
    }
    
    private static ArrayList<Tile> generateRandomPath(Difficulty diff){
        Random rand = new Random();
        ArrayList<ArrayList<Tile>> map = new ArrayList<>();
        ArrayList<Tile> row, path = new ArrayList<>(), neighbors = new ArrayList<>();
        int x, y, aimX = (nbTileX-1)/2, aimY = (nbTileY-1)/2;
        Tile road, previous;
        String dir, dirToCount;
        // nombre de road de la map restante
        int nbRoadLeft = diff.getNbRoad();
        // remplissage de la map par du vide
        for(int i = 0 ; i < nbTileY ; i++){
            row = new ArrayList<>();
            for(int j = 0 ; j < nbTileX ; j++)
                row.add(null);
            map.add(row);
        }
        // spawn random sur un bord
        x = rand.nextInt(2);
        if(x == 0){
            x = rand.nextInt(nbTileX);
            y = rand.nextInt(2)*(nbTileY-1);
        }
        else{
            x = rand.nextInt(2)*(nbTileX-1);
            y = rand.nextInt(nbTileY);
        }
        road = new Tile(x, y);
        if(x == 0)
            road.previousRoad = new Tile(x-1, y);
        else if(x == nbTileX-1)
            road.previousRoad = new Tile(x+1, y);
        else if(y == 0)
            road.previousRoad = new Tile(x, y-1);
        else
            road.previousRoad = new Tile(x, y+1);
        map.get(y).set(x, road);
        path.add(road);
        nbRoadLeft--;
        // Construction de la route
        int i = 0, r;
        int[][] check;
        int[] dirToSide;
        Tile up, down, left, right, accross, temp, lastOnSide = path.get(0);
        boolean changeLastOnSide = false;
        while(nbRoadLeft > 0 || path.get(i).getX()-1 >= 0 && path.get(i).getX()+1 <= nbTileX-1 && path.get(i).getY()-1 >= 0 && path.get(i).getY()+1 <= nbTileY-1){ // Tant qu'il n'est pas à côté d'un bord
            neighbors.clear();
            up = new Tile(x, y-1);
            down = new Tile(x, y+1);
            left = new Tile(x-1, y);
            right = new Tile(x+1, y);
            accross = null;
            previous = path.get(i).previousRoad;
            previous.setDirectionWithPos();
            // ajout des positions voisines si c'est ni un bord ni une road
            if(y-1 >= 0 && map.get(y-1).get(x) == null)
                neighbors.add(up);
            if(y+1 <= nbTileY-1 && map.get(y+1).get(x) == null)
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
                    if(toCheck[0] < 0 || toCheck[0] > nbTileX-1 || toCheck[1] < 0 || toCheck[1] > nbTileY-1){ //Si c'est un bord
                        if(toCheck[0] != x && toCheck[1] != y) // Si ce n'est pas la tuile d'en face
                            continue;
                        if(toCheck[0] < 0)
                            dir = "left";
                        else if(toCheck[0] > nbTileX-1)
                            dir = "right";
                        else if(toCheck[1] < 0)
                            dir = "up";
                        else
                            dir = "down";
                        dirToCount = directionToCount(lastOnSide, new int[]{x, y}, dir);
                        changeLastOnSide = true;
                        boolean stop = false;
                        int iX, iY;
                        int nbTileLockedUp = 0;
                        for(int k = path.indexOf(lastOnSide) ; k < path.size() ; k++){
                            if(path.get(k).getDirection() != dir)
                                continue;
                            iX = (int) path.get(k).getX();
                            iY = (int) path.get(k).getY();
                            while(!stop){
                                switch(dirToCount){
                                    case "up":
                                        iY--;
                                        break;
                                    case "down":
                                        iY++;
                                        break;
                                    case "left":
                                        iX--;
                                        break;
                                    default:
                                        iX++;
                                        break;
                                }
                                if(iY < 0 || iY > nbTileY-1 || iX < 0 || iX > nbTileX-1) // Si en dehors de la map
                                    stop = true;
                                else if(map.get(iY).get(iX) != null) // Si la tuile est une route
                                    stop = true;
                                else
                                    nbTileLockedUp++;
                            }
                            stop = false;
                        }
                        boolean insideBigger = nbTileLockedUp > (nbTileX*nbTileY - path.size()+1)/2;
                        switch(dirToCount){
                            case "up":
                                if(insideBigger && neighbors.contains(down))
                                    neighbors.remove(down);
                                else if(neighbors.contains(up))
                                    neighbors.remove(up);
                                break;
                            case "down":
                                if(insideBigger && neighbors.contains(up))
                                    neighbors.remove(up);
                                else if(neighbors.contains(down))
                                    neighbors.remove(down);
                                break;
                            case "left":
                                if(insideBigger && neighbors.contains(right))
                                    neighbors.remove(right);
                                else if(neighbors.contains(left))
                                    neighbors.remove(left);
                                break;
                            default:
                                if(insideBigger && neighbors.contains(left))
                                    neighbors.remove(left);
                                else if(neighbors.contains(right))
                                    neighbors.remove(right);
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
                            dir = "previous of spawn doesnt have direction";
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
                            default:
                                neighbors.remove(right);
                                break;
                        }
                        if(neighbors.size() > 1 && toCheck[0] != x && toCheck[1] != y && neighbors.contains(accross))// Si la tuile d'en face n'est pas la seule option et est libre, faut la remove
                            neighbors.remove(accross);
                        break;
                    }
                }
            }
            
            if(neighbors.isEmpty()){
                System.out.println("failure");
                PopupManager.Instance.popup("Error. Try again.");
                path.clear();
                break;
            }
            
            if(nbRoadLeft <= 0){ // On fait terminer path sur un bord
                dirToSide = directionToSide(path, map);
                if(dirToSide == null){
                    System.out.println("failure");
                    PopupManager.Instance.popup("Error. Try again.");
                    path.clear();
                    break;
                }
                x = dirToSide[0];
                y = dirToSide[1];
            }
            
            if(nbRoadLeft > 0 || map.get(y).get(x) != null){ // On continue le pathing
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

            if(changeLastOnSide){
                lastOnSide = path.get(i);
                changeLastOnSide = false;
            }
        }
        
        return path;
    }
    
    private static String directionToCount(Tile firstTile, int[] lastTilePos, String dir){
        if(dir.equals("left") || dir.equals("right")){
            if(firstTile.getY() > lastTilePos[1])
                return "down";
            else
                return "up";
        }
        else{
            if(firstTile.getX() > lastTilePos[0])
                return "right";
            else
                return "left";
        }
    } 
    
    private static int[] directionToSide(ArrayList<Tile> path, ArrayList<ArrayList<Tile>> map){
        int[] dir;
        Tile lastTile = path.get(path.size()-1), previousTile = path.get(path.size()-2);
        previousTile.setDirectionWithPos();
        String tileDir = previousTile.getDirection();
        String tileAccrossDir = "";
        int up = 0, down = 0, left = 0, right = 0, x, y;
        boolean stop = false;
        x = (int) lastTile.getX();
        y = (int) lastTile.getY();
        while(!stop){ // up
            y--;
            if(y < 0)
                stop = true;
            else if(map.get(y).get(x) != null){
                stop = true;
                up = 1000;
                if(tileDir == "up"){
                    map.get(y).get(x).setDirectionWithPos();
                    tileAccrossDir = map.get(y).get(x).getDirection();
                }
            }
            else
                up++;
        }
        y = (int) lastTile.getY();
        stop = false;
        while(!stop){ // down
            y++;
            if(y > nbTileY-1)
                stop = true;
            else if(map.get(y).get(x) != null){
                stop = true;
                down = 1000;
                if(tileDir == "down"){
                    map.get(y).get(x).setDirectionWithPos();
                    tileAccrossDir = map.get(y).get(x).getDirection();
                }
            }
            else
                down++;
        }
        y = (int) lastTile.getY();
        stop = false;
        while(!stop){ // left
            x--;
            if(x < 0)
                stop = true;
            else if(map.get(y).get(x) != null){
                stop = true;
                left = 1000;
                if(tileDir == "left"){
                    map.get(y).get(x).setDirectionWithPos();
                    tileAccrossDir = map.get(y).get(x).getDirection();
                }
            }
            else
                left++;
        }
        x = (int) lastTile.getX();
        stop = false;
        while(!stop){ // right
            x++;
            if(x > nbTileX-1)
                stop = true;
            else if(map.get(y).get(x) != null){
                stop = true;
                right = 1000;
                if(tileDir == "right"){
                    map.get(y).get(x).setDirectionWithPos();
                    tileAccrossDir = map.get(y).get(x).getDirection();
                }
            }
            else
                right++;
        }
        
        int min = Math.min(Math.min(up, down), Math.min(left, right));
        x = (int) lastTile.getX();
        y = (int) lastTile.getY();
        if(min == 1000){
            switch(tileAccrossDir){ // go opposé à tileAccrossDir
                case "up":
                    y++;
                    break;
                case "down":
                    y--;
                    break;
                case "left":
                    x++;
                    break;
                default:
                    x--;
                    break;
            }
            if(map.get(y).get(x) != null){
                x = (int) lastTile.getX();
                y = (int) lastTile.getY();
                switch(tileDir){
                    case "up":
                        y--;
                        break;
                    case "down":
                        y++;
                        break;
                    case "left":
                        x--;
                        break;
                    default:
                        x++;
                        break;
                }
            }
        }
        else if(min == up)
            y--;
        else if(min == down)
            y++;
        else if(min == left)
            x--;
        else if(min == right)
            x++;
        else
            return null;
        
        dir = new int[]{x, y};
        return dir;
    }
}
