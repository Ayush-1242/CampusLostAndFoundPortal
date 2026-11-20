package backend.servlet;

import backend.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private boolean verifyPassword(String password, String storedHash)
            throws Exception {

        String[] parts = storedHash.split(":");

        if (parts.length != 2) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] originalHash = Base64.getDecoder().decode(parts[1]);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, 210000, 256
        );

        byte[] enteredHash;

        try {
            enteredHash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } finally {
            spec.clearPassword();
        }

        return MessageDigest.isEqual(originalHash, enteredHash);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");

        if (identifier == null || password == null ||
                identifier.isBlank() || password.isBlank()) {

            response.setStatus(400);
            response.getWriter().write(
                    "{\"message\":\"USN/email and password are required.\"}"
            );
            return;
        }

        String sql = "SELECT student_id, full_name, usn, password " +
                "FROM students WHERE usn = ? OR email = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, identifier.trim());
            statement.setString(2, identifier.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    response.setStatus(401);
                    response.getWriter().write(
                            "{\"message\":\"Invalid credentials.\"}"
                    );
                    return;
                }

                String storedHash = result.getString("password");

                if (!verifyPassword(password, storedHash)) {
                    response.setStatus(401);
                    response.getWriter().write(
                            "{\"message\":\"Invalid credentials.\"}"
                    );
                    return;
                }

                int studentId = result.getInt("student_id");
                String name = result.getString("full_name");
                String usn = result.getString("usn");

                HttpSession session = request.getSession(true);
                session.setAttribute("studentId", studentId);
                session.setAttribute("studentName", name);
                session.setAttribute("studentUSN", usn);

                response.setStatus(200);
                response.getWriter().write(
                        "{\"message\":\"Login successful!\"," +
                                "\"studentId\":" + studentId + "," +
                                "\"fullName\":\"" + escapeJson(name) + "\"," +
                                "\"usn\":\"" + escapeJson(usn) + "\"}"
                );
            }

        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write(
                    "{\"message\":\"Login processing failed.\"}"
            );
        }
    }
}
