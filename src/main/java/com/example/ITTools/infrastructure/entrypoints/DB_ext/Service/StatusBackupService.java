package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.StatusBackupDatabase;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.ErrorLogRepository;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.StatusBackupRepository;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




@Service
public class StatusBackupService {

    @Autowired
    private StatusBackupRepository statusBackupRepository;
     @Autowired
    private ErrorLogRepository errorLogRepository;
     @Autowired
    private ServerBD_Repository serverBDRepository;

     public List<StatusBackupDatabase> statusBackupAll(){
         return  statusBackupRepository.findAll();
     }

     @Transactional
     public void checkStatusBackup(){
         List<ServerBD_Model> servers = serverBDRepository.findAll();
        statusBackupRepository.deleteAll();

        for (ServerBD_Model server: servers){
            processServer(server);
        }

     }


     public void processServer (ServerBD_Model server){
         try {
             System.out.println("Processing server: " + server.getIpServer());
             JdbcTemplate jdbcTemplate = getJdbcTemplate(server);
             List<StatusBackupDatabase> statusBackup= fetchStatusBackup(jdbcTemplate,server.getIpServer(), server.getServerName());
             statusBackupRepository.saveAll(statusBackup);
             System.out.println("Status Back Up saved for server  satisfactorily: "+ server.getIpServer());

         } catch (Exception ex){
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
        errorLog.setSp("StatusBackUpDatabase");
        errorLog.setDescription(description);

        errorLog.setTimestamp(LocalDateTime.now());
        errorLogRepository.save(errorLog);
        System.err.println("Error: " + ex.getMessage());
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



    private List<StatusBackupDatabase> fetchStatusBackup(JdbcTemplate jdbcTemplate, String ip, String serverName) {
        // Calcular el último sábado
        LocalDate lastSaturday = getLastSaturday(LocalDate.now());

        // Convertir a String en formato adecuado para SQL
        String formattedFechaSabado = lastSaturday.toString(); // Formato YYYY-MM-DD

        // Aquí usamos la dirección IP como parte de la consulta
        String query = "SELECT \n" +
                "    database_name,\n" +
                "    CASE type\n" +
                "        WHEN 'D' THEN 'Database'\n" +
                "        WHEN 'L' THEN 'Log'\n" +
                "        WHEN 'I' THEN 'Differential'\n" +
                "        ELSE 'Other'\n" +
                "    END AS backup_type,\n" +
                "    backup_finish_date,\n" +
                "    ROW_NUMBER() OVER (\n" +
                "        PARTITION BY database_name, type\n" +
                "        ORDER BY backup_finish_date DESC\n" +
                "    ) AS rownum,\n" +
                "    ISNULL(STR(ABS(DATEDIFF(day, GETDATE(), MAX(backup_finish_date)))), NULL) AS Days_Last_Backup,\n" +
                "    CASE\n" +
                "        WHEN MAX(CONVERT(datetime, backup_finish_date, 121)) < CONVERT(datetime, DATEADD(dd, -1, GETDATE()), 121) THEN 'Check'\n" +
                "        WHEN MAX(CONVERT(datetime, backup_finish_date, 121)) < '" + formattedFechaSabado + "' THEN 'Check'\n" +
                "        ELSE 'OK' END AS Status\n" +
                "FROM msdb.dbo.backupset\n" + // Sin servidor vinculado\n" +
                "WHERE database_name NOT IN ('master', 'model', 'msdb', 'tempdb', 'admbd', 'Billing', 'DBAudit', 'Conciliation', 'distribution', 'DBDBA',\n" +
                "                             'DBETopup', 'DBMonitor', 'EJBCA', 'SQLDW', 'DBML')\n" +
                "AND database_name NOT LIKE '%reportServer%' \n" +
                "AND database_name NOT LIKE '%Archive%' \n" +
                "AND database_name NOT LIKE '%back%'\n" +
                "AND (CONVERT(datetime, backup_start_date, 121) >= GETDATE() - 30)\n" +
                "GROUP BY database_name, type, backup_finish_date";


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
            StatusBackupDatabase backup = new StatusBackupDatabase();
            backup.setIp(ip);
            backup.setServerName(serverName);
            backup.setDatabaseName((String) row.get("database_name"));
            backup.setBackupFinishDate((Timestamp) row.get("backup_finish_date"));
            backup.setBackupType((String) row.get("backup_type"));
            backup.setDaysLastBackup((String) row.get("days_last_backup"));
            backup.setStatus((String) row.get("status"));
            return backup;
        }).collect(Collectors.toList());
    }
    private LocalDate getLastSaturday(LocalDate date) {
        // Restar días hasta llegar al sábado (DayOfWeek.SATURDAY es 6)
        int daysToSubtract = (date.getDayOfWeek().getValue() - DayOfWeek.SATURDAY.getValue() + 7) % 7;
        return date.minusDays(daysToSubtract);
    }
}
