package com.training;

import java.util.List;

public interface StudentDAO {
    void save(Student student);
    List<Student> findAll();
    void update(Student student);
    void delete(int id);
}
