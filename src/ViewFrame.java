import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.table.DefaultTableModel;

public class ViewFrame extends JFrame {
    private String loginId;
    private String userRole;
    private String nickname;
    private JTextField searchField;
    private JTable issueTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public ViewFrame(String loginId) {
        this.loginId = loginId;

        setTitle("Issue Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Issue Management System", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton homeButton = new JButton("Home");
        JButton logoutButton = new JButton("Logout");
        JButton viewButton = new JButton("View");
        JButton issueButton = new JButton("Issue");
        JButton statsButton = new JButton("Stats");

        homeButton.addActionListener(e -> {
            new HomeFrame(loginId).setVisible(true);
            dispose();
        });
        logoutButton.addActionListener(e -> logout());
        viewButton.addActionListener(e -> {
            new ViewFrame(loginId).setVisible(true);
            dispose();
        });
        issueButton.addActionListener(e -> {
            new IssueFrame(loginId).setVisible(true);
            dispose();
        });
        statsButton.addActionListener(e -> {
            // new StatsFrame(loginId).setVisible(true);
            dispose();
        });

        navPanel.add(homeButton);
        navPanel.add(viewButton);
        navPanel.add(issueButton);
        navPanel.add(statsButton);
        navPanel.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Search and Table Panel
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> loadIssues());

        searchPanel.add(new JLabel("State로 검색: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Issue", "Title", "Reporter", "State", "Priority", "Created"}, 0);
        issueTable = new JTable(tableModel);
        issueTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = issueTable.rowAtPoint(evt.getPoint());
                int col = issueTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == 0) {
                    int issueId = (int) issueTable.getValueAt(row, 0); // Ensure the issue ID is an Integer
                    new IssueDetailFrame(loginId, issueId).setVisible(true);
                    dispose();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(issueTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("");
        panel.add(statusLabel, BorderLayout.SOUTH);

        add(panel);

        loadUserInfo();
    }

    private void loadUserInfo() {
        try {
            URL url = new URL("http://localhost:8080/home/login/info?loginId=" + loginId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("role") && jsonResponse.has("nickname")) {
                    userRole = jsonResponse.getString("role");
                    nickname = jsonResponse.getString("nickname");

                    // Load issues based on user role
                    if ("TESTER".equals(userRole)) {
                        loadIssuesByReporter();
                    } else if ("DEV".equals(userRole)) {
                        loadIssuesByAssignee();
                    } else {
                        loadIssues();
                    }
                } else {
                    statusLabel.setText("Failed to load user info.");
                }
            } else {
                statusLabel.setText("Failed to load user info.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void loadIssues() {
        try {
            URL url = new URL("http://localhost:8080/issue/browse/1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("loadIssues Response Code: " + responseCode);
            if (responseCode == 200) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println("loadIssues Response Body: " + responseBody);

                JSONArray jsonResponse = new JSONArray(responseBody);
                tableModel.setRowCount(0); // Clear existing rows
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject issue = jsonResponse.getJSONObject(i);
                    tableModel.addRow(new Object[]{
                            issue.getInt("id"), // Ensure ID is added as Integer
                            issue.getString("title"),
                            issue.getString("reporter"),
                            issue.getString("status"), // Use "status" instead of "state" as per the API response
                            issue.getString("priority"),
                            issue.getString("reportedTime") // Use "reportedTime" instead of "created" as per the API response
                    });
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name());
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    System.out.println("loadIssues Error Response: " + errorResponse);
                }
                statusLabel.setText("Failed to load issues");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
            System.out.println("Exception in loadIssues: " + ex.getMessage());
        }
    }

    private void loadIssuesByReporter() {
        try {
            URL url = new URL("http://localhost:8080/issue/searchByReporter/1/" + nickname);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("loadIssuesByReporter Response Code: " + responseCode);
            if (responseCode == 200) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println("loadIssuesByReporter Response Body: " + responseBody);

                JSONArray jsonResponse = new JSONArray(responseBody);
                tableModel.setRowCount(0); // Clear existing rows
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject issue = jsonResponse.getJSONObject(i);
                    tableModel.addRow(new Object[]{
                            issue.getInt("id"), // Ensure ID is added as Integer
                            issue.getString("title"),
                            issue.getString("reporter"),
                            issue.getString("status"), // Use "status" instead of "state" as per the API response
                            issue.getString("priority"),
                            issue.getString("reportedTime") // Use "reportedTime" instead of "created" as per the API response
                    });
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name());
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    System.out.println("loadIssuesByReporter Error Response: " + errorResponse);
                }
                statusLabel.setText("Failed to load issues by reporter");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
            System.out.println("Exception in loadIssuesByReporter: " + ex.getMessage());
        }
    }

    private void loadIssuesByAssignee() {
        try {
            URL url = new URL("http://localhost:8080/issue/searchByAssignee/1/" + nickname);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("loadIssuesByAssignee Response Code: " + responseCode);
            if (responseCode == 200) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println("loadIssuesByAssignee Response Body: " + responseBody);

                JSONArray jsonResponse = new JSONArray(responseBody);
                tableModel.setRowCount(0); // Clear existing rows
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject issue = jsonResponse.getJSONObject(i);
                    tableModel.addRow(new Object[]{
                            issue.getInt("id"), // Ensure ID is added as Integer
                            issue.getString("title"),
                            issue.getString("reporter"),
                            issue.getString("status"), // Use "status" instead of "state" as per the API response
                            issue.getString("priority"),
                            issue.getString("reportedTime") // Use "reportedTime" instead of "created" as per the API response
                    });
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name());
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    System.out.println("loadIssuesByAssignee Error Response: " + errorResponse);
                }
                statusLabel.setText("Failed to load issues by assignee");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
            System.out.println("Exception in loadIssuesByAssignee: " + ex.getMessage());
        }
    }

    private void logout() {
        try {
            URL url = new URL("http://localhost:8080/home/logout");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("logout Response Code: " + responseCode);
            if (responseCode == 200) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println("logout Response Body: " + responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Logout successful")) {
                    dispose();
                    new LoginFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to log out.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name());
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    System.out.println("logout Error Response: " + errorResponse);
                }
                JOptionPane.showMessageDialog(this, "Failed to log out.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Exception in logout: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewFrame viewFrame = new ViewFrame("user");
            viewFrame.setVisible(true);
        });
    }
}
