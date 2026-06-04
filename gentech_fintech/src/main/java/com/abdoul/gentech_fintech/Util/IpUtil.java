package com.abdoul.gentech_fintech.Util;

import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ForbiddenException;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class IpUtil {
    private final GeoIpUtil geoIpUtil;

    public IpUtil (GeoIpUtil geoIpUtil){
        this.geoIpUtil = geoIpUtil;
    }

    public Map<String, String> getIpDetails (String ip){

        if (ip == null){
            throw new BadRequestException("Unable to locate you");
        }

        if (geoIpUtil.isVpn(ip)){
            throw new ForbiddenException("Vpns are not supported");
        }

        Map<String, String> infos = new LinkedHashMap<>();

        CityResponse city = geoIpUtil.getCity(ip);

        if (city == null){
            throw new ForbiddenException("Unable to determine your location");
        }

        if (!city.country().name().equals("Guinea") && !city.country().name().equals("United States")){
            throw new ForbiddenException("This service is unavailable in your current location");
        }

        infos.put("Country", city.country().name());
        infos.put("City", city.city().name());
        infos.put("Latitude", city.location().latitude().toString());
        infos.put("Longitude", city.location().longitude().toString());

        return infos;
    }

}
