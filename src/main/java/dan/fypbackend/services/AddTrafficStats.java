
package dan.fypbackend.services;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import dan.fypbackend.model.CarPark;
import dan.fypbackend.model.TrafficStat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimerTask;


public class AddTrafficStats extends TimerTask {
    private final ArrayList<CarPark> carParksList;
    private final DatabaseReference trafficDb = ((FirebaseDatabase.getInstance()).getReference("traffic"));

    public AddTrafficStats(ArrayList<CarPark> carParksList) {
        this.carParksList = carParksList;
    }


    @Override
    public void run() {
        String[] carParkNames = new String[carParksList.size()];
        StringBuilder urlStringEast = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=Youghal&destinations=");
        StringBuilder urlStringNorth = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=Mallow,Cork&destinations=");
        StringBuilder urlStringWest = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=Macroom,Cork&destinations=");
        StringBuilder urlStringSouth = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=Clonakilty,Cork&destinations=");
        int i = 0;
        while (i < carParksList.size()) {
            if (i == carParksList.size() - 1) {
                urlStringEast.append(String.valueOf(carParksList.get(i).getLatitude())).append(",").append(String.valueOf(carParksList.get(i).getLongitude()));
                urlStringNorth.append(String.valueOf(carParksList.get(i).getLatitude())).append(",").append(String.valueOf(carParksList.get(i).getLongitude()));
                urlStringWest.append(String.valueOf(carParksList.get(i).getLatitude())).append(",").append(String.valueOf(carParksList.get(i).getLongitude()));
                urlStringSouth.append(String.valueOf(carParksList.get(i).getLatitude())).append(",").append(String.valueOf(carParksList.get(i).getLongitude()));
            } else {
                urlStringEast.append(String.valueOf(carParksList.get(i).getLatitude())).append(",").append(String.valueOf(carParksList.get(i).getLongitude())).append("|");
                urlStringNorth.append(String.valueOf(carParksList.get(i).getLatitude())).append(",").append(String.valueOf(carParksList.get(i).getLongitude())).append("|");
                urlStringWest.append(String.valueOf(carParksList.get(i).getLatitude())).append(",").append(String.valueOf(carParksList.get(i).getLongitude())).append("|");
                urlStringSouth.append(String.valueOf(carParksList.get(i).getLatitude())).append(",").append(String.valueOf(carParksList.get(i).getLongitude())).append("|");
            }
            carParkNames[i] = carParksList.get(i).getName();
            i++;
        }
        urlStringEast.append("&traffic_model=pessimistic&departure_time=now&key=%20AIzaSyCzNHDdvhriV2eQ0I6gN1G7n_Vuu0chKdw");
        urlStringNorth.append("&traffic_model=pessimistic&departure_time=now&key=%20AIzaSyCzNHDdvhriV2eQ0I6gN1G7n_Vuu0chKdw");
        urlStringWest.append("&traffic_model=pessimistic&departure_time=now&key=%20AIzaSyCzNHDdvhriV2eQ0I6gN1G7n_Vuu0chKdw");
        urlStringSouth.append("&traffic_model=pessimistic&departure_time=now&key=%20AIzaSyCzNHDdvhriV2eQ0I6gN1G7n_Vuu0chKdw");
        JSONObject jsonTrafficEast = null, jsonTrafficWest = null, jsonTrafficSouth = null, jsonTrafficNorth = null, jsonWeather = null;
        try {
            jsonTrafficEast = RetrieveJsonObject.get(new URL(urlStringEast.toString()));
            jsonTrafficWest = RetrieveJsonObject.get(new URL(urlStringWest.toString()));
            jsonTrafficSouth = RetrieveJsonObject.get(new URL(urlStringSouth.toString()));
            jsonTrafficNorth = RetrieveJsonObject.get(new URL(urlStringNorth.toString()));
            jsonWeather = RetrieveJsonObject.get(new URL("http://api.openweathermap.org/data/2.5/weather?q=cork&appid=bd6ab1b7b59f866b3e68f34173c5c570"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap<String, JSONArray> jsonTraffics = new HashMap<>();
        try {
            if (jsonTrafficEast != null && jsonTrafficWest != null && jsonTrafficSouth != null && jsonTrafficNorth != null && jsonWeather != null) {
                jsonTraffics.put("east", ((jsonTrafficEast).getJSONArray("rows")).getJSONObject(0).getJSONArray("elements"));
                jsonTraffics.put("west", ((jsonTrafficWest).getJSONArray("rows")).getJSONObject(0).getJSONArray("elements"));
                jsonTraffics.put("south", ((jsonTrafficSouth).getJSONArray("rows")).getJSONObject(0).getJSONArray("elements"));
                jsonTraffics.put("north", ((jsonTrafficNorth).getJSONArray("rows")).getJSONObject(0).getJSONArray("elements"));
                String weatherString = (((jsonWeather).getJSONArray("weather")).getJSONObject(0)).getString("description");
                int count = 0;
                System.out.print("\n");
                for (HashMap.Entry<String, JSONArray> entry : jsonTraffics.entrySet()) {
                    for (int index = 0; index < carParkNames.length; index++) {
                        TrafficStat trafficStat = new TrafficStat();
                        trafficStat.setDirection(entry.getKey());
                        trafficStat.setDelay(getDelay(index, entry.getValue()));
                        trafficStat.setDate(System.currentTimeMillis());
                        trafficStat.setCarParkName(carParkNames[index]);
                        trafficStat.setWeather(weatherString);
                        try {
                            trafficDb.child(String.valueOf(new Date(System.currentTimeMillis()) + " " + count)).setValue(trafficStat, (error, ref) -> System.out.print("Added " + error));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        count += 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private double getDelay(int index, JSONArray traffic) throws JSONException {
        return traffic.getJSONObject(index).getJSONObject("duration_in_traffic").getDouble("value");
    }


}