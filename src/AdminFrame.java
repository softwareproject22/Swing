import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.table.DefaultTableModel;

public class AdminFrame extends JFrame {
    private String loginId;
    private JTextField nicknameField;
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField roleField;
    private JLabel statusLabel;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public AdminFrame(String loginId) {
        this.loginId = loginId;

        setTitle("Admin - Issue Management System");
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

        homeButton.addActionListener(e -> {
            new HomeFrame(loginId).setVisible(true);
            dispose(); // 현재 AdminFrame 창을 닫습니다.
        });
        logoutButton.addActionListener(e -> logout());

        navPanel.add(homeButton);
        navPanel.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Side Menu Panel
        JPanel sideMenuPanel = new JPanel();
        sideMenuPanel.setLayout(new GridLayout(4, 1));

        JButton projectButton = new JButton("프로젝트 추가");
        JButton accountButton = new JButton("계정 추가");
        JButton milestoneButton = new JButton("마일스톤");
        JButton componentButton = new JButton("컴포넌트");

        projectButton.addActionListener(e -> showNotImplementedDialog());
        milestoneButton.addActionListener(e -> showNotImplementedDialog());
        componentButton.addActionListener(e -> showNotImplementedDialog());

        sideMenuPanel.add(projectButton);
        sideMenuPanel.add(accountButton);
        sideMenuPanel.add(milestoneButton);
        sideMenuPanel.add(componentButton);

        panel.add(sideMenuPanel, BorderLayout.WEST);

        // Main content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        // Account Management Panel
        JPanel accountPanel = new JPanel();
        accountPanel.setLayout(new GridLayout(6, 2));

        accountPanel.add(new JLabel("Nickname:"));
        nicknameField = new JTextField();
        accountPanel.add(nicknameField);

        accountPanel.add(new JLabel("User Id:"));
        userIdField = new JTextField();
        accountPanel.add(userIdField);

        accountPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        accountPanel.add(passwordField);

        accountPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        accountPanel.add(confirmPasswordField);

        accountPanel.add(new JLabel("Role:"));
        roleField = new JTextField();
        accountPanel.add(roleField);

        JButton registerButton = new JButton("등록");
        registerButton.addActionListener(e -> registerAccount());

        accountPanel.add(registerButton);
        statusLabel = new JLabel("");
        accountPanel.add(statusLabel);

        contentPanel.add(accountPanel, BorderLayout.NORTH);

        // User List Table
        tableModel = new DefaultTableModel(new Object[]{"Account", "User Id", "Last Login"}, 0);
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        add(panel);
        loadUserList();
    }

    private void showNotImplementedDialog() {
        JOptionPane.showMessageDialog(this, "기능 미구현", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void registerAccount() {
        try {
            URL url = new URL("http://localhost:8080/home/join");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("loginId", userIdField.getText());
            jsonInput.put("password", new String(passwordField.getPassword()));
            jsonInput.put("passwordCheck", new String(confirmPasswordField.getPassword()));
            jsonInput.put("nickname", nicknameField.getText());
            jsonInput.put("role", roleField.getText());

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 201) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("message") && jsonResponse.getString("message").equals("회원 가입 성공")) {
                    statusLabel.setText("계정 추가 성공1");
                    loadUserList();
                } else {
                    statusLabel.setText("계정 추가 실패");
                }
            } else {
                statusLabel.setText("계정 추가 실패");
            }
        } catch (Exception ex) {
            statusLabel.setText("An error occurred: " + ex.getMessage());
        }
    }

    private void loadUserList() {
        try {
            URL url = new URL("http://localhost:8080/home/userlist");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONArray jsonResponse = new JSONArray(responseBody);
                tableModel.setRowCount(0); // Clear existing rows
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject user = jsonResponse.getJSONObject(i);
                    tableModel.addRow(new Object[]{user.getString("nickname"), user.getString("loginId"), ""});
                }
            } else {
                statusLabel.setText("Failed to load user list.");
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
                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Logout successful")) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminFrame adminFrame = new AdminFrame("admin");
            adminFrame.setVisible(true);
        });
    }
}
