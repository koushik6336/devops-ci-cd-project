package com.devops;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    @Value("${APP_COLOR:none}")
    private String color;

    @Value("${APP_VERSION:unknown}")
    private String version;

    @GetMapping("/version")
    public String version() {
        return "Color: " + color + " | Version: " + version;
    }
}