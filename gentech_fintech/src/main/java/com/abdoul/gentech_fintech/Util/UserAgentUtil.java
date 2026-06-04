package com.abdoul.gentech_fintech.Util;

import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import lombok.NonNull;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class
UserAgentUtil {
    private final UserAgentAnalyzer analyzer;

    public UserAgentUtil (){
        this.analyzer = UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats()
                .withCache(1000)
                .build();
    }

    public Map<String, String> getDeviceInfo(@NonNull String device) {
        try {
            UserAgent agent = analyzer.parse(device);

            Map<String, String> details = new LinkedHashMap<>();

            details.put("Device", agent.getValue("DeviceClass"));
            details.put("OS", agent.getValue("OperatingSystemNameVersion"));
            details.put("Browser", agent.getValue("AgentNameVersion"));

            return details;
        }
        catch (Exception ex){
            throw new BadRequestException("Unable to get device infos");
        }
    }
}
