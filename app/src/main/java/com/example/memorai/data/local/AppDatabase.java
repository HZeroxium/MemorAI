package com.example.memorai.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.dao.PhotoAlbumCrossRefDao;
import com.example.memorai.data.local.dao.PhotoDao;
import com.example.memorai.data.local.dao.UserDao;
import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.data.local.entity.PhotoAlbumCrossRef;
import com.example.memorai.data.local.entity.PhotoEntity;
import com.example.memorai.data.local.entity.UserEntity;

@Database(entities = {AlbumEntity.class, PhotoEntity.class, UserEntity.class, PhotoAlbumCrossRef.class},
        version = 4,  // Tăng version từ 3 lên 4
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    // Migration từ version 3 lên 4 (thêm cột is_private)
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE photos ADD COLUMN is_private INTEGER NOT NULL DEFAULT 0");
        }
    };

    public abstract AlbumDao albumDao();
    public abstract PhotoDao photoDao();
    public abstract UserDao userDao();
    public abstract PhotoAlbumCrossRefDao photoAlbumCrossRefDao();
}