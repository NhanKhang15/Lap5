package com.student.controller;

import com.student.dao.StudentDAO;
import com.student.model.Student;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/student")
public class StudentController extends HttpServlet {

    private StudentDAO studentDAO;

    @Override
    public void init() {
        studentDAO = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "new":
                showNewForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteStudent(request, response);
                break;
            case "search":
                searchStudents(request, response);
                break;
            case "sort":
                sortStudents(request, response);
                break;
            case "filter":
                filterStudents(request, response);
                break;
            default:
                listStudents(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        switch (action) {
            case "insert":
                insertStudent(request, response);
                break;
            case "update":
                updateStudent(request, response);
                break;
        }
    }

    // List all students
    private void listStudents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = 1;
        int recordsPerPage = 5;

        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int offset = (page - 1) * recordsPerPage;

        List<Student> students = studentDAO.getStudentsPaginated(offset, recordsPerPage);
        int noOfRecords = studentDAO.getTotalStudents();

        int noOfPages = (int) Math.ceil(noOfRecords * 1.0 / recordsPerPage);

        request.setAttribute("students", students);
        request.setAttribute("noOfPages", noOfPages);
        request.setAttribute("currentPage", page);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
        dispatcher.forward(request, response);
    }

    // Show form for new student
    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
        dispatcher.forward(request, response);
    }

    // Show form for editing student
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        Student existingStudent = studentDAO.getStudentById(id);

        request.setAttribute("student", existingStudent);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
        dispatcher.forward(request, response);
    }

    // Insert new student
    private void insertStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String studentCode = request.getParameter("studentCode");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String major = request.getParameter("major");

        Student newStudent = new Student(studentCode, fullName, email, major);

        // VALIDATION CHECK
        if (!validateStudent(newStudent, request)) {
            request.setAttribute("student", newStudent);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
            dispatcher.forward(request, response);
            return;
        }

        if (studentDAO.addStudent(newStudent)) {
            response.sendRedirect("student?action=list&message=Student added successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to add student");
        }
    }

    // Update student
    private void updateStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendRedirect("student?action=list&error=Invalid Student ID");
            return;
        }

        String studentCode = request.getParameter("studentCode");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String major = request.getParameter("major");

        Student student = new Student(studentCode, fullName, email, major);
        student.setId(id);

        // VALIDATION CHECK
        if (!validateStudent(student, request)) {
            request.setAttribute("student", student);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
            dispatcher.forward(request, response);
            return;
        }

        if (studentDAO.updateStudent(student)) {
            response.sendRedirect("student?action=list&message=Student updated successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to update student");
        }
    }

    // Delete student
    private void deleteStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        if (studentDAO.deleteStudent(id)) {
            response.sendRedirect("student?action=list&message=Student deleted successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to delete student");
        }
    }

    // Search Student
    private void searchStudents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Student> list = studentDAO.searchStudents(keyword);
        request.setAttribute("students", list);
        request.setAttribute("keyword", keyword); // Send back to keep in search box
        RequestDispatcher dispatcher = request.getRequestDispatcher("views/student-list.jsp");
        dispatcher.forward(request, response);
    }

    // Validate
    private boolean validateStudent(Student student, HttpServletRequest request) {
        boolean isValid = true;

        String codePattern = "[A-Z]{2}[0-9]{3,}";
        if (student.getStudentCode() == null || !student.getStudentCode().matches(codePattern)) {
            request.setAttribute("errorCode", "Invalid Code (Format: SV001)");
            isValid = false;
        }

        if (student.getFullName() == null || student.getFullName().length() < 2) {
            request.setAttribute("errorName", "Name must be at least 2 characters");
            isValid = false;
        }

        if (student.getMajor() == null || student.getMajor().isEmpty()) {
            request.setAttribute("errorMajor", "Major is required");
            isValid = false;
        }

        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (student.getEmail() != null && !student.getEmail().isEmpty()) {
            if (!student.getEmail().matches(emailPattern)) {
                request.setAttribute("errorEmail", "Invalid email format");
                isValid = false;
            }
        }

        return isValid;
    }

    // Handle sorting
    private void sortStudents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sortBy = request.getParameter("sortBy");
        String order = request.getParameter("order");

        List<Student> list = studentDAO.getStudentsSorted(sortBy, order);

        request.setAttribute("students", list);
        request.setAttribute("sortBy", sortBy); // Keep track of current sort column
        request.setAttribute("order", order);   // Keep track of current order

        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
        dispatcher.forward(request, response);
    }

    // Handle filtering
    private void filterStudents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String major = request.getParameter("major");

        if (major == null || major.isEmpty()) {
            response.sendRedirect("student?action=list");
            return;
        }

        List<Student> list = studentDAO.getStudentsByMajor(major);

        request.setAttribute("students", list);
        request.setAttribute("selectedMajor", major); // Keep the dropdown selected

        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
        dispatcher.forward(request, response);
    }
}


