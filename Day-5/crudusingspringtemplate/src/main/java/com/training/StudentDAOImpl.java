package com.training;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class StudentDAOImpl implements StudentDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void save(Student student) {
        String sql = "insert into student(id, name) values(?,?)";
        int rows = jdbcTemplate.update(sql, student.getId(), student.getName());
        System.out.println(rows + " Inserted ..");
    }

    @Override
    public List<Student> findAll() {
        String sql = "select * from student";

        return jdbcTemplate.query(sql,
                (rs, rowNum) ->
                        new Student(rs.getInt("id"),
                                rs.getString("name")));
    }

    @Override
    public void update(Student student) {
        String sql = "update student set name =? where id =?";
        int row = jdbcTemplate.update(sql, student.getName(), student.getId());
        System.out.println(row + " Updated ... ");
    }

    @Override
    public void delete(int id) {
        String sql = "delete from student where id=?";
        int row = jdbcTemplate.update(sql, id);
        System.out.println(row + " is removed ");

    }
}
