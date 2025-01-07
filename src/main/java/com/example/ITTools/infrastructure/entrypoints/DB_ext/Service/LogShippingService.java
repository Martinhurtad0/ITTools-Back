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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;

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
    public List<ErrorLog> findErrorsBySp(String sp) {
        return errorRepository.findBySp(sp);
    }

    @Transactional
    public void checkLogShippingStatus() {
        // Obtener todos los servidores desde la tabla ServerBD_Model
        List<ServerBD_Model> servers = serverBDRepository.findAll();
        errorRepository.deleteBySp("checkLogShippingStatus");
        logShippingStatusRepository.deleteAll(); // Limpia los registros previos

        Timestamp lastBackupDate = null; // Inicializar fuera de la iteración

        for (ServerBD_Model server : servers) {
            try {
                if (server.getLogShipping() == 1) {
                    // Si es un servidor primario, obtener lastBackupDate
                    if (server.getServerType() == 1) {
                        JdbcTemplate jdbcTemplatePrimary = getJdbcTemplate(server);
                        lastBackupDate = checkPrimaryServer(jdbcTemplatePrimary, server);
                        System.out.println("lastBackupDate obtained from primary: " + lastBackupDate);
                    }
                }
            } catch (Exception ex) {
                // Obtén el mensaje original del error
                String originalMessage = ex.getMessage();

                // Extrae la parte relevante del mensaje
                String filteredMessage = extractRelevantErrorMessage(originalMessage);

                // Maneja y registra el error
                handleError(filteredMessage,server);

                // Muestra el mensaje filtrado en consola
                System.err.println(
                        "Server error " + server + ": " + filteredMessage);
            }
        }

        // Procesar servidores secundarios después de obtener lastBackupDate
        for (ServerBD_Model server : servers) {
            try {
                if (server.getLogShipping() == 1) {
                    // Si es un servidor secundario, usar lastBackupDate
                    if (server.getServerType() == 0) {
                        JdbcTemplate jdbcTemplateSecondary = getJdbcTemplate(server);
                        checkSecondaryServer(jdbcTemplateSecondary, server, lastBackupDate);
                        System.out.println("Query made to the secondary server with lastBackupDate: " + lastBackupDate);
                    }
                }
            } catch (Exception ex) {
                // Obtén el mensaje original del error
                String originalMessage = ex.getMessage();

                // Extrae la parte relevante del mensaje
                String filteredMessage = extractRelevantErrorMessage(originalMessage);

                // Maneja y registra el error
                handleError(filteredMessage,server);

                // Muestra el mensaje filtrado en consola
                System.err.println("Server error " + server + ": " + originalMessage);
            }
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








    private Timestamp checkPrimaryServer(JdbcTemplate jdbcTemplate, ServerBD_Model server) {
        String query = "SELECT primary_database, last_backup_date FROM msdb.dbo.log_shipping_monitor_primary";
        Timestamp lastBackupDate = null;

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            if (results.isEmpty()) {
                System.err.println("No results were found for the query for the primary server: " + server.getIpServer());
            } else {
                Map<String, Object> firstRow = results.get(0);
                System.out.println("Results obtained: "+firstRow);
                lastBackupDate = (Timestamp) firstRow.get("last_backup_date");

            }
        } catch (Exception ex) {
            System.err.println("Error executing query for primary server: " + ex.getMessage());
        }

        return lastBackupDate;
    }



    private void checkSecondaryServer(JdbcTemplate jdbcTemplate, ServerBD_Model server, Timestamp lastBackupDate) {
        String query = "SELECT primary_server, secondary_server, primary_database, last_copied_date, last_restored_date FROM msdb.dbo.log_shipping_monitor_secondary";

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            if (results.isEmpty()) {
                System.out.println("The query returned no results for the server: " + server.getIpServer());
            } else {
                System.out.println("Results obtained:");
                for (Map<String, Object> row : results) {
                    System.out.println(row);
                }
            }

            for (Map<String, Object> row : results) {
                LogShippingStatus status = new LogShippingStatus();

                String region = server.getRegion().getNameRegion();
                status.setRegion(region);
                status.setIp(server.getIpServer());
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
            throw new RuntimeException("Error querying secondary server: " + ex.getMessage(), ex);
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
        // Configurar la fuente de datos
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + server.getIpServer() + ":" + server.getPortServer() +
                        ";databaseName=" + server.getServerDB() + ";;encrypt=true;trustServerCertificate=true")
                .username(server.getUserLogin())
                .password(server.getPassword())
                .build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            // Realizar una consulta sencilla para verificar si la conexión funciona
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println("Successful connection to server : " + server.getIpServer() + " databaseName: " + server.getServerDB());
        } catch (DataAccessException dae) {
            // Obtener la causa raíz del error
            Throwable rootCause = dae.getRootCause();
            String detailedMessage;

            if (rootCause instanceof java.sql.SQLException) {
                java.sql.SQLException sqlException = (java.sql.SQLException) rootCause;

                // Manejo de errores basado en el código de error
                switch (sqlException.getErrorCode()) {
                    case 18456: // Error de autenticación (usuario/contraseña incorrectos)
                        detailedMessage = "Authentication failed: Check username or password.";
                        break;
                    case 4060: // Base de datos no encontrada o inaccesible
                        detailedMessage = "Database error: The database " + server.getServerDB() + " does not exist or is inaccessible.";
                        break;
                    case 10054: // Problema con la conexión al servidor
                        detailedMessage = "Connection error: Unable to connect to the server at " + server.getIpServer();
                        break;
                    case 229: // Permisos insuficientes
                        detailedMessage = "Permission denied: The user " + server.getUserLogin() + " does not have sufficient permissions.";
                        break;
                    default: // Otros errores de SQL
                        detailedMessage = "SQL Error: " + sqlException.getMessage();
                        break;
                }
            } else {
                // Si no es SQLException, usar el mensaje de la excepción raíz
                detailedMessage = rootCause != null ? rootCause.getMessage() : "Unknown error occurred.";
            }

            // Lanzar excepción con un mensaje más detallado
            throw new RuntimeException("Error connecting to server " + server.getIpServer() + " databaseName " + server.getServerDB() + ": " + detailedMessage, dae);
        } catch (Exception e) {
            // Manejo de otras excepciones generales
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }

        return jdbcTemplate;
    }



    private void handleError(String filteredMessage, ServerBD_Model server) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setIp(server.getIpServer());
        errorLog.setServerName(server.getServerName());
        errorLog.setSp("checkLogShippingStatus");
        errorLog.setDescription(filteredMessage);
        errorLog.setTimestamp(LocalDateTime.now());

        errorRepository.save(errorLog);
    }
}
