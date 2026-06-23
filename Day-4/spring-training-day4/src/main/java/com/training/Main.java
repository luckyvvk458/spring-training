package com.training;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/jdbc_training";
        String username = "root";
        String password = "root";

        String sql = "SELECT * FROM student";

        try (
                Connection con = DriverManager.getConnection(url, username, password);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {

            while (rs.next()) {

                int id = rs.getInt("id");
                String name = rs.getString("name");
                String city = rs.getString("city");

                System.out.println(id + " " + name + " " + city);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}