package dan.fypbackend.services;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

class RetrieveJsonObject {
    public static JSONObject get(String urls) {
        try {
            URL url = new URL(urls);
            URLConnection urlConnection = url.openConnection();
//            urlConnection.setConnectTimeout(1000);
            InputStream is = urlConnection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            is.close();
            return new JSONObject(jsonText);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}