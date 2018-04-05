package dan.fypbackend.services;

import com.google.firebase.database.*;

import java.util.TimerTask;

public class CalculateTrafficPrediction extends TimerTask {

    @Override
    public void run() {
        DatabaseReference traffic = FirebaseDatabase.getInstance().getReference("traffic");
        traffic.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    System.out.print(snapshot.getValue().toString() + "\n");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}