package com.abdoul.gentech_fintech.Util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TwoFactorUtil {


    public String create2FactorCode(){
        int secureRandom = new SecureRandom().nextInt(100000,1000000);
        return String.valueOf(secureRandom);
    }
}
