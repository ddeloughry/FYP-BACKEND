
package dan.fypbackend.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import dan.fypbackend.model.CarPark;
import dan.fypbackend.model.TrafficDelay;
import dan.fypbackend.services.AddTrafficStats;
import dan.fypbackend.services.CalculateTrafficPrediction;
import dan.fypbackend.services.LoadCarParks;
import dan.fypbackend.services.RemoveReservations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;

@RestController
public class Controllers {

    public Controllers() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("fyp-db-e0afa-firebase-adminsdk-wyhwk-f33a87ff3e.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://fyp-db-e0afa.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);
        Timer timer = new Timer();
        timer.schedule(new RemoveReservations(), 0, 5000);
        URL url = new URL("http://data.corkcity.ie/api/action/datastore_search?resource_id=6cc1028e-7388-4bc5-95b7-667a59aa76dc");
        ArrayList<CarPark> carParksList = LoadCarParks.get(url);
        Timer timer1 = new Timer();
        timer1.schedule(new AddTrafficStats(carParksList), 0, 900000);
        Timer timer2 = new Timer();
        timer2.schedule(new CalculateTrafficPrediction(carParksList), 0, 900000);
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/")
    public String servicesRunning() {
        return "FYP - Services are running!";
    }

    @GetMapping("/traffic")
    public ArrayList<TrafficDelay> getTraffic(@RequestParam(value = "direction", defaultValue = "all") String direction) throws IOException, JSONException {
        ArrayList<TrafficDelay> trafficDelays = new ArrayList<>();
        FileReader fileReader = new FileReader("machine_learning/result.json");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        JSONArray jsonArray = new JSONArray(bufferedReader.readLine());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject current = (JSONObject) jsonArray.get(i);
            if (!direction.equalsIgnoreCase("all")) {
                if (current.getString("direction").equalsIgnoreCase(direction)) {
                    trafficDelays = addDelay(current, trafficDelays);
                }
            } else {
                trafficDelays = addDelay(current, trafficDelays);
            }
        }
        return trafficDelays;
    }

    private ArrayList<TrafficDelay> addDelay(JSONObject current, ArrayList<TrafficDelay> trafficDelays) throws JSONException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, current.getInt("hour"));
        calendar.set(Calendar.MINUTE, current.getInt("minute"));
        TrafficDelay trafficDelay = new TrafficDelay(current.getString("car_park_name"),
                current.getString("direction"),
                calendar.getTimeInMillis(),
                current.getDouble("time")
        );
        trafficDelays.add(trafficDelay);
        return trafficDelays;
    }


}