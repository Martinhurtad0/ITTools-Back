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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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


    public List<ErrorLog> findErrorsBySp(String sp) {
        return errorLogRepository.findBySp(sp);
    }

     @Transactional
     public void checkStatusBackup(){
         List<ServerBD_Model> servers = serverBDRepository.findAll();
        statusBackupRepository.deleteAll();
        errorLogRepository.deleteBySp("StatusBackUpDatabase");

        for (ServerBD_Model server: servers){
            processServer(server);
        }

     }


     public void processServer (ServerBD_Model server){
         try {
             if(server.getServerType()==1) {
                 System.out.println("Processing server: " + server.getIpServer());
                 JdbcTemplate jdbcTemplate = getJdbcTemplate(server);
                 List<StatusBackupDatabase> statusBackup = fetchStatusBackup(jdbcTemplate, server.getIpServer(), server.getServerName());
                 statusBackupRepository.saveAll(statusBackup);
                 System.out.println("Status Back Up saved for server  satisfactorily: " + server.getIpServer());
             }

         } catch (Exception ex) {
             // Obtén el mensaje original del error
             String originalMessage = ex.getMessage();

             // Extrae la parte relevante del mensaje
             String filteredMessage = extractRelevantErrorMessage(originalMessage);

             // Maneja y registra el error
             handleError(server, filteredMessage);

             // Muestra el mensaje filtrado en consola
             System.err.println("Server error " + server + ": " + filteredMessage);
         }

     }


    private String extractRelevantErrorMessage(String message) {
        // Expresión regular para buscar el mensaje después de "];"
        String regex = "];\\s(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Retorna la parte relevante del mensaje
            return matcher.group(1).trim();
        }
        // Si no coincide, retorna el mensaje original como respaldo
        return message;
    }



    private void handleError(ServerBD_Model server, String filteredMessage) {



        ErrorLog errorLog = new ErrorLog();
        errorLog.setIp(server.getIpServer());
        errorLog.setServerName(server.getServerName());
        errorLog.setSp("StatusBackUpDatabase");
        errorLog.setDescription(filteredMessage);

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
            System.out.println("Successful connection to server " + server.getIpServer() + " databaseName: " + server.getServerDB());
        } catch (Exception e) {
            // Capturar cualquier error y lanzar una excepción detallada
            throw new RuntimeException("Error connecting to server " + server.getIpServer() + " databaseName " + server.getServerDB() + ": " + e.getMessage(), e);
        }

        return jdbcTemplate;
    }



    private List<StatusBackupDatabase> fetchStatusBackup(JdbcTemplate jdbcTemplate, String ip, String serverName) {


        // Consulta SQL con filtro para obtener solo el respaldo más reciente
        String query = "WITH backup_cte AS (\n" +
                "    SELECT \n" +
                "        database_name,\n" +
                "        CASE type\n" +
                "            WHEN 'D' THEN 'Database'\n" +
                "            WHEN 'L' THEN 'Log'\n" +
                "            WHEN 'I' THEN 'Differential'\n" +
                "            ELSE 'Other'\n" +
                "        END AS backup_type,\n" +
                "        backup_finish_date,\n" +
                "        ROW_NUMBER() OVER (\n" +
                "            PARTITION BY database_name, type\n" +
                "            ORDER BY backup_finish_date DESC\n" +
                "        ) AS rownum,\n" +
                "        ABS(DATEDIFF(day, GETDATE(), backup_finish_date)) AS Days_Last_Backup,\n" +
                "        CASE\n" +
                "            WHEN MAX(backup_finish_date) OVER (PARTITION BY database_name) < DATEADD(day, -1, GETDATE()) \n" +
                "                 AND EXISTS (SELECT 1 FROM msdb.dbo.backupset AS bs WHERE bs.database_name = backupset.database_name)\n" +
                "            THEN 'Check'\n" +
                "            WHEN MAX(backup_finish_date) OVER (PARTITION BY database_name) < '2024-12-14'\n" +
                "            THEN 'Check'\n" +
                "            ELSE 'OK'\n" +
                "        END AS status\n" +
                "    FROM msdb.dbo.backupset AS backupset\n" +
                "    WHERE database_name NOT IN ('master', 'model', 'msdb', 'tempdb', 'admbd', 'Billing', 'DBAudit', 'Conciliation', 'distribution', 'DBDBA',\n" +
                "                                 'DBETopup', 'DBMonitor', 'EJBCA', 'SQLDW', 'DBML')\n" +
                "      AND database_name NOT LIKE '%reportServer%'\n" +
                "      AND database_name NOT LIKE '%Archive%'\n" +
                "      AND database_name NOT LIKE '%back%'\n" +
                "      AND backup_start_date >= DATEADD(day, -30, GETDATE())\n" +
                ")\n" +
                "SELECT \n" +
                "    database_name, backup_type, backup_finish_date, Days_Last_Backup, status\n" +
                "FROM backup_cte\n" +
                "WHERE rownum = 1\n" +
                "ORDER BY database_name;\n";

        // Ejecutar la consulta
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        // Validar si no hay resultados
        if (results.isEmpty()) {
            System.out.println("The query returned no results for the server:" + ip);
        } else {
            System.out.println("Results obtained:");
            for (Map<String, Object> row : results) {
                System.out.println(row);
            }
        }

        // Mapear resultados a la lista de objetos StatusBackupDatabase
        return results.stream().map(row -> {
            StatusBackupDatabase backup = new StatusBackupDatabase();
            backup.setIp(ip);
            backup.setServerName(serverName);
            backup.setDatabaseName((String) row.get("database_name"));
            backup.setBackupFinishDate((Timestamp) row.get("backup_finish_date"));
            backup.setBackupType((String) row.get("backup_type"));
            backup.setDaysLastBackup(row.get("Days_Last_Backup").toString());
            backup.setStatus((String) row.get("status"));
            return backup;
        }).collect(Collectors.toList());
    }



}
