package managers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ennemies.Enemy;
import rvb.RvB;
import static rvb.RvB.game;
import towers.Tower;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatsManager {
    public static StatsManager Instance;
    
    public int progression=0, progressionPalier=0, progressionMax=300, progressionLvl=1, raztechLvlMax=0;
    public int modeEasyNbGames=0, modeEasyNbWins=0, modeEasyNbWaves=0, modeMediumNbGames=0, modeMediumNbWins=0, modeMediumNbWaves=0, modeHardNbGames=0, modeHardNbWins=0, modeHardNbWaves=0, modeHardcoreNbGames=0, modeHardcoreNbWins=0, modeHardcoreNbWaves=0;
    public String modeEasyBestScore="", modeMediumBestScore="", modeHardBestScore="", modeHardcoreBestScore="";
    public int basicEnemyKilled=0, fastEnemyKilled=0, trickyEnemyKilled=0, strongEnemyKilled=0, flyingEnemyKilled=0, bossEnemyKilled=0;
    public int basicTowerPlaced=0, circleTowerPlaced=0, bigTowerPlaced=0, flameTowerPlaced=0;
    
    public StatsManager(){
        
    }
    
    public static void initialize(){
        if(Instance == null)
            Instance = new StatsManager();
    }
    
    public void addProgressionPoints(){
        int points;
        if(game.gameWin)
            points = (int) (game.difficulty.nbWaveMax*game.difficulty.nbWaveMax*1.5*game.difficulty.riskValue);
        else{
            points = (int) (game.waveNumber*game.waveNumber*game.difficulty.riskValue);
        }
        progression += points;
        progressionPalier += points;
        while(progressionPalier >= progressionMax)
            levelUp();
    }
    
    public void cheatAddProgressionPoints(int pp){
        progression += pp;
        progressionPalier += pp;
        while(progressionPalier >= progressionMax)
            levelUp();
    }
    
    public void updateProgression(){
        progressionPalier = progression;
        while(progressionPalier >= progressionMax)
            levelUp();
    }
    
    private void levelUp(){
        progressionPalier -= progressionMax;
        progressionLvl++;
        progressionMax *= 1.3;
    }
    
    public void updateBestScore(){
        int newNbWave = game.waveNumber, newLifePercent = (int)(100*game.life/game.difficulty.life);
        String bestScore = null, newScore = newNbWave+";"+newLifePercent;
        switch(game.difficulty.name){
            case "EASY":
                bestScore = modeEasyBestScore;
                break;
            case "MEDIUM":
                bestScore = modeMediumBestScore;
                break;
            case "HARD":
                bestScore = modeHardBestScore;
                break;
            case "HARDCORE":
                bestScore = modeHardcoreBestScore;
                break;
        }
        if(bestScore == null || bestScore.isEmpty()){
            changeBestScore(newScore);
            return;
        }
        int nbWave = Integer.parseInt(bestScore.split(";")[0]);
        int lifePercent = Integer.parseInt(bestScore.split(";")[1]);
        if(nbWave == game.difficulty.nbWaveMax+1){
            RvB.debug("yo");
            if(newNbWave == game.difficulty.nbWaveMax+1 && newLifePercent > lifePercent){
                changeBestScore(newScore);
                return;
            }
        }
        if(newNbWave > nbWave){
            changeBestScore(newScore);
        }
    }
    
    private void changeBestScore(String newBestScore){
        switch(game.difficulty.name){
            case "EASY":
                modeEasyBestScore = newBestScore;
                break;
            case "MEDIUM":
                modeMediumBestScore = newBestScore;
                break;
            case "HARD":
                modeHardBestScore = newBestScore;
                break;
            case "HARDCORE":
                modeHardcoreBestScore = newBestScore;
                break;
        }
    }
    
    public void updateModeStats(){
        switch(game.difficulty.name){
            case "EASY":
                modeEasyNbGames++;
                if(game.gameWin)
                    modeEasyNbWins++;
                modeEasyNbWaves += game.waveNumber;
                break;
            case "MEDIUM":
                modeMediumNbGames++;
                if(game.gameWin)
                    modeMediumNbWins++;
                modeMediumNbWaves += game.waveNumber;
                break;
            case "HARD":
                modeHardNbGames++;
                if(game.gameWin)
                    modeHardNbWins++;
                modeHardNbWaves += game.waveNumber;
                break;
            case "HARDCORE":
                modeHardcoreNbGames++;
                if(game.gameWin)
                    modeHardcoreNbWins++;
                modeHardcoreNbWaves += game.waveNumber;
                break;
        }
    }
    
    public void updateTowerPlaced(Tower.Type type){
        if(type == Tower.Type.BASIC)
            basicTowerPlaced++;
        else if(type == Tower.Type.CIRCLE)
            circleTowerPlaced++;
        else if(type == Tower.Type.BIG)
            bigTowerPlaced++;
        else if(type == Tower.Type.FLAME)
            flameTowerPlaced++;
    }
    
    public void updateEnemyKilled(Enemy.Type type){
        if(type == Enemy.Type.BASIC)
            basicEnemyKilled++;
        else if(type == Enemy.Type.FAST)
            fastEnemyKilled++;
        else if(type == Enemy.Type.TRICKY)
            trickyEnemyKilled++;
        else if(type == Enemy.Type.STRONG)
            strongEnemyKilled++;
        else if(type == Enemy.Type.FLYING)
            flyingEnemyKilled++;
        else if(type == Enemy.Type.BOSS)
            bossEnemyKilled++;
    }
    
    public String getJSON(){
        return "{"
                + "\"progression\":"+progression+","
                + "\"raztechLvlMax\":"+raztechLvlMax+","
                + "\"modeEasyNbGames\":"+modeEasyNbGames+","
                + "\"modeEasyNbWins\":"+modeEasyNbWins+","
                + "\"modeEasyNbWaves\":"+modeEasyNbWaves+","
                + "\"modeEasyBestScore\":\""+modeEasyBestScore+"\","
                + "\"modeMediumNbGames\":"+modeMediumNbGames+","
                + "\"modeMediumNbWins\":"+modeMediumNbWins+","
                + "\"modeMediumNbWaves\":"+modeMediumNbWaves+","
                + "\"modeMediumBestScore\":\""+modeMediumBestScore+"\","
                + "\"modeHardNbGames\":"+modeHardNbGames+","
                + "\"modeHardNbWins\":"+modeHardNbWins+","
                + "\"modeHardNbWaves\":"+modeHardNbWaves+","
                + "\"modeHardBestScore\":\""+modeHardBestScore+"\","
                + "\"modeHardcoreNbGames\":"+modeHardcoreNbGames+","
                + "\"modeHardcoreNbWins\":"+modeHardcoreNbWins+","
                + "\"modeHardcoreNbWaves\":"+modeHardcoreNbWaves+","
                + "\"modeHardcoreBestScore\":\""+modeHardcoreBestScore+"\","
                + "\"basicEnemyKilled\":"+basicEnemyKilled+","
                + "\"fastEnemyKilled\":"+fastEnemyKilled+","
                + "\"trickyEnemyKilled\":"+trickyEnemyKilled+","
                + "\"strongEnemyKilled\":"+strongEnemyKilled+","
                + "\"flyingEnemyKilled\":"+flyingEnemyKilled+","
                + "\"bossEnemyKilled\":"+bossEnemyKilled+","
                + "\"basicTowerPlaced\":"+basicTowerPlaced+","
                + "\"circleTowerPlaced\":"+circleTowerPlaced+","
                + "\"bigTowerPlaced\":"+bigTowerPlaced+","
                + "\"flameTowerPlaced\":"+flameTowerPlaced
             + "}";
    }
}
