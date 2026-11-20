package backend.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import backend.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/claims")
public class ClaimServlet extends HttpServlet {

    // POST: Submit a claim
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(
                    "{\"message\":\"Please login first.\"}");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        String itemIdText = request.getParameter("itemId");
        String claimDescription = request.getParameter("claimDescription");

        if (itemIdText == null || claimDescription == null
                || claimDescription.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(
                    "{\"message\":\"Required fields are missing.\"}");
            return;
        }

        int itemId;

        try {
            itemId = Integer.parseInt(itemIdText);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(
                    "{\"message\":\"Invalid item ID.\"}");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            // Check whether item is valid and claimable
            String checkQuery = """
                    SELECT student_id
                    FROM items
                    WHERE item_id = ?
                    AND item_type = 'FOUND'
                    AND status = 'OPEN'
                    """;

            try (PreparedStatement checkStatement =
                         connection.prepareStatement(checkQuery)) {

                checkStatement.setInt(1, itemId);

                try (ResultSet result = checkStatement.executeQuery()) {

                    if (!result.next()) {
                        response.setStatus(
                                HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println(
                                "{\"message\":\"Item is unavailable for claiming.\"}");
                        return;
                    }

                    int itemOwnerId = result.getInt("student_id");

                    if (itemOwnerId == studentId) {
                        response.setStatus(
                                HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().println(
                                "{\"message\":\"You cannot claim your own item.\"}");
                        return;
                    }
                }
            }

            // Prevent duplicate claims by the same student
            String duplicateQuery = """
                    SELECT claim_id
                    FROM claims
                    WHERE item_id = ?
                    AND student_id = ?
                    AND status = 'PENDING'
                    """;

            try (PreparedStatement duplicateStatement =
                         connection.prepareStatement(duplicateQuery)) {

                duplicateStatement.setInt(1, itemId);
                duplicateStatement.setInt(2, studentId);

                try (ResultSet result = duplicateStatement.executeQuery()) {

                    if (result.next()) {
                        response.setStatus(
                                HttpServletResponse.SC_CONFLICT);
                        response.getWriter().println(
                                "{\"message\":\"You already have a pending claim for this item.\"}");
                        return;
                    }
                }
            }

            // Insert claim
            String insertQuery = """
                    INSERT INTO claims
                    (item_id, student_id, claim_description)
                    VALUES (?, ?, ?)
                    """;

            try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertQuery)) {

                insertStatement.setInt(1, itemId);
                insertStatement.setInt(2, studentId);
                insertStatement.setString(
                        3, claimDescription.trim());

                insertStatement.executeUpdate();

                response.setStatus(
                        HttpServletResponse.SC_CREATED);

                response.getWriter().println(
                        "{\"message\":\"Claim submitted successfully!\"}");
            }

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().println(
                    "{\"message\":\"Claim submission failed.\"}");
        }
    }


    // GET: View claims submitted by the logged-in student
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(
                    "{\"message\":\"Please login first.\"}");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        String query = """
                SELECT c.claim_id, c.item_id, c.claim_description,
                       c.status, c.created_at, i.item_name,
                       i.location, i.item_date
                FROM claims c
                JOIN items i ON c.item_id = i.item_id
                WHERE c.student_id = ?
                ORDER BY c.created_at DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setInt(1, studentId);

            try (ResultSet result = statement.executeQuery()) {

                StringBuilder json = new StringBuilder("[");
                boolean first = true;

                while (result.next()) {

                    if (!first) {
                        json.append(",");
                    }

                    first = false;

                    json.append("{")
                            .append("\"claimId\":")
                            .append(result.getInt("claim_id"))
                            .append(",\"itemId\":")
                            .append(result.getInt("item_id"))
                            .append(",\"itemName\":\"")
                            .append(escape(result.getString("item_name")))
                            .append("\",\"claimDescription\":\"")
                            .append(escape(result.getString(
                                    "claim_description")))
                            .append("\",\"location\":\"")
                            .append(escape(result.getString("location")))
                            .append("\",\"itemDate\":\"")
                            .append(result.getDate("item_date"))
                            .append("\",\"status\":\"")
                            .append(result.getString("status"))
                            .append("\",\"createdAt\":\"")
                            .append(result.getTimestamp("created_at"))
                            .append("\"}");
                }

                json.append("]");

                response.getWriter().println(json);
            }

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().println(
                    "{\"message\":\"Unable to retrieve claims.\"}");
        }
    }


    // Escape special characters for JSON
    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}