
package dan.fypbackend.services;

import dan.fypbackend.model.CarPark;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class LoadCarParks {
    public static ArrayList<CarPark> get(URL url) {
        JSONObject s = RetrieveJsonObject.get(url);
        if (s != null) {
            return CarParkJSONParser.get(s);
        } else {
            JSONObject jsonObject = null;
            try {
                String cmd = "python EditJson.py";
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FileReader fileReader = new FileReader("backup.json");
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                jsonObject = new JSONObject(bufferedReader.readLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (jsonObject != null) {
                return CarParkJSONParser.get(jsonObject);
            }
        }
        return null;
    }
}


