package managers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.logging.Level;
import java.util.logging.Logger;
import rvb.RvB;

public class RVBDB {
    
    public static RVBDB Instance;
    private static Connection connection;
    
    public RVBDB(){
        initDB();
    }
    
    public static void initialize(){
        if(Instance == null)
            Instance = new RVBDB();
    }
    
    private static void initDB(){
        String userHome = System.getProperty("user.home");
        File appFolder = new File(userHome, "RvB");
        if (!appFolder.exists()) {
            appFolder.mkdir();
        }
        // Création de la connection
        try {
            String dataBaseURL = "jdbc:derby:" + appFolder.getAbsolutePath() + File.separator + "RvBDB;create=true";
            connection = DriverManager.getConnection(dataBaseURL);
            System.out.println("Connected to the data base");
        } catch (SQLException ex) {
            Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Regarde si les tables existent, si non, les créer
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM properties WHERE 1 = 0");
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            // si la requête s'est exécutée sans erreur, la table existe
        } catch (SQLException e) {
            // si une exception est levée, la table n'existe pas
            try {
                createTables();
            } catch (SQLException ex) {
                Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Fetch les données et les initialises
        try {
            Statement stmt = connection.createStatement();
            ResultSet prop = getProperties(stmt);
            if(prop.getBoolean("ingame")){
                Statement stmt2 = connection.createStatement();
                ResultSet gameSet = stmt2.executeQuery("SELECT * FROM game FETCH FIRST 1 ROWS ONLY");
                gameSet.next();
                RvB.initPropertiesAndGame(prop.getInt("progression"), prop.getString("progressiontuto"), true, prop.getBoolean("cheatson"), prop.getString("language"), prop.getString("bestscoreeasy"), prop.getString("bestscoremedium"), prop.getString("bestscorehard"), prop.getString("bestscorehardcore"), gameSet.getString("path"), gameSet.getString("difficulty"), gameSet.getInt("life"), gameSet.getInt("money"), gameSet.getInt("wavenumber"), gameSet.getString("towers"), gameSet.getString("buffs"), gameSet.getString("buffsused"));
                gameSet.close();
                stmt2.close();
            }
            else
                RvB.initPropertiesAndGame(prop.getInt("progression"), prop.getString("progressiontuto"), false, prop.getBoolean("cheatson"), prop.getString("language"), prop.getString("bestscoreeasy"), prop.getString("bestscoremedium"), prop.getString("bestscorehard"), prop.getString("bestscorehardcore"), "", "", 0, 0, 0, null, null, null);
            
            prop.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void createTables() throws SQLException{
        Statement createProperties = connection.createStatement();
        String sql = "CREATE TABLE properties (\n" +
                    "    ingame BOOLEAN DEFAULT false,\n" +
                    "    progression INTEGER DEFAULT 0,\n" +
                    "    progressiontuto VARCHAR(20) DEFAULT null,\n" +
                    "    cheatson BOOLEAN DEFAULT false,\n" +
                    "    language VARCHAR(5) DEFAULT 'ENG',\n" +
                    "    bestscoreeasy VARCHAR(10) DEFAULT null,\n" +
                    "    bestscoremedium VARCHAR(10) DEFAULT null,\n" +
                    "    bestscorehard VARCHAR(10) DEFAULT null,\n" +
                    "    bestscorehardcore VARCHAR(10) DEFAULT null\n" +
                    ")";
        createProperties.executeUpdate(sql);
        createProperties.close();

        Statement createGame = connection.createStatement();
        sql = "CREATE TABLE game (\n" +
            "    wavenumber INTEGER DEFAULT 1,\n" +
            "    money INTEGER DEFAULT 0,\n" +
            "    life INTEGER DEFAULT 0,\n" +
            "    path VARCHAR(4600) DEFAULT '',\n" +
            "    difficulty VARCHAR(20) DEFAULT '',\n" +
            "    towers VARCHAR(10000) DEFAULT null,\n" +
            "    buffs VARCHAR(5000) DEFAULT '',\n" +
            "    buffsused VARCHAR(2000) DEFAULT ''\n" +
            ")";
        createGame.executeUpdate(sql);
        createGame.close();
    }
    
    private static ResultSet getProperties(Statement stmt) throws SQLException{
        ResultSet prop = stmt.executeQuery("SELECT * FROM properties FETCH FIRST 1 ROWS ONLY");
        if(!prop.next()){
            Statement stmt2 = connection.createStatement();
            stmt2.executeUpdate("INSERT INTO properties (ingame, progression, progressiontuto, cheatson, language, bestscoreeasy, bestscoremedium, bestscorehard, bestscorehardcore) VALUES (false, 0, null, false, 'ENG', null, null, null, null)");
            stmt2.close();
            prop = stmt.executeQuery("SELECT * FROM properties FETCH FIRST 1 ROWS ONLY");
            prop.next();
        }
        else{
            /*prop.close();
            checkPropertiesColumns();*/
            prop = stmt.executeQuery("SELECT * FROM properties FETCH FIRST 1 ROWS ONLY");
            prop.next();
        }
        return prop;
    }
    
    private static void checkPropertiesColumns() throws SQLException{
        // Exemple de quoi faire pour checker si la colonne existe et les créer si non
        // (à faire si on rajoute des colonnes plus tard, pour pouvoir garder la DB de l'utilisateur intact)
        /*Statement stmt1 = connection.createStatement();
        ResultSet res = stmt1.executeQuery("SELECT COLUMNNAME FROM SYS.SYSCOLUMNS WHERE COLUMNNAME='BESTSCOREEASY'");
        if(res.next()){
            res.close();
            stmt1.close();
            return;
        }
        res.close();
        stmt1.close();
        
        Statement stmt2 = connection.createStatement();
        stmt2.executeUpdate("ALTER TABLE properties ADD COLUMN bestscoreeasy VARCHAR(10) DEFAULT null");
        stmt2.executeUpdate("ALTER TABLE properties ADD COLUMN bestscoremedium VARCHAR(10) DEFAULT null");
        stmt2.executeUpdate("ALTER TABLE properties ADD COLUMN bestscorehard VARCHAR(10) DEFAULT null");
        stmt2.executeUpdate("ALTER TABLE properties ADD COLUMN bestscorehardcore VARCHAR(10) DEFAULT null");
        stmt2.close();*/
    }
    
    public static boolean saveGame(int waveNumber, int money, int life, String pathString, String difficulty, String towers, String buffs, String buffsUsed) throws SQLException{
        Statement stmt1 = connection.createStatement();
        ResultSet gameSet = stmt1.executeQuery("SELECT * FROM game FETCH FIRST 1 ROWS ONLY");
        String sql;
        if(!gameSet.next())
            sql = "INSERT INTO game (wavenumber, money, life, path, difficulty, towers, buffs, buffsused) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        else
            sql = "UPDATE game SET wavenumber = ?, money = ?, life = ?, path = ?, difficulty = ?, towers = ?, buffs = ?, buffsused = ?";
        gameSet.close();
        stmt1.close();
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, waveNumber);
        pstmt.setInt(2, money);
        pstmt.setInt(3, life);
        pstmt.setString(4, pathString);
        pstmt.setString(5, difficulty);
        pstmt.setString(6, towers);
        pstmt.setString(7, buffs);
        pstmt.setString(8, buffsUsed);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateInGame(boolean inGame) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET ingame = ?");
        pstmt.setBoolean(1, inGame);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateProgressionTuto(String progressionTuto) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET progressiontuto = ?");
        pstmt.setString(1, progressionTuto);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateProgressionPoints(int progression) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET progression = ?");
        pstmt.setInt(1, progression);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateLanguage(String language) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET language = ?");
        pstmt.setString(1, language);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateBestScore(String newBestScore, String difficulty) throws SQLException{
        String sql = "UPDATE properties SET ";
        switch(difficulty){
            case "EASY":
                sql += "bestscoreeasy = ?";
                break;
            case "MEDIUM":
                sql += "bestscoremedium = ?";
                break;
            case "HARD":
                sql += "bestscorehard = ?";
                break;
            case "HARDCORE":
                sql += "bestscorehardcore = ?";
                break;
        }
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, newBestScore);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static void exitDB(){
        try {
            String shutdownURL = "jdbc:derby:;shutdown=true"; 
            connection = DriverManager.getConnection(shutdownURL);
            connection.close();
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("XJ015")) {
                System.out.println("Data base closed");
            } else {
                Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
