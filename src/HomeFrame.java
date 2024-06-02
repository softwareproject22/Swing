import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONObject;

public class HomeFrame extends JFrame {
    private String loginId;
    private JLabel nicknameLabel;
    private JLabel messageLabel;

    public HomeFrame(String loginId) {
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
        JButton adminButton = new JButton("Admin");

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
        adminButton.addActionListener(e -> {
            if ("admin".equals(loginId)) {
                new AdminFrame(loginId).setVisible(true);
                dispose();
            }
        });

        navPanel.add(homeButton);
        navPanel.add(viewButton);
        navPanel.add(issueButton);
        navPanel.add(statsButton);
        navPanel.add(adminButton);
        navPanel.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Main content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(2, 1));
        nicknameLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel = new JLabel("", SwingConstants.CENTER);
        contentPanel.add(nicknameLabel);
        contentPanel.add(messageLabel);

        panel.add(contentPanel, BorderLayout.CENTER);

        add(panel);
        loadHomePanel();  // 로그인 성공 시 홈 패널을 로드
    }

    private void loadHomePanel() {
        try {
            URL url = new URL("http://localhost:8080/home?loginId=" + loginId);
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
                if (jsonResponse.has("nickname") && jsonResponse.has("message")) {
                    String nickname = jsonResponse.getString("nickname");
                    String message = jsonResponse.getString("message");
                    nicknameLabel.setText("Welcome, " + nickname + "!");
                    messageLabel.setText(message);
                } else {
                    nicknameLabel.setText("Welcome to our issue management system!");
                    messageLabel.setText("");
                }
            } else {
                nicknameLabel.setText("Failed to load home information.");
                messageLabel.setText("");
            }
        } catch (Exception ex) {
            nicknameLabel.setText("An error occurred: " + ex.getMessage());
            messageLabel.setText("");
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
            HomeFrame homeFrame = new HomeFrame("user");
            homeFrame.setVisible(true);
        });
    }
}
