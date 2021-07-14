package com.dildarkhan.cirriusattendance;

import static com.dildarkhan.cirriusattendance.ui.GetAttendanceScreen.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dildarkhan.cirriusattendance.db.Attendance;
import com.dildarkhan.cirriusattendance.db.DatabaseClient;
import com.dildarkhan.cirriusattendance.ui.GetAttendanceScreen;
import com.dildarkhan.cirriusattendance.util.DailySync;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
TextView tvDbCount;
Button btnGetAttendance, btnGetDbCount,btnAddTemp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDbCount=(TextView)findViewById(R.id.tvDbCount);

        btnGetAttendance=(Button)findViewById(R.id.btGetAttendance);
        btnGetDbCount=(Button)findViewById(R.id.btDbCount);
        btnAddTemp=(Button)findViewById(R.id.btAddTemp);
        btnGetAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAttendanceScreen=new Intent(MainActivity.this, GetAttendanceScreen.class);
                startActivity(intentAttendanceScreen);
            }
        });

        btnGetDbCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDbCount();
            }
        });

        btnAddTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempData();
            }
        });



        Calendar calendar = Calendar.getInstance();

        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                7, 0, 0);
        setAlarm(calendar.getTimeInMillis());

    }


    private void getDbCount(){
        //get attendance table row count
        final ProgressDialog pDialog1=new ProgressDialog(this);
        pDialog1.setTitle("Please Wait!");
        pDialog1.setMessage("Loading...");
        pDialog1.setCancelable(false);
        pDialog1.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int num = (Integer) DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .attendanceDao()
                        .getCount();

                updateUI(num);
                if(pDialog1.isShowing()){
                    pDialog1.dismiss();
                }
            }
        });
        t.setPriority(10);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            if(pDialog1.isShowing()){
                pDialog1.dismiss();
            }
        }

    }

    private void updateUI(int num){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //code that updates the UI
                tvDbCount.setText("DB row count is: "+num);
            }
        });

    }


    private void addTempData(){
        //add temporary data
        List<Attendance> attendanceList=new ArrayList<>();
        for (int i=0;i<50000;i++){
            Attendance attendance=new Attendance();
            attendance.setRollno("tmp "+i);
            attendance.setStudentName("TMP student "+i);
            attendance.setSubject("TMP Subject "+i);

            attendanceList.add(attendance);

        }
        class AddAttendance extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                //adding to database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .attendanceDao()
                        .insert(attendanceList);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //do on completion
                Log.d(TAG, "Temp Attendance Save to DB");
                Toast.makeText(MainActivity.this, "Added Bulk attendance for testing purpose!\n", Toast.LENGTH_LONG).show();
            }
        }
        AddAttendance addAttendance=new AddAttendance();
        addAttendance.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.syncWithBackend:
                syncWithBackend();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void syncWithBackend(){
        //code to sync local db to backend
        Toast.makeText(MainActivity.this, "Sync In Progress!\nPlease wait...", Toast.LENGTH_SHORT).show();
        //export db to json file
        //upload this json file to firebase storage
        //on trigger of file export that latest file and load all file objects to firebase firestore database.
        //on trigger of database document element notify to the subscribed users by sending notification.
        exportDbToJSON();
    }

    private void exportDbToJSON(){
        class GetAttendanceData extends AsyncTask<Void, Void, List<Attendance>> {
            @Override
            protected List<Attendance> doInBackground(Void... voids) {
                List<Attendance> attendanceList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .attendanceDao()
                        .getAllAttendance();
                return attendanceList;
            }

            @Override
            protected void onPostExecute(final List<Attendance> attendanceList) {
                super.onPostExecute(attendanceList);
                if(attendanceList.size()>0 && !attendanceList.isEmpty()) {
                    JSONArray jsonArray=new JSONArray();
                    for (int i = 0; i < attendanceList.size(); i++) {
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("RollNo",attendanceList.get(i).getRollno());
                            jsonObject.put("StudentName",attendanceList.get(i).getStudentName());
                            jsonObject.put("Subject",attendanceList.get(i).getSubject());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArray.put(jsonObject);

                    }

                    Log.d("ARRAY: ",jsonArray.toString());
                    //create file using generated json data
                    try{
                        File rootFolder = getApplicationContext().getExternalFilesDir(null);
                        File jsonFile = new File( rootFolder,"Attendance.json");
                        FileWriter writer = new FileWriter(jsonFile);
                        writer.write(jsonArray.toString());
                        writer.close();
                        //Log.d(TAG,jsonArray.length()+" SIZE");
                        proceedToUploadFile(jsonFile);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    Log.d("Worker: ","list is empty");
                }
            }
        }
        GetAttendanceData getData = new GetAttendanceData();
        getData.execute();
    }


    private void proceedToUploadFile(File dataFile){
        final ProgressDialog pDialog2=new ProgressDialog(this);
        pDialog2.setTitle("Please Wait");
        pDialog2.setMessage("Uploading...");
        pDialog2.setCancelable(false);
        pDialog2.show();


        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("jsonData/jsonDataFile.json");

        Uri file= Uri.fromFile(dataFile);
        UploadTask uploadTask = fileRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                if(pDialog2.isShowing()){
                    pDialog2.dismiss();
                }
                Toast.makeText(MainActivity.this, "ERROR !\nPlease try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG,"Upload success");
                if(pDialog2.isShowing()){
                    pDialog2.dismiss();
                }
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                Log.d(TAG,"File URL "+fileRef.getDownloadUrl());
                // Continue with the task to get the download URL
                return fileRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                } else {
                    // Handle failures
                    // ...
                }
                if(pDialog2.isShowing()){
                    pDialog2.dismiss();
                }
            }
        });
    }


    private void setAlarm(long time) {
        Log.d("KHAN","inside alarm method");
        //getting the alarm manager
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //creating a new intent specifying the broadcast receiver
        Intent i = new Intent(this, DailySync.class);

        //creating a pending intent using the intent
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);

        //setting the repeating alarm that will be fired every day
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 30000, AlarmManager.INTERVAL_DAY, pi);
        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show();
    }

}