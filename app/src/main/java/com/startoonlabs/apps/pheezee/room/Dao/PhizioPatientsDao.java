package com.startoonlabs.apps.pheezee.room.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.startoonlabs.apps.pheezee.patientsRecyclerView.PatientsListData;
import com.startoonlabs.apps.pheezee.room.Entity.PhizioPatients;

import java.util.List;

@Dao
public interface PhizioPatientsDao {
    @Insert
    void insert(PhizioPatients patient);

    @Update
    void update(PhizioPatients patient);

    @Delete
    void delete(PhizioPatients patient);

    @Insert
    void insertAllPatients(List<PhizioPatients> patients);

    @Query("SELECT * from phizio_patients WHERE status LIKE 'Active'")
    LiveData<List<PhizioPatients>> getAllActivePatients();

    @Query("Select * from phizio_patients WHERE patientid=:id")
    PhizioPatients getPatient(String id);

    @Query("UPDATE phizio_patients SET status=:status WHERE patientid=:patientid")
    void updatePatientStatus(String status, String patientid);
}
