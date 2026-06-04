package com.abdoul.gentech_fintech.Configuration;

import com.maxmind.geoip2.DatabaseReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

@Configuration
public class GeoIpConfig {
    @Bean
    public DatabaseReader cityReader () throws IOException {
        File cityDb = new ClassPathResource("mmdbFiles/GeoLite2-City.mmdb").getFile();
        return new DatabaseReader.Builder(cityDb).build();
    }

    @Bean
    public DatabaseReader asnReader () throws IOException{
        File asnDb = new ClassPathResource("mmdbFiles/GeoLite2-ASN.mmdb").getFile();
        return new DatabaseReader.Builder(asnDb).build();
    }
}
