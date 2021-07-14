package com.dildarkhan.cirriusattendance.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dildarkhan.cirriusattendance.MainActivity;
import com.dildarkhan.cirriusattendance.R;
import com.dildarkhan.cirriusattendance.db.Attendance;
import com.dildarkhan.cirriusattendance.db.DatabaseClient;
import com.google.android.material.textfield.TextInputLayout;

public class GetAttendanceScreen extends AppCompatActivity {
    public static final String TAG = "GetAttendanceScreen";
    private EditText inputRollNo,inputName,inputSubject;
    private TextInputLayout inputLayoutRollNo,inputLayoutName,inputLayoutSubject;
    Button btnSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_attendance_screen);

        inputLayoutRollNo = (TextInputLayout) findViewById(R.id.input_layout_rollNo);
        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);
        inputLayoutSubject = (TextInputLayout) findViewById(R.id.input_layout_subject);

        inputRollNo=(EditText)findViewById(R.id.input_rollNo);
        inputName=(EditText)findViewById(R.id.input_name);
        inputSubject=(EditText) findViewById(R.id.input_subject);


        btnSave=(Button) findViewById(R.id.btSaveAttendance);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForSave();
            }
        });
    }

    private void validateForSave(){
        if (!validateName()) {
            return;
        }
        if (!validateRollNo()) {
            return;
        }
        if (!validateSubject()) {
            return;
        }
        saveToDb();
    }
    private boolean validateRollNo() {
        if (inputRollNo.getText().toString().trim().isEmpty()) {
            inputLayoutRollNo.setError("Please Enter Student Roll No");
            inputLayoutRollNo.setBackgroundColor(Color.WHITE);
            requestFocus(inputRollNo);
            return false;
        } else {
            inputLayoutRollNo.setErrorEnabled(false);
        }
        return true;
    }
    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError("Please Enter Student Name");
            inputLayoutName.setBackgroundColor(Color.WHITE);
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateSubject() {
        if (inputSubject.getText().toString().trim().isEmpty()) {
            inputLayoutSubject.setError("Please Enter Student Subject");
            inputLayoutSubject.setBackgroundColor(Color.WHITE);
            requestFocus(inputSubject);
            return false;
        } else {
            inputLayoutSubject.setErrorEnabled(false);
        }
        return true;
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void saveToDb(){
        String rollNo=inputRollNo.getText().toString();
        String name=inputName.getText().toString();
        String subject=inputSubject.getText().toString();
        Attendance attendance=new Attendance(rollNo,name,subject);
        class AddAttendance extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                //adding to database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .attendanceDao()
                        .insert(attendance);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //do on completion
                Log.d(TAG,"Attendance Save to DB");
                Toast.makeText(GetAttendanceScreen.this, "Attendance Collected Successfully !", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(GetAttendanceScreen.this, MainActivity.class);
                startActivity(intent);
                GetAttendanceScreen.this.finish();
            }
        }
        AddAttendance addAttendance=new AddAttendance();
        addAttendance.execute();

    }
}