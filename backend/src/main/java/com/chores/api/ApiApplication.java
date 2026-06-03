package com.chores.api;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiApplication {

  public static void main(String[] args) {

    TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"));
    SpringApplication.run(ApiApplication.class, args);
  }
}
