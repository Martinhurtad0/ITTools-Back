package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.JobFailed;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.ErrorLogRepository;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.JobFailedRepository;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;
import com.google.api.client.util.DateTime;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.google.api.client.util.DateTime;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class JobFailedService {


    @Autowired
    private ServerBD_Repository serverBDRepository;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @Autowired
    private JobFailedRepository jobFailedRepository;



    public List<JobFailed> JobFailedAll(){
        return jobFailedRepository.findAll();
    }

    @Transactional
    public void checkJobFailed() {
        List<ServerBD_Model> servers = serverBDRepository.findAll();
        System.out.println("Total servers found: " + servers.size());

        jobFailedRepository.deleteAll();

        for (ServerBD_Model server : servers) {
            processServer(server);
        }
    }

    public void processServer(ServerBD_Model server) {
        try {
            System.out.println("Processing server: " + server.getIpServer());
            JdbcTemplate jdbcTemplate = getJdbcTemplate(server);

            List<JobFailed> jobFailedList = fetchJobFailed(jdbcTemplate, server.getIpServer(), server.getServerName());
            // Guardar los datos
            jobFailedRepository.saveAll(jobFailedList);
            System.out.println("Jobs failed saved for server satisfactorily: " + server.getIpServer());
        } catch (Exception ex) {
            handleError(server, ex);
            System.err.println("Error: " + ex.getMessage());
        }
    }



    private void handleError(ServerBD_Model server, Exception ex) {

        String description = ex.getMessage();
        int maxLength = 100; // Longitud máxima deseada

        if (description != null && description.length() > maxLength) {
            description = description.substring(0, maxLength) + "..."; // Truncar y agregar '...'
        }

        ErrorLog errorLog = new ErrorLog();
        errorLog.setIp(server.getIpServer());
        errorLog.setServerName(server.getServerName());
        errorLog.setSp("SP_Check_Jobs_Failed");
        errorLog.setDescription(description);
        errorLog.setTimestamp(LocalDateTime.now());
        errorLogRepository.save(errorLog);
    }




    private JdbcTemplate getJdbcTemplate(ServerBD_Model server) {
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + server.getIpServer() + ":" + server.getPortServer() + ";databaseName=" + server.getServerDB() + ";encrypt=true;trustServerCertificate=true")
                .username(server.getUserLogin())
                .password(server.getPassword())
                .build();

        // Crear un JdbcTemplate con el DataSource
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            // Realizar una consulta sencilla para verificar si la conexión funciona
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println("Conexión exitosa al servidor: " + server.getIpServer() + " databaseName: " + server.getServerDB());
        } catch (Exception e) {
            // Capturar cualquier error y lanzar una excepción detallada
            throw new RuntimeException("Error al conectar con el servidor " + server.getIpServer() + " databaseName " + server.getServerDB() + ": " + e.getMessage(), e);
        }

        return jdbcTemplate;
    }

    private List<JobFailed> fetchJobFailed(JdbcTemplate jdbcTemplate, String ip, String serverName) {
        String query = """
                SELECT
                                   j.name AS Job_Name,
                                           jh.step_id AS Step_ID,
                                           jh.step_name AS Step_Name,
                                           SUBSTRING(jh.message, 0, 100) AS Message,
                                           CAST(CAST(jh.run_date AS CHAR(8)) + ' ' +\s
                                                STUFF(STUFF(RIGHT('000000' + CONVERT(VARCHAR(6), jh.run_time), 6), 3, 0, ':'), 6, 0, ':') AS DATETIME) AS Run_Date,
                                           CASE jh.run_status
                                               WHEN 0 THEN 'Failed'
                                               WHEN 1 THEN 'Succeeded'
                                               WHEN 2 THEN 'Retry'
                                               WHEN 3 THEN 'Canceled'
                                           END AS Run_Status,
                                           ROW_NUMBER() OVER (PARTITION BY j.name, jh.step_id ORDER BY run_date DESC, run_time DESC) AS Row_Num
                                       FROM \s
                                           msdb.dbo.sysjobhistory AS jh
                                       LEFT JOIN \s
                                           msdb.dbo.sysjobs AS j ON jh.job_id = j.job_id
                                       WHERE\s
                                           jh.run_status = 0
                                           AND CAST(CAST(jh.run_date AS CHAR(8)) + ' ' +\s
                                                    STUFF(STUFF(RIGHT('000000' + CONVERT(VARCHAR(6), jh.run_time), 6), 3, 0, ':'), 6, 0, ':') AS DATETIME) >= DATEADD(day, -1, GETDATE())
                                           AND j.enabled = 1
                                           AND jh.step_id <> 0;
        """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        if (results.isEmpty()) {
            System.out.println("La consulta no devolvió resultados para el servidor: " + ip);
        } else {
            System.out.println("Resultados obtenidos:");
            for (Map<String, Object> row : results) {
                System.out.println(row);
            }

        }

        return results.stream().map(row -> {
            JobFailed job = new JobFailed();
            job.setIp(ip);
            job.setServerName(serverName);
            job.setJobName((String) row.get("job_name"));
            job.setStepName((String) row.get("step_name"));

            // Convertir step_id a String, o asignar null si no es compatible
            Object stepIdObj = row.get("step_id");
            if (stepIdObj instanceof Long) {
                Long stepIdValue = (Long) stepIdObj;
                job.setStepID(String.valueOf(stepIdValue)); // Convertir Long a String
            } else if (stepIdObj instanceof Integer) {
                Integer stepIdValue = (Integer) stepIdObj;
                job.setStepID(String.valueOf(stepIdValue.longValue())); // Convertir Integer a String
            } else {
                System.err.println("Warning: step_id no es ni Long ni Integer. Se asigna null.");
                job.setStepID(null); // Asignar null si no es compatible
            }

            job.setMessage((String) row.get("message"));

            // Convertir run_date a DateTime
            job.setRunDate((Timestamp) row.get("run_date"));

            job.setRunStatus((String) row.get("run_status"));

            // Asegúrate de convertir row_num a String o asignar null si no es compatible
            Object rowNumObj = row.get("row_num");
            if (rowNumObj instanceof Long) {
                job.setRowNum(String.valueOf(rowNumObj)); // Convertir Long a String
            } else if (rowNumObj instanceof Integer) {
                job.setRowNum(String.valueOf(rowNumObj)); // Convertir Integer a String
            } else {
                System.err.println("Warning: row_num no es ni Long ni Integer. Se asigna null.");
                job.setRowNum(null); // Asignar null si no es compatible
            }

            return job;
        }).collect(Collectors.toList());
    }


}
