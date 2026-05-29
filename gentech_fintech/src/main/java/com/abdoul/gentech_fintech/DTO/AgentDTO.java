package com.abdoul.gentech_fintech.DTO;

import lombok.Getter;
import lombok.Setter;

public class AgentDTO {
    @Getter
    @Setter
    public static class UserCredentials{
        private String email;
        private String phone;
    }
}
