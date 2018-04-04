
package dan.fypbackend.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import dan.fypbackend.model.CarPark;
import dan.fypbackend.services.AddTrafficStats;
import dan.fypbackend.services.LoadCarParks;
import dan.fypbackend.services.RemoveReservations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
        ArrayList<CarPark> carParksList = LoadCarParks.get("http://data.corkcity.ie/api/action/datastore_search?resource_id=6cc1028e-7388-4bc5-95b7-667a59aa76dc");
        Timer timer1 = new Timer();
        timer1.schedule(new AddTrafficStats(carParksList), 0, 600000);
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/")
    public String showServicesAreRunning() {
        return "Service is running";
    }

}