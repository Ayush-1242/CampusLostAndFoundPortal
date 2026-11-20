package backend.servlet;

import backend.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {

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

        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");
        String itemType = request.getParameter("itemType");

        StringBuilder sql = new StringBuilder(
                "SELECT item_id, item_name, category, description, " +
                        "location, item_date, item_type, status " +
                        "FROM items WHERE 1=1"
        );

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND item_name LIKE ?");
        }

        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
        }

        if (itemType != null && !itemType.isBlank()) {
            itemType = itemType.trim().toUpperCase();

            if (!itemType.equals("LOST") && !itemType.equals("FOUND")) {
                response.setStatus(400);
                response.getWriter().write(
                        "{\"message\":\"Invalid item type.\"}"
                );
                return;
            }

            sql.append(" AND item_type = ?");
        }

        sql.append(" ORDER BY created_at DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            int index = 1;

            if (keyword != null && !keyword.isBlank()) {
                statement.setString(index++, "%" + keyword.trim() + "%");
            }

            if (category != null && !category.isBlank()) {
                statement.setString(index++, category.trim());
            }

            if (itemType != null && !itemType.isBlank()) {
                statement.setString(index++, itemType);
            }

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
                response.setStatus(200);
                response.getWriter().write(json.toString());
            }

        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().write(
                    "{\"message\":\"Unable to search items.\"}"
            );
        }
    }
}