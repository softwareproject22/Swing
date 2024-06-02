import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

    public static String sendGetRequest(String requestUrl) throws Exception {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP GET Request Failed with Error code : " + responseCode);
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        } catch (Exception e) {
            throw new Exception("Error occurred while sending GET request: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // Log or print the error if necessary
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String sendPostRequest(String requestUrl, String jsonInputString) throws Exception {
        HttpURLConnection connection = null;
        OutputStream os = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            os = connection.getOutputStream();
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new Exception("HTTP POST Request Failed with Error code : " + responseCode);
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;

            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return response.toString();
        } catch (Exception e) {
            throw new Exception("Error occurred while sending POST request: " + e.getMessage(), e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    // Log or print the error if necessary
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // Log or print the error if necessary
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
