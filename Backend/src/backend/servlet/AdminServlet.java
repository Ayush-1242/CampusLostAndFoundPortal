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
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    // ROUTING
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        String path = request.getPathInfo();

        if (path == null) {
            send(response, 404, "Invalid admin endpoint.");
            return;
        }

        switch (path) {
            case "/setup":
                setupAdmin(request, response);
                break;

            case "/login":
                loginAdmin(request, response);
                break;

            case "/claims/update":
                updateClaim(request, response);
                break;

            case "/items/update":
                updateItem(request, response);
                break;

            case "/items/delete":
                deleteItem(request, response);
                break;

            default:
                send(response, 404, "Endpoint not found.");
        }
    }


    // GET ROUTING
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        String path = request.getPathInfo();

        if (path == null) {
            send(response, 404, "Invalid admin endpoint.");
            return;
        }

        if (!isAdminLoggedIn(request)) {
            send(response, 401, "Admin login required.");
            return;
        }

        switch (path) {
            case "/items":
                getAllItems(response);
                break;

            case "/claims":
                getAllClaims(response);
                break;

            default:
                send(response, 404, "Endpoint not found.");
        }
    }


    // INITIAL ADMIN SETUP - ONLY WHEN TABLE IS EMPTY
    private void setupAdmin(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null
                || username.trim().isEmpty()
                || password.length() < 8) {

            send(response, 400,
                    "Username required and password must be at least 8 characters.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            String checkQuery = "SELECT COUNT(*) FROM admins";

            try (PreparedStatement check =
                         connection.prepareStatement(checkQuery);
                 ResultSet result = check.executeQuery()) {

                result.next();

                if (result.getInt(1) > 0) {
                    send(response, 403,
                            "Admin setup is already completed.");
                    return;
                }
            }

            String hashedPassword = hashPassword(password);

            String insertQuery =
                    "INSERT INTO admins (username, password) VALUES (?, ?)";

            try (PreparedStatement statement =
                         connection.prepareStatement(insertQuery)) {

                statement.setString(1, username.trim());
                statement.setString(2, hashedPassword);
                statement.executeUpdate();

                send(response, 201, "Admin account created successfully!");
            }

        } catch (Exception e) {
            send(response, 500, "Admin setup failed.");
        }
    }


    // ADMIN LOGIN
    private void loginAdmin(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            send(response, 400, "Username and password required.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            String query =
                    "SELECT admin_id, password FROM admins WHERE username = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(query)) {

                statement.setString(1, username.trim());

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {
                        send(response, 401, "Invalid admin credentials.");
                        return;
                    }

                    String storedPassword =
                            result.getString("password");

                    if (!verifyPassword(password, storedPassword)) {
                        send(response, 401,
                                "Invalid admin credentials.");
                        return;
                    }

                    int adminId = result.getInt("admin_id");

                    HttpSession oldSession =
                            request.getSession(false);

                    if (oldSession != null) {
                        oldSession.invalidate();
                    }

                    HttpSession session =
                            request.getSession(true);

                    session.setAttribute("adminId", adminId);
                    session.setAttribute("adminUsername", username.trim());

                    send(response, 200, "Admin login successful!");
                }
            }

        } catch (Exception e) {
            send(response, 500, "Admin login failed.");
        }
    }


    // VIEW ALL ITEMS
    private void getAllItems(HttpServletResponse response)
            throws IOException {

        String query = """
                SELECT i.item_id, i.student_id, s.full_name,
                       i.item_name, i.category, i.description,
                       i.location, i.item_date, i.item_type,
                       i.status, i.created_at
                FROM items i
                JOIN students s ON i.student_id = s.student_id
                ORDER BY i.created_at DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (result.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                json.append("{")
                        .append("\"itemId\":")
                        .append(result.getInt("item_id"))
                        .append(",\"studentId\":")
                        .append(result.getInt("student_id"))
                        .append(",\"studentName\":\"")
                        .append(escape(result.getString("full_name")))
                        .append("\",\"itemName\":\"")
                        .append(escape(result.getString("item_name")))
                        .append("\",\"category\":\"")
                        .append(escape(result.getString("category")))
                        .append("\",\"description\":\"")
                        .append(escape(result.getString("description")))
                        .append("\",\"location\":\"")
                        .append(escape(result.getString("location")))
                        .append("\",\"itemDate\":\"")
                        .append(result.getDate("item_date"))
                        .append("\",\"itemType\":\"")
                        .append(result.getString("item_type"))
                        .append("\",\"status\":\"")
                        .append(result.getString("status"))
                        .append("\",\"createdAt\":\"")
                        .append(result.getTimestamp("created_at"))
                        .append("\"}");
            }

            json.append("]");

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(json);

        } catch (Exception e) {
            send(response, 500, "Unable to retrieve items.");
        }
    }


    // VIEW ALL CLAIMS
    private void getAllClaims(HttpServletResponse response)
            throws IOException {

        String query = """
                SELECT c.claim_id, c.item_id, c.student_id,
                       s.full_name, s.usn, i.item_name,
                       c.claim_description, c.status,
                       c.created_at
                FROM claims c
                JOIN students s ON c.student_id = s.student_id
                JOIN items i ON c.item_id = i.item_id
                ORDER BY c.created_at DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

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
                        .append(",\"studentId\":")
                        .append(result.getInt("student_id"))
                        .append(",\"studentName\":\"")
                        .append(escape(result.getString("full_name")))
                        .append("\",\"usn\":\"")
                        .append(escape(result.getString("usn")))
                        .append("\",\"itemName\":\"")
                        .append(escape(result.getString("item_name")))
                        .append("\",\"claimDescription\":\"")
                        .append(escape(result.getString(
                                "claim_description")))
                        .append("\",\"status\":\"")
                        .append(result.getString("status"))
                        .append("\",\"createdAt\":\"")
                        .append(result.getTimestamp("created_at"))
                        .append("\"}");
            }

            json.append("]");

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(json);

        } catch (Exception e) {
            send(response, 500, "Unable to retrieve claims.");
        }
    }


    // APPROVE OR REJECT CLAIM
    private void updateClaim(HttpServletRequest request,
                             HttpServletResponse response)
            throws IOException {

        if (!isAdminLoggedIn(request)) {
            send(response, 401, "Admin login required.");
            return;
        }

        String claimIdText = request.getParameter("claimId");
        String action = request.getParameter("action");

        if (claimIdText == null || action == null) {
            send(response, 400, "Required parameters missing.");
            return;
        }

        action = action.toUpperCase();

        if (!action.equals("APPROVE") && !action.equals("REJECT")) {
            send(response, 400, "Action must be APPROVE or REJECT.");
            return;
        }

        int claimId;

        try {
            claimId = Integer.parseInt(claimIdText);
        } catch (NumberFormatException e) {
            send(response, 400, "Invalid claim ID.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                String claimQuery = """
                        SELECT item_id, status
                        FROM claims
                        WHERE claim_id = ?
                        FOR UPDATE
                        """;

                int itemId;
                String currentStatus;

                try (PreparedStatement statement =
                             connection.prepareStatement(claimQuery)) {

                    statement.setInt(1, claimId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            send(response, 404, "Claim not found.");
                            return;
                        }

                        itemId = result.getInt("item_id");
                        currentStatus = result.getString("status");
                    }
                }

                if (!currentStatus.equals("PENDING")) {
                    connection.rollback();
                    send(response, 409,
                            "This claim has already been processed.");
                    return;
                }

                if (action.equals("APPROVE")) {

                    String itemQuery = """
                            SELECT status
                            FROM items
                            WHERE item_id = ?
                            FOR UPDATE
                            """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(itemQuery)) {

                        statement.setInt(1, itemId);

                        try (ResultSet result = statement.executeQuery()) {

                            if (!result.next()
                                    || !result.getString("status")
                                    .equals("OPEN")) {

                                connection.rollback();
                                send(response, 409,
                                        "Item is no longer open.");
                                return;
                            }
                        }
                    }

                    // Approve selected claim
                    String approveQuery = """
                            UPDATE claims
                            SET status = 'APPROVED'
                            WHERE claim_id = ?
                            """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(approveQuery)) {

                        statement.setInt(1, claimId);
                        statement.executeUpdate();
                    }

                    // Reject other pending claims for same item
                    String rejectOthersQuery = """
                            UPDATE claims
                            SET status = 'REJECTED'
                            WHERE item_id = ?
                            AND claim_id <> ?
                            AND status = 'PENDING'
                            """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         rejectOthersQuery)) {

                        statement.setInt(1, itemId);
                        statement.setInt(2, claimId);
                        statement.executeUpdate();
                    }

                    // Mark item claimed
                    String itemUpdate = """
                            UPDATE items
                            SET status = 'CLAIMED'
                            WHERE item_id = ?
                            """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(itemUpdate)) {

                        statement.setInt(1, itemId);
                        statement.executeUpdate();
                    }

                } else {

                    // Reject selected claim
                    String rejectQuery = """
                            UPDATE claims
                            SET status = 'REJECTED'
                            WHERE claim_id = ?
                            """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(rejectQuery)) {

                        statement.setInt(1, claimId);
                        statement.executeUpdate();
                    }
                }

                connection.commit();

                send(response, 200,
                        "APPROVE".equals(action)
                                ? "Claim approved successfully!"
                                : "Claim rejected successfully!");

            } catch (Exception e) {
                connection.rollback();
                send(response, 500, "Claim update failed.");
            }

        } catch (Exception e) {
            send(response, 500, "Claim update failed.");
        }
    }


    // UPDATE ITEM STATUS
    private void updateItem(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {

        if (!isAdminLoggedIn(request)) {
            send(response, 401, "Admin login required.");
            return;
        }

        String itemIdText = request.getParameter("itemId");
        String status = request.getParameter("status");

        if (itemIdText == null || status == null) {
            send(response, 400, "Required parameters missing.");
            return;
        }

        status = status.toUpperCase();

        if (!status.equals("OPEN")
                && !status.equals("CLAIMED")
                && !status.equals("CLOSED")) {

            send(response, 400, "Invalid item status.");
            return;
        }

        int itemId;

        try {
            itemId = Integer.parseInt(itemIdText);
        } catch (NumberFormatException e) {
            send(response, 400, "Invalid item ID.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            String query =
                    "UPDATE items SET status = ? WHERE item_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(query)) {

                statement.setString(1, status);
                statement.setInt(2, itemId);

                int rows = statement.executeUpdate();

                if (rows == 0) {
                    send(response, 404, "Item not found.");
                    return;
                }

                send(response, 200, "Item status updated successfully!");
            }

        } catch (Exception e) {
            send(response, 500, "Item update failed.");
        }
    }


    // DELETE ITEM AND ITS CLAIMS
    private void deleteItem(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {

        if (!isAdminLoggedIn(request)) {
            send(response, 401, "Admin login required.");
            return;
        }

        String itemIdText = request.getParameter("itemId");

        if (itemIdText == null) {
            send(response, 400, "Item ID required.");
            return;
        }

        int itemId;

        try {
            itemId = Integer.parseInt(itemIdText);
        } catch (NumberFormatException e) {
            send(response, 400, "Invalid item ID.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Delete related claims first
                String deleteClaims =
                        "DELETE FROM claims WHERE item_id = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteClaims)) {

                    statement.setInt(1, itemId);
                    statement.executeUpdate();
                }

                // Delete item
                String deleteItem =
                        "DELETE FROM items WHERE item_id = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteItem)) {

                    statement.setInt(1, itemId);

                    int rows = statement.executeUpdate();

                    if (rows == 0) {
                        connection.rollback();
                        send(response, 404, "Item not found.");
                        return;
                    }
                }

                connection.commit();
                send(response, 200, "Item deleted successfully!");

            } catch (Exception e) {
                connection.rollback();
                send(response, 500, "Item deletion failed.");
            }

        } catch (Exception e) {
            send(response, 500, "Item deletion failed.");
        }
    }


    // ADMIN SESSION CHECK
    private boolean isAdminLoggedIn(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        return session != null
                && session.getAttribute("adminId") != null;
    }


    // PASSWORD HASHING
    private String hashPassword(String password)
            throws Exception {

        byte[] salt = new byte[16];

        new SecureRandom().nextBytes(salt);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                210000,
                256
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }


    // PASSWORD VERIFICATION
    private boolean verifyPassword(String password,
                                   String storedPassword)
            throws Exception {

        String[] parts = storedPassword.split(":");

        if (parts.length != 2) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                210000,
                256
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] actualHash = factory.generateSecret(spec).getEncoded();

        return MessageDigest.isEqual(expectedHash, actualHash);
    }


    // JSON ESCAPING
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


    // RESPONSE HELPER
    private void send(HttpServletResponse response,
                      int status,
                      String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String escaped = escape(message);

        response.getWriter().println(
                "{\"message\":\"" + escaped + "\"}");
    }
}
