import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class IssueDetailFrame extends JFrame {
    private String loginId;
    private int issueId;
    private String plNickname;
    private JLabel statusLabel;
    private JTextArea commentArea;
    private JTextField assigneeField;
    private JComboBox<String> assigneeComboBox;
    private String userRole;
    private JLabel issueIdLabel;
    private JLabel statusValueLabel;
    private JLabel titleValueLabel;
    private JLabel reporterValueLabel;
    private JLabel priorityValueLabel;
    private JLabel tagValueLabel;
    private JLabel fixerValueLabel;
    private JTextArea descriptionArea;

    public IssueDetailFrame(String loginId, int issueId) {
        this.loginId = loginId;
        this.issueId = issueId;

        setTitle("Issue Detail");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel(), BorderLayout.NORTH);
        panel.add(createContentPanel(), BorderLayout.CENTER);

        add(panel);

        loadUserRoleAndNickname();
        loadIssueDetails();
        loadAssigneeOptions();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Issue Management System", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        String[] buttons = {"Home", "Logout", "View", "Issue", "Stats"};
        for (String btnText : buttons) {
            JButton button = new JButton(btnText);
            button.addActionListener(e -> handleNavButtonClick(btnText));
            navPanel.add(button);
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridLayout(10, 2));

        issueIdLabel = new JLabel("#" + issueId);
        statusValueLabel = new JLabel();
        titleValueLabel = new JLabel();
        reporterValueLabel = new JLabel();
        assigneeField = new JTextField();
        assigneeField.setEditable(false);
        assigneeComboBox = new JComboBox<>();
        priorityValueLabel = new JLabel();
        tagValueLabel = new JLabel();
        fixerValueLabel = new JLabel();
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);

        commentArea = new JTextArea();
        statusLabel = new JLabel("");

        addLabelAndComponent(contentPanel, "Issue ID:", issueIdLabel);
        addLabelAndComponent(contentPanel, "Status:", statusValueLabel);
        addLabelAndComponent(contentPanel, "Title:", titleValueLabel);
        addLabelAndComponent(contentPanel, "Reporter:", reporterValueLabel);
        addLabelAndComponent(contentPanel, "Assignee:", assigneeField);
        addLabelAndComponent(contentPanel, "Change Assignee:", assigneeComboBox);
        addLabelAndComponent(contentPanel, "Priority:", priorityValueLabel);
        addLabelAndComponent(contentPanel, "Tag:", tagValueLabel);
        addLabelAndComponent(contentPanel, "Fixer:", fixerValueLabel);
        addLabelAndComponent(contentPanel, "Description:", new JScrollPane(descriptionArea));
        addLabelAndComponent(contentPanel, "Comment:", new JScrollPane(commentArea));

        addButton(contentPanel, "Add Comment", e -> addComment());
        addButton(contentPanel, "Change Status", e -> changeIssueStatus());
        addButton(contentPanel, "Change Assignee", e -> changeAssignee());
        addButton(contentPanel, "코드수정", e -> fixCode());

        contentPanel.add(statusLabel);

        return contentPanel;
    }

    private void addLabelAndComponent(JPanel panel, String labelText, JComponent component) {
        panel.add(new JLabel(labelText));
        panel.add(component);
    }

    private void addButton(JPanel panel, String buttonText, ActionListener actionListener) {
        JButton button = new JButton(buttonText);
        button.addActionListener(actionListener);
        panel.add(button);
    }

    private void handleNavButtonClick(String buttonText) {
        JFrame targetFrame;
        switch (buttonText) {
            case "Home":
                targetFrame = new HomeFrame(loginId);
                break;
            case "Logout":
                logout();
                return;
            case "View":
                targetFrame = new ViewFrame(loginId);
                break;
            case "Issue":
                targetFrame = new IssueFrame(loginId);
                break;
            case "Stats":
                // Implement StatsFrame if needed
                return;
            default:
                return;
        }
        targetFrame.setVisible(true);
        dispose();
    }

    private void loadUserRoleAndNickname() {
        try {
            URL url = new URL("http://localhost:8080/home/login/info?loginId=" + loginId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                JSONObject jsonResponse = new JSONObject(getResponseString(connection));
                if (jsonResponse.has("role") && jsonResponse.has("nickname")) {
                    userRole = jsonResponse.getString("role");
                    plNickname = jsonResponse.getString("nickname");
                } else {
                    statusLabel.setText("Failed to load user role or nickname.");
                }
            } else {
                statusLabel.setText("Failed to load user role or nickname.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void loadIssueDetails() {
        try {
            URL url = new URL("http://localhost:8080/issue/" + issueId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                JSONObject jsonResponse = new JSONObject(getResponseString(connection));

                issueIdLabel.setText("#" + jsonResponse.getInt("id"));
                statusValueLabel.setText(jsonResponse.getString("status"));
                titleValueLabel.setText(jsonResponse.getString("title"));
                reporterValueLabel.setText(jsonResponse.getString("reporter"));
                assigneeField.setText(jsonResponse.optString("assignee", "-"));
                priorityValueLabel.setText(jsonResponse.getString("priority"));
                tagValueLabel.setText(jsonResponse.getJSONArray("tags").getJSONObject(0).getString("category"));
                fixerValueLabel.setText(jsonResponse.optString("fixer", "-"));
                descriptionArea.setText(jsonResponse.getString("description"));

                System.out.println("Loaded issue details: " + jsonResponse.toString());
            } else {
                statusLabel.setText("Failed to load issue details.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void loadAssigneeOptions() {
        try {
            URL url = new URL("http://localhost:8080/home/userlist");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                JSONArray jsonResponse = new JSONArray(getResponseString(connection));
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject user = jsonResponse.getJSONObject(i);
                    if ("DEV".equals(user.getString("role"))) {
                        assigneeComboBox.addItem(user.getString("nickname"));
                    }
                }
            } else {
                statusLabel.setText("Failed to load assignee options.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void changeIssueStatus() {
        try {
            URL url = new URL("http://localhost:8080/issue/changeStatus/" + issueId);
            HttpURLConnection connection = createPostConnection(url);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("nickname", plNickname);
            jsonInput.put("status", getStatusForRole());

            sendRequest(connection, jsonInput);

            if (isResponseSuccessful(connection)) {
                statusLabel.setText("Issue status updated successfully.");
                loadIssueDetails();  // Refresh the issue details
            } else {
                statusLabel.setText("Failed to update issue status.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void changeAssignee() {
        try {
            URL url = new URL("http://localhost:8080/issue/changeAssignee/" + issueId);
            HttpURLConnection connection = createPostConnection(url);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("nickname", (String) assigneeComboBox.getSelectedItem());
            jsonInput.put("pl", plNickname);

            sendRequest(connection, jsonInput);

            if (isResponseSuccessful(connection)) {
                changeAssignedStatus();
            } else {
                statusLabel.setText("Failed to change assignee.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void changeAssignedStatus() {
        try {
            URL url = new URL("http://localhost:8080/issue/changeAssigned/" + issueId);
            HttpURLConnection connection = createPostConnection(url);

            sendRequest(connection, new JSONObject());

            if (isResponseSuccessful(connection)) {
                statusLabel.setText("Issue status assigned successfully.");
                loadIssueDetails();  // Refresh the issue details
            } else {
                statusLabel.setText("Failed to assign issue status.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void fixCode() {
        changeIssueStatus();
        changeIssueFixer();
    }

    private void changeIssueFixer() {
        try {
            URL url = new URL("http://localhost:8080/issue/changeFixer/" + issueId);
            HttpURLConnection connection = createPostConnection(url);

            sendRequest(connection, new JSONObject());

            if (isResponseSuccessful(connection)) {
                statusLabel.setText("Issue fixer updated successfully.");
                loadIssueDetails();  // Refresh the issue details
            } else {
                statusLabel.setText("Failed to update issue fixer.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void addComment() {
        try {
            URL url = new URL("http://localhost:8080/comment/add");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("content", commentArea.getText());
            jsonInput.put("issueId", issueId);
            jsonInput.put("sender", plNickname);

            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                statusLabel.setText("Comment added successfully.");
                commentArea.setText("");  // Clear the comment area
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name());
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    System.out.println("Error Response: " + errorResponse);
                }
                statusLabel.setText("Failed to add comment.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void logout() {
        try {
            URL url = new URL("http://localhost:8080/home/logout");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                JSONObject jsonResponse = new JSONObject(getResponseString(connection));
                if (jsonResponse.has("message") && "Logout successful".equals(jsonResponse.getString("message"))) {
                    dispose();
                    new LoginFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to log out.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to log out.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private HttpURLConnection createPostConnection(URL url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

    private void sendRequest(HttpURLConnection connection, JSONObject jsonInput) throws Exception {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private boolean isResponseSuccessful(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);  // Print the response code
        if (responseCode == 200) {
            String responseBody = getResponseString(connection);
            System.out.println("Response Body: " + responseBody);  // Print the response body
            return responseBody.contains("성공적으로");
        } else {
            return false;
        }
    }

    private String getResponseString(HttpURLConnection connection) throws Exception {
        try (InputStream responseStream = connection.getInputStream();
             Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private String getStatusForRole() {
        switch (userRole) {
            case "DEV":
                return "fixed";
            case "TESTER":
                return "resolved";
            case "PL":
                return "closed";
            default:
                return "";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IssueDetailFrame issueDetailFrame = new IssueDetailFrame("user", 1);
            issueDetailFrame.setVisible(true);
        });
    }
}
