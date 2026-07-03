package com.substring.foodies.service;


import com.substring.foodies.dto.FileData;
import com.substring.foodies.exception.FileNotFoundException;
import com.substring.foodies.exception.InvalidPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {

    private Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public FileData uploadFile(MultipartFile file, String path) throws IOException {

        if(path.isBlank())
        {
            throw new InvalidPathException("Invalid Path.");
        }

        String contentType=file.getContentType();
        if(contentType.equals("image/jpg") || contentType.equals("image/jpeg") || contentType.equals("image/png"))
        {
            Path folderPath= Paths.get(path.substring(0, path.lastIndexOf("/")+1));
            if(!Files.exists(folderPath))
            {
                Files.createDirectories(folderPath);
            }
        }
        else {
            throw new InvalidPathException("Invalid File Content.");
        }

        String fileName=path.substring(path.lastIndexOf("/")+1);
        String fileExtension=fileName.substring(fileName.lastIndexOf("."));

        if(fileExtension.equals(".jpg") || fileExtension.equals(".jpeg") || fileExtension.equals(".png") || fileExtension.equals(".gif"))
        {
            Path filePath=Paths.get(path);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        else
        {
            throw new InvalidPathException("Invalid Upload Extension.");
        }

        FileData fileData=new FileData(fileName, path);
        return fileData;
    }

    @Override
    public void deleteFile(String path) throws IOException {

        if(path.isBlank())
        {
            throw new InvalidPathException("Invalid Path");
        }

        Path actualPath=Paths.get(path);
        if(!Files.exists(actualPath))
        {
            throw new FileNotFoundException("Files does not exist at the given path.");
        }
        else {
           try {
               Files.delete(actualPath);
           }catch (Exception ex)
           {
               throw new java.io.FileNotFoundException("Not able to delete File");
           }
        }
    }

    @Override
    public Resource loadFile(String path) {
        return null;
    }
}
