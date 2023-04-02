package managers;

public final class TextManager {
    
    public enum Text{
        // RANDOM TEXT
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
            new String[]{"Vous avez gagné !", "Bazoo s'incline et reparre vers ses terres bredouille !"},
            new String[]{"You win !", "Bazoo bows and leave back to his lands empty-handed !"}),
        WAVE(
            new String[]{"Vague"},
            new String[]{"Wave"}),
        MENU(
            new String[]{"Retourner au menu"},
            new String[]{"Return to menu"}),
        ALL_ENEMIES(
            new String[]{"Tous les ennemies"},
            new String[]{"All enemies"}),
        SELL(
            new String[]{"Vendre"},
            new String[]{"Sell"}),
        NO_OPTIONS(
            new String[]{"Bazoo a hacké le système !", "Les options sont indisponibles..."},
            new String[]{"Bazoo had the system hacked !", "Options are unavailable..."}),
        SECRET_REVEAL(
            new String[]{"Bazoo est en fait le gentil ici.", "Raztech défend quelque chose qu'il lui a prit."},
            new String[]{"Bazoo is actually the real good guy here.", "Raztech is defending something he took from him."}),
        WHAT(
            new String[]{"Quoi ?"},
            new String[]{"What ?"}),
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
        
        
        // MENU
        ADVENTURE(
            new String[]{"Aventure"},
            new String[]{"Adventure"}),
        RANDOM_MAP(
            new String[]{"Combattre Bazoo"},
            new String[]{"Fight Bazoo"}),
        CREATE_MAP(
            new String[]{"Créer une map"},
            new String[]{"Create a map"}),
        CONTINUE(
            new String[]{"Continuer"},
            new String[]{"Continue"}),
        REGENERATE(
            new String[]{"Regénérer"},
            new String[]{"Regenerate"}),
        MODIFY(
            new String[]{"Modifier"},
            new String[]{"Modify"}),
        MISSING_FILE_LEVELS(
            new String[]{"Hmmm... Étrange...", "Créer un dossier \"levels\" dans", "le même endroit que ton jeu."},
            new String[]{"Hmmm... Very strange...", "Create a directory \"levels\" in", "the same location than your game."}),
        
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
            new String[]{"Tour de portée"},
            new String[]{"Range tower"}),
        TOWER_POWER(
            new String[]{"Tour de puissance"},
            new String[]{"Power tower"}),
        TOWER_SHOOTRATE(
            new String[]{"Tour de vitesse"},
            new String[]{"Speed tower"}),
        FOCUS_SWITCH(
            new String[]{"Premier", "Dernier", "Plus fort", "Plus faible", "Plus proche"},
            new String[]{"First", "Last", "Strongest", "Weakest", "Closest"}),
        FOCUS(
            new String[]{"Vise le"},
            new String[]{"Focus the"}),
        
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
            new String[]{"Les balles de Raztech", "ralentissent les", "ennemies de +10%", "pendant 1 sec."},
            new String[]{"Raztech's bullets", "slow enemies by +10%", "for 1 sec."}),
        BUFF_XP(
            new String[]{"Rune bleue"},
            new String[]{"Blue rune"}),
        BUFF_XP_DESC(
            new String[]{"Raztech gagne", "+20% d'XP."},
            new String[]{"Raztech gains", "+20% XP."}),
        BUFF_OS(
            new String[]{"Vieux crâne"},
            new String[]{"Old skull"}),
        BUFF_OS_DESC(
            new String[]{"Les balles de Raztech", "ont +1% de chance", "d'infliger 1600%", "dégât de base."},
            new String[]{"Raztech's bullets", "have +1% chance to", "deal 1600%", "base damage."}),
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
        
        // POPUPS
        SELECT_DIFF(
            new String[]{"Choisis une difficulté"},
            new String[]{"Select a difficulty"}),
        CANCEL(
            new String[]{"Échape pour annuler"},
            new String[]{"Escape to cancel"}),
        EASY(
            new String[]{"Facile"},
            new String[]{"Easy"}),
        NORMAL(
            new String[]{"Normal"},
            new String[]{"Normal"}),
        HARD(
            new String[]{"Difficile"},
            new String[]{"Hard"}),
        HARDCORE(
            new String[]{"Hardcore"},
            new String[]{"Hardcore"}),
        BOSS_DEFEATED(
            new String[]{"Je reviendrai plus fort !", "Tu ne devrais pas plaisanter avec moi vermine !", "Ce n'est qu'une question de temps...", "Je vais libérer ma vrai puissance !", "Plus je souffre, plus je deviens puissant !"},
            new String[]{"I will be back much stronger !", "You should not mess with me filthy bug !", "It is just a matter of time...", "I will unleash my true power !", "The more I suffer, the more powerful I get !"}),
        BOSS_NOT_DEFEATED(
            new String[]{"Tu es faible.", "Je suis la fin, et j'arrive vite !", "MEUUUURS !", "Ça a toujours été une question de temps.", "Je n'ai pas fait attention, m'as-tu touché ?"},
            new String[]{"You are weak.", "I am the end, and I am coming fast !", "You shall DIE !", "It has always been a matter of time.", "I have not noticed, did you even hit me ?"}),
        BOSS_DEFEATED_ANSWER(
            new String[]{"Toujours debout !", "Où es-tu ?", "J'attends...", "Même pas peur !", "Looser !"},
            new String[]{"Still standing !", "Where're you ?", "I'm waiting...", "Not afraid !", "Looser !"}),
        BOSS_NOT_DEFEATED_ANSWER(
            new String[]{"Noooon !", "Toujours vivant !", "Ouch !", "Attention à toi", "Grrrrr..."},
            new String[]{"Noooo !", "Still alive !", "Ouch !", "Watch yourself", "Grrrrr..."}),
        HOW_TO_PLAY(
            new String[]{"Comment jouer"},
            new String[]{"How to play"}),
        GUIDE(
            new String[]{"Vague par vague, vous allez être submergé d'ennemies.", "Votre but est de les empêcher de tout traverser.", "Sinon vous perdrez des points de vie.", "La partie s'achève lorsque vos points de vie tombent à 0.", "Vous gagnerez des pièces en tuant des ennemies et en survivant aux vagues.", "Ça sera utile pour acheter des tourelles pour défendre votre terrain !"},
            new String[]{"Wave by wave, you will be submerged by enemies.", "Your goal is to prevent them from going all the way through.", "Otherwise you'll lose health points.", "The game ends whenever your health hits 0.", "You will gain coins by killing enemies and surviving waves.", "It'll be useful to buy towers to defend your terrain !"}),
        INFO(
            new String[]{"Informations"},
            new String[]{"Informations"}),
        INFO_GUIDE(
            new String[]{"Déplacer Raztech coûte 20% de l'XP de son prochain niveau.", "Vous pouvez sélectionner des ennemies et des tourelles. Clickez n'importe où pour désélectionner.", "Vous pouvez annuler la construction d'une nouvelle tour avec clique droit."},
            new String[]{"Moving Raztech costs 20% of his next level's XP.", "You can select enemies and towers. Click anywhere else to unselect.", "You can cancel a new tower construction by right clicking."}),
        SHORTCUTS(
            new String[]{"Raccourcis"},
            new String[]{"Shortcuts"}),
        SHORTCUTS_GUIDE(
            new String[]{"R : Placer Raztech", "1 à 4 : Construire une nouvelle tour", "Espace : Lancer la prochaine vague", "P : Pause/Reprendre", "V : Changer la vitesse", "H : Ouvre cette fenêtre", "F1+D : Fenêtre debug", "Échap. : Menu"},
            new String[]{"R : Place Raztech", "1 to 4 : Build a new tower", "Space : Start next wave", "P : Pause/Unpause", "V : Change the speed", "H : Open this window", "F1+D : Debug window", "Esc. : Menu"});

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
}
