package towers;

import java.util.ArrayList;
import javax.sound.sampled.Clip;
import managers.SoundManager;
import org.newdawn.slick.opengl.Texture;
import towser.*;
import static towser.Towser.ref;

public class BigTower extends Tower{
    
    public static int startPrice = 500;
    public static int priceP = startPrice;
    private Clip explodeClip;
    
    private boolean exploding = false;
    private double checkTime = 0;
    private int nbSprite = 10;
    private ArrayList<Texture> sprites = new ArrayList<>();
    
    public BigTower() {
        super("bigTower");
        textures.add(Towser.textures.get("grass"));
        textures.add(Towser.textures.get("bigTowerBase"));
        textures.add(Towser.textures.get("bigTowerTurret"));
        rotateIndex = 2;
        textureStatic = Towser.textures.get("bigTower");
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*Towser.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Razkull";
        explode = true;
        explodeRadius = 3*Towser.unite/4;
        follow = false;
        isMultipleShot = false;
        volume = SoundManager.Volume.MEDIUM;
        clip = SoundManager.Instance.getClip("fatCannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        explodeClip = SoundManager.Instance.getClip("explode");
        SoundManager.Instance.setClipVolume(explodeClip, SoundManager.Volume.SEMI_LOW);
        bulletSprite = Towser.textures.get("bulletBlue");
        
        range = 7*Towser.unite/3;
        power = 20;
        shootRate = 0.2f;
        bulletSpeed = 8;
        upgrades.add(new Upgrade("Range", range, 1.2f, "Multiplicate", 120f, 1.5f, 3));
        upgrades.add(new Upgrade("Power", power, 1.6f, "Multiplicate", 350f, 1.6f, 2));
        upgrades.add(new Upgrade("Attack speed", shootRate, 1.3f, "Multiplicate", 200f, 1.5f, 3));
        upgrades.add(new Upgrade("Bullet speed", bulletSpeed, 10f, "Add", 150f, 1.8f, 2));
        upgrades.add(new Upgrade("Explode radius", explodeRadius, 2*Towser.unite/5, "Add", 200f, 2f, 2));
        
        growth = (12/5)*ref;
    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.1;
        price = priceP;
    }
    
    public void bombExplode(){
        SoundManager.Instance.playOnce(explodeClip);
        exploding = true;
    }
    
    @Override
    public void update(){
        super.update();
        if(exploding && System.currentTimeMillis()-checkTime >= 20){
            
            checkTime = System.currentTimeMillis();
        }
    }
}

