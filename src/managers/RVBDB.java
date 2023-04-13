package managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        try {
            String dataBaseURL = "jdbc:derby:RvBDB";
            connection = DriverManager.getConnection(dataBaseURL);
            System.out.println("Connected to the data base");
            Statement stmt1 = connection.createStatement();
            ResultSet prop = stmt1.executeQuery("SELECT * FROM properties FETCH FIRST 1 ROWS ONLY");
            if(!prop.next()){
                Statement stmt2 = connection.createStatement();
                stmt2.executeUpdate("INSERT INTO properties (ingame, progression, progressiontuto, cheatson, language) VALUES (false, 0, null, false, 'FR')");
                stmt2.close();
                prop = stmt1.executeQuery("SELECT * FROM properties FETCH FIRST 1 ROWS ONLY");
                prop.next();
            }
            
            if(prop.getBoolean("ingame")){
                Statement stmt3 = connection.createStatement();
                ResultSet gameSet = stmt3.executeQuery("SELECT * FROM game FETCH FIRST 1 ROWS ONLY");
                gameSet.next();
                RvB.initPropertiesAndGame(prop.getInt("progression"), prop.getString("progressiontuto"), true, prop.getBoolean("cheatson"), prop.getString("language"), gameSet.getString("path"), gameSet.getString("difficulty"), gameSet.getInt("life"), gameSet.getInt("money"), gameSet.getInt("wavenumber"), gameSet.getString("towers"), gameSet.getString("buffs"), gameSet.getString("buffsused"));
                gameSet.close();
                stmt3.close();
            }
            else
                RvB.initPropertiesAndGame(prop.getInt("progression"), prop.getString("progressiontuto"), false, prop.getBoolean("cheatson"), prop.getString("language"), "", "", 0, 0, 0, null, null, null);
            
            prop.close();
            stmt1.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    public static boolean saveProperties(boolean inGame, int progression, String progressionTuto, boolean cheatsOn, String language) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET ingame = ?, progression = ?, progressiontuto = ?, cheatson = ?, language = ?");
        pstmt.setBoolean(1, inGame);
        pstmt.setInt(2, progression);
        pstmt.setString(3, progressionTuto);
        pstmt.setBoolean(4, cheatsOn);
        pstmt.setString(5, language);
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
