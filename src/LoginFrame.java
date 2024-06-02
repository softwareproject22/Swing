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
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        loginIdField = new JTextField();
        passwordField = new JPasswordField();
        statusLabel = new JLabel("");

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginButtonListener());

        panel.add(new JLabel("Login ID:"));
        panel.add(loginIdField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
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
