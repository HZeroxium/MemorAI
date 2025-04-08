// data/local/dao/PhotoAlbumCrossRefDao.java
package com.example.memorai.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.memorai.data.local.entity.PhotoAlbumCrossRef;

import java.util.List;

@Dao
public interface PhotoAlbumCrossRefDao {

    // Thêm một liên kết
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCrossRef(PhotoAlbumCrossRef crossRef);

    // Thêm nhiều liên kết cùng lúc (tối ưu cho đồng bộ hóa)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCrossRefs(List<PhotoAlbumCrossRef> crossRefs);

    // Xóa một liên kết bằng đối tượng (giữ nguyên từ phiên bản của bạn)
    @Delete
    void deleteCrossRef(PhotoAlbumCrossRef crossRef);

    // Xóa một liên kết bằng photoId và albumId (thêm để tiện dụng hơn)
    @Query("DELETE FROM photo_album_cross_ref WHERE photoId = :photoId AND albumId = :albumId")
    void deleteCrossRef(String photoId, String albumId);

    // Lấy tất cả liên kết của một Album
    @Query("SELECT * FROM photo_album_cross_ref WHERE albumId = :albumId")
    List<PhotoAlbumCrossRef> getCrossRefsForAlbum(String albumId);

    // Lấy tất cả liên kết của một Photo
    @Query("SELECT * FROM photo_album_cross_ref WHERE photoId = :photoId")
    List<PhotoAlbumCrossRef> getCrossRefsForPhoto(String photoId);

    // Xóa tất cả liên kết của một Album
    @Query("DELETE FROM photo_album_cross_ref WHERE albumId = :albumId")
    void deleteCrossRefsForAlbum(String albumId);

    // Xóa tất cả liên kết của một Photo
    @Query("DELETE FROM photo_album_cross_ref WHERE photoId = :photoId")
    void deleteCrossRefsForPhoto(String photoId);

    // Kiểm tra xem một Photo có trong Album không (tùy chọn)
    @Query("SELECT COUNT(*) > 0 FROM photo_album_cross_ref WHERE photoId = :photoId AND albumId = :albumId")
    boolean isPhotoInAlbum(String photoId, String albumId);

    @Query("DELETE FROM photo_album_cross_ref")
    void deleteAllCrossRefs();

}
