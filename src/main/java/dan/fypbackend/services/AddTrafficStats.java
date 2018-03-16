package dan.fypbackend.services;

import com.google.firebase.database.FirebaseDatabase;
import dan.fypbackend.model.CarPark;
import dan.fypbackend.model.TrafficStat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

public class AddTrafficStats extends TimerTask {
    private final ArrayList<CarPark> carParksList;
    private JSONObject weather;
    private String[] carParkNames;

    public AddTrafficStats(ArrayList<CarPark> carParksList) {
        this.carParksList = carParksList;
    }

    @Deprecated
    @Override
    public void run() {
        assert carParksList != null;
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
        ArrayList<TrafficStat> trafficStatsEast = new ArrayList<>();
        ArrayList<TrafficStat> trafficStatsNorth = new ArrayList<>();
        ArrayList<TrafficStat> trafficStatsWest = new ArrayList<>();
        ArrayList<TrafficStat> trafficStatsSouth = new ArrayList<>();
        JSONObject jsonTrafficEast = RetrieveJsonObject.get(urlStringEast.toString());
        JSONObject jsonTrafficWest = RetrieveJsonObject.get(urlStringWest.toString());
        JSONObject jsonTrafficSouth = RetrieveJsonObject.get(urlStringSouth.toString());
        JSONObject jsonTrafficNorth = RetrieveJsonObject.get(urlStringNorth.toString());
        JSONObject jsonWeather = RetrieveJsonObject.get("http://api.openweathermap.org/data/2.5/weather?q=cork&appid=bd6ab1b7b59f866b3e68f34173c5c570");
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
            weather = (jsonWeather.getJSONArray("weather")).getJSONObject(0);
            for (int index = 0; index < jsonTrafficElementsEast.length(); index++) {
                trafficStatsEast = addTrafficStat(index, trafficStatsEast, jsonTrafficElementsEast);
                trafficStatsWest = addTrafficStat(index, trafficStatsWest, jsonTrafficElementsWest);
                trafficStatsNorth = addTrafficStat(index, trafficStatsNorth, jsonTrafficElementsNorth);
                trafficStatsSouth = addTrafficStat(index, trafficStatsSouth, jsonTrafficElementsSouth);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((FirebaseDatabase.getInstance()).getReference("traffic").child("east_traffic").child(String.valueOf(new Date(System.currentTimeMillis())))).setValue(trafficStatsEast);
        ((FirebaseDatabase.getInstance()).getReference("traffic").child("north_traffic").child(String.valueOf(new Date(System.currentTimeMillis())))).setValue(trafficStatsNorth);
        ((FirebaseDatabase.getInstance()).getReference("traffic").child("west_traffic").child(String.valueOf(new Date(System.currentTimeMillis())))).setValue(trafficStatsWest);
        ((FirebaseDatabase.getInstance()).getReference("traffic").child("south_traffic").child(String.valueOf(new Date(System.currentTimeMillis())))).setValue(trafficStatsSouth);
    }

    private ArrayList<TrafficStat> addTrafficStat(int i, ArrayList<TrafficStat> trafArr, JSONArray trafJsonArr) {
        TrafficStat eastTrafficStat;
        eastTrafficStat = new TrafficStat();
        eastTrafficStat.setCarParkName(carParkNames[i]);
        eastTrafficStat.setDayOfWeek((Calendar.getInstance()).get(Calendar.DAY_OF_WEEK));
        eastTrafficStat.setMinutes(Double.parseDouble((((
                trafJsonArr.getJSONObject(i)).getJSONObject("duration_in_traffic")).getString("text")).replaceAll("[^\\d.]", "")));
        eastTrafficStat.setTimeOfDay(System.currentTimeMillis());
        eastTrafficStat.setWeather(weather.getString("description"));
        trafArr.add(eastTrafficStat);
        return trafArr;
    }
}
