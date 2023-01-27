package towers;

import java.util.ArrayList;
import rvb.RvB;
import managers.SoundManager;
import managers.TextManager.Text;
import org.lwjgl.input.Mouse;
import static rvb.RvB.game;
import static rvb.RvB.unite;
import rvb.Tile;

public class Raztech extends Tower{
    
    public static int startPrice = 100;
    public static int priceP = startPrice;
    
    public int lvl = 1;
    private boolean right = true;
    
    public Raztech() {
        super("raztech");
        textures.add(RvB.textures.get("raztech"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("raztech");
        if(game.raztech != null)
            textureStatic = RvB.textures.get("place");
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*RvB.unite/5;
        hitboxWidth = width;
        size = width;
        totalMoneySpent = priceP;
        name = Text.TOWER_BASIC.getText();
        explode = false;
        follow = false;
        isMultipleShot = false;
        clip = SoundManager.Instance.getClip("gun");
        volume = SoundManager.Volume.LOW;
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = RvB.textures.get("gun_bullet");
        
        range = 4*RvB.unite;
        power = 6;
        shootRate = 2f;
        bulletSpeed = 20;
        growth = 0;
    }
    
    @Override
    public void shoot(){
        enemiesTouched.clear();
        lastShoot = System.currentTimeMillis();
        float x = (float)(this.x+size/2*Math.cos(Math.toRadians(angle)));
        float y = (float)(this.y+size/2*Math.sin(Math.toRadians(angle)));
        if(right){ 
            x -= size/4*Math.sin(Math.toRadians(angle)); 
            y += +size/4*Math.cos(Math.toRadians(angle));
        }
        else{
            x += size/4*Math.sin(Math.toRadians(angle)); 
            y -= +size/4*Math.cos(Math.toRadians(angle));
        }
        right = !right;
        x = Math.abs(x) < 2 ? 0 : x;
        y = Math.abs(y) < 2 ? 0 : y;
        Bullet bullet = new Bullet(this, x, y, enemyAimed, size/4 + bulletSizeBonus, bulletSprite, false);
        bullets.add(bullet);
        if(clip != null){
            if(continuousSound){
                if(!soundPlayed){
                    SoundManager.Instance.playLoop(clip);
                    soundPlayed = true;
                }
            }
            else
                SoundManager.Instance.playOnce(clip);
        }
    }
    
    @Override
    public void place(ArrayList<ArrayList<Tile>> map){
        if(!game.raztech.isPlaced){
            initOverlay();
            game.getOverlays().get(0).getButtons().get(game.getOverlays().get(0).getButtons().size()-1).setBG(RvB.textures.get("placeRaztech"));
        }
        else{
            game.raztech.x = (game.raztech.x-unite/2)/unite;
            game.raztech.y = (game.raztech.y-unite/2)/unite;
            map.get((int) game.raztech.y).set((int) game.raztech.x, new Tile("grass"));
            game.towersDestroyed.add(this);
            game.towerSelected = null;
        }   
        game.raztech.x = Math.floorDiv(Mouse.getX(), unite);
        game.raztech.y = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite);
        map.get((int) game.raztech.y).set((int) game.raztech.x, null);
        game.raztech.x = game.raztech.x*unite+unite/2;
        game.raztech.y = game.raztech.y*unite+unite/2;
        game.raztech.isPlaced = true;
        
        game.money -= price;
        raisePrice();
        
        if(Math.random() < 0.5)
            SoundManager.Instance.playOnce(SoundManager.SOUND_RAZTECH1);
        else
            SoundManager.Instance.playOnce(SoundManager.SOUND_RAZTECH2);
    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.2;
        price = priceP;
    }
}
