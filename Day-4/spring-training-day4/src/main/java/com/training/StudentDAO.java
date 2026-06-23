package com.training;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StudentDAO {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void saveStudnet(Student student) {
        String sql = "insert into student(id, name) values (?,?)";
        int row = jdbcTemplate.update(sql, student.getId(), student.getName());
        System.out.println("Student updated successfully ..." + row + " Updated successully ");
    }
}
