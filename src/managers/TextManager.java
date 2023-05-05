package managers;

public final class TextManager {
    
    public enum Text{
        // RANDOM TEXT
        ERROR(
            new String[]{"Erreur"},
            new String[]{"Error"}),
        SUCCESS(
            new String[]{"Succès"},
            new String[]{"Success"}),
        MS(
            new String[]{"vitesse de déplacement"},
            new String[]{"move speed"}),
        HP(
            new String[]{"point de vie"},
            new String[]{"health point"}),
        DEFENDING(
            new String[]{"Je défends..."},
            new String[]{"Defending..."}),
        START_WAVE(
            new String[]{"C'est parti !"},
            new String[]{"Go !"}),
        WAITING(
            new String[]{"En attente..."},
            new String[]{"Waiting..."}),
        GAME_OVER(
            new String[]{"Bazoo a été plus fort...", "Il a gagné une bataille, mais pas la guerre."},
            new String[]{"Bazoo have been stronger...", "He's won a battle, but not the war."}),
        GAME_WIN(
            new String[]{"Vous avez gagné !", "Bazoo s'incline et repart vers ses terres bredouille !"},
            new String[]{"You win !", "Bazoo bows and leave back to his lands empty-handed !"}),
        WAVE(
            new String[]{"Vague"},
            new String[]{"Wave"}),
        MENU(
            new String[]{"Retourner au menu"},
            new String[]{"Return to menu"}),
        ALL_ENEMIES(
            new String[]{"Tous les ennemis"},
            new String[]{"All enemies"}),
        SELL(
            new String[]{"Vendre"},
            new String[]{"Sell"}),
        NO_OPTIONS(
            new String[]{"Bazoo a hacké le système !", "Les options sont indisponibles..."},
            new String[]{"Bazoo had the system hacked !", "Options are unavailable..."}),
        OK(
            new String[]{"Ok"},
            new String[]{"Ok"}),
        DOTS(
            new String[]{"..."},
            new String[]{"..."}),
        XP(
            new String[]{"XP"},
            new String[]{"XP"}),
        LVL(
            new String[]{"Niv. "},
            new String[]{"Lvl "}),
        LEVEL(
            new String[]{"Niveau "},
            new String[]{"Level "}),
        POWER(
            new String[]{"Dégât"},
            new String[]{"Damage"}),
        RANGE(
            new String[]{"Portée"},
            new String[]{"Range"}),
        SHOOTRATE(
            new String[]{"Vitesse d'attaque"},
            new String[]{"Attack speed"}),
        RAZTECH_LVLUP(
            new String[]{"Raztech a gagné un niveau !"},
            new String[]{"Raztech has leveled up !"}),
        SELECT_REWARD(
            new String[]{"Choisis une récompense."},
            new String[]{"Choose a reward."}),
        NOTHING_LEFT(
            new String[]{"Il n'y a plus de récompense..."},
            new String[]{"There's no reward left..."}),
        TOWER_UNLOCKED(
            new String[]{"Nouvelle tourelle débloquée"},
            new String[]{"New tower unlocked"}),
        CLOSE(
            new String[]{"Fermer"},
            new String[]{"Close"}),
        SHORT(
            new String[]{"Court"},
            new String[]{"Short"}),
        LONG(
            new String[]{"Long"},
            new String[]{"Long"}),
        MAP_DOWNLOADED(
            new String[]{"Map téléchargée !"},
            new String[]{"Map downloaded !"}),
        PLAY(
            new String[]{"Jouer"},
            new String[]{"Play"}),  
        CLEAR(
            new String[]{"Nettoyer"},
            new String[]{"Clear"}),
        LOAD(
            new String[]{"Charger"},
            new String[]{"Load"}),
        PATH_NOT_VALID(
            new String[]{"Chemin invalide.", "Il doit commencer et finir", "depuis le côté gauche ou droite.", "Il ne peut pas y avoir plusieurs chemins."},
            new String[]{"Path not valid.", "It has to begin and end from the left or right side.", "It cannot have extra roads."}),
        PATH_NOT_ALONE(
            new String[]{"Il ne peut pas y avoir plus d'un chemin !"},
            new String[]{"There can't be more than one path !"}),
        PATH_NOT_LOOP(
            new String[]{"Ça ne peut pas être une boucle !"},
            new String[]{"It can't be a loop !"}),
        PATH_TOO_LONG(
            new String[]{"Le chemin est trop long", "pour la difficulté choisie.", "Vous n'obtiendrez pas de points de", "progression et le meilleur score ne pourra", "pas être battu."},
            new String[]{"The path is too long", "for the chosen difficulty.", "You won't get any progression", "points and the best score cannot", "be beaten."}),
        WON(
            new String[]{"Gagné"},
            new String[]{"Won"}),
        X1(
            new String[]{"x1"},
            new String[]{"x1"}),
        X2(
            new String[]{"x2"},
            new String[]{"x2"}),
        X4(
            new String[]{"x4"},
            new String[]{"x4"}),
        NEXT(
            new String[]{" >> "},
            new String[]{" >> "}),
        
        // TOWERS
        RAZTECH(
            new String[]{"Raztech"},
            new String[]{"Raztech"}),
        TOWER_BASIC(
            new String[]{"Razanon"},
            new String[]{"Razannon"}),
        TOWER_CIRCLE(
            new String[]{"Razaillette"},
            new String[]{"Razingun"}),
        TOWER_BIG(
            new String[]{"Mortech"},
            new String[]{"Razkull"}),
        TOWER_FLAME(
            new String[]{"Flametech"},
            new String[]{"Flametech"}),
        TOWER_RANGE(
            new String[]{"Tour d'ampleur"},
            new String[]{"Range tower"}),
        TOWER_POWER(
            new String[]{"Tour de force"},
            new String[]{"Power tower"}),
        TOWER_SHOOTRATE(
            new String[]{"Tour de célérité"},
            new String[]{"Speed tower"}),
        FOCUS_SWITCH(
            new String[]{"Premier", "Dernier", "Plus fort", "Plus faible", "Plus proche"},
            new String[]{"First", "Last", "Strongest", "Weakest", "Closest"}),
        FOCUS(
            new String[]{"Vise le"},
            new String[]{"Focus the"}),
        
        // ENEMIES
        ENEMY_BASIC(
            new String[]{"Bazooldat"},
            new String[]{"Bazooldier"}),
        ENEMY_FAST(
            new String[]{"Quazoo"},
            new String[]{"Quazoo"}),
        ENEMY_TRICKY(
            new String[]{"Groupe de Bazooldats"},
            new String[]{"Bazooldier group"}),
        ENEMY_STRONG(
            new String[]{"Bazank"},
            new String[]{"Bazank"}),
        ENEMY_FLYING(
            new String[]{"Bazooptère"},
            new String[]{"Bazoopter"}),
        ENEMY_BOSS(
            new String[]{"Bazoo"},
            new String[]{"Bazoo"}),
        
        // STATS
        STATS(
            new String[]{"Statistiques"},
            new String[]{"Statistics"}),
        GLOBAL(
            new String[]{"Globale"},
            new String[]{"Global"}),
        PROGRESSION(
            new String[]{"Progression"},
            new String[]{"Progress"}),
        RAZTECH_LVL_MAX(
            new String[]{"Niveau max avec Raztech"},
            new String[]{"Level max with Raztech"}),
        ENEMIES_KILLED(
            new String[]{"Ennemis tués"},
            new String[]{"Enemies killed"}),
        DAMAGES_DONE(
            new String[]{"Dégâts commis"},
            new String[]{"Damages done"}),
        TOTAL(
            new String[]{"Total"},
            new String[]{"Total"}),
        THIS_WAVE(
            new String[]{"Cette vague"},
            new String[]{"This wave"}),
        PB(
            new String[]{"Record personnel"},
            new String[]{"Personal best"}),
        BASICTOWER_PLACED(
            new String[]{Text.TOWER_BASIC.FR_text[0]+" placés"},
            new String[]{Text.TOWER_BASIC.ENG_text[0]+" placed"}),
        CIRCLETOWER_PLACED(
            new String[]{Text.TOWER_CIRCLE.FR_text[0]+" placées"},
            new String[]{Text.TOWER_CIRCLE.ENG_text[0]+" placed"}),
        BIGTOWER_PLACED(
            new String[]{Text.TOWER_BIG.FR_text[0]+" placés"},
            new String[]{Text.TOWER_BIG.ENG_text[0]+" placed"}),
        FLAMETOWER_PLACED(
            new String[]{Text.TOWER_FLAME.FR_text[0]+" placés"},
            new String[]{Text.TOWER_FLAME.ENG_text[0]+" placed"}),
        BASICENEMY_KILLED(
            new String[]{Text.ENEMY_BASIC.FR_text[0]+" tués"},
            new String[]{Text.ENEMY_BASIC.ENG_text[0]+" killed"}),
        FASTENEMY_KILLED(
            new String[]{Text.ENEMY_FAST.FR_text[0]+" tués"},
            new String[]{Text.ENEMY_FAST.ENG_text[0]+" killed"}),
        TRICKYENEMY_KILLED(
            new String[]{Text.ENEMY_TRICKY.FR_text[0]+" tués"},
            new String[]{Text.ENEMY_TRICKY.ENG_text[0]+" killed"}),
        STRONGENEMY_KILLED(
            new String[]{Text.ENEMY_STRONG.FR_text[0]+" tués"},
            new String[]{Text.ENEMY_STRONG.ENG_text[0]+" killed"}),
        FLYINGENEMY_KILLED(
            new String[]{Text.ENEMY_FLYING.FR_text[0]+" tués"},
            new String[]{Text.ENEMY_FLYING.ENG_text[0]+" killed"}),
        BOSS_KILLED(
            new String[]{Text.ENEMY_BOSS.FR_text[0]+" tués"},
            new String[]{Text.ENEMY_BOSS.ENG_text[0]+" killed"}),
        NB_GAMES(
            new String[]{"Nombre de parties"},
            new String[]{"Number of games"}),
        WINRATE(
            new String[]{"Taux de réussite"},
            new String[]{"Win rate"}),
        AVERAGE_WAVENUMBER(
            new String[]{"Nombre de vagues moyen"},
            new String[]{"Average number of waves"}),
        
        // MENU
        ADVENTURE(
            new String[]{"Aventure"},
            new String[]{"Adventure"}),
        FIGHT(
            new String[]{"Combattre Bazoo"},
            new String[]{"Fight Bazoo"}),
        RANDOM_MAP(
            new String[]{"Map aléatoire"},
            new String[]{"Random map"}),
        LOAD_MAP(
            new String[]{"Charger une map"},
            new String[]{"Load a map"}),
        CREATION(
            new String[]{"Création"},
            new String[]{"Creation"}),
        CONTINUE(
            new String[]{"Continuer"},
            new String[]{"Continue"}),
        NEW_GAME(
            new String[]{"Nouvelle partie"},
            new String[]{"New game"}),
        MODIFY(
            new String[]{"Modifier"},
            new String[]{"Modify"}),
        MISSING_FILE_LEVELS(
            new String[]{"Hmmm... Étrange...", "Créer un dossier \"levels\" dans", "le même endroit que ton jeu."},
            new String[]{"Hmmm... Very strange...", "Create a directory \"levels\" in", "the same location than your game."}),
        
        // BUFFS
        BUFF_UPGRADE(
            new String[]{"Amélioration"},
            new String[]{"Upgrade"}),
        BUFF_UPGRADE_DESC(
            new String[]{"Raztech :", " ", "Portée +8%", "Puissance +10%", "Vitesse d'attaque +8%"},
            new String[]{"Raztech :", " ", "Range +8%", "Power +10%", "Attack speed +8%"}),
        BUFF_SLOW(
            new String[]{"Pieds froids"},
            new String[]{"Cold feet"}),
        BUFF_SLOW_DESC(
            new String[]{"Les balles de Raztech", "ralentissent les", "ennemis de +20%", "pendant 1 sec."},
            new String[]{"Raztech's bullets", "slow enemies by +20%", "for 1 sec."}),
        BUFF_XP(
            new String[]{"Rune bleue"},
            new String[]{"Blue rune"}),
        BUFF_XP_DESC(
            new String[]{"L'XP que Raztech", "gagne est", "+25% efficace."},
            new String[]{"XP Raztech gains is", "+25% more effective."}),
        BUFF_OS(
            new String[]{"Vieux crâne"},
            new String[]{"Old skull"}),
        BUFF_OS_DESC(
            new String[]{"Les balles de Raztech", "ont +1% de chance", "d'infliger 1800%", "dégât de base."},
            new String[]{"Raztech's bullets", "have +1% chance to", "deal 1800%", "base damage."}),
        BUFF_RANGE_TOWER_DESC(
            new String[]{"Nouvelle tour :", " ", "Donne +15% de portée", "aux tourelles proches."},
            new String[]{"New tower :", " ", "Gives +15% range", "to near towers."}),
        BUFF_POWER_TOWER_DESC(
            new String[]{"Nouvelle tour :", " ", "Donne +10% de dégât", "aux tourelles proches."},
            new String[]{"New tower :", " ", "Gives +10% damage", "to near towers."}),
        BUFF_SHOOTRATE_TOWER_DESC(
            new String[]{"Nouvelle tour :", " ", "Donne +10%", "de vitesse d'attaque", "aux tourelles proches."},
            new String[]{"New tower :", " ", "Gives +10%", "attack speed", "to near towers."}),
        BUFF_UP_POWER_TOWER(
            new String[]{"Électricité"},
            new String[]{"Electricity"}),
        BUFF_UP_POWER_TOWER_DESC(
            new String[]{"La tour de puissance", "donne +5% de dégât."},
            new String[]{"The power tower", "gives +5% damage."}),
        BUFF_UP_RANGE_TOWER(
            new String[]{"Oeil flottant"},
            new String[]{"Floating eye"}),
        BUFF_UP_RANGE_TOWER_DESC(
            new String[]{"La tour de portée", "donne +5% de portée."},
            new String[]{"The range tower", "gives +5% range."}),
        BUFF_UP_SHOOTRATE_TOWER(
            new String[]{"Balle polie"},
            new String[]{"Polished bullet"}),
        BUFF_UP_SHOOTRATE_TOWER_DESC(
            new String[]{"La tour de vitesse", "donne +5% de", "vitesse d'attaque."},
            new String[]{"The speed tower", "gives +5%", "attack speed."}),
        
        // TUTO
        WLCM_RND(
            new String[]{"Bienvenue sur le champ de bataille !", "C'est ici que vous affronterez Bazoo --bazooZoomed-- ", "et son armée."},
            new String[]{"Welcome to the battle field !", "It's here that you'll fight --bazooZoomed-- ", "and his army."}),
        WLCM_RND2(
            new String[]{"Pour cela, vous aurez besoin de pièces --coins--.", "C'est avec ceci que vous pourrez acquérir", "de nouvelles tours qui vous", "aideront à vous défendre.", "Vous en récoltez en tuant des ennemis", " et après chaque vague."},
            new String[]{"To achieve that, you'll need coins --coins--.", "With these, you'll be able to buy", "new towers which will help", "you defend yourself.", "You'll earn some of it by killing enemies", "and after every wave."}),
        WLCM_RND3(
            new String[]{"Si des ennemis passent le chemin,", "vous perdrez des points de vies --heart--.", "Arrivé à 0, la partie est perdu.", "Pour gagner, il faut persister", "jusqu'à que --bazooZoomed-- abandonne.", "Vous pouvez quitter le jeu avec une", " partie en cours, elle sera sauvegardée."},
            new String[]{"If enemies come to cross the field,", "you'd lose health points --heart--.", "Down to 0, the game is lost.", "To win, you must persist until --bazooZoomed-- gives up.", "You can exit the game with a war", "in progress, it'll be saved."}),
        WLCM_RND4(
            new String[]{"Commençons par placer Raztech --raztech-- ", "à côté du chemin !"},
            new String[]{"Let's begin by placing Raztech --raztech-- ", "next to the path !"}),
        RZTCH_PLCD(
            new String[]{"--raztech-- est la seule tourelle qui", "gagne des points d'expériences", "en tuant des ennemis."},
            new String[]{"--raztech-- is the only tower that", "gains experience points", "by killing enemies."}),
        RZTCH_PLCD2(
            new String[]{"En ayant placé --raztech--, vous avez débloqué", "une nouvelle tour : la Razanon --basicTower--.", "Placez en une.", "Pour désélectionner une tour,", "cliquez autre part."},
            new String[]{"By placing --raztech--, you've unlocked", "a new tower : the Razannon --basicTower--.", "Place one of those.", "To deselect a tower,", "click anywhere else."}),
        TWR_PLCD(
            new String[]{"Parfait !", " ", "Vous pouvez désormais lancer", "la première vague."},
            new String[]{"Perfect !", " ", "You can now start", "the first wave."}),
        TWR_PLCD2(
            new String[]{"Vous pouvez changer la vitesse du jeu ici."},
            new String[]{"You can change the game speed here."}),
        FRST_WV(
            new String[]{"Grâce aux --coins-- que vous récoltez, vous", "pouvez également améliorer vos tours !", "(sauf --raztech-- qui lui augmente en niveau)", "Sélectionnez votre --basicTower-- ", "et améliorez sa puissance --powerIcon-- !"},
            new String[]{"With --coins-- you earn, you", "can also upgrade your towers !", "(except --raztech-- who's leveling up)", "Select your --basicTower-- ", "and upgrade its power --powerIcon-- !"}),
        PWR_PGRDD(
            new String[]{"Super !", " ", "Les améliorations dépendent", "du type de la tourelle."},
            new String[]{"Great !", " ", "Upgrades depend on the", "tower's type."}),
        PWR_PGRDD2(
            new String[]{"Voici ce que --basicTower-- peut améliorer :", " ", "--rangeIcon-- : La portée", "--powerIcon-- : La puissance, les dégâts", "--attackSpeedIcon-- : La vitesse de tire", "--bulletSpeedIcon-- : La vitesse des balles"},
            new String[]{"Here's what --basicTower-- can upgrade :", " ", "--rangeIcon-- : The range", "--powerIcon-- : The power, the damages", "--attackSpeedIcon-- : The shooting speed", "--bulletSpeedIcon-- : The bullets' speed"}),
        SCND_WV(
            new String[]{"Il est possible de déplacer --raztech-- en", "cliquant sur --placeRaztech--, mais cela coûte 20%", "de l'XP max avant le prochaine niveau."},
            new String[]{"It's possible to move --raztech-- by", "clicking on --placeRaztech--, but it costs 20%", "of the max XP before the next level."}),
        SCND_WV2(
            new String[]{"Toute tourelle qui vise peut changer", "sa cible."},
            new String[]{"Every tower that aims can change", "their focus"}),
        SCND_WV3(
            new String[]{"Toutes les tourelles (sauf --raztech--) peuvent", "être vendues pour 50% des --coins-- ", "qui ont été investi dedans."},
            new String[]{"Every tower (except --raztech--) can", "be sold for 50% of --coins-- ", "invested in it."}),
        THRD_WV(
            new String[]{"Il est possible de sélectionner des", "ennemis pour voir leurs --heart--."},
            new String[]{"It's possible to select", "enemies to see their --heart--."}),
        FRTH_WV(
            new String[]{"Vous pouvez mettre pause avec ESPACE", "pendant une vague pour prendre", "votre temps."},
            new String[]{"You can pause the game with SPACE", "in order to take your time."}),
        FRTH_WV2(
            new String[]{"En sélectionnant une tourelle,", "il est possible de voir ses statistiques", "en appuyant sur TAB."},
            new String[]{"By selecting a tower", "you can see its statistics", "by pressing TAB."}),
        FRTH_WV3(
            new String[]{"Pas d'inquiétude, vous avez un bouton d'aide", "pour vous rappeler des grandes lignes", "et connaître quelques raccourcis !"},
            new String[]{"No worries, you have a help button", "to remind you of the main information", "and to learn some shortcuts !"}),
        LVL_P(
            new String[]{"--raztech-- est passé niveau 2 !", " ", "À chaque niveau passé, les caractéristiques", "de --raztech-- s'améliorent.", "Cela débloque aussi des nouvelles tourelles."},
            new String[]{"--raztech-- has leveled up !", " ", "At each level gained, --raztech--'s characteristics", "get better.", "It also unlocks new towers."}),
        LVL_P2(
            new String[]{"Quand --raztech-- passe un niveau,", "cela vous permet également de", "choisir un bonus parmi les", "trois proposés."},
            new String[]{"When --raztech-- levels up,", "you can also choose one of the", "three bonus offered."}),
        GM_NDD(
            new String[]{"Vous pouvez télécharger la map", "pour la rejouer, la partager ou la modifier."},
            new String[]{"You can download the map", "to play it again, share it or edit it"}),
        GM_NDD2(
            new String[]{"En partie, le bouton se trouve ici."},
            new String[]{"In game, the button appears here."}),
        
        // POPUPS
        SELECT_DIFF(
            new String[]{"Choisis une difficulté"},
            new String[]{"Select a difficulty"}),
        SELECT_MODE(
            new String[]{"Choisis un mode"},
            new String[]{"Select a mode"}),
        CANCEL(
            new String[]{"Échap pour annuler"},
            new String[]{"Escape to cancel"}),
        EASY(
            new String[]{"Facile"},
            new String[]{"Easy"}),
        MEDIUM(
            new String[]{"Moyen"},
            new String[]{"Medium"}),
        HARD(
            new String[]{"Difficile"},
            new String[]{"Hard"}),
        HARDCORE(
            new String[]{"Hardcore"},
            new String[]{"Hardcore"}),
        LIFEPOINT_LEFT(
            new String[]{"Points de vie restant"},
            new String[]{"Life points left"}),
        BOSS_DEFEATED(
            new String[]{"«Je reviendrai plus fort !»", "«Tu ne devrais pas plaisanter avec moi vermine !»", "«Ce n'est qu'une question de temps...»", "«Je vais libérer ma vraie puissance !»", "«Plus je souffre, plus je deviens puissant !»"},
            new String[]{"\"I will be back much stronger !\"", "\"You should not mess with me filthy bug !\"", "\"It is just a matter of time...\"", "\"I will unleash my true power !\"", "\"The more I suffer, the more powerful I get !\""}),
        BOSS_NOT_DEFEATED(
            new String[]{"«Tu es faible.»", "«Je suis la fin, et j'arrive vite !»", "«MEUUUURS !»", "«Ça a toujours été une question de temps.»", "«Je n'ai pas fait attention, m'as-tu touché ?»"},
            new String[]{"\"You are weak.\"", "\"I am the end, and I am coming fast !\"", "\"You shall DIE !\"", "\"It has always been a matter of time.\"", "\"I have not noticed, did you even hit me ?\""}),
        BOSS_DEFEATED_ANSWER(
            new String[]{"«Toujours debout !»", "«Où es-tu ?»", "«J'attends...»", "«Même pas peur !»", "«Looser !»"},
            new String[]{"\"Still standing !\"", "\"Where're you ?\"", "\"I'm waiting...\"", "\"Not afraid !\"", "\"Looser !\""}),
        BOSS_NOT_DEFEATED_ANSWER(
            new String[]{"«Noooon !", "«Toujours vivant !", "«Ouch !", "«Attention à toi»", "«Grrrrr...»"},
            new String[]{"\"Noooo !\"", "\"Still alive !\"", "\"Ouch !\"", "\"Watch yourself\"", "\"Grrrrr...\""}),
        HOW_TO_PLAY(
            new String[]{"Comment jouer"},
            new String[]{"How to play"}),
        GUIDE(
            new String[]{"Vague par vague, vous allez être submergés d'ennemis.", "Votre but est de les empêcher de tout traverser.", "Sinon vous perdrez des points de vie.", "Combattez Bazoo, ou mourez !", "Vous gagnerez des pièces en tuant des ennemis et en survivant aux vagues.", "Ce sera utile pour acheter des tourelles pour défendre votre terrain !"},
            new String[]{"Wave by wave, you will be submerged by enemies.", "Your goal is to prevent them from going all the way through.", "Otherwise you'll lose health points.", "Fight Bazoo or die !", "You will gain coins by killing enemies and surviving waves.", "It'll be useful to buy towers to defend your terrain !"}),
        INFO(
            new String[]{"Informations"},
            new String[]{"Informations"}),
        INFO_GUIDE(
            new String[]{"Déplacer Raztech coûte 20% de l'XP de son prochain niveau.", "Vous pouvez sélectionner des ennemis et des tourelles. Cliquez n'importe où pour désélectionner.", "Vous pouvez annuler la construction d'une nouvelle tour avec clique droit."},
            new String[]{"Moving Raztech costs 20% of his next level's XP.", "You can select enemies and towers. Click anywhere else to unselect.", "You can cancel a new tower construction by right clicking."}),
        SHORTCUTS(
            new String[]{"Raccourcis"},
            new String[]{"Shortcuts"}),
        SHORTCUTS_GUIDE(
            new String[]{"R : Placer Raztech", "1 à 4 : Construire une nouvelle tour", "Espace : Lancer la vague / Pause / Reprendre", "V : Changer la vitesse", "Tab : Afficher les stats de la tour séléctionnée", "H : Ouvrir cette fenêtre", "F1+D : Fenêtre debug", "Échap : Menu"},
            new String[]{"R : Place Raztech", "1 to 4 : Build a new tower", "Space : Start wave / Pause / Unpause", "V : Change the speed", "Tab : Display selected tower's stats", "H : Open this window", "F1+D : Debug window", "Esc. : Menu"});

        private final String[] FR_text, ENG_text;
        
        private Text(String[] FR_text, String[] ENG_text){
            this.FR_text = FR_text;
            this.ENG_text = ENG_text;
        }
        
        public String getText(){
            if(currentLanguage.equals("FR"))
                return FR_text[0];
            return ENG_text[0];
        }
        
        public String[] getLines(){
            if(currentLanguage.equals("FR"))
                return FR_text;
            return ENG_text;
        }
    }
    
    public static TextManager Instance;
    
    private static String currentLanguage;
    
    public TextManager(){
        currentLanguage = "ENG";
    }

    public static void initialize(){
        if(Instance == null)
            Instance = new TextManager();
    }
    
    public void setLanguage(String l){
        currentLanguage = l;
    }
    
    public String getLanguage(){
        return currentLanguage;
    }
}
