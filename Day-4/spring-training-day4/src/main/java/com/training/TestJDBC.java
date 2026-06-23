package com.training;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestJDBC {
    public static void main(String[] args) {
        Connection con= null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            List<Student> list = new ArrayList<>();
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc_training","root","root");
             statement = con.createStatement();
             resultSet = statement.executeQuery("select * from student");
            while (resultSet.next()){
                Student student = new Student(resultSet.getInt("id"), resultSet.getString("name"));
                list.add(student);
            }
            System.out.println(list);
        }catch (ClassNotFoundException exception){
            exception.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            if(con!=null){
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if(statement!=null){
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
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
