package com.predix.bff.controller;

import com.predix.bff.service.SystemHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final SystemHealthService systemHealthService;

    public SystemController(SystemHealthService systemHealthService) {
        this.systemHealthService = systemHealthService;
    }

    @GetMapping("/dependencies/health")
    public Map<String, Object> dependenciesHealth() {
        return systemHealthService.dependenciesHealth();
    }
}
