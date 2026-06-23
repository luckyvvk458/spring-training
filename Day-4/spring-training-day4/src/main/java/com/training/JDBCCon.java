package com.training;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCCon {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/jdbc_training";
        String uname = "root";
        String password = "root";
        List<Student> list = new ArrayList<>();
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            // Load mysql driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
            //get connection from driver manager
            con = DriverManager.getConnection(url, uname, password);
            System.out.println("Connection established successfully ...." + con);
            statement = con.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM student");
            while (resultSet.next()) {
                Student student = new Student(
                        resultSet.getInt("id"), resultSet.getString("name"));
                list.add(student);
            }
            System.out.println(list);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                if(statement!=null) {
                    statement.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(resultSet!=null){
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
