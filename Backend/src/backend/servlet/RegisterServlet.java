package backend.servlet;

import backend.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private String hashPassword(String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, 210000, 256
        );

        byte[] hash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        return Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String name = request.getParameter("fullName");
        String usn = request.getParameter("usn");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (name == null || usn == null ||
                email == null || password == null ||
                name.isBlank() || usn.isBlank() ||
                email.isBlank() || password.isBlank()) {

            response.setStatus(400);
            response.getWriter().write(
                    "{\"message\":\"All fields are required.\"}"
            );
            return;
        }

        if (password.length() < 8) {
            response.setStatus(400);
            response.getWriter().write(
                    "{\"message\":\"Password must contain at least 8 characters.\"}"
            );
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            response.setStatus(400);
            response.getWriter().write(
                    "{\"message\":\"Enter a valid email address.\"}"
            );
            return;
        }

        String sql = "INSERT INTO students " +
                "(full_name, usn, email, password) VALUES (?, ?, ?, ?)";

        try {
            String hashedPassword = hashPassword(password);

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, name.trim());
                statement.setString(2, usn.trim());
                statement.setString(3, email.trim());
                statement.setString(4, hashedPassword);

                statement.executeUpdate();

                response.setStatus(201);
                response.getWriter().write(
                        "{\"message\":\"Registration successful!\"}"
                );
            }

        } catch (SQLException e) {

            if ("23000".equals(e.getSQLState())) {
                response.setStatus(409);
                response.getWriter().write(
                        "{\"message\":\"USN or email already registered.\"}"
                );
            } else {
                response.setStatus(500);
                response.getWriter().write(
                        "{\"message\":\"Database error occurred.\"}"
                );
            }

        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write(
                    "{\"message\":\"Password processing failed.\"}"
            );
        }
    }
}
