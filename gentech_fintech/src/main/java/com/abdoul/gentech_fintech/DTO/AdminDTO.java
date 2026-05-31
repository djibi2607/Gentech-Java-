package com.abdoul.gentech_fintech.DTO;

import lombok.Getter;
import lombok.Setter;

public class AdminDTO {
    @Getter
    @Setter
    public static class Unflag{
        private Long id;
        private String email;
        private String phone;
    }
}
