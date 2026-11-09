package com.ilmister.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurazioneApplicazione {

    /** L'orologio dell'applicazione: nei test si sostituisce con uno fisso */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
