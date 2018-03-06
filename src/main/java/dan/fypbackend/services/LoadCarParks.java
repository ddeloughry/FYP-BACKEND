package dan.fypbackend.services;

import dan.fypbackend.model.CarPark;

import java.util.ArrayList;
import java.util.Objects;

public class LoadCarParks {
    public static ArrayList<CarPark> get(String urlStr) {
//        if (isOnline()) {
        return CarParkJSONParser.get(Objects.requireNonNull(RetrieveJsonObject.get(urlStr)));
//        }
//        return null;
    }

//    private static boolean isOnline() {
//        return true;
//    }
}
