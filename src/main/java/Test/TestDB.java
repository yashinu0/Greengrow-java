package Test;

import Utils.MyDB;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = MyDB.getInstance().getCon();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES FROM greengrow")) {

            System.out.println("Tables dans greengrow :");
            while (rs.next()) {
                System.out.println("- " + rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}