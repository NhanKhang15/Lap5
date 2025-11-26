package com.student.controller;

import java.io.IOException;

import com.student.dao.UserDAO;
import com.student.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/change-password")
public class ChangePasswordController extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }

        request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }

        User user = (User) session.getAttribute("user");

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate input
        if (currentPassword == null || currentPassword.isEmpty() ||
                newPassword == null || newPassword.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {

            request.setAttribute("error", "All fields are required");
            request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
            return;
        }

        // Validate new password length
        if (newPassword.length() < 8) {
            request.setAttribute("error", "New password must be at least 8 characters long");
            request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
            return;
        }

        // Validate password match
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "New passwords do not match");
            request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
            return;
        }

        // Verify current password
        // Need to fetch fresh user data to get the hashed password
        User currentUser = userDAO.getUserById(user.getId());
        if (currentUser == null || !BCrypt.checkpw(currentPassword, currentUser.getPassword())) {
            request.setAttribute("error", "Incorrect current password");
            request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
            return;
        }

        // Hash new password
        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        // Update password
        boolean success = userDAO.updatePassword(user.getId(), newHashedPassword);

        if (success) {
            request.setAttribute("success", "Password changed successfully");
        } else {
            request.setAttribute("error", "Failed to update password. Please try again.");
        }

        request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
    }
}
