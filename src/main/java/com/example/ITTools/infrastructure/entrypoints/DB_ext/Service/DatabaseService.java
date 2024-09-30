package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ListJob;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.ListWho5;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService {

    @Autowired
    private ServerBD_Repository serverBDRepository;

    public JdbcTemplate getJdbcTemplate(int serverId) {
        ServerBD_Model server = serverBDRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + server.getIpServer() + ":" + server.getPortServer() + ";databaseName=" + server.getServerDB() + ";encrypt=true;trustServerCertificate=true")
                .username(server.getUserLogin())
                .password(server.getPassword())
                .build();

        return new JdbcTemplate(dataSource);
    }

    /**
     * Lista los trabajos en ejecución en el servidor especificado.
     *
     * @param serverId ID del servidor para obtener los trabajos.
     * @return Lista de trabajos en ejecución.
     */
    public List<ListJob> listRunningJobs(int serverId) {
        String sql = "SELECT\n" +
                "    ja.job_id,\n" +
                "    j.name AS job_name,\n" +
                "    ja.start_execution_date,\n" +
                "    js.step_name\n" +
                "FROM msdb.dbo.sysjobactivity ja \n" +
                "LEFT JOIN msdb.dbo.sysjobhistory jh ON ja.job_history_id = jh.instance_id\n" +
                "JOIN msdb.dbo.sysjobs j ON ja.job_id = j.job_id\n" +
                "JOIN msdb.dbo.sysjobsteps js ON ja.job_id = js.job_id AND ISNULL(ja.last_executed_step_id,0)+1 = js.step_id\n" +
                "WHERE ja.session_id = (SELECT TOP 1 session_id FROM msdb.dbo.syssessions ORDER BY agent_start_date DESC)\n" +
                "AND start_execution_date IS NOT NULL\n" +
                "AND stop_execution_date IS NULL\n" +
                "ORDER BY j.name";

        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);

        return jdbcTemplate.query(sql, new RowMapper<ListJob>() {
            @Override
            public ListJob mapRow(ResultSet rs, int rowNum) throws SQLException {
                ListJob job = new ListJob();
                job.setIdJob(rs.getString("job_id"));
                job.setJobName(rs.getString("job_name"));
                job.setStartDate(rs.getString("start_execution_date"));
                job.setStepName(rs.getString("step_name"));
                return job;
            }
        });
    }

    /**
     * Lista los trabajos programados en el servidor especificado que comienzan por 'maintenance'.
     *
     * @param serverId ID del servidor para obtener los trabajos programados.
     * @return Lista de trabajos programados que comienzan por 'maintenance'.
     */
    public List<ListJob> listScheduledJobs(int serverId) {
        String sql = "SELECT\n" +
                "    ja.job_id,\n" +
                "    j.name AS job_name,\n" +
                "    ja.next_scheduled_run_date,\n" +
                "    ja.start_execution_date,\n" +
                "    ja.stop_execution_date,\n" +
                "    DATEDIFF(minute, ja.start_execution_date, ja.stop_execution_date) AS Execution_Time\n" +
                "FROM msdb.dbo.sysjobactivity ja \n" +
                "LEFT JOIN msdb.dbo.sysjobhistory jh ON ja.job_history_id = jh.instance_id\n" +
                "JOIN msdb.dbo.sysjobs j ON ja.job_id = j.job_id\n" +
                "WHERE ja.session_id IN (SELECT TOP 1 session_id FROM msdb.dbo.syssessions ORDER BY agent_start_date DESC)\n" +
                "AND next_scheduled_run_date IS NOT NULL\n" +
                "AND j.name LIKE 'maintenance%'\n" +
                "ORDER BY j.name";

        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);

        return jdbcTemplate.query(sql, new RowMapper<ListJob>() {
            @Override
            public ListJob mapRow(ResultSet rs, int rowNum) throws SQLException {
                ListJob job = new ListJob();
                job.setIdJob(rs.getString("job_id"));
                job.setJobName(rs.getString("job_name"));
                job.setScheduledDate(rs.getString("next_scheduled_run_date"));
                job.setStartDate(rs.getString("start_execution_date"));
                job.setStopDate(rs.getString("stop_execution_date"));
                job.setExecutionTime(rs.getString("Execution_Time"));
                return job;
            }
        });
    }

    /**
     * Lista los procesos que se encuentran en ejecución utilizando sp_who5.
     *
     * @param serverId ID del servidor para obtener los procesos.
     * @return Lista de procesos en ejecución.
     */
    public List<ListWho5> listQuerys(int serverId) {
        List<ListWho5> listWho5 = new ArrayList<>();

        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId); // Obtiene el JdbcTemplate para el servidor especificado

            // Ejecuta el procedimiento almacenado sp_who5 usando JdbcTemplate
            String sql = "EXEC sp_who5";

            listWho5 = jdbcTemplate.query(sql, new RowMapper<ListWho5>() {
                @Override
                public ListWho5 mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ListWho5 who5 = new ListWho5();
                    who5.setDatabaseName(rs.getString("DatabaseName"));
                    who5.setTimeSec(rs.getString("TimeSec"));
                    who5.setUsername(rs.getString("usuario"));
                    who5.setHostname(rs.getString("Hostname"));
                    who5.setSpid(rs.getString("SPID"));
                    who5.setStatus(rs.getString("Status"));
                    who5.setCommand(rs.getString("Command"));
                    who5.setProceso(rs.getString("proceso"));
                    who5.setSqlBatchText(rs.getString("SQLBatchText"));
                    who5.setBloqueando(rs.getString("Bloqueando"));
                    who5.setPhysicalIO(rs.getString("Physical_IO"));
                    who5.setCpu(rs.getString("CPU"));
                    who5.setWrites(rs.getString("writes"));
                    who5.setReads(rs.getString("reads"));
                    who5.setLogicalReads(rs.getString("logical_reads"));
                    who5.setSchedulerId(rs.getString("scheduler_id"));
                    who5.setLastWaitType(rs.getString("LastWaitType"));
                    who5.setLoginTime(rs.getString("LoginTime"));
                    return who5;
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Error al ejecutar el procedimiento sp_who5", ex);
        }

        return listWho5; // Retorna la lista de procesos
    }

    //metodo para eliminar el proiceso en ejecucion

    //exepcion de error
    public class ProcessTerminationException extends RuntimeException {
        public ProcessTerminationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    //metodo para matar el proceso
    public boolean killProcess(int serverId, String spid) {
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId); // Obtiene el JdbcTemplate para el servidor especificado

            // Crear la consulta para matar el proceso
            String sql = String.format("KILL %s", spid);

            // Ejecutar la consulta
            jdbcTemplate.execute(sql);
            return true; // Indica éxito
        } catch (Exception ex) {
            throw new ProcessTerminationException("Error al matar el proceso con SPID: " + spid, ex);
        }
    }



}