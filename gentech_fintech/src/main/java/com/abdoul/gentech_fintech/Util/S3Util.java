package com.abdoul.gentech_fintech.Util;

import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class S3Util {
    @Autowired
    private S3Client s3Client;

    @Value("${s3.bucket.name}")
    private String bucket;

    private final List<String> allowedIdEx = List.of(".jpeg", ".jpg", ".png", ".pdf");

    private final List<String> allowedSelfieEx = List.of(".jpeg", ".jpg", ".png");

    private final List<String> allowedIdContentTypes = List.of("image/jpeg", "image/png", "image/jpg", "application/pdf");

    private final List<String> allowedSelfieContentTypes = List.of("image/jpeg", "image/png", "image/jpg");

    public String uploadIdFileToS3 (String id, MultipartFile file) throws IOException {
        String ogFileName = file.getOriginalFilename();

        if (ogFileName == null){
            throw new BadRequestException("Invalid file");
        }

        String fileName = "ID/" + id + "_" + UUID.randomUUID() + "_" + ogFileName;

        String ext = ogFileName.substring(ogFileName.lastIndexOf("."));

        if (!allowedIdEx.contains(ext)){
            throw new BadRequestException("File type not supported");
        }

        if (!allowedIdContentTypes.contains(file.getContentType())){
            throw new BadRequestException("File type not supported");
        }

        if (file.getSize() > 5 * 1024 * 1024){
            throw new BadRequestException("File must be less than 5 MB");
        }

        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(fileName).contentType(file.getContentType()).build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        return fileName;
    }

    public String uploadPictureFileToS3 (String id, MultipartFile file) throws IOException{
        String ogFileName = file.getOriginalFilename();

        if (ogFileName == null){
            throw new BadRequestException("Invalid file");
        }

        String fileName = "SELFIE/" + id + "_" + UUID.randomUUID() + "_" + ogFileName;

        String ext = ogFileName.substring(ogFileName.lastIndexOf("."));

        if (!allowedSelfieEx.contains(ext)){
            throw new BadRequestException("File type not supported");
        }

        if (!allowedSelfieContentTypes.contains(file.getContentType())){
            throw new BadRequestException("File type not supported");
        }

        if (file.getSize() > 5 * 1024 * 1024){
            throw new BadRequestException("File too large");
        }
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(fileName).contentType(file.getContentType()).build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        return fileName;
    }
}
