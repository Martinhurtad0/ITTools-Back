package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Databases;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Properties;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.PropertiesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/database")
public class PropertiesController {
    @Autowired
    private final PropertiesService propertiesService;

    public PropertiesController(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    @GetMapping("/list")
    public List<Databases> getDatabases(@RequestParam int serverId, HttpServletRequest request) {
        return propertiesService.listDatabases(serverId, request);
    }

    @GetMapping("/properties")
    public List<Properties> getProperties(@RequestParam int serverId, @RequestParam String dataName, HttpServletRequest request) {
        return propertiesService.listProperties(serverId, dataName, request);
    }
}
