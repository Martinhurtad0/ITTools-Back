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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
   public List<ErrorLog> findErrorsBySp(String sp) {
        return errorLogRepository.findBySp(sp);
    }

    @Transactional
    public void checkStatusAlways() {
        List<ServerBD_Model> servers = serverBDRepository.findAll();

        errorLogRepository.deleteBySp("StatusAlways");

        statusAlwaysRepository.deleteAll();

        for (ServerBD_Model server : servers) {
            processServer(server);
        }
    }
    private void processServer(ServerBD_Model server) {
        try {
            if(server.getLogShipping()==0 && server.getServerType()==1) {
                System.out.println("Processing server: " + server.getIpServer());

                // Verificar la conexión primero
                JdbcTemplate jdbcTemplate = getJdbcTemplate(server);

                // Si la conexión es exitosa, proceder con la consulta
                Map<String, List<StatusAlways>> statuses = fetchStatusAlways(jdbcTemplate, server.getIpServer());
                for (List<StatusAlways> statusList : statuses.values()) {
                    statusAlwaysRepository.saveAll(statusList);
                }
                System.out.println("Status AlwaysOn saved for server: " + server.getIpServer());
            }else {
                System.out.println("servers do not have AlwaysOn: "+ server.getIpServer() +" databaseName: "+server.getServerDB());
            }
            // Guardar todos los StatusAlways que están en las listas dentro del mapa


        }catch (Exception ex) {
            // Obtén el mensaje original del error
            String originalMessage = ex.getMessage();

            // Extrae la parte relevante del mensaje
            String filteredMessage = extractRelevantErrorMessage(originalMessage);

            // Maneja y registra el error
            handleError(server, filteredMessage);

            // Muestra el mensaje filtrado en consola
            System.err.println("Server error" + server + ": " + originalMessage);
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

        // Truncar la descripción del mensaje de error


        // Crear y guardar el registro de error
        ErrorLog errorLog = new ErrorLog();
        errorLog.setIp(server.getIpServer());
        errorLog.setServerName(server.getServerName());
        errorLog.setSp("StatusAlways");
        errorLog.setDescription(filteredMessage); // Usar la descripción truncada
        errorLog.setTimestamp(LocalDateTime.now());

        // Guardar en el repositorio
        errorLogRepository.save(errorLog);
    }


    private JdbcTemplate getJdbcTemplate(ServerBD_Model server) {
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + server.getIpServer() + ":" + server.getPortServer() + ";databaseName=" + server.getServerDB() + ";encrypt=true;trustServerCertificate=true;applicationIntent=ReadOnly;")
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
            // Si no se puede conectar, lanzar una excepción detallada
            throw new RuntimeException("Error connecting to server: " + server.getIpServer() + " databaseName " + server.getServerDB() + ": " + e.getMessage(), e);
        }

        return jdbcTemplate;
    }

    private Map<String, List<StatusAlways>> fetchStatusAlways(JdbcTemplate jdbcTemplate, String ip) {
        String query = "DECLARE @AGname NVARCHAR(128);\n" +
                "\n" +
                "SET @AGname = (SELECT name FROM sys.availability_groups); -- SET AGname for a specific AG for SET to NULL for ALL AGs\n" +
                "\n" +
                "IF (@AGname IS NULL OR EXISTS (SELECT [Name] FROM [master].[sys].[availability_groups] WHERE [Name] = @AGname))\n" +
                "BEGIN\n" +
                "    SELECT \n" +
                "        [AG].[name] AS [AvailabilityGroupName],\n" +
                "        [AR].[replica_server_name] AS [AvailabilityReplicaServerName],\n" +
                "        [dbcs].[database_name] AS [AvailabilityDatabaseName],\n" +
                "        ISNULL([arstates].[role_desc], '3') AS [ReplicaRole],\n" +
                "        [AR].[availability_mode_desc] AS [AvailabilityMode],\n" +
                "        ISNULL([dbr].[suspend_reason_desc], '-') AS [SuspendReason], -- Corregido aquí\n" +
                "        ISNULL([dbr].[synchronization_state_desc], '0') AS [SynchronizationState],\n" +
                "        ISNULL([dbr].[last_redone_time], '1900-01-01 00:00:00') AS [LastRedoneTime],\n" +
                "        ISNULL([dbr].[last_sent_time], '1900-01-01 00:00:00') AS [LastSentTime],\n" +
                "        ISNULL([dbr].[last_commit_time], '1900-01-01 00:00:00') AS [LastCommitTime]\n" +
                "    FROM  \n" +
                "        [master].[sys].[availability_groups] AS [AG]\n" +
                "    INNER JOIN \n" +
                "        [master].[sys].[availability_replicas] AS [AR] ON [AR].[group_id] = [AG].[group_id]\n" +
                "    INNER JOIN  \n" +
                "        [master].[sys].[dm_hadr_database_replica_cluster_states] AS [dbcs] ON [dbcs].[replica_id] = [AR].[replica_id]\n" +
                "    LEFT OUTER JOIN  \n" +
                "        [master].[sys].[dm_hadr_database_replica_states] AS [dbr] ON [dbcs].[replica_id] = [dbr].[replica_id]\n" +
                "        AND [dbcs].[group_database_id] = [dbr].[group_database_id]\n" +
                "    LEFT OUTER JOIN  \n" +
                "        (SELECT \n" +
                "            [ars].[role],\n" +
                "            [drs].[database_id],\n" +
                "            [drs].[replica_id],\n" +
                "            [drs].[last_commit_time]\n" +
                "         FROM   \n" +
                "            [master].[sys].[dm_hadr_database_replica_states] AS [drs]\n" +
                "         LEFT JOIN \n" +
                "            [master].[sys].[dm_hadr_availability_replica_states] AS [ars] ON [drs].[replica_id] = [ars].[replica_id]\n" +
                "         WHERE  \n" +
                "            [ars].[role] = 1) AS [dbrp] ON [dbr].[database_id] = [dbrp].[database_id]\n" +
                "    INNER JOIN  \n" +
                "        [master].[sys].[dm_hadr_availability_replica_states] AS [arstates] ON [arstates].[replica_id] = [AR].[replica_id]\n" +
                "    WHERE \n" +
                "        [AG].[name] = ISNULL(@AGname, [AG].[name])\n" +
                "    ORDER BY \n" +
                "        [AvailabilityReplicaServerName] ASC, [AvailabilityDatabaseName] ASC;\n" +
                "END;\n";


        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        if (results.isEmpty()) {
            System.out.println("The query returned no results for the server: " + ip);
        } else {
            System.out.println("Results obtained:");
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
                    status.setSuspendReason((String) row.get("suspend_reason"));
                    status.setLastCommitTime((Timestamp) row.get("last_commit_time"));
                    status.setLastRedoneTime((Timestamp) row.get("last_redone_time"));
                    status.setLastSentTime((Timestamp) row.get("last_sent_time"));
                    return status;
                })
                .collect(Collectors.groupingBy(StatusAlways::getAvailabilityGroupName));



        return groupedStatuses;


    }


}
