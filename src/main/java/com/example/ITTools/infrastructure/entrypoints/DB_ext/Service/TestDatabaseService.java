package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.BackupInfo;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TestDatabaseService {

    @Autowired
    private ServerBD_Repository serverBDRepository;

    public JdbcTemplate getJdbcTemplate(ServerBD_Model server) {
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + server.getIpServer() + ":" + server.getPortServer() + ";databaseName=" + server.getServerDB() + ";encrypt=true;trustServerCertificate=true")
                .username(server.getUserLogin())
                .password(server.getPassword())
                .build();

        return new JdbcTemplate(dataSource);
    }

    public List<BackupInfo> getAllBackupInfo() {
        List<ServerBD_Model> servers = serverBDRepository.findAll();
        List<CompletableFuture<List<BackupInfo>>> futures = new ArrayList<>();

        for (ServerBD_Model server : servers) {
            CompletableFuture<List<BackupInfo>> future = CompletableFuture.supplyAsync(() -> {
                List<BackupInfo> backupInfoList = new ArrayList<>();
                try {
                    JdbcTemplate jdbcTemplate = getJdbcTemplate(server);
                    String sqlScript = "SELECT Region, IP, Primary_Server, Secondary_Server, Primary_Database, " +
                            "Last_Backup_Date, Last_Copied_Date, Last_Restored_Date, Status FROM BackupStatusTable";
                    backupInfoList = jdbcTemplate.query(sqlScript, (ResultSet rs, int rowNum) -> {
                        BackupInfo info = new BackupInfo();
                        info.setRegion(rs.getString("Region"));
                        info.setIp(rs.getString("IP"));
                        info.setPrimaryServer(rs.getString("Primary_Server"));
                        info.setSecondaryServer(rs.getString("Secondary_Server"));
                        info.setPrimaryDatabase(rs.getString("Primary_Database"));
                        info.setLastBackupDate(rs.getTimestamp("Last_Backup_Date").toLocalDateTime());
                        info.setLastCopiedDate(rs.getTimestamp("Last_Copied_Date").toLocalDateTime());
                        info.setLastRestoredDate(rs.getTimestamp("Last_Restored_Date").toLocalDateTime());
                        info.setStatus(rs.getString("Status"));
                        return info;
                    });
                } catch (Exception e) {
                    System.err.println("Error : " + server.getIpServer() + " - " + e.getMessage());
                }
                return backupInfoList;
            });
            futures.add(future);
        }

        List<BackupInfo> allBackupInfo = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return allBackupInfo;
    }


}
