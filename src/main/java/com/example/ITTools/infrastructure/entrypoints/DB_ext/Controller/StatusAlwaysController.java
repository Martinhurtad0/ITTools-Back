package com.example.ITTools.infrastructure.entrypoints.DB_ext.Controller;


import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.StatusAlways;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Service.StatusAlwaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statusAlways")
public class StatusAlwaysController {

    @Autowired
    private StatusAlwaysService statusAlwaysService;

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public List<StatusAlways> getStatusAlways() {
        return statusAlwaysService.getAllStatusAlways();
    }




}
