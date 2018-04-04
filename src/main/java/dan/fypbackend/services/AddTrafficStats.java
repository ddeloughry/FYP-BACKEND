
package dan.fypbackend.services;

import com.google.firebase.database.FirebaseDatabase;
import dan.fypbackend.model.CarPark;
import dan.fypbackend.model.TrafficStat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class AddTrafficStats extends TimerTask {
    private final ArrayList<CarPark> carParksList;
    private JSONObject weather;
    private String[] carParkNames;
    private String weatherString;

    public AddTrafficStats(ArrayList<CarPark> carParksList) {
        this.carParksList = carParksList;
    }

    @Deprecated
    @Override
    public void run() {
        carParkNames = new String[carParksList.size()];
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
        JSONObject jsonTrafficEast = RetrieveJsonObject.get(urlStringEast.toString());
        JSONObject jsonTrafficWest = RetrieveJsonObject.get(urlStringWest.toString());
        JSONObject jsonTrafficSouth = RetrieveJsonObject.get(urlStringSouth.toString());
        JSONObject jsonTrafficNorth = RetrieveJsonObject.get(urlStringNorth.toString());
        JSONObject jsonWeather = RetrieveJsonObject.get("http://api.openweathermap.org/data/2.5/weather?q=cork&appid=bd6ab1b7b59f866b3e68f34173c5c570");
        HashMap<String, JSONArray> jsonTraffics = new HashMap<>();
        try {
            jsonTraffics.put("east", (Objects.requireNonNull(jsonTrafficEast).getJSONArray("rows")).getJSONObject(0).getJSONArray("elements"));
            jsonTraffics.put("west", (Objects.requireNonNull(jsonTrafficWest).getJSONArray("rows")).getJSONObject(0).getJSONArray("elements"));
            jsonTraffics.put("south", (Objects.requireNonNull(jsonTrafficSouth).getJSONArray("rows")).getJSONObject(0).getJSONArray("elements"));
            jsonTraffics.put("north", (Objects.requireNonNull(jsonTrafficNorth).getJSONArray("rows")).getJSONObject(0).getJSONArray("elements"));
            weather = (Objects.requireNonNull(jsonWeather).getJSONArray("weather")).getJSONObject(0);
            weatherString = weather.getString("description");
            int count = 0;
            for (HashMap.Entry<String, JSONArray> entry : jsonTraffics.entrySet()) {
                for (int index = 0; index < carParkNames.length; index++) {
                    TrafficStat trafficStat = new TrafficStat();
                    trafficStat.setDirection(entry.getKey());
                    trafficStat.setDelay(getDelay(index, entry.getValue()));
                    trafficStat.setDate(System.currentTimeMillis());
                    trafficStat.setCarParkName(carParkNames[index]);
                    trafficStat.setWeather(weatherString);
                    ((FirebaseDatabase.getInstance()).getReference("traffic")).child(String.valueOf(new Date(System.currentTimeMillis()) + " " + count)).setValue(trafficStat);
                    count += 1;
                }
            }
        } catch (Exception e) {
            System.out.print(e.toString());
        }
    }

    private double getDelay(int index, JSONArray traffic) {
        return traffic.getJSONObject(index).getJSONObject("duration_in_traffic").getDouble("value");
    }
}