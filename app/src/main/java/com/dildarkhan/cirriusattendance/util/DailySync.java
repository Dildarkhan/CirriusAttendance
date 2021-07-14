package com.dildarkhan.cirriusattendance.util;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dildarkhan.cirriusattendance.MainActivity;
import com.dildarkhan.cirriusattendance.db.Attendance;
import com.dildarkhan.cirriusattendance.db.DatabaseClient;
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

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class DailySync extends BroadcastReceiver {
    Context mContext;

    //the method will be fired when the alarm is triggerred
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        //code to sync local db to backend
        Toast.makeText(context, "Sync In Progress!\nPlease wait...", Toast.LENGTH_SHORT).show();
        //export db to json file
        //upload this json file to firebase storage
        //on trigger of file export that latest file and load all file objects to firebase firestore database.
        //on trigger of database document element notify to the subscribed users by sending notification.
        try {
            syncWithBackend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void syncWithBackend(){
        //code to sync local db to backend
        Toast.makeText(mContext, "Sync In Progress!\nPlease wait...", Toast.LENGTH_SHORT).show();
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
                        .getInstance(mContext)
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
                        File rootFolder = mContext.getExternalFilesDir(null);
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
        final ProgressDialog pDialog2=new ProgressDialog(mContext);
        pDialog2.setTitle("Please Wait");
        pDialog2.setMessage("Uploading...");
        pDialog2.setCancelable(false);
        //pDialog2.show();


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
                Toast.makeText(mContext, "ERROR !\nPlease try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d("DailySync","Upload success");
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
                Log.d("DailySync","File URL "+fileRef.getDownloadUrl());
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

}