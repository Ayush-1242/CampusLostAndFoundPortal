package backend.servlet;

import backend.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/my-reports")
public class MyReportsServlet extends HttpServlet {

    private String escapeJson(String value) {
        if (value == null) return "";

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.setStatus(401);
            response.getWriter().write(
                    "{\"message\":\"Please login first.\"}"
            );
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        String sql = "SELECT item_id, item_name, category, description, " +
                "location, item_date, item_type, status " +
                "FROM items WHERE student_id = ? " +
                "ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            try (ResultSet result = statement.executeQuery()) {

                StringBuilder json = new StringBuilder("[");
                boolean first = true;

                while (result.next()) {

                    if (!first) json.append(",");
                    first = false;

                    json.append("{")
                            .append("\"itemId\":").append(result.getInt("item_id")).append(",")
                            .append("\"itemName\":\"").append(escapeJson(result.getString("item_name"))).append("\",")
                            .append("\"category\":\"").append(escapeJson(result.getString("category"))).append("\",")
                            .append("\"description\":\"").append(escapeJson(result.getString("description"))).append("\",")
                            .append("\"location\":\"").append(escapeJson(result.getString("location"))).append("\",")
                            .append("\"itemDate\":\"").append(result.getDate("item_date")).append("\",")
                            .append("\"itemType\":\"").append(result.getString("item_type")).append("\",")
                            .append("\"status\":\"").append(result.getString("status")).append("\"")
                            .append("}");
                }

                json.append("]");
                response.getWriter().write(json.toString());
            }

        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().write(
                    "{\"message\":\"Unable to retrieve your reports.\"}"
            );
        }
    }
}