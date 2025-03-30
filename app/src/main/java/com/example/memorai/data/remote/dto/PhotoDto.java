// data/remote/dto/PhotoDto.java
package com.example.memorai.data.remote.dto;

import java.util.List;

public class PhotoDto {
    public String id;
    public String albumId;
    public String filePath;
    public List<String> tags;
    public boolean isPrivate;
    public long createdAt;
    public long updatedAt;
}
