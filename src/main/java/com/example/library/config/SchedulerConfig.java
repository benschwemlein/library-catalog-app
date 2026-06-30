package com.example.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Enables @Scheduled annotation processing.
    // Scheduled tasks are defined in com.example.library.scheduler package.
}
