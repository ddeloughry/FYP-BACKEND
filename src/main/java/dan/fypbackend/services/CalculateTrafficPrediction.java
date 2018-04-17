package dan.fypbackend.services;

import com.google.firebase.database.*;
import dan.fypbackend.model.CarPark;
import dan.fypbackend.model.TrafficDelay;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;

public class CalculateTrafficPrediction extends TimerTask {
    private final ArrayList<CarPark> carParksList;
    private final DatabaseReference traffic = FirebaseDatabase.getInstance().getReference("traffic");
    private final DatabaseReference delays = FirebaseDatabase.getInstance().getReference("delays");

    public CalculateTrafficPrediction(ArrayList<CarPark> carParksList) {
        this.carParksList = carParksList;
    }

    @Override
    public void run() {
        traffic.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (updateTrainCsv(dataSnapshot)) {
                    System.out.println("Training CSV update success");
                }
                try {
                    if (updateTodayCsv()) {
                        System.out.println("Today CSV update success");
                    }
                } catch (MalformedURLException | JSONException e) {
                    e.printStackTrace();
                }
                if (doMlAndExportJson()) {
                    System.out.println("Machine learning and update JSON success");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ArrayList<TrafficDelay> trafficDelays = new ArrayList<>();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader("machine_learning/result.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = null;
        if (fileReader != null) {
            bufferedReader = new BufferedReader(fileReader);
        }
        JSONArray jsonArray = null;
        try {
            if (bufferedReader != null) {
                jsonArray = new JSONArray(bufferedReader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject current = (JSONObject) jsonArray.get(i);
                trafficDelays = addDelay(current, trafficDelays);
            }
        }
        int count = 0;
        for (TrafficDelay delay : trafficDelays) {
            if (delays.child(String.valueOf(count)) != null) {
                delays.child(String.valueOf(count)).removeValue((error, ref) -> System.out.print("Removed " + error));
            }
            delays.child(String.valueOf(count)).setValue(delay, (error, ref) -> System.out.print("Added " + error));
            count++;
        }
    }

    private boolean doMlAndExportJson() {
        String cmd = "python FYP_MachineLearning.py";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean updateTodayCsv() throws MalformedURLException, JSONException {
        JSONObject jsonWeather = RetrieveJsonObject.get(new URL("http://api.openweathermap.org/data/2.5/weather?q=cork&appid=bd6ab1b7b59f866b3e68f34173c5c570"));
        if (jsonWeather != null) {
            String weatherString = (((jsonWeather).getJSONArray("weather")).getJSONObject(0)).getString("description");
            ArrayList<String> directions = new ArrayList<>();
            directions.add("north");
            directions.add("south");
            directions.add("east");
            directions.add("west");
            String[] carParkNames = new String[carParksList.size()];
            int i = 0;
            while (i < carParksList.size()) {
                carParkNames[i] = carParksList.get(i).getName();
                i++;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            String fileName = "machine_learning/today.csv";
            File file = new File(fileName);
            FileWriter fileWriter;
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write("day,hour,minute,car_park_name,weather,direction\n");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            for (String direction : directions) {
                for (String carParkName : carParkNames) {
                    for (int j = 0; j < 24; j++) {
                        for (int minute = 0; minute < 31; minute += 30) {
                            StringBuilder output = new StringBuilder();
                            output.append(calendar.get(Calendar.DAY_OF_WEEK)).append(",")
                                    .append(j).append(",")
                                    .append(minute).append(",")
                                    .append(carParkName).append(",")
                                    .append(weatherString).append(",")
                                    .append(direction).append("\n");
                            try {
                                (fileWriter).write(String.valueOf(output));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        }
                    }
                }
            }
            try {
                (fileWriter).close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean updateTrainCsv(DataSnapshot dataSnapshot) {
        String fileName = "machine_learning/data.csv";
        File file = new File(fileName);
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write("day,hour,minute,car_park_name,time,weather,direction\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            StringBuilder output = new StringBuilder();
            if (snapshot.child("date").getValue() != null) {
                long longDate = (long) snapshot.child("date").getValue();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(longDate);
                output.append(calendar.get(Calendar.DAY_OF_WEEK)).append(",")
                        .append(calendar.get(Calendar.HOUR_OF_DAY)).append(",")
                        .append(calendar.get(Calendar.MINUTE)).append(",")
                        .append(snapshot.child("carParkName").getValue()).append(",")
                        .append(snapshot.child("delay").getValue()).append(",")
                        .append(snapshot.child("weather").getValue()).append(",")
                        .append(snapshot.child("direction").getValue()).append("\n");
                try {
                    fileWriter.write(String.valueOf(output));
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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