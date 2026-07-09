package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScanHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ScanHistory)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM scan_history")
    suspend fun clearHistory()
}

@Dao
interface ScamReportDao {
    @Query("SELECT * FROM scam_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ScamReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ScamReport)

    @Update
    suspend fun updateReport(report: ScamReport)

    @Query("DELETE FROM scam_reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)
}

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_searches ORDER BY timestamp DESC")
    fun getAllSavedSearches(): Flow<List<SavedSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSearch(search: SavedSearch)

    @Query("DELETE FROM saved_searches WHERE id = :id")
    suspend fun deleteSavedSearchById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_searches WHERE type = :type AND query = :query)")
    fun isSaved(type: String, query: String): Flow<Boolean>

    @Query("DELETE FROM saved_searches WHERE type = :type AND query = :query")
    suspend fun deleteSavedByQuery(type: String, query: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Database(
    entities = [ScanHistory::class, ScamReport::class, SavedSearch::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun scamReportDao(): ScamReportDao
    abstract fun savedSearchDao(): SavedSearchDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scam_shield_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
