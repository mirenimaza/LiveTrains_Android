package android.trains;

import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The class that is used to retrieve data from website
 */
public class JSONTask extends AsyncTask<String, String, String> {
    HttpURLConnection urlConnection;
    String urlString;
    String jsonResult;

    public JSONTask(String url) {
        super();
        urlString = url;
    }

    @Override
    protected String doInBackground(String... args) {

        StringBuilder result = null;
        result = new StringBuilder();

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        jsonResult = result.toString();

        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

}