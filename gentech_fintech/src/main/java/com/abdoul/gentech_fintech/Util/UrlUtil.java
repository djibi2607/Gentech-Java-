package com.abdoul.gentech_fintech.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Component
public class UrlUtil {
    @Autowired
    private S3Presigner preSigner;

    @Value("${s3.bucket.name}")
    private String bucket;

    public String generatePreSignedUrl (String fileName){
        GetObjectPresignRequest request = GetObjectPresignRequest
                .builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(r -> r.bucket(bucket).key(fileName))
                .build();

        return preSigner.presignGetObject(request).url().toString();
    }
}
