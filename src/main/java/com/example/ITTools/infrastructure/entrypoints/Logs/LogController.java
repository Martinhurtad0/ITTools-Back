package com.example.ITTools.infrastructure.entrypoints.Logs;




import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/{agentId}")
    public List<String> getLogs(@PathVariable int agentId, @RequestHeader("Authorization") String authorizationHeader) {
        // Extraer el token del encabezado "Authorization"
        String token = authorizationHeader.replace("Bearer ", "");

        // Llamar al servicio para obtener los logs usando el token
        return logService.fetchLogsFromAgent(agentId, token);
    }
}
