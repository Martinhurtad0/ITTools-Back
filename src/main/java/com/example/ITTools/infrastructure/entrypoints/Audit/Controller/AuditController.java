package com.example.ITTools.infrastructure.entrypoints.Audit.Controller;


import com.example.ITTools.infrastructure.entrypoints.Audit.Model.AuditModel;
import com.example.ITTools.infrastructure.entrypoints.Audit.Repository.AuditRepository;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/audits")
public class AuditController {

    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private AuditService auditService;

    //metodo para traer todas las uditorias de aditService
    @GetMapping
    public List<AuditModel> getAllAudits() {
        return auditRepository.findAll();

    }



}
