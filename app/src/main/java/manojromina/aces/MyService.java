package manojromina.aces;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class MyService extends Service {



    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    NotificationCompat.Builder notification;
    private static final int uniqueID = 1;
    public MyService(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.print("Service Started\n");
        Log.d("MYSERVICE","SERVICE STARTED");

        Runnable runnable=new Runnable() {
            @Override
            public void run() {

                mAuth= FirebaseAuth.getInstance();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                mDatabaseReference= mFirebaseDatabase.getReference().child("Message");


                if (mAuth.getCurrentUser()!=null){



                    /*
                    mDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            System.out.print("Data is changed in Sevice");
                            send_notification();



                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    */


                    mDatabaseReference.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            send_notification();
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }
            }
        };

        Thread thread= new Thread(runnable);
        thread.start();

        return Service.START_STICKY;




    }

    public void send_notification(){
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        //Build the notification
        notification.setSmallIcon(R.drawable.ic_message);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());

        notification.setContentTitle("Alert");
        notification.setContentText("Intrusion Detected");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        //Builds notification and issues it
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());


    }


}