package com.dildarkhan.cirriusattendance.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Attendance.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AttendanceDao attendanceDao();

}