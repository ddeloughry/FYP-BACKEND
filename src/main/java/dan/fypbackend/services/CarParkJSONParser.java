package dan.fypbackend.services;

import dan.fypbackend.model.CarPark;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class CarParkJSONParser {
    public static ArrayList<CarPark> get(JSONObject json) {
        ArrayList<CarPark> cParks = new ArrayList<>();
        try {
            JSONObject items = json.getJSONObject("result");
            JSONArray itemArray = items.getJSONArray("records");
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject j = itemArray.getJSONObject(i);
                CarPark carPark = new CarPark();
                carPark.setName(j.getString("name"));
                carPark.setLatitude(j.getDouble("latitude"));
                carPark.setLongitude(j.getDouble("longitude"));
                cParks.add(carPark);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cParks;
    }
}