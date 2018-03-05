package dan.fypbackend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;

@RestController
public class Controllers {


    @GetMapping("/")
    public void doServices(Model model) throws IOException {
        FileInputStream serviceAccount = new FileInputStream("fyp-db-e0afa-firebase-adminsdk-wyhwk-f33a87ff3e.json");
        final String[] string = {""};
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://fyp-db-e0afa.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);
        Timer timer = new Timer();
        timer.schedule(new RemoveReservations(), 0, 5000);
        model.addAttribute("message", string);
//        return "index";
    }

}