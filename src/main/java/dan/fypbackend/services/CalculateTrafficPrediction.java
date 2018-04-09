package dan.fypbackend.services;

import com.google.firebase.database.*;
import dan.fypbackend.model.CarPark;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimerTask;

public class CalculateTrafficPrediction extends TimerTask {
    private final ArrayList<CarPark> carParksList;
    private final DatabaseReference traffic = FirebaseDatabase.getInstance().getReference("traffic");

    public CalculateTrafficPrediction(ArrayList<CarPark> carParksList) {
        this.carParksList = carParksList;
    }

    @Override
    public void run() {
        traffic.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateTrainCsv(dataSnapshot);
                updateTodayCsv();
                String cmd = "python FYP_MachineLearning.py";
                Process exec;
                try {
                    exec = Runtime.getRuntime().exec(cmd);
                    System.out.print(exec.getErrorStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean updateTodayCsv() {
        JSONObject jsonWeather = RetrieveJsonObject.get("http://api.openweathermap.org/data/2.5/weather?q=cork&appid=bd6ab1b7b59f866b3e68f34173c5c570");
        String weatherString = ((Objects.requireNonNull(jsonWeather).getJSONArray("weather")).getJSONObject(0)).getString("description");

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
        } catch (IOException e) {
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
                            Objects.requireNonNull(fileWriter).write(String.valueOf(output));
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            }
        }
        try {
            Objects.requireNonNull(fileWriter).close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}