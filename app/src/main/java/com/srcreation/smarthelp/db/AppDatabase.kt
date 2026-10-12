package com.srcreation.smarthelp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicine_reminders WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllMedicines(profileId: Int): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicine_reminders")
    suspend fun getFullMedicineList(): List<MedicineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicineInternal(medicine: MedicineEntity)

    @Query("DELETE FROM medicine_reminders WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)

    @Query("SELECT * FROM medicine_reminders WHERE synced = 0")
    suspend fun getUnsyncedMedicines(): List<MedicineEntity>

    @Query("UPDATE medicine_reminders SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface MedicineDoseDao {
    @Query("SELECT * FROM medicine_doses WHERE profileId = :profileId")
    fun getAllDoses(profileId: Int): Flow<List<MedicineDoseEntity>>

    @Query("SELECT * FROM medicine_doses")
    suspend fun getFullDoseList(): List<MedicineDoseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDose(dose: MedicineDoseEntity)

    @Query("SELECT * FROM medicine_doses WHERE synced = 0")
    suspend fun getUnsyncedDoses(): List<MedicineDoseEntity>

    @Query("UPDATE medicine_doses SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM medicine_doses WHERE medicineId = :medicineId")
    suspend fun deleteDosesByMedicineIdInternal(medicineId: String)
}

@Dao
interface PendingDeletionDao {
    @Query("SELECT * FROM pending_deletions")
    suspend fun getAllPendingDeletions(): List<PendingDeletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pending: PendingDeletionEntity)

    @Delete
    suspend fun delete(pending: PendingDeletionEntity)
}

@Dao
interface BpDao {
    @Query("SELECT * FROM bp_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<BpEntity>>

    @Query("SELECT * FROM bp_records")
    suspend fun getFullBpList(): List<BpEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BpEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BpEntity>)

    @Delete
    suspend fun deleteBpInternal(record: BpEntity)

    @Query("DELETE FROM bp_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)

    @Query("SELECT * FROM bp_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<BpEntity>

    @Query("UPDATE bp_records SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface InsulinDao {
    @Query("SELECT * FROM insulin_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<InsulinEntity>>

    @Query("SELECT * FROM insulin_records")
    suspend fun getFullInsulinList(): List<InsulinEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: InsulinEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<InsulinEntity>)

    @Delete
    suspend fun deleteInsulinInternal(record: InsulinEntity)

    @Query("DELETE FROM insulin_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)

    @Query("SELECT * FROM insulin_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<InsulinEntity>

    @Query("UPDATE insulin_records SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface StepDao {
    @Query("SELECT * FROM step_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<StepEntity>>

    @Query("SELECT * FROM step_records")
    suspend fun getFullStepList(): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: StepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<StepEntity>)

    @Delete
    suspend fun deleteRecord(record: StepEntity)

    @Query("DELETE FROM step_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)

    @Query("SELECT * FROM step_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<StepEntity>

    @Query("UPDATE step_records SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface PeriodDao {
    @Query("SELECT * FROM period_settings WHERE profileId = :profileId")
    suspend fun getPeriodSettings(profileId: Int): PeriodEntity?

    @Query("SELECT * FROM period_settings WHERE profileId = :profileId")
    fun getSettings(profileId: Int): Flow<PeriodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: PeriodEntity)

    @Query("SELECT * FROM period_settings")
    suspend fun getFullSettingsList(): List<PeriodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: PeriodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSettings(settings: List<PeriodEntity>)

    @Query("SELECT * FROM period_logs WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllLogs(profileId: Int): Flow<List<PeriodLogEntity>>

    @Query("SELECT * FROM period_logs")
    suspend fun getFullLogList(): List<PeriodLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PeriodLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<PeriodLogEntity>)

    @Delete
    suspend fun deleteLog(log: PeriodLogEntity)
}

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccine_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllVaccines(profileId: Int): Flow<List<VaccineEntity>>

    @Query("SELECT * FROM vaccine_records")
    suspend fun getFullVaccineList(): List<VaccineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccine(vaccine: VaccineEntity)

    @Update
    suspend fun updateVaccine(vaccine: VaccineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vaccines: List<VaccineEntity>)

    @Delete
    suspend fun deleteVaccine(vaccine: VaccineEntity)

    @Query("DELETE FROM vaccine_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)
}

@Dao
interface BmiDao {
    @Query("SELECT * FROM bmi_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<BmiEntity>>

    @Query("SELECT * FROM bmi_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllBmiRecords(profileId: Int): Flow<List<BmiEntity>>

    @Query("SELECT * FROM health_goals WHERE profileId = :profileId")
    fun getAllHealthGoals(profileId: Int): Flow<List<HealthGoalEntity>>

    @Query("SELECT * FROM health_goals WHERE profileId = :profileId AND type = :type")
    fun getHealthGoals(profileId: Int, type: String): Flow<List<HealthGoalEntity>>

    @Query("SELECT * FROM bmi_records")
    suspend fun getFullBmiList(): List<BmiEntity>

    @Query("SELECT * FROM health_goals")
    suspend fun getFullGoalList(): List<HealthGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BmiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBmi(record: BmiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthGoal(goal: HealthGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBmi(bmiList: List<BmiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<HealthGoalEntity>)

    @Delete
    suspend fun deleteRecord(record: BmiEntity)

    @Query("DELETE FROM bmi_records WHERE id = :id")
    suspend fun deleteBmiById(id: Int)

    @Query("DELETE FROM health_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Int)

    @Query("DELETE FROM bmi_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)
}

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<MedicalRecordEntity>>

    @Query("SELECT * FROM medical_records")
    suspend fun getFullRecordList(): List<MedicalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MedicalRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MedicalRecordEntity>)

    @Delete
    suspend fun deleteRecord(record: MedicalRecordEntity)

    @Query("DELETE FROM medical_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)
}

@Dao
interface MedicalIdDao {
    @Query("SELECT * FROM user_medical_id WHERE profileId = :profileId")
    suspend fun getMedicalIdSuspend(profileId: Int): MedicalIdEntity?

    @Query("SELECT * FROM user_medical_id WHERE profileId = :profileId")
    fun getMedicalId(profileId: Int): Flow<MedicalIdEntity?>

    @Query("SELECT * FROM user_medical_id")
    suspend fun getFullMedicalIdList(): List<MedicalIdEntity>

    @Query("SELECT * FROM emergency_contacts")
    suspend fun getFullContactList(): List<EmergencyContactEntity>

    @Query("SELECT * FROM emergency_contacts WHERE profileId = :profileId")
    fun getContactList(profileId: Int): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE profileId = :profileId")
    fun getAllContacts(profileId: Int): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalId(medicalId: MedicalIdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMedicalId(medicalId: MedicalIdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedicalIds(medicalIds: List<MedicalIdEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContacts(contacts: List<EmergencyContactEntity>)

    @Query("DELETE FROM user_medical_id WHERE profileId = :profileId")
    suspend fun deleteMedicalIdByProfile(profileId: Int)

    @Query("DELETE FROM emergency_contacts WHERE profileId = :profileId")
    suspend fun deleteContactsByProfile(profileId: Int)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllNotifications(profileId: Int): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications")
    suspend fun getFullNotificationList(): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE profileId = :profileId AND id IN (:ids)")
    suspend fun deleteMultiple(profileId: Int, ids: List<Int>)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllLogs(profileId: Int): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE profileId = :profileId AND date = :date")
    fun getLogsByDate(profileId: Int, date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs")
    suspend fun getAllWaterLogs(): List<WaterLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWaterLogs(logs: List<WaterLogEntity>)

    @Delete
    suspend fun deleteLog(log: WaterLogEntity)

    @Query("DELETE FROM water_logs WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM doctor_appointments WHERE profileId = :profileId ORDER BY appointmentDate DESC")
    fun getAllAppointments(profileId: Int): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM doctor_appointments")
    suspend fun getFullAppointmentList(): List<AppointmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAppointments(appointments: List<AppointmentEntity>)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Query("DELETE FROM doctor_appointments WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)
}

@Dao
interface IoDao {
    @Query("SELECT * FROM io_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<IoRecordEntity>>

    @Query("SELECT * FROM io_records WHERE profileId = :profileId AND date = :date")
    fun getRecordsByDate(profileId: Int, date: String): Flow<List<IoRecordEntity>>

    @Query("SELECT * FROM io_records LIMIT 1")
    suspend fun getAnyRecord(): List<IoRecordEntity>

    @Query("SELECT * FROM io_records")
    suspend fun getFullIoList(): List<IoRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: IoRecordEntity)

    @Delete
    suspend fun deleteRecord(record: IoRecordEntity)

    @Query("DELETE FROM io_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)
}

@Dao
interface GyneDao {
    @Query("SELECT * FROM gyne_profiles WHERE profileId = :profileId")
    fun getGyneProfile(profileId: Int): Flow<GyneProfileEntity?>

    @Query("SELECT * FROM gyne_profiles")
    suspend fun getFullGyneList(): List<GyneProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGyneProfile(profile: GyneProfileEntity)

    @Query("UPDATE gyne_profiles SET isPregnant = :isPregnant WHERE profileId = :profileId")
    suspend fun updatePregnancyStatus(profileId: Int, isPregnant: Boolean)

    @Query("DELETE FROM gyne_profiles WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)

    @Query("SELECT * FROM pregnancy_kick_logs WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllKicks(profileId: Int): Flow<List<KickLogEntity>>

    @Query("SELECT * FROM pregnancy_kick_logs")
    suspend fun getFullKickList(): List<KickLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKick(kick: KickLogEntity)

    @Delete
    suspend fun deleteKick(kick: KickLogEntity)
}

@Dao
interface PregnancyDao {
    @Query("SELECT * FROM pregnancy_kick_logs WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllKickLogs(profileId: Int): Flow<List<KickLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKickLog(log: KickLogEntity)

    @Delete
    suspend fun deleteKickLog(log: KickLogEntity)
}

@Dao
interface TempDao {
    @Query("SELECT * FROM temp_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<TempEntity>>

    @Query("SELECT * FROM temp_records")
    suspend fun getFullTempList(): List<TempEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TempEntity)

    @Delete
    suspend fun deleteTempInternal(record: TempEntity)

    @Query("DELETE FROM temp_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)

    @Query("SELECT * FROM temp_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<TempEntity>

    @Query("UPDATE temp_records SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface LabDao {
    @Query("SELECT * FROM lab_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<LabRecordEntity>>

    @Query("SELECT * FROM lab_records")
    suspend fun getFullLabList(): List<LabRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LabRecordEntity)

    @Delete
    suspend fun deleteRecord(record: LabRecordEntity)

    @Query("DELETE FROM lab_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)
}

@Dao
interface PulseDao {
    @Query("SELECT * FROM pulse_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<PulseEntity>>

    @Query("SELECT * FROM pulse_records")
    suspend fun getFullPulseList(): List<PulseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PulseEntity)

    @Delete
    suspend fun deletePulseInternal(record: PulseEntity)

    @Query("DELETE FROM pulse_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)

    @Query("SELECT * FROM pulse_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<PulseEntity>

    @Query("UPDATE pulse_records SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface SpO2Dao {
    @Query("SELECT * FROM spo2_records WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllRecords(profileId: Int): Flow<List<SpO2Entity>>

    @Query("SELECT * FROM spo2_records")
    suspend fun getFullSpO2List(): List<SpO2Entity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SpO2Entity)

    @Delete
    suspend fun deleteSpO2Internal(record: SpO2Entity)

    @Query("DELETE FROM spo2_records WHERE profileId = :profileId")
    suspend fun deleteByProfile(profileId: Int)

    @Query("SELECT * FROM spo2_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<SpO2Entity>

    @Query("UPDATE spo2_records SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfile(id: Int): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): ProfileEntity?

    @Query("SELECT * FROM profiles")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles")
    suspend fun getProfileList(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfileIgnore(profile: ProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProfiles(profiles: List<ProfileEntity>)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)
}

@Database(
    entities = [
        ProfileEntity::class,
        MedicineEntity::class,
        MedicineDoseEntity::class,
        BpEntity::class,
        InsulinEntity::class,
        StepEntity::class,
        PeriodEntity::class,
        VaccineEntity::class,
        BmiEntity::class,
        HealthGoalEntity::class,
        PeriodLogEntity::class,
        MedicalRecordEntity::class,
        MedicalIdEntity::class,
        EmergencyContactEntity::class,
        NotificationEntity::class,
        WaterLogEntity::class,
        AppointmentEntity::class,
        IoRecordEntity::class,
        GyneProfileEntity::class,
        KickLogEntity::class,
        TempEntity::class,
        LabRecordEntity::class,
        PulseEntity::class,
        SpO2Entity::class,
        DietFoodEntity::class,
        MealEntity::class,
        MealFoodItemEntity::class,
        UserDietProfileEntity::class,
        PendingDeletionEntity::class
    ], 
    version = 31, 
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun medicineDao(): MedicineDao
    abstract fun medicineDoseDao(): MedicineDoseDao
    abstract fun bpDao(): BpDao
    abstract fun insulinDao(): InsulinDao
    abstract fun stepDao(): StepDao
    abstract fun periodDao(): PeriodDao
    abstract fun vaccineDao(): VaccineDao
    abstract fun bmiDao(): BmiDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun medicalIdDao(): MedicalIdDao
    abstract fun notificationDao(): NotificationDao
    abstract fun waterDao(): WaterDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun ioDao(): IoDao
    abstract fun gyneDao(): GyneDao
    abstract fun pregnancyDao(): PregnancyDao
    abstract fun tempDao(): TempDao
    abstract fun labDao(): LabDao
    abstract fun pulseDao(): PulseDao
    abstract fun spo2Dao(): SpO2Dao
    abstract fun dietDao(): DietDao
    abstract fun pendingDeletionDao(): PendingDeletionDao

    companion object {
        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. bp_records
                database.execSQL("CREATE TABLE bp_records_new (id TEXT NOT NULL, profileId INTEGER NOT NULL, systolic INTEGER NOT NULL, diastolic INTEGER NOT NULL, timeCategory TEXT NOT NULL, date TEXT NOT NULL, timestamp INTEGER NOT NULL, synced INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id));")
                database.execSQL("INSERT INTO bp_records_new (id, profileId, systolic, diastolic, timeCategory, date, timestamp, synced) SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))), profileId, systolic, diastolic, timeCategory, date, timestamp, 0 FROM bp_records;")
                database.execSQL("DROP TABLE bp_records;")
                database.execSQL("ALTER TABLE bp_records_new RENAME TO bp_records;")

                // 2. insulin_records
                database.execSQL("CREATE TABLE insulin_records_new (id TEXT NOT NULL, profileId INTEGER NOT NULL, glucose TEXT NOT NULL, unit TEXT NOT NULL, dose TEXT NOT NULL, iUnit TEXT NOT NULL, mealTime TEXT NOT NULL, dayTime TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL, timestamp INTEGER NOT NULL, synced INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id));")
                database.execSQL("INSERT INTO insulin_records_new (id, profileId, glucose, unit, dose, iUnit, mealTime, dayTime, date, time, timestamp, synced) SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))), profileId, glucose, unit, dose, iUnit, mealTime, dayTime, date, time, timestamp, 0 FROM insulin_records;")
                database.execSQL("DROP TABLE insulin_records;")
                database.execSQL("ALTER TABLE insulin_records_new RENAME TO insulin_records;")

                // 3. step_records
                database.execSQL("CREATE TABLE step_records_new (id TEXT NOT NULL, profileId INTEGER NOT NULL, steps INTEGER NOT NULL, date TEXT NOT NULL, timestamp INTEGER NOT NULL, synced INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id));")
                database.execSQL("INSERT INTO step_records_new (id, profileId, steps, date, timestamp, synced) SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))), profileId, steps, date, timestamp, 0 FROM step_records;")
                database.execSQL("DROP TABLE step_records;")
                database.execSQL("ALTER TABLE step_records_new RENAME TO step_records;")

                // 4. pulse_records
                database.execSQL("CREATE TABLE pulse_records_new (id TEXT NOT NULL, profileId INTEGER NOT NULL, pulseBpm INTEGER NOT NULL, activityState TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL, synced INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id));")
                database.execSQL("INSERT INTO pulse_records_new (id, profileId, pulseBpm, activityState, date, time, synced) SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))), profileId, pulseBpm, activityState, date, time, synced FROM pulse_records;")
                database.execSQL("DROP TABLE pulse_records;")
                database.execSQL("ALTER TABLE pulse_records_new RENAME TO pulse_records;")

                // 5. spo2_records
                database.execSQL("CREATE TABLE spo2_records_new (id TEXT NOT NULL, profileId INTEGER NOT NULL, spo2Percentage INTEGER NOT NULL, pulseRate INTEGER NOT NULL, breathingStatus TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL, synced INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id));")
                database.execSQL("INSERT INTO spo2_records_new (id, profileId, spo2Percentage, pulseRate, breathingStatus, date, time, synced) SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))), profileId, spo2Percentage, pulseRate, breathingStatus, date, time, synced FROM spo2_records;")
                database.execSQL("DROP TABLE spo2_records;")
                database.execSQL("ALTER TABLE spo2_records_new RENAME TO spo2_records;")

                // 6. temp_records
                database.execSQL("CREATE TABLE temp_records_new (id TEXT NOT NULL, profileId INTEGER NOT NULL, tempValue REAL NOT NULL, unit TEXT NOT NULL, bodySite TEXT NOT NULL, status TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL, timestamp INTEGER NOT NULL, synced INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id));")
                database.execSQL("INSERT INTO temp_records_new (id, profileId, tempValue, unit, bodySite, status, date, time, timestamp, synced) SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))), profileId, tempValue, unit, bodySite, status, date, time, timestamp, synced FROM temp_records;")
                database.execSQL("DROP TABLE temp_records;")
                database.execSQL("ALTER TABLE temp_records_new RENAME TO temp_records;")

                // 7. medicine_reminders
                database.execSQL("CREATE TABLE medicine_reminders_new (id TEXT NOT NULL, profileId INTEGER NOT NULL, name TEXT NOT NULL, dosageValue TEXT NOT NULL, dosageUnit TEXT NOT NULL, category TEXT NOT NULL, foodRelation TEXT NOT NULL, selectedTimes TEXT NOT NULL, customTime TEXT, durationDays INTEGER NOT NULL, isContinuous INTEGER NOT NULL, initialStock INTEGER NOT NULL, currentStock INTEGER NOT NULL, lastTakenTimestamp INTEGER NOT NULL, isActive INTEGER NOT NULL, synced INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id));")
                database.execSQL("INSERT INTO medicine_reminders_new (id, profileId, name, dosageValue, dosageUnit, category, foodRelation, selectedTimes, customTime, durationDays, isContinuous, initialStock, currentStock, lastTakenTimestamp, isActive, synced) SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))), profileId, name, dosageValue, dosageUnit, category, foodRelation, selectedTimes, customTime, durationDays, isContinuous, initialStock, currentStock, lastTakenTimestamp, isActive, synced FROM medicine_reminders;")
                database.execSQL("DROP TABLE medicine_reminders;")
                database.execSQL("ALTER TABLE medicine_reminders_new RENAME TO medicine_reminders;")
            }
        }

        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS medicine_doses (id TEXT NOT NULL, profileId INTEGER NOT NULL, medicineId TEXT NOT NULL, slot TEXT NOT NULL, date TEXT NOT NULL, taken INTEGER NOT NULL, takenAt INTEGER NOT NULL, synced INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id));")
            }
        }

        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `pending_deletions` (`id` TEXT NOT NULL, `ownerUid` TEXT NOT NULL, `kind` TEXT NOT NULL, `docId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `pending_deletions` (`id` TEXT NOT NULL, `ownerUid` TEXT NOT NULL, `kind` TEXT NOT NULL, `docId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_help_database"
                )
                .addMigrations(MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
