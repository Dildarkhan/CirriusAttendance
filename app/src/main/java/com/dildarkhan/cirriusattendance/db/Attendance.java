package com.dildarkhan.cirriusattendance.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by DILDARKHAN PATHAN
 * Email Id: m.dildarkhan@gmail.com
 */
@Entity
public class Attendance implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "rollNo")
    private String rollno;
    @ColumnInfo(name = "studentName")
    private String studentName;
    @ColumnInfo(name = "subject")
    private String subject;

    public Attendance(){

    }

    public Attendance(String rollno, String studentName, String subject) {
        this.rollno = rollno;
        this.studentName = studentName;
        this.subject = subject;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRollno() {
        return rollno;
    }

    public void setRollno(String rollno) {
        this.rollno = rollno;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


}