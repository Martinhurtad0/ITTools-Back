package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;


import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ErrorLog;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.StatusAlways;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.ErrorLogRepository;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository.StatusAlwaysRepository;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;
import com.google.api.client.util.DateTime;
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

public class StatusAlwaysService {

@Autowired
private ServerBD_Repository serverBDRepository;

@Autowired
private StatusAlwaysRepository statusAlwaysRepository;

@Autowired
private ErrorLogRepository errorLogRepository;


 public List<StatusAlways> getAllStatusAlways(){
     return statusAlwaysRepository.findAll();
   }


    @Transactional
    public void checkStatusAlways() {
        List<ServerBD_Model> servers = serverBDRepository.findAll();
        System.out.println("Total servers found: " + servers.size());

        statusAlwaysRepository.deleteAll();

        for (ServerBD_Model server : servers) {
            processServer(server);
        }
    }
    private void processServer(ServerBD_Model server) {
        try {
            System.out.println("Processing server: " + server.getIpServer());

            // Verificar la conexión primero
            JdbcTemplate jdbcTemplate = getJdbcTemplate(server);

            // Si la conexión es exitosa, proceder con la consulta
            Map<String, List<StatusAlways>> statuses = fetchStatusAlways(jdbcTemplate, server.getIpServer());

            // Guardar todos los StatusAlways que están en las listas dentro del mapa
            for (List<StatusAlways> statusList : statuses.values()) {
                statusAlwaysRepository.saveAll(statusList);
            }
            System.out.println("Status AlwaysOn saved for server: " + server.getIpServer());

        } catch (Exception ex) {
            // Si ocurrió un error, manejarlo
            handleError(server, ex);
            System.err.println("Error: " + ex.getMessage());
        }
    }


    private void handleError(ServerBD_Model server, Exception ex) {

        // Truncar la descripción del mensaje de error
        String description = ex.getMessage();
        int maxLength = 100; // Longitud máxima deseada

        if (description != null && description.length() > maxLength) {
            description = description.substring(0, maxLength) + "..."; // Truncar y agregar '...'
        }

        // Crear y guardar el registro de error
        ErrorLog errorLog = new ErrorLog();
        errorLog.setIp(server.getIpServer());
        errorLog.setServerName(server.getServerName());
        errorLog.setSp("StatusAlways");
        errorLog.setDescription(description); // Usar la descripción truncada
        errorLog.setTimestamp(LocalDateTime.now());

        // Guardar en el repositorio
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
            // Si no se puede conectar, lanzar una excepción detallada
            throw new RuntimeException("Error al conectar con el servidor " + server.getIpServer() + " databaseName " + server.getServerDB() + ": " + e.getMessage(), e);
        }

        return jdbcTemplate;
    }

    private Map<String, List<StatusAlways>> fetchStatusAlways(JdbcTemplate jdbcTemplate, String ip) {
        String query = "SELECT \n" +
                "    AG.name AS AGname,\n" +
                "    AR.replica_server_name AS AvailabilityReplicaServerName,\n" +
                "    dbcs.database_name AS AvailabilityDatabaseName,\n" +
                "    ISNULL(arstates.role_desc, 'Unknown') AS ReplicaRole, -- Asegúrate de que arstates esté definido\n" +
                "    AR.availability_mode_desc AS AvailabilityMode,\n" +
                "    ISNULL(dbr.suspend_reason_desc, 'No reason') AS SuspendReason,\n" +
                "    ISNULL(dbr.synchronization_state_desc, 0) AS SynchronizationState,\n" +
                "    ISNULL(dbr.last_redone_time, 0) AS LastRedoneTime,\n" +
                "    ISNULL(dbr.last_sent_time, 0) AS LastSentTime,\n" +
                "    ISNULL(dbr.last_commit_time, 0) AS LastCommitTime\n" +
                "FROM \n" +
                "    sys.availability_groups AS AG\n" +
                "INNER JOIN \n" +
                "    sys.availability_replicas AS AR ON AR.group_id = AG.group_id\n" +
                "INNER JOIN \n" +
                "    sys.dm_hadr_database_replica_cluster_states AS dbcs ON dbcs.replica_id = AR.replica_id\n" +
                "LEFT JOIN \n" +
                "    sys.dm_hadr_database_replica_states AS dbr ON dbcs.replica_id = dbr.replica_id\n" +
                "LEFT JOIN \n" +
                "    sys.dm_hadr_availability_replica_states AS arstates ON AR.replica_id = arstates.replica_id -- Asegúrate de incluir esta unión\n" +
                "ORDER BY \n" +
                "    AG.name ASC, \n" +
                "    AvailabilityReplicaServerName ASC;";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        if (results.isEmpty()) {
            System.out.println("La consulta no devolvió resultados para el servidor: " + ip);
        } else {
            System.out.println("Resultados obtenidos:");
            for (Map<String, Object> row : results) {
                System.out.println(row);
            }

        }

        // Agrupar por AGname
        Map<String, List<StatusAlways>> groupedStatuses = results.stream()
                .map(row -> {
                    StatusAlways status = new StatusAlways();
                    status.setAvailabilityGroupName((String) row.get("AGname"));
                    status.setAvailabilityReplicaServerName((String) row.get("availability_replica_server_name"));
                    status.setAvailabilityDatabaseName((String) row.get("availability_database_name"));
                    status.setReplicaRole((String) row.get("replica_role"));
                    status.setAvailabilityMode((String) row.get("availability_mode"));
                    status.setSynchronizationState((String) row.get("Synchronization_state"));
                    status.setLastCommitTime((Timestamp) row.get("last_commit_time"));
                    status.setLastRedoneTime((Timestamp) row.get("last_redone_time"));
                    status.setLastSentTime((Timestamp) row.get("last_sent_time"));
                    return status;
                })
                .collect(Collectors.groupingBy(StatusAlways::getAvailabilityGroupName));



        return groupedStatuses;


    }


}
