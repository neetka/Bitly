package com.bitly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Bitly URL Shortener application.
 */
@SpringBootApplication
public class BitlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BitlyApplication.class, args);
    }
}
