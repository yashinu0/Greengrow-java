package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {

    String url="jdbc:mysql://localhost:3306/greengrow";
    String user="root";
    String password="";
    private Connection con;
    private static MyDB instanc;

    private MyDB() {
        try {
            con= DriverManager.getConnection(url,user,password);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.out.println(e.getMessage());;
        }

    }

    public static MyDB getInstance(){
        if(instanc == null){
            instanc= new MyDB();
        }
        return instanc;
    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }
}
