package com.devops;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    @GetMapping("/version")
    public String version() {
        return "Day 3 Updated – CI/CD Pipeline Working 🚀";
    }
}