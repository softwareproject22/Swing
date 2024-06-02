import javax.swing.*;
import java.awt.*;
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

        setTitle("Issue Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Issue Management System", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(234, 185, 151));

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setBackground(Color.WHITE);

        String[] buttons = {"Home", "View", "Issue", "Stats", "Logout"};
        for (String btnText : buttons) {
            JButton button = new JButton(btnText);
            button.setForeground(new Color(234, 185, 151));
            button.setFont(new Font("SansSerif", Font.PLAIN, 14));
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(e -> handleNavButtonClick(btnText));
            navPanel.add(button);
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Main content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel formTitleLabel = new JLabel("Issue 등록하기", SwingConstants.CENTER);
        formTitleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        formTitleLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(formTitleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(createLabel("Title:"), gbc);

        gbc.gridx = 1;
        titleField = createTextField();
        contentPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(createLabel("Description:"), gbc);

        gbc.gridx = 1;
        descriptionArea = createTextArea();
        contentPanel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(createLabel("Tag:"), gbc);

        gbc.gridx = 1;
        tagComboBox = new JComboBox<>();
        contentPanel.add(tagComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(createLabel("Priority:"), gbc);

        gbc.gridx = 1;
        priorityComboBox = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        styleComboBox(priorityComboBox);
        contentPanel.add(priorityComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        contentPanel.add(createLabel("Reporter:"), gbc);

        gbc.gridx = 1;
        reporterField = createTextField();
        reporterField.setEditable(false);
        contentPanel.add(reporterField, gbc);

        JButton registerButton = new JButton("등록");
        registerButton.setBackground(new Color(234, 185, 151));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> registerIssue());

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        contentPanel.add(registerButton, gbc);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        contentPanel.add(statusLabel, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);

        add(panel);

        loadUserInfo();  // Load user info to get the nickname
        loadTags();      // Load tags from the server
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return textField;
    }

    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea(5, 20);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return textArea;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
    }

    private void handleNavButtonClick(String buttonText) {
        JFrame targetFrame;
        switch (buttonText) {
            case "Home":
                targetFrame = new HomeFrame(loginId);
                break;
            case "View":
                targetFrame = new ViewFrame(loginId);
                break;
            case "Issue":
                targetFrame = new IssueFrame(loginId);
                break;
            case "Stats":
                targetFrame = new StatsFrame(loginId);
                break;
            case "Logout":
                logout();
                return;
            default:
                return;
        }
        targetFrame.setVisible(true);
        dispose();
    }

    private void loadUserInfo() {
        try {
            URL url = new URL("http://localhost:8080/home/login/info?loginId=" + loginId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String responseBody = getResponseString(connection);
                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("nickname")) {
                    String nickname = jsonResponse.getString("nickname");
                    reporterField.setText(nickname);
                } else {
                    statusLabel.setText("Failed to load user info");
                }
            } else {
                statusLabel.setText("Failed to load user info");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void loadTags() {
        try {
            URL url = new URL("http://localhost:8080/tags");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String responseBody = getResponseString(connection);
                JSONArray jsonResponse = new JSONArray(responseBody);
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject tag = jsonResponse.getJSONObject(i);
                    tagComboBox.addItem(tag.getString("category"));
                    tagIds.add(tag.getInt("id"));
                }
            } else {
                statusLabel.setText("Failed to load tags");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
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
            if (responseCode == 201) {
                String responseBody = getResponseString(connection);
                if ("이슈가 생성되었습니다.".equals(responseBody)) {
                    statusLabel.setText("Issue created successfully");
                } else {
                    statusLabel.setText("Failed to create issue");
                }
            } else {
                statusLabel.setText("Failed to create issue");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
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
                String responseBody = getResponseString(connection);
                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Logout successful")) {
                    dispose();
                    new LoginFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to log out.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                statusLabel.setText("Failed to log out.");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private String getResponseString(HttpURLConnection connection) throws Exception {
        try (InputStream responseStream = connection.getInputStream();
             Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IssueFrame issueFrame = new IssueFrame("user");
            issueFrame.setVisible(true);
        });
    }
}
