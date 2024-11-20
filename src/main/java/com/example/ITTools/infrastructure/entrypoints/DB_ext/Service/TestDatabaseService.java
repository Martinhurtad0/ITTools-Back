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
                try {
                    JdbcTemplate jdbcTemplate = getJdbcTemplate(server);

                    // Llamar al procedimiento almacenado SP_Check_LogShipping
                    String sqlScript = "EXEC SP_Check_LogShipping";

                    // Ejecutar el procedimiento almacenado
                    jdbcTemplate.execute(sqlScript);

                    // No hay resultados directos que mapear, pero puedes realizar otras acciones aquí si es necesario
                } catch (Exception e) {
                    System.err.println("Error : " + server.getIpServer() + " - " + e.getMessage());
                }
                return new ArrayList<>(); // Retornar una lista vacía ya que no hay datos directos
            });
            futures.add(future);
        }

        // Esperar a que todas las tareas asíncronas se completen
        futures.forEach(CompletableFuture::join);

        // Aquí puedes retornar una lista vacía o realizar otra acción si es necesario
        return new ArrayList<>();
    }

        // Esperar a que todas las tareas asíncronas se completen y combinar los resultados





}
