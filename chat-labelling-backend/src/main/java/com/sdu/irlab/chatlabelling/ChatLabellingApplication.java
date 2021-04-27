package com.sdu.irlab.chatlabelling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChatLabellingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatLabellingApplication.class, args);
    }

}
