package towers;

import rvb.RvB;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.Clip;
import managers.SoundManager;
import org.newdawn.slick.opengl.Texture;
import static rvb.RvB.ref;

public class BigTower extends Tower{
    
    public static int startPrice = 450;
    public static int priceP = startPrice;
    private Clip explodeClip;
    
    private float explodeX, explodeY;
    private double checkTime = 0;
    private final Texture[] sprites = {RvB.textures.get("flame")};
    private static Random rand = new Random();
    private int nbFlames, count;
    private ArrayList<Integer> flamesX = new ArrayList<>(), flamesY = new ArrayList<>();
    
    public BigTower() {
        super("bigTower");
        textures.add(RvB.textures.get("bigTowerBase"));
        textures.add(RvB.textures.get("bigTowerTurret"));
        rotateIndex = 1;
        textureStatic = RvB.textures.get("bigTower");
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*RvB.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Razkull";
        explode = true;
        follow = false;
        isMultipleShot = false;
        volume = SoundManager.Volume.MEDIUM;
        clip = SoundManager.Instance.getClip("fatCannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        explodeClip = SoundManager.Instance.getClip("explode");
        SoundManager.Instance.setClipVolume(explodeClip, SoundManager.Volume.SEMI_LOW);
        bulletSprite = RvB.textures.get("shell");
        bulletSizeBonus = 10;
        
        range = 7*RvB.unite/3;
        power = 20;
        shootRate = 0.2f;
        bulletSpeed = 8;
        explodeRadius = 3*RvB.unite/4;
        upgrades.add(new Upgrade("Range", range, 1.2f, "*", 120f, 1.5f, 3));
        upgrades.add(new Upgrade("Power", power, 1.5f, "*", 275f, 1.6f, 2));
        upgrades.add(new Upgrade("Attack speed", shootRate, 1.3f, "*", 200f, 1.5f, 2));
        upgrades.add(new Upgrade("Bullet speed", bulletSpeed, 10f, "+", 150f, 1.8f, 2));
        upgrades.add(new Upgrade("Explode radius", explodeRadius, RvB.unite/4, "+", 180f, 1.8f, 3));
        
        int n = 0;
        for(int i = 0 ; i < upgrades.size() ; i++)
            n += upgrades.get(i).maxClick;
        growth = 20*ref/n;
    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.1;
        price = priceP;
    }
    
    public void bombExplode(float x, float y){
        SoundManager.Instance.playOnce(explodeClip);
        checkTime = 0;
        count = 0;
        explodeX = x;
        explodeY = y;
        nbFlames = 10*explodeRadius/50;
    }
    
    @Override
    public void update(){
        super.update();
        if(explodeX < 10000){
            if(System.currentTimeMillis()-checkTime >= 30){
                count++;
                flamesX.clear();
                flamesY.clear();
                for(int i = 0 ; i < nbFlames ; i++){
                    flamesX.add((int)((-1+rand.nextFloat()*2)*(explodeRadius-25)));
                    flamesY.add((int)((-1+rand.nextFloat()*2)*(explodeRadius-25)));
                }
                checkTime = System.currentTimeMillis();
            }  
            for(int i = 0 ; i < nbFlames ; i++)
                RvB.drawFilledRectangle(explodeX + flamesX.get(i), explodeY + flamesY.get(i), 50, 50, RvB.textures.get("flame"), 0);
            if(count > 8)
                explodeX = 10000;
        }
    }
}

