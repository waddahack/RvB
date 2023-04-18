package rvb;

import managers.StatsManager;
import managers.TextManager.Text;
import rvb.RvB.Difficulty;
import static rvb.RvB.State.MENU;
import static rvb.RvB.fonts;
import static rvb.RvB.ref;
import ui.Button;
import ui.Overlay;

public class StatsWindow extends Window{
    
    public StatsWindow(){
        Overlay o = new Overlay(0, 0, RvB.windWidth, RvB.windHeight/8);
        Button b = new Button((int)(50*ref), (int)(50*ref), (int)(32*ref), (int)(32*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setBG(RvB.textures.get("arrowBack"));
        b.setFunction(__ -> {
            RvB.switchStateTo(MENU);
        });
        o.addButton(b);
        overlays.add(o);
        
        int mTop = (int)(60*ref);
        o = new Overlay(0, RvB.windHeight/8+mTop, RvB.windWidth, 7*RvB.windHeight/16);
        overlays.add(o);
        
        o = new Overlay(0, 9*RvB.windHeight/16+2*mTop, RvB.windWidth, 7*RvB.windHeight/16);
        overlays.add(o);
    }
    
    @Override
    protected void render(){
        super.render();
        Overlay top = overlays.get(0);
        top.drawText(top.getW()/2, top.getH()/2, Text.STATS.getText(), fonts.get("titleXL"));
        // GLOBAL STATS
        Overlay gs = overlays.get(1);
        // Progress
        gs.drawText(gs.getW()/2, 0, Text.LEVEL.getText()+" "+StatsManager.Instance.progressionLvl, fonts.get("title"));
        int width = (int) (420*ref), height = (int) (22*ref);
        int currentProgress = (int)(((double)StatsManager.Instance.progressionPalier/(double)StatsManager.Instance.progressionMax)*width);
        RvB.drawFilledRectangle(gs.getX()+gs.getW()/2-width/2, gs.getY()+gs.getH()/8-height/2, width, height, RvB.colors.get("lightGreen"), 1, null);
        RvB.drawFilledRectangle(gs.getX()+gs.getW()/2-width/2, gs.getY()+gs.getH()/8-height/2, currentProgress, height, RvB.colors.get("lightBlue"), 1, null);
        RvB.drawRectangle(gs.getX()+gs.getW()/2-width/2, gs.getY()+gs.getH()/8-height/2, width, height, RvB.colors.get("green_dark"), 0.8f, 2);
        gs.drawText(gs.getW()/2, gs.getH()/8, StatsManager.Instance.progressionPalier+"/"+StatsManager.Instance.progressionMax, fonts.get("normalBlack"));
        // Towers stats
        int posX = gs.getW()/2, m = (int)(600*ref), m2 = (int)(330*ref);
        gs.drawText(posX-m, 2*gs.getH()/8, Text.RAZTECH_LVL_MAX.getText(), fonts.get("titleS"), "midLeft");
        gs.drawText(posX-m2, 2*gs.getH()/8, StatsManager.Instance.raztechLvlMax+"", fonts.get("normalL"), "midLeft");
        
        gs.drawText(posX-m, 3*gs.getH()/8, Text.BASICTOWER_PLACED.getText(), fonts.get("titleS"), "midLeft");
        gs.drawText(posX-m2, 3*gs.getH()/8, StatsManager.Instance.basicTowerPlaced+"", fonts.get("normalL"), "midLeft");
        
        gs.drawText(posX-m, 4*gs.getH()/8, Text.CIRCLETOWER_PLACED.getText(), fonts.get("titleS"), "midLeft");
        gs.drawText(posX-m2, 4*gs.getH()/8, StatsManager.Instance.circleTowerPlaced+"", fonts.get("normalL"), "midLeft");
        
        gs.drawText(posX-m, 5*gs.getH()/8, Text.BIGTOWER_PLACED.getText(), fonts.get("titleS"), "midLeft");
        gs.drawText(posX-m2, 5*gs.getH()/8, StatsManager.Instance.bigTowerPlaced+"", fonts.get("normalL"), "midLeft");
        
        gs.drawText(posX-m, 6*gs.getH()/8, Text.FLAMETOWER_PLACED.getText(), fonts.get("titleS"), "midLeft");
        gs.drawText(posX-m2, 6*gs.getH()/8, StatsManager.Instance.flameTowerPlaced+"", fonts.get("normalL"), "midLeft");
        
        gs.drawText(posX-m, 7*gs.getH()/8, "-", fonts.get("titleS"), "midLeft");
        gs.drawText(posX-m2, 7*gs.getH()/8, "-", fonts.get("normalL"), "midLeft");
        // Enemies stats
        gs.drawText(posX+m, 2*gs.getH()/8, Text.BASICENEMY_KILLED.getText(), fonts.get("titleS"), "midRight");
        gs.drawText(posX+m2, 2*gs.getH()/8, StatsManager.Instance.basicEnemyKilled+"", fonts.get("normalL"), "midRight");
        
        gs.drawText(posX+m, 3*gs.getH()/8, Text.FASTENEMY_KILLED.getText(), fonts.get("titleS"), "midRight");
        gs.drawText(posX+m2, 3*gs.getH()/8, StatsManager.Instance.fastEnemyKilled+"", fonts.get("normalL"), "midRight");
        
        gs.drawText(posX+m, 4*gs.getH()/8, Text.TRICKYENEMY_KILLED.getText(), fonts.get("titleS"), "midRight");
        gs.drawText(posX+m2, 4*gs.getH()/8, StatsManager.Instance.trickyEnemyKilled+"", fonts.get("normalL"), "midRight");
        
        gs.drawText(posX+m, 5*gs.getH()/8, Text.STRONGENEMY_KILLED.getText(), fonts.get("titleS"), "midRight");
        gs.drawText(posX+m2, 5*gs.getH()/8, StatsManager.Instance.strongEnemyKilled+"", fonts.get("normalL"), "midRight");
        
        gs.drawText(posX+m, 6*gs.getH()/8, Text.FLYINGENEMY_KILLED.getText(), fonts.get("titleS"), "midRight");
        gs.drawText(posX+m2, 6*gs.getH()/8, StatsManager.Instance.flyingEnemyKilled+"", fonts.get("normalL"), "midRight");
        
        gs.drawText(posX+m, 7*gs.getH()/8, Text.BOSS_KILLED.getText(), fonts.get("titleS"), "midRight");
        gs.drawText(posX+m2, 7*gs.getH()/8, StatsManager.Instance.bossEnemyKilled+"", fonts.get("normalL"), "midRight");
        // MODES STATS
        Overlay ms = overlays.get(2);
        String pb, nbWave, lifePercent;
        int nbGames, nbWins, avWave;
        // Easy
        posX = ms.getW()/8-(int)(40*ref);
        pb = StatsManager.Instance.modeEasyBestScore;
        nbGames = StatsManager.Instance.modeEasyNbGames;
        nbWins = StatsManager.Instance.modeEasyNbWins;
        avWave = StatsManager.Instance.modeEasyNbWaves;
        ms.drawText(posX, 0, Text.EASY.getText(), fonts.get("title"));
        
        ms.drawText(posX, ms.getH()/8, Text.NB_GAMES.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.NB_GAMES.getText()+" : ")/2, ms.getH()/8, nbGames+"", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 2*ms.getH()/8, Text.PB.getText()+" : ", fonts.get("titleS"));
        if(pb != null && !pb.isEmpty()){
            nbWave = pb.split(";")[0];
            lifePercent = pb.split(";")[1];
            ms.drawText(posX+fonts.get("titleS").getWidth(Text.PB.getText()+" : ")/2, 2*ms.getH()/8, Integer.parseInt(nbWave) < Difficulty.EASY.nbWaveMax ? nbWave : Text.WON.getText()+"/"+lifePercent+"%", fonts.get("normalL"), "midLeft");
        }
        else
            ms.drawText(posX+fonts.get("titleS").getWidth(Text.PB.getText()+" : ")/2, 2*ms.getH()/8, "-", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 3*ms.getH()/8, Text.WINRATE.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.WINRATE.getText()+" : ")/2, 3*ms.getH()/8, nbGames>0 ? Math.round(100*nbWins/nbGames)+"%" : "-", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 4*ms.getH()/8, Text.AVERAGE_WAVENUMBER.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.AVERAGE_WAVENUMBER.getText()+" : ")/2, 4*ms.getH()/8, nbGames>0 ? Math.round(avWave/nbGames)+"" : "-", fonts.get("normalL"), "midLeft");
        // Medium
        posX = 3*ms.getW()/8-(int)(40*ref);
        pb = StatsManager.Instance.modeMediumBestScore;
        nbGames = StatsManager.Instance.modeMediumNbGames;
        nbWins = StatsManager.Instance.modeMediumNbWins;
        avWave = StatsManager.Instance.modeMediumNbWaves;
        ms.drawText(posX, 0, Text.MEDIUM.getText(), fonts.get("title"));
        
        ms.drawText(posX, ms.getH()/8, Text.NB_GAMES.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.NB_GAMES.getText()+" : ")/2, ms.getH()/8, nbGames+"", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 2*ms.getH()/8, Text.PB.getText()+" : ", fonts.get("titleS"));
        if(pb != null && !pb.isEmpty()){
            nbWave = pb.split(";")[0];
            lifePercent = pb.split(";")[1];
            ms.drawText(posX+fonts.get("titleS").getWidth(Text.PB.getText()+" : ")/2, 2*ms.getH()/8, Integer.parseInt(nbWave) < Difficulty.MEDIUM.nbWaveMax ? nbWave : Text.WON.getText()+"/"+lifePercent+"%", fonts.get("normalL"), "midLeft");
        }
        else
            ms.drawText(posX+fonts.get("titleS").getWidth(Text.PB.getText()+" : ")/2, 2*ms.getH()/8, "-", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 3*ms.getH()/8, Text.WINRATE.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.WINRATE.getText()+" : ")/2, 3*ms.getH()/8, nbGames>0 ? Math.round(100*nbWins/nbGames)+"%" : "-", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 4*ms.getH()/8, Text.AVERAGE_WAVENUMBER.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.AVERAGE_WAVENUMBER.getText()+" : ")/2, 4*ms.getH()/8, nbGames>0 ? Math.round(avWave/nbGames)+"" : "-", fonts.get("normalL"), "midLeft");
        // Hard
        posX = 5*ms.getW()/8-(int)(40*ref);
        pb = StatsManager.Instance.modeHardBestScore;
        nbGames = StatsManager.Instance.modeHardNbGames;
        nbWins = StatsManager.Instance.modeHardNbWins;
        avWave = StatsManager.Instance.modeHardNbWaves;
        ms.drawText(posX, 0, Text.HARD.getText(), fonts.get("title"));
        
        ms.drawText(posX, ms.getH()/8, Text.NB_GAMES.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.NB_GAMES.getText()+" : ")/2, ms.getH()/8, nbGames+"", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 2*ms.getH()/8, Text.PB.getText()+" : ", fonts.get("titleS"));
        if(pb != null && !pb.isEmpty()){
            nbWave = pb.split(";")[0];
            lifePercent = pb.split(";")[1];
            ms.drawText(posX+fonts.get("titleS").getWidth(Text.PB.getText()+" : ")/2, 2*ms.getH()/8, Integer.parseInt(nbWave) < Difficulty.HARD.nbWaveMax ? nbWave : Text.WON.getText()+"/"+lifePercent+"%", fonts.get("normalL"), "midLeft");
        }
        else
            ms.drawText(posX+fonts.get("titleS").getWidth(Text.PB.getText()+" : ")/2, 2*ms.getH()/8, "-", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 3*ms.getH()/8, Text.WINRATE.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.WINRATE.getText()+" : ")/2, 3*ms.getH()/8, nbGames>0 ? Math.round(100*nbWins/nbGames)+"%" : "-", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 4*ms.getH()/8, Text.AVERAGE_WAVENUMBER.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.AVERAGE_WAVENUMBER.getText()+" : ")/2, 4*ms.getH()/8, nbGames>0 ? Math.round(avWave/nbGames)+"" : "-", fonts.get("normalL"), "midLeft");
        // Hardcore
        posX = 7*ms.getW()/8-(int)(40*ref);
        pb = StatsManager.Instance.modeHardcoreBestScore;
        nbGames = StatsManager.Instance.modeHardcoreNbGames;
        nbWins = StatsManager.Instance.modeHardcoreNbWins;
        avWave = StatsManager.Instance.modeHardcoreNbWaves;
        ms.drawText(posX, 0, Text.HARDCORE.getText(), fonts.get("title"));
        
        ms.drawText(posX, ms.getH()/8, Text.NB_GAMES.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.NB_GAMES.getText()+" : ")/2, ms.getH()/8, nbGames+"", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 2*ms.getH()/8, Text.PB.getText()+" : ", fonts.get("titleS"));
        if(pb != null && !pb.isEmpty()){
            nbWave = pb.split(";")[0];
            lifePercent = pb.split(";")[1];
            ms.drawText(posX+fonts.get("titleS").getWidth(Text.PB.getText()+" : ")/2, 2*ms.getH()/8, Integer.parseInt(nbWave) < Difficulty.HARDCORE.nbWaveMax ? nbWave : Text.WON.getText()+"/"+lifePercent+"%", fonts.get("normalL"), "midLeft");
        }
        else
            ms.drawText(posX+fonts.get("titleS").getWidth(Text.PB.getText()+" : ")/2, 2*ms.getH()/8, "-", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 3*ms.getH()/8, Text.WINRATE.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.WINRATE.getText()+" : ")/2, 3*ms.getH()/8, nbGames>0 ? Math.round(100*nbWins/nbGames)+"%" : "-", fonts.get("normalL"), "midLeft");
        
        ms.drawText(posX, 4*ms.getH()/8, Text.AVERAGE_WAVENUMBER.getText()+" : ", fonts.get("titleS"));
        ms.drawText(posX+fonts.get("titleS").getWidth(Text.AVERAGE_WAVENUMBER.getText()+" : ")/2, 4*ms.getH()/8, nbGames>0 ? Math.round(avWave/nbGames)+"" : "-", fonts.get("normalL"), "midLeft");
    }
}