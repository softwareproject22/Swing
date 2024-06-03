import javax.swing.*;
import java.awt.*;
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
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Issue Management System", SwingConstants.LEFT);
        titleLabel.setFont(new Font("돋움", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 87, 34));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setBackground(Color.WHITE);

        String[] buttons = {"Home", "Logout", "View", "Issue", "Stats"};
        for (String btnText : buttons) {
            JButton button = createNavButton(btnText);
            navPanel.add(button);
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("돋움", Font.PLAIN, 14));
        button.setForeground(new Color(102, 102, 102));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.addActionListener(e -> handleNavButtonClick(text));
        return button;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        issueIdLabel = new JLabel("#" + issueId);
        issueIdLabel.setFont(new Font("돋움", Font.BOLD, 18));
        issueIdLabel.setForeground(new Color(255, 87, 34));
        statusValueLabel = new JLabel();
        statusValueLabel.setFont(new Font("돋움", Font.PLAIN, 16));
        titleValueLabel = new JLabel();
        titleValueLabel.setFont(new Font("돋움", Font.BOLD, 18));
        reporterValueLabel = new JLabel();
        assigneeField = new JTextField(15);
        assigneeField.setEditable(false);
        assigneeComboBox = new JComboBox<>();
        priorityValueLabel = new JLabel();
        tagValueLabel = new JLabel();
        fixerValueLabel = new JLabel();
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        commentArea = new JTextArea(3, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        statusLabel = new JLabel("");

        // First Row
        addLabelAndComponent(contentPanel, "Issue ID:", issueIdLabel, gbc, 0, 0);
        addLabelAndComponent(contentPanel, "Status:", statusValueLabel, gbc, 2, 0);
        addLabelAndComponent(contentPanel, "Title:", titleValueLabel, gbc, 0, 1, 4);

        // Second Row
        addLabelAndComponent(contentPanel, "Reporter:", reporterValueLabel, gbc, 0, 2);
        addLabelAndComponent(contentPanel, "Assignee:", assigneeField, gbc, 2, 2);

        // Third Row
        addLabelAndComponent(contentPanel, "Change Assignee:", assigneeComboBox, gbc, 0, 3, 4);

        // Fourth Row
        addLabelAndComponent(contentPanel, "Priority:", priorityValueLabel, gbc, 0, 4);
        addLabelAndComponent(contentPanel, "Tag:", tagValueLabel, gbc, 2, 4);

        // Fifth Row
        addLabelAndComponent(contentPanel, "Fixer:", fixerValueLabel, gbc, 0, 5, 4);

        // Sixth Row
        addLabelAndComponent(contentPanel, "Description:", new JScrollPane(descriptionArea), gbc, 0, 6, 4);

        // Add a button to view comments in a new window
        JButton viewCommentsButton = new JButton("View Comments");
        viewCommentsButton.setFont(new Font("돋움", Font.PLAIN, 14));
        viewCommentsButton.setForeground(Color.WHITE);
        viewCommentsButton.setBackground(new Color(255, 87, 34));
        viewCommentsButton.setFocusPainted(false);
        viewCommentsButton.addActionListener(e -> new CommentDetailFrame(issueId).setVisible(true));

        // Add the button to the content panel
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(viewCommentsButton, gbc);

        // Seventh Row
        addLabelAndComponent(contentPanel, "Comment:", new JScrollPane(commentArea), gbc, 0, 8, 4);

        // Button Row
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(createActionButton("Add Comment", e -> addComment()));
        buttonPanel.add(createActionButton("Change Status", e -> changeIssueStatus()));
        buttonPanel.add(createActionButton("Change Assignee", e -> changeAssignee()));
        buttonPanel.add(createActionButton("Fix Code", e -> fixCode()));

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(buttonPanel, gbc);

        gbc.gridy = 10;
        contentPanel.add(statusLabel, gbc);

        return contentPanel;
    }

    private JButton createActionButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setFont(new Font("돋움", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 87, 34));
        button.setFocusPainted(false);
        button.addActionListener(actionListener);
        return button;
    }

    private void addLabelAndComponent(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc, int x, int y) {
        addLabelAndComponent(panel, labelText, component, gbc, x, y, 1);
    }

    private void addLabelAndComponent(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc, int x, int y, int width) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("돋움", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = x + 1;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(component, gbc);
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
                targetFrame = new StatsFrame(loginId);
                break;
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
                    JOptionPane.showMessageDialog(this, "Failed to log out1.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to log out2.", "Error", JOptionPane.ERROR_MESSAGE);
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
