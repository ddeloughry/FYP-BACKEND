package dan.fypbackend.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import dan.fypbackend.services.AddTrafficStats;
import dan.fypbackend.services.RemoveReservations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;

@Controller
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

        Timer timer1 = new Timer();
        timer1.schedule(new AddTrafficStats(), 0, 600000);
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/")
    public String showServicesAreRunning(Model model) {
        model.addAttribute("message", "Service is running");
        return "index";
    }

}