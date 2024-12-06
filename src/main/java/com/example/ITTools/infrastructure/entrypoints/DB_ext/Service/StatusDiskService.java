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

        for (ServerBD_Model server : servers) {
            processServer(server);
        }
    }

    public void processServer(ServerBD_Model server) {
        try {
            System.out.println("Processing server: " + server.getIpServer());
            JdbcTemplate jdbcTemplate = getJdbcTemplate(server);

            List<StatusDisk> statusDiskList = fetchStatusDisk(jdbcTemplate, server.getIpServer(), server.getServerName(),server);
            // Guardar los datos
            statusDiskRepository.saveAll(statusDiskList);
            System.out.println(" Status Disk saved for server satisfactorily: " + server.getIpServer());
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
        errorLog.setServerName(server.getServerName());
        errorLog.setSp("StatusDisk");
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
            System.out.println("La consulta no devolvió resultados para el servidor: " + ip);
        } else {
            System.out.println("Resultados obtenidos:");
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
