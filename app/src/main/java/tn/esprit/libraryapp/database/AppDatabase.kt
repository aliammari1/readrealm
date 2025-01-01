package tn.esprit.libraryapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import tn.esprit.libraryapp.dao.BookProgressDao
import tn.esprit.libraryapp.models.BookProgress

@Database(entities = [BookProgress::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookProgressDao(): BookProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                ?: synchronized(this) {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "book_progress_db"
                    )
                        .build()
                        .also { INSTANCE = it }
                }
        }
    }
}
