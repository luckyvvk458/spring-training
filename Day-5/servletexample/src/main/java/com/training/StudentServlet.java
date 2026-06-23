package com.training;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/student")
public class StudentServlet extends HttpServlet {

    @Override
    public void init() {
        System.out.println("StudentServlet Loaded");
    }

    @Override
    public void doGet(HttpServletRequest httpServletRequest,
                      HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        httpServletResponse.setContentType("text/html");
        httpServletResponse.getWriter().println("<h2>Id : 101</h2>");
        httpServletResponse.getWriter().println("<h2>Name : Rahul</h2>");
        System.out.println("Do Get method is called ..");

    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {
        // Plumbing 1
        String id = request.getParameter("id");
        // Plumbing 2
        String name = request.getParameter("name");
        // Plumbing 3
        int studentId = Integer.parseInt(id);
        // Plumbing 4
        Student student = new Student();
        student.setId(studentId);
        student.setName(name);

//        // Plumbing 5
//        StudentService service =
//                new StudentService();
//
//        service.save(student);

        // Plumbing 6
        response.setContentType("text/html");

        // Plumbing 7
        response.getWriter()
                .println("Student Saved");
    }

    @Override
    public void destroy() {
        System.out.println("destroy() called - Servlet Destroyed");
    }

}
