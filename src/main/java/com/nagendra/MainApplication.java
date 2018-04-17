package com.nagendra;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;

/**
 * Created by nagendra on 17/04/2018.
 */
@EnableJms
@ComponentScan(basePackages = "com.nagendra")
@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MainApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}
