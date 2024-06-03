import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class CommentDetailFrame extends JFrame {
    private int issueId;
    private JTextArea commentHistoryArea;

    public CommentDetailFrame(int issueId) {
        this.issueId = issueId;

        setTitle("Comment Details");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        commentHistoryArea = new JTextArea();
        commentHistoryArea.setEditable(false);
        commentHistoryArea.setLineWrap(true);
        commentHistoryArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(commentHistoryArea);

        add(scrollPane, BorderLayout.CENTER);

        loadComments();
    }

    private void loadComments() {
        try {
            URL url = new URL("http://localhost:8080/comment/" + issueId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                JSONArray jsonResponse = new JSONArray(getResponseString(connection));
                StringBuilder commentsBuilder = new StringBuilder();
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject commentObject = jsonResponse.getJSONObject(i);
                    String author = commentObject.optString("sender", "Unknown author");
                    String content = commentObject.optString("content", "No content available");
                    commentsBuilder.append(author).append(": ").append(content).append("\n");
                }
                commentHistoryArea.setText(commentsBuilder.toString());
            } else {
                commentHistoryArea.setText("Failed to load comments.");
            }
        } catch (Exception ex) {
            commentHistoryArea.setText("An error occurred: " + ex.getMessage());
        }
    }

    private String getResponseString(HttpURLConnection connection) throws Exception {
        try (InputStream responseStream = connection.getInputStream();
             Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }
}
