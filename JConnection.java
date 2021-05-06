
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import oracle.jdbc.OracleDriver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author anvis
 */
public class JConnection {
    private static Connection con;
    
    public void setConnection() {
        con = null;
        String connection_str = "jdbc:oracle:thin:@localhost:1521:XE";
        String username = "roohi";
        String password = "root";
        // Connect To Oracle Database with user/password
        try {
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", username, password);
        } catch (Exception e) {
            System.out.println("Exception found"+e.toString());
        }
        return;
    }
    
    public Connection getConnection()
    {
        return con;
    }
}
