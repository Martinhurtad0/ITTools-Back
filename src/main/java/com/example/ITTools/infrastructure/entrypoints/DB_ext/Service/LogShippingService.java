package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.LogShippingStatus;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.ErrorLogRepository;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.LogShippingStatusRepository;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LogShippingService {

    @Autowired
    private LogShippingStatusRepository logShippingStatusRepository;

    @Autowired
    private ErrorLogRepository errorRepository;

    @Autowired
    private ServerBD_Repository serverBDRepository;
   public List<LogShippingStatus> getAll(){
       return logShippingStatusRepository.findAll();
   }
    @Transactional
    public void checkLogShippingStatus() {
        List<ServerBD_Model> servers = serverBDRepository.findAll();
        logShippingStatusRepository.deleteAll(); // Limpia los registros previos

        for (ServerBD_Model server : servers) {
            try {
                JdbcTemplate jdbcTemplatePrimary = getJdbcTemplate(server);
                JdbcTemplate jdbcTemplateSecondary = getJdbcTemplateServerSecondary(server);

                // Obtener el last_backup_date del servidor primario
                Timestamp lastBackupDate = checkPrimaryServer(jdbcTemplatePrimary, server);

                // Pasar el last_backup_date al servidor secundario
                checkSecondaryServer(jdbcTemplateSecondary, server, lastBackupDate);

            } catch (Exception ex) {
                handleError(ex, server);
                System.err.println("Error: " + ex.getMessage());
            }
        }
    }




    private Timestamp checkPrimaryServer(JdbcTemplate jdbcTemplate, ServerBD_Model server) {
        String query = "SELECT primary_database, last_backup_date FROM msdb.dbo.log_shipping_monitor_primary";
        Timestamp lastBackupDate = null;

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            if (results.isEmpty()) {
                System.out.println("La consulta no devolvió resultados para el servidor: " + server.getIpServerSecondary());
            } else {
                System.out.println("Resultados obtenidos:");
                for (Map<String, Object> row : results) {
                    System.out.println(row);
                }

                // Obtener el último backup_date (puedes ajustar según los requisitos)
                Map<String, Object> firstRow = results.get(0);
                lastBackupDate = (Timestamp) firstRow.get("last_backup_date");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error al consultar el servidor primario: " + ex.getMessage(), ex);
        }

        return lastBackupDate;
    }


    private void checkSecondaryServer(JdbcTemplate jdbcTemplate, ServerBD_Model server, Timestamp lastBackupDate) {
        String query = "SELECT primary_server, secondary_server, primary_database, last_copied_date, last_restored_date FROM msdb.dbo.log_shipping_monitor_secondary";

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            if (results.isEmpty()) {
                System.out.println("La consulta no devolvió resultados para el servidor: " + server.getIpServer());
            } else {
                System.out.println("Resultados obtenidos:");
                for (Map<String, Object> row : results) {
                    System.out.println(row);
                }
            }

            for (Map<String, Object> row : results) {
                LogShippingStatus status = new LogShippingStatus();

                String region = server.getRegion().getNameRegion();
                status.setRegion(region);
                status.setIp(server.getIpServerSecondary());
                status.setLastBackupDate(lastBackupDate); // Usar el valor de la consulta primaria
                status.setPrimaryServer((String) row.get("primary_server"));
                status.setSecondaryServer((String) row.get("secondary_server"));
                status.setPrimaryDatabase((String) row.get("primary_database"));
                status.setLastCopiedDate((Timestamp) row.get("last_copied_date"));
                status.setLastRestoredDate((Timestamp) row.get("last_restored_date"));

                // Determinar el estado basado en las fechas
                status.setStatus(determineStatus(status.getLastCopiedDate(), status.getLastRestoredDate()));

                logShippingStatusRepository.save(status);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error al consultar el servidor secundario: " + ex.getMessage(), ex);
        }
    }


    private String determineStatus(Timestamp lastCopied, Timestamp lastRestored) {
        if (lastCopied == null || lastRestored == null) {
            return "Check";
        }

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        Timestamp oneDayAgoTimestamp = Timestamp.valueOf(oneDayAgo);

        return lastRestored.before(oneDayAgoTimestamp) ? "Check" : "OK";
    }

    private JdbcTemplate getJdbcTemplate(ServerBD_Model server) {
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + server.getIpServer() + ":" + server.getPortServer() +
                        ";databaseName=" + server.getServerDB() + ";encrypt=true;trustServerCertificate=true")
                .username(server.getUserLogin())
                .password(server.getPassword())
                .build();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            // Realizar una consulta sencilla para verificar si la conexión funciona
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println("Conexión exitosa al servidor primario: " + server.getIpServer() + " databaseName: " + server.getServerDB());
        } catch (Exception e) {
            // Capturar cualquier error y lanzar una excepción detallada
            throw new RuntimeException("Error al conectar con el servidor primario " + server.getIpServer() + " databaseName " + server.getServerDB() + ": " + e.getMessage(), e);
        }

        return jdbcTemplate;


    }

    private JdbcTemplate getJdbcTemplateServerSecondary(ServerBD_Model server) {
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + server.getIpServerSecondary() + ":" + server.getPortServer() +
                        ";databaseName=" + server.getServerDB() + ";encrypt=true;trustServerCertificate=true")
                .username(server.getUserLogin())
                .password(server.getPassword())
                .build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            // Realizar una consulta sencilla para verificar si la conexión funciona
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println("Conexión exitosa al servidor Secundario: " + server.getIpServerSecondary() + " databaseName: " + server.getServerDB());
        } catch (Exception e) {
            // Capturar cualquier error y lanzar una excepción detallada
            throw new RuntimeException("Error al conectar con el servidor  secundario: " + server.getIpServerSecondary() + " databaseName " + server.getServerDB() + ": " + e.getMessage(), e);
        }

        return jdbcTemplate;


    }

    private void handleError(Exception ex, ServerBD_Model server) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setIp(server.getIpServer());
        errorLog.setServerName(server.getServerName());
        errorLog.setSp("checkLogShippingStatus");
        errorLog.setDescription(ex.getMessage().length() > 100 ? ex.getMessage().substring(0, 100) : ex.getMessage());
        errorLog.setTimestamp(LocalDateTime.now());

        errorRepository.save(errorLog);
    }
}
