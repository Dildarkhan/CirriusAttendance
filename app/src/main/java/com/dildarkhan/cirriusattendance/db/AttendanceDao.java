package com.dildarkhan.cirriusattendance.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Created by DILDARKHAN PATHAN
 * Email Id: m.dildarkhan@gmail.com
 */
@Dao
public interface AttendanceDao {
    @Query("SELECT * FROM Attendance")
    List<Attendance> getAllAttendance();


     /*//"SELECT * FROM locationupdates WHERE pid = :pid LIMIT 1"
    @Query("SELECT * FROM locationupdates WHERE pid =:pid LIMIT 1")
    LocationUpdates getLocationUpdatesExists(int pid);*/

    @Query("SELECT * FROM Attendance WHERE studentName =:studName")
    List<Attendance> getAttendance(String studName);

    @Insert
    void insert(Attendance attendance);
    @Insert
    void insert(List<Attendance> attendanceList);

    @Delete
    void delete(Attendance attendance);

    @Update
    void update(Attendance attendance);

    @Query("DELETE FROM attendance")
    void nukeTable();


    @Query("Select count(id) from attendance")
    Integer getCount();
}
