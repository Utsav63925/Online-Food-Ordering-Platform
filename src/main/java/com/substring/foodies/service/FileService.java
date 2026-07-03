package com.substring.foodies.service;

import com.substring.foodies.dto.FileData;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    FileData uploadFile(MultipartFile file, String path) throws IOException;
    void deleteFile(String path) throws IOException;
    Resource loadFile(String path);
}
