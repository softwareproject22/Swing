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
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Issue Management System", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(234, 185, 151));

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setBackground(Color.WHITE);

        String[] buttonLabels = {"Home", "Logout", "View", "Issue", "Stats", "Admin"};
        JButton[] buttons = new JButton[buttonLabels.length];

        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = new JButton(buttonLabels[i]);
            buttons[i].setForeground(buttonLabels[i].equals("Home") ? new Color(234, 185, 151) : Color.BLACK);
            buttons[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            buttons[i].setBorderPainted(false);
            buttons[i].setContentAreaFilled(false);
            navPanel.add(buttons[i]);
        }

        buttons[0].addActionListener(e -> {
            new HomeFrame(loginId).setVisible(true);
            dispose();
        });
        buttons[1].addActionListener(e -> logout());
        buttons[2].addActionListener(e -> {
            new ViewFrame(loginId).setVisible(true);
            dispose();
        });
        buttons[3].addActionListener(e -> {
            new IssueFrame(loginId).setVisible(true);
            dispose();
        });
        buttons[4].addActionListener(e -> {
            new StatsFrame(loginId).setVisible(true);
            dispose();
        });
        buttons[5].addActionListener(e -> {
            if ("admin".equals(loginId)) {
                new AdminFrame(loginId).setVisible(true);
                dispose();
            }
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Main content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);

        nicknameLabel = new JLabel("", SwingConstants.CENTER);
        nicknameLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        nicknameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(nicknameLabel);
        contentPanel.add(Box.createVerticalStrut(20));
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
