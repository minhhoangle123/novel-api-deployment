package com.crawldata.back_end.configurations;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptPasswordEncoderExample {
    public static void main(String[] args) {
        BCryptPasswordEncoder bpe = new BCryptPasswordEncoder();
        String passwordEncoder = bpe.encode("123456");
        System.out.println(passwordEncoder);
    }
}
