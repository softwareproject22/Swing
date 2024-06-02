import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignUpFrame extends JFrame {
    private JTextField nicknameField;
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField roleField;
    private JButton registerButton;

    public SignUpFrame() {
        setTitle("Account Registration");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Nickname:"));
        nicknameField = new JTextField();
        panel.add(nicknameField);

        panel.add(new JLabel("User Id:"));
        userIdField = new JTextField();
        panel.add(userIdField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        panel.add(new JLabel("Role:"));
        roleField = new JTextField();
        panel.add(roleField);

        registerButton = new JButton("Register");
        panel.add(registerButton);

        add(panel);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 회원가입 버튼 클릭 시 동작
                String nickname = nicknameField.getText();
                String userId = userIdField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                String role = roleField.getText();

                if (password.equals(confirmPassword)) {
                    try {
                        String requestBody = String.format(
                                "{\"nickname\": \"%s\", \"userId\": \"%s\", \"password\": \"%s\", \"role\": \"%s\"}",
                                nickname, userId, password, role
                        );
                        String response = HttpUtil.sendPostRequest("http://localhost:8082/login/joinhome/join", requestBody);
                        JOptionPane.showMessageDialog(null, response.equals("성공 시: 로그인 페이지로 다시 돌아감(리디렉션)") ? "Registration successful" : response);
                        dispose();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Registration failed: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Passwords do not match");
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SignUpFrame().setVisible(true);
            }
        });
    }
}
