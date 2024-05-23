package com.fastcampus.sns.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfiguration {

    @Bean // Spring Security에 존재하는 BCryptPasswordEncoer를 하나 Bean으로 띄워주고, UserService class에서 private final로 바로 받아오자
    public BCryptPasswordEncoder encodePassword() {
        return new BCryptPasswordEncoder();
    }
}
