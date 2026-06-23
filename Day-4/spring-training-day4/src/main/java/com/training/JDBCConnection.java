package com.training;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCConnection {
    public static void main(String[] args) {

        List<Student> list = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc_training", "root", "root");
//                    Statement statement = con.createStatement();
                    PreparedStatement statement = con.prepareStatement("select * from student where id = ?");

//                    ResultSet resultSet = statement.executeQuery("select * from student");
            ) {
                statement.setInt(1, 102);
                ResultSet resultSet = statement.executeQuery();
                System.out.println("Connection is established ... ");
                while (resultSet.next()) {

                    list.add(new Student(resultSet.getInt("id"), resultSet.getString("name")));
                }
                System.out.println(list);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not found ");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
