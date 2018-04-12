
package dan.fypbackend.services;

import dan.fypbackend.model.CarPark;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class LoadCarParks {
    public static ArrayList<CarPark> get(URL url) throws IOException {
        if (RetrieveJsonObject.get(url) != null) {
            return CarParkJSONParser.get(Objects.requireNonNull(RetrieveJsonObject.get(url)));
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
            return CarParkJSONParser.get(Objects.requireNonNull(jsonObject));
        }
    }
}


