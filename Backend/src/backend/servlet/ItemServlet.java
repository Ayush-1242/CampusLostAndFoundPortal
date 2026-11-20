package backend.servlet;

import backend.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/items")
public class ItemServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
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

        String itemName = request.getParameter("itemName");
        String category = request.getParameter("category");
        String description = request.getParameter("description");
        String location = request.getParameter("location");
        String itemDate = request.getParameter("itemDate");
        String itemType = request.getParameter("itemType");

        if (itemName == null || category == null ||
                location == null || itemDate == null ||
                itemType == null ||
                itemName.isBlank() || category.isBlank() ||
                location.isBlank() || itemDate.isBlank()) {

            response.setStatus(400);
            response.getWriter().write(
                    "{\"message\":\"Please fill all required fields.\"}"
            );
            return;
        }

        itemType = itemType.trim().toUpperCase();

        if (!itemType.equals("LOST") && !itemType.equals("FOUND")) {
            response.setStatus(400);
            response.getWriter().write(
                    "{\"message\":\"Item type must be LOST or FOUND.\"}"
            );
            return;
        }

        try {
            Date date = Date.valueOf(itemDate.trim());

            int studentId = (Integer) session.getAttribute("studentId");

            String sql = "INSERT INTO items " +
                    "(student_id, item_name, category, description, " +
                    "location, item_date, item_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, studentId);
                statement.setString(2, itemName.trim());
                statement.setString(3, category.trim());
                statement.setString(4,
                        description == null ? "" : description.trim());
                statement.setString(5, location.trim());
                statement.setDate(6, date);
                statement.setString(7, itemType);

                statement.executeUpdate();

                response.setStatus(201);
                response.getWriter().write(
                        "{\"message\":\"" + itemType +
                                " item reported successfully!\"}"
                );
            }

        } catch (IllegalArgumentException e) {
            response.setStatus(400);
            response.getWriter().write(
                    "{\"message\":\"Invalid date. Use YYYY-MM-DD.\"}"
            );

        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().write(
                    "{\"message\":\"Database error occurred.\"}"
            );
        }
    }
}