import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONObject;

public class LoginFrame extends JFrame {
    private JTextField loginIdField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Login Page");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Issue Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(234, 185, 151));

        // Login ID field
        loginIdField = new JTextField("ID");
        loginIdField.setPreferredSize(new Dimension(200, 40));
        loginIdField.setMaximumSize(new Dimension(200, 40));
        loginIdField.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginIdField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginIdField.setBorder(BorderFactory.createLineBorder(new Color(234, 185, 151), 2));
        loginIdField.setForeground(Color.GRAY);

        // Password field
        passwordField = new JPasswordField("PW");
        passwordField.setPreferredSize(new Dimension(200, 40));
        passwordField.setMaximumSize(new Dimension(200, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(234, 185, 151), 2));
        passwordField.setForeground(Color.GRAY);

        // Login Button
        JButton loginButton = new JButton("Log in");
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setMaximumSize(new Dimension(200, 40));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(new Color(234, 185, 151));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(new LoginButtonListener());

        // Status Label
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.RED);

        // Adding components to panel
        panel.add(Box.createVerticalStrut(50)); // Spacer
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(50)); // Spacer
        panel.add(loginIdField);
        panel.add(Box.createVerticalStrut(10)); // Spacer
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(10)); // Spacer
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(20)); // Spacer
        panel.add(statusLabel);

        add(panel);
    }

    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String loginId = loginIdField.getText();
            String password = new String(passwordField.getPassword());

            try {
                // 서버에 POST 요청을 보내는 메서드 호출
                String response = sendLoginRequest(loginId, password);
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has("message")) {
                    statusLabel.setText("Login successful");
                    statusLabel.setForeground(new Color(0, 128, 0)); // Green for success
                    // 로그인 성공 시 홈 프레임을 생성하고 보이기
                    HomeFrame homeFrame = new HomeFrame(loginId);
                    homeFrame.setVisible(true);
                    dispose();  // 현재 로그인 프레임 닫기
                } else if (jsonResponse.has("loginFail")) {
                    statusLabel.setText("로그인 아이디 또는 비밀번호가 틀렸습니다.");
                }
            } catch (Exception ex) {
                statusLabel.setText("An error occurred: " + ex.getMessage());
            }
        }
    }

    private String sendLoginRequest(String loginId, String password) throws Exception {
        URL url = new URL("http://localhost:8080/home/login");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        JSONObject jsonInput = new JSONObject();
        jsonInput.put("loginId", loginId);
        jsonInput.put("password", password);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        }

        return response.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginPage = new LoginFrame();
            loginPage.setVisible(true);
        });
    }
}
