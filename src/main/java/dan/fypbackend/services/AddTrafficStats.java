package dan.fypbackend.services;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import dan.fypbackend.model.CarPark;
import dan.fypbackend.model.TrafficStat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;

public class AddTrafficStats extends TimerTask {
    @Deprecated
    @Override
    public void run() {
        ArrayList<CarPark> carParksList = LoadCarParks.get("http://data.corkcity.ie/api/action/datastore_search?resource_id=6cc1028e-7388-4bc5-95b7-667a59aa76dc");
        assert carParksList != null;
        String[] carParkNames = new String[carParksList.size()];

        // east
        StringBuilder urlStringEast = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=Youghal&destinations=");
        // north
        StringBuilder urlStringNorth = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=Mallow,Cork&destinations=");
        // west
        StringBuilder urlStringWest = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=Macroom,Cork&destinations=");
        // south
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


        // east
        ArrayList<TrafficStat> trafficStatsEast = new ArrayList<>();
        // east
        ArrayList<TrafficStat> trafficStatsNorth = new ArrayList<>();
        // east
        ArrayList<TrafficStat> trafficStatsWest = new ArrayList<>();
        // east
        ArrayList<TrafficStat> trafficStatsSouth = new ArrayList<>();

        JSONObject jsonTrafficEast = RetrieveJsonObject.get(urlStringEast.toString());
        JSONObject jsonTrafficWest = RetrieveJsonObject.get(urlStringWest.toString());
        JSONObject jsonTrafficSouth = RetrieveJsonObject.get(urlStringSouth.toString());
        JSONObject jsonTrafficNorth = RetrieveJsonObject.get(urlStringNorth.toString());


        JSONObject jsonWeather = RetrieveJsonObject.get("http://api.openweathermap.org/data/2.5/weather?q=cork&appid=bd6ab1b7b59f866b3e68f34173c5c570");

        Calendar now = Calendar.getInstance();
        try {
            assert jsonTrafficEast != null;
            JSONArray jsonTrafficElementsEast = (jsonTrafficEast.getJSONArray("rows")).getJSONObject(0).getJSONArray("elements");
            assert jsonTrafficWest != null;
            JSONArray jsonTrafficElementsWest = (jsonTrafficWest.getJSONArray("rows")).getJSONObject(0).getJSONArray("elements");
            assert jsonTrafficSouth != null;
            JSONArray jsonTrafficElementsSouth = (jsonTrafficSouth.getJSONArray("rows")).getJSONObject(0).getJSONArray("elements");
            assert jsonTrafficNorth != null;
            JSONArray jsonTrafficElementsNorth = (jsonTrafficNorth.getJSONArray("rows")).getJSONObject(0).getJSONArray("elements");
            assert jsonWeather != null;
            JSONObject weather = (jsonWeather.getJSONArray("weather")).getJSONObject(0);
            for (int index = 0; index < jsonTrafficElementsEast.length(); index++) {
                // east
                TrafficStat eastTrafficStat = new TrafficStat();
                eastTrafficStat.setCarParkName(carParkNames[index]);
                eastTrafficStat.setDayOfWeek(now.get(Calendar.DAY_OF_WEEK));
                eastTrafficStat.setMinutes(Double.parseDouble((((
                        jsonTrafficElementsEast.getJSONObject(index)).getJSONObject("duration_in_traffic")).getString("text")).replaceAll("[^\\d.]", "")));
                eastTrafficStat.setTimeOfDay(System.currentTimeMillis());
                eastTrafficStat.setWeather(weather.getString("description"));
                trafficStatsEast.add(eastTrafficStat);
                // west
                TrafficStat westTrafficStat = new TrafficStat();
                westTrafficStat.setCarParkName(carParkNames[index]);
                westTrafficStat.setDayOfWeek(now.get(Calendar.DAY_OF_WEEK));
                westTrafficStat.setMinutes(Double.parseDouble((((
                        jsonTrafficElementsWest.getJSONObject(index)).getJSONObject("duration_in_traffic")).getString("text")).replaceAll("[^\\d.]", "")));
                westTrafficStat.setTimeOfDay(System.currentTimeMillis());
                westTrafficStat.setWeather(weather.getString("description"));
                trafficStatsWest.add(westTrafficStat);
                // south
                TrafficStat southTrafficStat = new TrafficStat();
                southTrafficStat.setCarParkName(carParkNames[index]);
                southTrafficStat.setDayOfWeek(now.get(Calendar.DAY_OF_WEEK));
                southTrafficStat.setMinutes(Double.parseDouble((((
                        jsonTrafficElementsSouth.getJSONObject(index)).getJSONObject("duration_in_traffic")).getString("text")).replaceAll("[^\\d.]", "")));
                southTrafficStat.setTimeOfDay(System.currentTimeMillis());
                southTrafficStat.setWeather(weather.getString("description"));
                trafficStatsSouth.add(southTrafficStat);
                // north
                TrafficStat northTrafficStat = new TrafficStat();
                northTrafficStat.setCarParkName(carParkNames[index]);
                northTrafficStat.setDayOfWeek(now.get(Calendar.DAY_OF_WEEK));
                northTrafficStat.setMinutes(Double.parseDouble((((
                        jsonTrafficElementsNorth.getJSONObject(index)).getJSONObject("duration_in_traffic")).getString("text")).replaceAll("[^\\d.]", "")));
                northTrafficStat.setTimeOfDay(System.currentTimeMillis());
                northTrafficStat.setWeather(weather.getString("description"));
                trafficStatsNorth.add(northTrafficStat);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // east traffic
        DatabaseReference reservationDbEast = (FirebaseDatabase.getInstance()).getReference("traffic").child("east_traffic").child(String.valueOf(System.currentTimeMillis()));
        reservationDbEast.setValue(trafficStatsEast);
        // north traffic
        DatabaseReference reservationDbNorth = (FirebaseDatabase.getInstance()).getReference("traffic").child("north_traffic").child(String.valueOf(System.currentTimeMillis()));
        reservationDbNorth.setValue(trafficStatsNorth);
        // west traffic
        DatabaseReference reservationDbWest = (FirebaseDatabase.getInstance()).getReference("traffic").child("west_traffic").child(String.valueOf(System.currentTimeMillis()));
        reservationDbWest.setValue(trafficStatsWest);
        // south traffic
        DatabaseReference reservationDbSouth = (FirebaseDatabase.getInstance()).getReference("traffic").child("south_traffic").child(String.valueOf(System.currentTimeMillis()));
        reservationDbSouth.setValue(trafficStatsSouth);
    }
}
