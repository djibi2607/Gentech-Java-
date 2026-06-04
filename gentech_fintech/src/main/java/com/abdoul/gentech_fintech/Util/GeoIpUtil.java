package com.abdoul.gentech_fintech.Util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
@Slf4j
@Component
public class GeoIpUtil {
    private final DatabaseReader asnReader;
    private final DatabaseReader cityReader;

    public GeoIpUtil (DatabaseReader asnReader, DatabaseReader cityReader){
        this.asnReader = asnReader;
        this.cityReader = cityReader;
    }

    public CityResponse getCity(String ip){
        try{
            InetAddress address = InetAddress.getByName(ip);
            return cityReader.city(address);
        }
        catch (Exception ex){
            return null;
        }
    }

    public AsnResponse getAsn (String ip){
        try{
            InetAddress address = InetAddress.getByName(ip);
            return asnReader.asn(address);
        }
        catch (Exception ex){
            return null;
        }
    }

    public boolean isVpn (String ip){
        AsnResponse asn = getAsn(ip);
        if (asn == null ||  asn.autonomousSystemOrganization() == null){
            return false;
        }
        String org = asn.autonomousSystemOrganization();
        return org.contains("vpn") || org.contains("proxy") ||
                org.contains("hosting") || org.contains("datacenter") ||
                org.contains("digitalocean") || org.contains("amazon") ||
                org.contains("google") || org.contains("microsoft");
    }
}
