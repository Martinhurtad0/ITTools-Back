package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.StatusDisk;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.ErrorLogRepository;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.StatusDiskRepository;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;

import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class StatusDiskService {

    @Autowired
    private ServerBD_Repository serverBDRepository;
    @Autowired
    private ErrorLogRepository errorLogRepository;
    @Autowired
    private StatusDiskRepository statusDiskRepository;

   public  List<StatusDisk> statusDiskAll(){
       return statusDiskRepository.findAll();
   }


    @Transactional
    public void checkStatusDisk() {
        List<ServerBD_Model> servers = serverBDRepository.findAll();
        System.out.println("Total servers found: " + servers.size());

        statusDiskRepository.deleteAll();
        errorLogRepository.deleteBySp("StatusDisk");

        for (ServerBD_Model server : servers) {
            processServer(server);
        }
    }

    public List<ErrorLog> findErrorsBySp(String sp) {
        return errorLogRepository.findBySp(sp);
    }


    public void processServer(ServerBD_Model server) {
        try {
            if(server.getServerType()==1) {
                System.out.println("Processing server: " + server.getIpServer());
                JdbcTemplate jdbcTemplate = getJdbcTemplate(server);

                List<StatusDisk> statusDiskList = fetchStatusDisk(jdbcTemplate, server.getIpServer(), server.getServerName(), server);
                // Guardar los datos
                statusDiskRepository.saveAll(statusDiskList);
                System.out.println(" Status Disk saved for server satisfactorily: " + server.getIpServer());
           }
        }catch (Exception ex) {
            // Obtén el mensaje original del error
            String originalMessage = ex.getMessage();

            // Extrae la parte relevante del mensaje
            String filteredMessage = extractRelevantErrorMessage(originalMessage);

            // Maneja y registra el error
            handleError(server, filteredMessage);

            // Muestra el mensaje filtrado en consola
            System.err.println("Server error: " + server + ": " + filteredMessage);
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


        // Crea el objeto de registro de error
        ErrorLog errorLog = new ErrorLog();
        errorLog.setIp(server.getIpServer());
        errorLog.setServerName(server.getServerName());
        errorLog.setSp("StatusDisk");
        errorLog.setDescription(filteredMessage); // Almacena el mensaje filtrado
        errorLog.setTimestamp(LocalDateTime.now());

        // Guarda el error en la base de datos
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
            System.out.println("Successful connection to server: " + server.getIpServer() + " databaseName: " + server.getServerDB());
        } catch (Exception e) {
            // Capturar cualquier error y lanzar una excepción detallada
            throw new RuntimeException("Error connecting to server" + server.getIpServer() + " databaseName " + server.getServerDB() + ": " + e.getMessage(), e);
        }

        return jdbcTemplate;
    }

    private List<StatusDisk> fetchStatusDisk(JdbcTemplate jdbcTemplate, String ip, String serverName, ServerBD_Model server) {
        String query = "SELECT DISTINCT " +
                "SUBSTRING(volume_mount_point, 1, 1) AS Disk, " +
                "total_bytes / 1024 / 1024 / 1024 AS Total_Space_GB, " +
                "available_bytes / 1024 / 1024 / 1024 AS Free_Space_GB, " +
                "ISNULL(ROUND(available_bytes / CAST(NULLIF(total_bytes, 0) AS FLOAT) * 100, 2), 0) AS Percent_Available, " +
                "CASE " +
                "WHEN available_bytes / 1024 / 1024 / 1024 <= 15 THEN 'CRITICAL ALERT' " +
                "WHEN available_bytes / 1024 / 1024 / 1024 > 15 AND available_bytes / 1024 / 1024 / 1024 <= 30 THEN 'Check' " +
                "ELSE 'OK' " +
                "END AS Status " +
                "FROM master.sys.master_files AS f " +
                "CROSS APPLY master.sys.dm_os_volume_stats(f.database_id, f.file_id);";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        if (results.isEmpty()) {
            System.out.println("The query returned no results for the server: " + ip);
        } else {
            System.out.println("Results obtained:");
            for (Map<String, Object> row : results) {
                System.out.println(row);
            }

        }

        return results.stream().map(row -> {
            StatusDisk disk = new StatusDisk();
            disk.setIp(ip);
            disk.setServerName(serverName);
            String regionName = server.getRegion() != null ? server.getRegion().getNameRegion() : "Sin Región";
            disk.setRegion(regionName);
            disk.setDisk((String) row.get("disk"));
            disk.setTotalSpace((Long) row.get("total_space_GB"));
            disk.setFreeSpace((Long) row.get("free_space_GB"));
            disk.setPercentAvailable((Double) row.get("percent_available"));
            disk.setStatus((String) row.get("status"));


            return disk;
        }).collect(Collectors.toList());
    }


}
