package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Databases;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Properties;
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
import java.util.List;

@Service
public class PropertiesService {

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
     * Lista las bases de datos en el servidor especificado.
     *
     * @param serverId ID del servidor para obtener las bases de datos.
     * @return Lista de bases de datos.
     */
    public List<Databases> listDatabases(int serverId) {
        String sql = "SELECT database_id, name FROM sys.databases";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);

        return jdbcTemplate.query(sql, new RowMapper<Databases>() {
            @Override
            public Databases mapRow(ResultSet rs, int rowNum) throws SQLException {
                Databases database = new Databases();
                database.setDatabase_id(rs.getInt("database_id"));
                database.setName(rs.getString("name"));
                return database;
            }
        });
    }

    /**
     * Lista las propiedades de una base de datos específica.
     *
     * @param serverId ID del servidor donde se encuentra la base de datos.
     * @param dataName Nombre de la base de datos.
     * @return Lista de propiedades.
     */
    public List<Properties> listProperties(int serverId, String dataName) {
        String sql = String.format("SELECT property_id, project, property, module, value, instance FROM %s.dbo.properties", dataName);
        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);

        return jdbcTemplate.query(sql, new RowMapper<Properties>() {
            @Override
            public Properties mapRow(ResultSet rs, int rowNum) throws SQLException {
                Properties property = new Properties();
                property.setProperty_id(rs.getInt("property_id"));
                property.setProject(rs.getString("project"));
                property.setProperty(rs.getString("property"));
                property.setModule(rs.getString("module"));
                property.setValue(rs.getString("value"));
                property.setInstance(rs.getString("instance"));
                return property;
            }
        });
    }
}