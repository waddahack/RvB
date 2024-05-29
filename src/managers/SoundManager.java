package managers;

import ennemies.Enemy;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.Clip;  
import javax.sound.sampled.FloatControl;
import static rvb.RvB.game;
import rvb.Shootable;

public final class SoundManager
{ 
    public static SoundManager Instance;

    public static enum Volume{
        VERY_HIGH(0), HIGH(-6), SEMI_HIGH(-11), MEDIUM(-14), SEMI_LOW(-17), LOW(-22), VERY_LOW(-28);
        
        public int value;
        
        private Volume(int value){
            this.value = value;
        }
    }
    
    private final ArrayList<Clip> clipsToClose;
    private final ArrayList<Clip> clipsToPlayNextFrame;
    private final Map<String, Clip> ambianceClips;
    private boolean ready = true;
    public static Clip SOUND_BUILD, SOUND_WAVE, SOUND_RAZTECH1, SOUND_RAZTECH2, SOUND_LEVELUP, SOUND_GAME_WIN, SOUND_BAZOO_ENTRANCE, SOUND_BAZOO_DEFEATED, SOUND_BAZOO_LAUGH;
    
    private SoundManager(){ 
        clipsToClose = new ArrayList<>();
        clipsToPlayNextFrame = new ArrayList<>();
        ambianceClips = new HashMap<>();
        addAmbianceSound("war", Volume.SEMI_LOW);
        
        SOUND_BUILD = getClip("build");
        setClipVolume(SOUND_BUILD, Volume.SEMI_HIGH);
        SOUND_WAVE = getClip("click_wave");
        setClipVolume(SOUND_WAVE, Volume.VERY_HIGH);
        SOUND_RAZTECH1 = getClip("raztech_laugh1");
        setClipVolume(SOUND_RAZTECH1, Volume.SEMI_HIGH);
        SOUND_RAZTECH2 = getClip("raztech_laugh2");
        setClipVolume(SOUND_RAZTECH2, Volume.SEMI_HIGH);
        SOUND_LEVELUP = getClip("level_up");
        setClipVolume(SOUND_LEVELUP, Volume.MEDIUM);
        SOUND_GAME_WIN = getClip("game_win");
        setClipVolume(SOUND_GAME_WIN, Volume.HIGH);
        SOUND_BAZOO_ENTRANCE = getClip("boss_wave");
        setClipVolume(SOUND_BAZOO_ENTRANCE, Volume.HIGH);
        SOUND_BAZOO_DEFEATED = getClip("boss_defeated");
        setClipVolume(SOUND_BAZOO_DEFEATED, Volume.SEMI_HIGH);
        SOUND_BAZOO_LAUGH = getClip("boss_laugh");
        setClipVolume(SOUND_BAZOO_LAUGH, Volume.VERY_HIGH);
    }

    public static void initialize(){
        if(Instance == null)
            Instance = new SoundManager();
    }
    
    public void update(){
        for(int i = 0 ; i < clipsToPlayNextFrame.size() ; i++)
            if(clipsToPlayNextFrame.get(i) != null)
                playOnce(clipsToPlayNextFrame.get(i));
        clipsToPlayNextFrame.clear();
    }
    
    public void closeAllClips(){
        ready = false;
        new Thread(){
            @Override
            public void run(){
                clipsToClose.forEach(clip -> clip.close());
                clipsToClose.clear();
                ready = true;
            }
        }.start();
    }
    
    public void clipToClose(Clip clip){
        clipsToClose.add(clip);
    }
    
    public boolean isReady(){
        return ready;
    }
    
    public void addAmbianceSound(String name, Volume v){
        try {
            Clip clip;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File("assets/audio/"+name+".wav").getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            audioStream.close();
            FloatControl volume = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(v.value);
            ambianceClips.put("AMBIANCE"+name, clip);
        } catch (Exception ex) {
            Logger.getLogger(SoundManager.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    public void playAllAmbiance(){
        for(Clip c : ambianceClips.values())
            c.loop(Clip.LOOP_CONTINUOUSLY);
    }
    
    public void setClipVolume(Clip clip, Volume v){
        if(clip == null)
            return;
        FloatControl volume = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
        volume.setValue(v.value);
    }
    
    public void playOnce(Clip clip){
        if(!clip.isRunning()){
            clip.setMicrosecondPosition(0);
            clip.start();
        }
        else{
            clip.stop();
            clip.setMicrosecondPosition(0);
            clipsToPlayNextFrame.add(clip);
        }
    }
    
    public void playLoop(Clip clip){
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    
    public void stopClip(Clip clip){
        clip.stop();
    }
    
    public Clip getClip(String soundName) 
    {
        Clip clip = null;
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File("assets/audio/"+soundName+".wav").getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            audioStream.close();
        } catch (Exception ex) {
            Logger.getLogger(SoundManager.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return clip;
    }
    
    public void pauseAll(){
        for(Shootable enemy : game.enemies){
            Enemy e = (Enemy) enemy;
            if(e.getStepEveryMilli() <= 1 && e.hasStarted() && e.getClipWalk() != null)
                e.getClipWalk().stop();
            if(e.isSoundContinuous() && e.getClip() != null)
                e.getClip().stop();
        }
        for(Shootable t : game.towers)
            if(t.isSoundContinuous() && t.getClip() != null)
                t.getClip().stop();
    }
    
    public void unpauseAll(){
        for(Shootable enemy : game.enemies){
            Enemy e = (Enemy) enemy;
            if(e.getStepEveryMilli() <= 1 && e.hasStarted() && e.getClipWalk() != null)
                e.getClipWalk().loop(Clip.LOOP_CONTINUOUSLY);
            /*if(e.getStepEveryMilli() <= 1 && e.hasStarted() && e.getClip() != null)
                e.getClip().loop(Clip.LOOP_CONTINUOUSLY);*/
        }  
        for(Shootable t : game.towers)
            if(t.isSoundContinuous() && t.getClip() != null && t.getEnemyAimed() != null)
                t.getClip().loop(Clip.LOOP_CONTINUOUSLY);
    }
    
    public void pauseAllAmbiance(){
        for(Clip c : ambianceClips.values())
            c.stop();
    }
}