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
import static towser.Towser.game;

public class SoundManager
{ 
    public static SoundManager Instance;

    public static enum Volume{
        VERY_HIGH(4), HIGH(-2), SEMI_HIGH(-7), MEDIUM(-10), SEMI_LOW(-13), LOW(-18), VERY_LOW(-24);
        
        public int value;
        
        private Volume(int value){
            this.value = value;
        }
    }
    
    private ArrayList<Clip> clipsToClose;
    private Map<String, Clip> ambianceClips;
    private boolean ready = true;
    
    public SoundManager(){ 
        clipsToClose = new ArrayList<Clip>();
        ambianceClips = new HashMap<String, Clip>();
        addAmbianceSound("war", SoundManager.Volume.SEMI_LOW);
    }

    public static void initialize(){
        if(Instance == null)
            Instance = new SoundManager();
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
        FloatControl volume = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
        volume.setValue(v.value);
    }
    
    public void playOnce(Clip clip){
        clip.setMicrosecondPosition(0);
        clip.start();
    }
    
    public void playLoop(Clip clip){
        clip.loop(Clip.LOOP_CONTINUOUSLY);
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
        for(Enemy e : game.enemies)
            if(e.getStepEveryMilli() <= 1 && e.hasStarted())
                e.getClip().stop();
    }
    
    public void unpauseAll(){
        for(Enemy e : game.enemies)
            if(e.getStepEveryMilli() <= 1 && e.hasStarted())
                e.getClip().loop(Clip.LOOP_CONTINUOUSLY);
    }
    
    public void pauseAllAmbiance(){
        for(Clip c : ambianceClips.values())
            c.stop();
    }
}