import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class IssueFrame extends JFrame {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> priorityComboBox;
    private JTextField reporterField;
    private JComboBox<String> tagComboBox;
    private JLabel statusLabel;
    private String loginId;
    private ArrayList<Integer> tagIds;

    public IssueFrame(String loginId) {
        this.loginId = loginId;
        this.tagIds = new ArrayList<>();

        // 로그인 ID 출력
        System.out.println("Login ID: " + this.loginId);

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
            //new StatsFrame(loginId).setVisible(true);
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

        // Main content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(7, 2));

        contentPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        contentPanel.add(titleField);

        contentPanel.add(new JLabel("Description:"));
        descriptionArea = new JTextArea();
        contentPanel.add(new JScrollPane(descriptionArea));

        contentPanel.add(new JLabel("Priority:"));
        priorityComboBox = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        contentPanel.add(priorityComboBox);

        contentPanel.add(new JLabel("Reporter:"));
        reporterField = new JTextField();
        reporterField.setEditable(false);
        contentPanel.add(reporterField);

        contentPanel.add(new JLabel("Tag:"));
        tagComboBox = new JComboBox<>();
        contentPanel.add(tagComboBox);

        JButton registerButton = new JButton("등록");
        registerButton.addActionListener(e -> registerIssue());
        contentPanel.add(registerButton);

        statusLabel = new JLabel("");
        contentPanel.add(statusLabel);

        panel.add(contentPanel, BorderLayout.CENTER);

        add(panel);

        loadUserInfo();  // Load user info to get the nickname
        loadTags();      // Load tags from the server
    }

    private void loadUserInfo() {
        try {
            URL url = new URL("http://localhost:8080/home/login/info?loginId=" + loginId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("loadUserInfo Response Code: " + responseCode);
            if (responseCode == 200) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println("loadUserInfo Response Body: " + responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("nickname")) {
                    String nickname = jsonResponse.getString("nickname");

                    // 닉네임 출력
                    System.out.println("Nickname: " + nickname);

                    reporterField.setText(nickname);
                } else {
                    statusLabel.setText("Failed to load user info");
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name());
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    System.out.println("loadUserInfo Error Response: " + errorResponse);
                }
                statusLabel.setText("Failed to load user info");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
            System.out.println("Exception in loadUserInfo: " + ex.getMessage());
        }
    }

    private void loadTags() {
        try {
            URL url = new URL("http://localhost:8080/tags");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("loadTags Response Code: " + responseCode);
            if (responseCode == 200) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println("loadTags Response Body: " + responseBody);

                JSONArray jsonResponse = new JSONArray(responseBody);
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject tag = jsonResponse.getJSONObject(i);
                    tagComboBox.addItem(tag.getString("category"));
                    tagIds.add(tag.getInt("id"));
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name());
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    System.out.println("loadTags Error Response: " + errorResponse);
                }
                statusLabel.setText("Failed to load tags");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
            System.out.println("Exception in loadTags: " + ex.getMessage());
        }
    }

    private void registerIssue() {
        try {
            URL url = new URL("http://localhost:8080/issue");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("projectId", "1");
            jsonInput.put("title", titleField.getText());
            jsonInput.put("description", descriptionArea.getText());
            jsonInput.put("code", "코드");  // 코드 필드는 고정값으로 처리
            jsonInput.put("priority", priorityComboBox.getSelectedItem().toString());
            jsonInput.put("reporter", reporterField.getText());
            int selectedTagIndex = tagComboBox.getSelectedIndex();
            int selectedTagId = tagIds.get(selectedTagIndex);
            jsonInput.put("tags", new int[]{selectedTagId});  // 태그 필드는 선택한 태그로 처리

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("registerIssue Response Code: " + responseCode);
            if (responseCode == 201) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println("registerIssue Response Body: " + responseBody);

                if ("이슈가 생성되었습니다.".equals(responseBody)) {
                    statusLabel.setText("Issue created successfully");
                } else {
                    statusLabel.setText("Failed to create issue");
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name());
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    System.out.println("registerIssue Error Response: " + errorResponse);
                }
                statusLabel.setText("Failed to create issue");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
            System.out.println("Exception in registerIssue: " + ex.getMessage());
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
            IssueFrame issueFrame = new IssueFrame("user");
            issueFrame.setVisible(true);
        });
    }
}
