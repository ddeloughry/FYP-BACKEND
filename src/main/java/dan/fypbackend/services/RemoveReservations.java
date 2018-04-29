package dan.fypbackend.services;

import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.TimerTask;

public class RemoveReservations extends TimerTask {

    @Deprecated
    @Override
    public void run() {
        FirebaseDatabase.getInstance().getReference("reservation")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            long endTime = (long) snapshot.child("endTime").getValue();
                            Calendar calEnd = Calendar.getInstance();
                            calEnd.setTimeInMillis(endTime);
                            Calendar now = Calendar.getInstance();
                            if (calEnd.before(now)) {
                                snapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        System.out.print("-,");
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}
