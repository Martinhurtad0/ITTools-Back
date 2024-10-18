package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.Audit.Model.RecyclingAudit;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.LogTransactionServers;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Pins;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.valueOf;

@Service
public class RecyclingPingService {

    @Autowired
    private ServerBD_Repository serverBDRepository;

    @Autowired
    private AuditService auditService;

    public JdbcTemplate getJdbcTemplate(int serverId) {
        ServerBD_Model server = serverBDRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server with ID " + serverId + " not found"));

        if (server.getIpServer() == null || server.getIpServer().isEmpty()) {
            throw new RuntimeException("Server IP is missing for server ID: " + serverId);
        }
        if (server.getPortServer() == null || server.getPortServer().isEmpty()) {
            throw new RuntimeException("Server port is missing for server ID: " + serverId);
        }
        if (server.getRecyclingDB() == null || server.getRecyclingDB().isEmpty()) {
            throw new RuntimeException("Database name is missing for server ID: " + serverId);
        }
        if (server.getUserLogin() == null || server.getUserLogin().isEmpty()) {
            throw new RuntimeException("Username is missing for server ID: " + serverId);
        }
        if (server.getPassword() == null || server.getPassword().isEmpty()) {
            throw new RuntimeException("Password is missing for server ID: " + serverId);
        }

        String url;
        if (server.getInstance() != null && !server.getInstance().isEmpty()) {
            url = String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;instanceName=%s;encrypt=true;trustServerCertificate=true",
                    server.getIpServer(), server.getPortServer(), server.getRecyclingDB(), server.getInstance());
        } else {
            url = String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true",
                    server.getIpServer(), server.getPortServer(), server.getRecyclingDB());
        }

        DataSource dataSource = DataSourceBuilder.create()
                .url(url)
                .username(server.getUserLogin())
                .password(server.getPassword())
                .build();

        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(2)) {
                throw new SQLException("Connection test failed.");
            }
            System.out.println("Conexión a la base de datos exitosa.");
        } catch (SQLException e) {
            System.err.println("Error al probar la conexión JDBC: " + e.getMessage());
            throw new RuntimeException("Error al probar la conexión JDBC: " + e.getMessage(), e);
        }

        return new JdbcTemplate(dataSource);
    }

    public String testDatabaseConnection(int serverId) {
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
            return "Conexión a la base de datos exitosa.";
        } catch (Exception e) {
            return "Error al conectar a la base de datos: " + e.getMessage();
        }
    }

    public List<Pins> listPin(String pinIn, int serverId) {
        List<Pins> listPin = new ArrayList<>();

        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
            String sqlPins = "SELECT * FROM pins WHERE pin = ?";

            listPin = jdbcTemplate.query(sqlPins, new Object[]{pinIn}, (rs, rowNum) -> {
                Pins pin = new Pins();
                pin.setProductId(rs.getString("product_id"));
                pin.setPin(pinIn);
                pin.setControlNo(rs.getString("control_no"));
                pin.setAmount(rs.getDouble("amount"));
                pin.setAni(rs.getString("ani"));
                pin.setInsertDate(valueOf(rs.getDate("insert_date")));
                pin.setActivationDate(valueOf(rs.getDate("activation_date")));
                pin.setRecycleDate(valueOf(rs.getDate("recycle_date")));
                pin.setTransactionCount(rs.getInt("transaction_count"));
                pin.setBatchID(rs.getInt("BatchID"));
                pin.setExpirationDate(valueOf(rs.getDate("expirationDate")));

                int pinStatusId = rs.getInt("pinStatusId");
                System.out.println("pinStatusId: " + pinStatusId);

                String sqlPinStatus = "SELECT code FROM PINStatus WHERE id = ?";
                String state = jdbcTemplate.queryForObject(sqlPinStatus, new Object[]{pinStatusId}, String.class);
                pin.setState(state);

                return pin;
            });

            return listPin;

        } catch (EmptyResultDataAccessException e) {
            System.out.println("No se encontraron resultados para el PIN: " + pinIn);
            return listPin;
        } catch (DataAccessException e) {
            System.err.println("Error de acceso a datos: " + e.getMessage());
            throw new RuntimeException("Error al acceder a la base de datos: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener pins: " + e.getMessage(), e);
        }
    }

    public void recyclePins(List<Pins> selectedPinsTry, int serverId, String auditTicket, String authorization) {
        if (auditTicket == null || auditTicket.isEmpty()) {
            throw new RuntimeException("Audit ticket is required.");
        }

        if (authorization == null || authorization.isEmpty()) {
            throw new RuntimeException("Authorization is required. Please provide who authorized the operation.");
        }

        if (selectedPinsTry == null || selectedPinsTry.isEmpty()) {
            throw new RuntimeException("No pins selected for recycling.");
        }

        try {
            for (Pins pin : selectedPinsTry) {
                if (!"SOLD".equals(pin.getState())) {
                    RecyclingAudit audit = new RecyclingAudit();
                    audit.setPin(pin.getPin());
                    audit.setTicket(auditTicket);
                    audit.setStatusPinBefore(pin.getState());
                    audit.setDescriptionError("The pin does not comply with the parameter ('Status = Sold')");
                    audit.setUsername(authorization); // Guardamos el autorizador en el campo de la auditoría
                    auditService.saveRecyclingAudit(audit);
                    throw new RuntimeException(String.format("The pin [%s] does not comply with the parameter ('Status = Sold')", pin.getPin()));
                }

                // Aquí se pasa el serverId al método updatePin
                if (updatePin(pin, serverId)) {
                    RecyclingAudit audit = new RecyclingAudit();
                    audit.setPin(pin.getPin());
                    audit.setTicket(auditTicket);
                    audit.setStatusPinBefore("SOLD");
                    audit.setStatusPinAfter("RECYCLED");
                    audit.setDescriptionError("");
                    audit.setUsername(authorization); // Aquí también se guarda el nombre del autorizador
                    auditService.saveRecyclingAudit(audit);
                    System.out.println("Updated recycling pin: " + pin.getPin());
                } else {
                    throw new RuntimeException(String.format("Pin [%s] was not updated successfully", pin.getPin()));
                }
            }

            if (!selectedPinsTry.isEmpty()) {
                List<Pins> listPinTry = consultPins(selectedPinsTry, serverId);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error during pin recycling process: " + e.getMessage(), e);
        }
    }

    public boolean updatePin(Pins pinIn, int serverId) {
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);

            String sql = "UPDATE pins SET activation_date = ?, recycle_date = GETDATE(), pinStatusId = ? " +
                    "WHERE pin = ? AND product_id = ? AND control_no = ?";

            return jdbcTemplate.update(sql,
                    pinIn.getActivationDate(),
                    1,
                    pinIn.getPin(),
                    pinIn.getProductId(),
                    pinIn.getControlNo()) > 0;

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el pin: " + e.getMessage(), e);
        }
    }

    public List<Pins> uploadFile(MultipartFile file, int serverId) {
        List<Pins> listPinExcel = new ArrayList<>();
        try (InputStream fileIn = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(fileIn);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            DataFormatter formatter = new DataFormatter();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (row.getRowNum() == 0) {
                    continue;
                }

                Pins pin = new Pins();
                pin.setPin(formatter.formatCellValue(row.getCell(0)));
                pin.setProductId(formatter.formatCellValue(row.getCell(1)));
                pin.setControlNo(formatter.formatCellValue(row.getCell(2)));

                try {
                    Cell amountCell = row.getCell(3);
                    if (amountCell != null) {
                        pin.setAmount(Double.parseDouble(formatter.formatCellValue(amountCell)));
                    } else {
                        pin.setAmount(0.0);
                    }
                } catch (NumberFormatException e) {
                    pin.setAmount(0.0);
                }

                pin.setAni(formatter.formatCellValue(row.getCell(4)));
                pin.setInsertDate(formatter.formatCellValue(row.getCell(5)));
                pin.setActivationDate(formatter.formatCellValue(row.getCell(6)));
                pin.setRecycleDate(formatter.formatCellValue(row.getCell(7)));

                try {
                    Cell batchIDCell = row.getCell(9);
                    if (batchIDCell != null) {
                        pin.setBatchID(Integer.parseInt(formatter.formatCellValue(batchIDCell)));
                    } else {
                        pin.setBatchID(0);
                    }
                } catch (NumberFormatException e) {
                    pin.setBatchID(0);
                }

                pin.setExpirationDate(formatter.formatCellValue(row.getCell(10)));

                listPinExcel.add(pin);
            }

            return listPinExcel;

        } catch (IOException | EncryptedDocumentException e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }

    public List<Pins> consultPins(List<Pins> listExcel, int serverId) {
        List<Pins> listPin = new ArrayList<>();

        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
            String sqlPins = "SELECT * FROM pins WHERE control_no = ?";

            for (Pins controlNo : listExcel) {
                List<Pins> pinsFromDB = jdbcTemplate.query(sqlPins, new Object[]{controlNo.getControlNo()}, (rs, rowNum) -> {
                    Pins pin = new Pins();
                    pin.setProductId(rs.getString("productId"));
                    pin.setPin(rs.getString("pin"));
                    pin.setControlNo(valueOf(rs.getInt("control_no")));
                    pin.setAmount(rs.getDouble("amount"));
                    pin.setAni(rs.getString("ani"));
                    pin.setInsertDate(valueOf(rs.getDate("insert_date")));
                    pin.setActivationDate(valueOf(rs.getDate("activation_date")));
                    pin.setRecycleDate(valueOf(rs.getDate("recycle_date")));
                    pin.setTransactionCount(rs.getInt("transaction_count"));
                    pin.setBatchID(rs.getInt("BatchID"));
                    pin.setExpirationDate(valueOf(rs.getDate("expirationDate")));

                    String sqlPinStatus = "SELECT code FROM PINStatus WHERE id = ?";
                    String state = jdbcTemplate.queryForObject(sqlPinStatus, new Object[]{rs.getInt("pinStatusId")}, String.class);
                    pin.setState(state);

                    return pin;
                });

                listPin.addAll(pinsFromDB);
            }

            return listPin;
        } catch (Exception e) {
            throw new RuntimeException("Error when querying pins: " + e.getMessage(), e);
        }
    }
}
