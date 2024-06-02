import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import org.json.JSONObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class StatsFrame extends JFrame {
    private String loginId;

    public StatsFrame(String loginId) {
        this.loginId = loginId;

        setTitle("Issue Statistics");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));

        // Fetch and display statistics
        displayDailyStats(panel);
        displayMonthlyStats(panel);
        displayStatusStats(panel);
        displayTagsStats(panel);

        add(panel);
    }

    private void displayDailyStats(JPanel panel) {
        try {
            URL url = new URL("http://localhost:8080/analyze/daily");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String responseBody = getResponseString(connection);
                JSONObject jsonResponse = new JSONObject(responseBody);
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                Iterator<String> keys = jsonResponse.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    dataset.addValue(jsonResponse.getInt(key), "Issues", key);
                }

                JFreeChart chart = ChartFactory.createBarChart(
                        "Daily Issues",
                        "Date",
                        "Count",
                        dataset,
                        PlotOrientation.VERTICAL,
                        true, true, false);
                ChartPanel chartPanel = new ChartPanel(chart);
                panel.add(chartPanel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void displayMonthlyStats(JPanel panel) {
        try {
            URL url = new URL("http://localhost:8080/analyze/monthly");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String responseBody = getResponseString(connection);
                JSONObject jsonResponse = new JSONObject(responseBody);
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                Iterator<String> keys = jsonResponse.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    dataset.addValue(jsonResponse.getInt(key), "Issues", key);
                }

                JFreeChart chart = ChartFactory.createBarChart(
                        "Monthly Issues",
                        "Month",
                        "Count",
                        dataset,
                        PlotOrientation.VERTICAL,
                        true, true, false);
                ChartPanel chartPanel = new ChartPanel(chart);
                panel.add(chartPanel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void displayStatusStats(JPanel panel) {
        try {
            URL url = new URL("http://localhost:8080/analyze/status");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String responseBody = getResponseString(connection);
                JSONObject jsonResponse = new JSONObject(responseBody);
                DefaultPieDataset dataset = new DefaultPieDataset();

                Iterator<String> keys = jsonResponse.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    dataset.setValue(key, jsonResponse.getInt(key));
                }

                JFreeChart chart = ChartFactory.createPieChart(
                        "Status Issues",
                        dataset,
                        true, true, false);
                ChartPanel chartPanel = new ChartPanel(chart);
                panel.add(chartPanel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void displayTagsStats(JPanel panel) {
        try {
            URL url = new URL("http://localhost:8080/analyze/tags");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String responseBody = getResponseString(connection);
                JSONObject jsonResponse = new JSONObject(responseBody);
                DefaultPieDataset dataset = new DefaultPieDataset();

                Iterator<String> keys = jsonResponse.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    dataset.setValue(key, jsonResponse.getInt(key));
                }

                JFreeChart chart = ChartFactory.createPieChart(
                        "Tags Issues",
                        dataset,
                        true, true, false);
                ChartPanel chartPanel = new ChartPanel(chart);
                panel.add(chartPanel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
            StatsFrame statsFrame = new StatsFrame("user");
            statsFrame.setVisible(true);
        });
    }
}
