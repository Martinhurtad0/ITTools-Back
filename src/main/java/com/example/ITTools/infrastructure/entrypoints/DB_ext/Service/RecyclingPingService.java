package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class RecyclingPingService {

    @Autowired
    private ServerBD_Repository serverBDRepository;

    // Metodo para hacer la conexion a la bases de datos externas
    public JdbcTemplate getJdbcTemplate(int serverId) {
        ServerBD_Model server = serverBDRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        String url;
        if (server.getInstance() != null && !server.getInstance().isEmpty()) {
            url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;instanceName=%s;encrypt=true;trustServerCertificate=true",
                    server.getIpServer(), server.getPortServer(), server.getServerDB(), server.getInstance());
        } else {
            url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true",
                    server.getIpServer(), server.getPortServer(), server.getServerDB());
        }

        try {
            DataSource dataSource = DataSourceBuilder.create()
                    .url(url)
                    .username(server.getUserLogin())
                    .password(server.getPassword())
                    .build();

            // Probar la conexión
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("Conexión a la base de datos exitosa.");
            }

            return new JdbcTemplate(dataSource);
        } catch (SQLException e) {
            System.err.println("Error al obtener la conexión JDBC: " + e.getMessage());
            throw new RuntimeException("Error al obtener la conexión JDBC: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error al crear JdbcTemplate: " + e.getMessage());
            throw new RuntimeException("Error al crear JdbcTemplate: " + e.getMessage(), e);
        }
    }

    public String testDatabaseConnection(int serverId) {
        try {
            // Obtener JdbcTemplate
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);

            // Si llegamos aquí, significa que se pudo crear el JdbcTemplate
            return "Conexión a la base de datos exitosa.";
        } catch (Exception e) {
            // Captura cualquier excepción y devuelve un mensaje de error
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
                pin.setProductId(String.valueOf(rs.getInt("product_id")));
                pin.setPin(pinIn);
                pin.setControlNo(String.valueOf(rs.getInt("control_no")));
                pin.setAmount(rs.getDouble("amount"));
                pin.setAni(rs.getString("ani"));
                pin.setInsertDate(String.valueOf(rs.getDate("insert_date")));
                pin.setActivationDate(String.valueOf(rs.getDate("activation_date")));
                pin.setRecycleDate(String.valueOf(rs.getDate("recycle_date")));
                pin.setTransactionCount(rs.getInt("transaction_count"));
                pin.setBatchID(rs.getInt("BatchID"));
                pin.setExpirationDate(String.valueOf(rs.getDate("expirationDate")));

                // Validación adicional antes de la consulta
                int pinStatusId = rs.getInt("pinStatusId");
                System.out.println("pinStatusId: " + pinStatusId); // Imprime para depuración

                // Consulta adicional para obtener el estado del pin
                String sqlPinStatus = "SELECT code FROM PINStatus WHERE id = ?";
                String state = jdbcTemplate.queryForObject(sqlPinStatus, new Object[]{pinStatusId}, String.class);
                pin.setState(state);

                return pin;
            });

            return listPin;

        } catch (EmptyResultDataAccessException e) {
            System.out.println("No se encontraron resultados para el PIN: " + pinIn);
            return listPin; // Retorna lista vacía si no hay resultados
        } catch (DataAccessException e) {
            System.err.println("Error de acceso a datos: " + e.getMessage());
            throw new RuntimeException("Error al acceder a la base de datos: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el stack trace para depuración
            throw new RuntimeException("Error al obtener pins: " + e.getMessage(), e);
        }
    }
    // Método para actualizar un pin
    public boolean updatePin(Pins pinIn, LogTransactionServers serv) {
        try {
            // Obtener JdbcTemplate usando el método getJdbcTemplate
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serv.getServerId()); // Asegúrate de tener el serverId en LogTransactionServers

            String sql = "UPDATE pins SET activation_date = ?, recycle_date = GETDATE(), pinStatusId = ? " +
                    "WHERE pin = ? AND product_id = ? AND control_no = ?";

            // Ejecutar la actualización
            return jdbcTemplate.update(sql, pinIn.getActivationDate(), 1, pinIn.getPin(), pinIn.getProductId(), pinIn.getControlNo()) > 0;

        } catch (Exception e) {
            // Manejo de otras excepciones
            throw new RuntimeException("Error al actualizar el pin: " + e.getMessage(), e);
        }
    }



    // Método para subir archivo
    public List<Pins> uploadFile(MultipartFile file, int serverId) {
        List<Pins> listPinExcel = new ArrayList<>();
        try (InputStream fileIn = file.getInputStream()) { // Elimina FileInputStream, usa directamente InputStream
            Workbook workbook = WorkbookFactory.create(fileIn); // Carga el archivo Excel
            Sheet sheet = workbook.getSheetAt(0); // Obtén la primera hoja
            Iterator<Row> rowIterator = sheet.iterator(); // Itera sobre las filas
            DataFormatter formatter = new DataFormatter(); // Formateador para las celdas

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (row.getRowNum() == 0) { // Salta la fila de encabezados
                    continue;
                }

                Pins pin = new Pins(); // Instancia del objeto Pins
                pin.setPin(formatter.formatCellValue(row.getCell(0))); // Pin
                pin.setProductId(formatter.formatCellValue(row.getCell(1))); // Product ID
                pin.setControlNo(formatter.formatCellValue(row.getCell(2))); // Control No

                // Manejo seguro para Amount
                try {
                    Cell amountCell = row.getCell(3);
                    if (amountCell != null) {
                        pin.setAmount(Double.parseDouble(formatter.formatCellValue(amountCell)));
                    } else {
                        pin.setAmount(0.0); // Valor por defecto o manejo alternativo
                    }
                } catch (NumberFormatException e) {
                    pin.setAmount(0.0); // Manejo del error
                }

                pin.setAni(formatter.formatCellValue(row.getCell(4))); // ANI
                pin.setInsertDate(formatter.formatCellValue(row.getCell(5))); // Insert Date
                pin.setActivationDate(formatter.formatCellValue(row.getCell(6))); // Activation Date
                pin.setRecycleDate(formatter.formatCellValue(row.getCell(7))); // Recycle Date

                // Manejo seguro para Batch ID
                try {
                    Cell batchIDCell = row.getCell(9);
                    if (batchIDCell != null) {
                        pin.setBatchID(Integer.parseInt(formatter.formatCellValue(batchIDCell)));
                    } else {
                        pin.setBatchID(0); // Valor por defecto
                    }
                } catch (NumberFormatException e) {
                    pin.setBatchID(0); // Manejo del error
                }

                pin.setExpirationDate(formatter.formatCellValue(row.getCell(10))); // Expiration Date

                listPinExcel.add(pin); // Añadir a la lista
            }

            return listPinExcel; // Retorna la lista de Pins

        } catch (IOException | EncryptedDocumentException e) {
            throw new RuntimeException("Error al cargar el archivo: " + e.getMessage(), e);
        }
    }


    public List<Pins> consultPins(List<Pins> listExcel, LogTransactionServers serv) {
        List<Pins> listPin = new ArrayList<>();

        try {
            // Obtener JdbcTemplate usando el método getJdbcTemplate
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serv.getServerId());

            // Consulta para obtener los pins
            String sqlPins = "SELECT * FROM pins WHERE control_no = ?";

            for (Pins controlNo : listExcel) {
                // Ejecutar consulta para cada control_no
                List<Pins> pinsFromDB = jdbcTemplate.query(sqlPins, new Object[]{controlNo.getControlNo()}, (rs, rowNum) -> {
                    Pins pin = new Pins();
                    pin.setProductId(String.valueOf(rs.getInt("product_id")));
                    pin.setPin(rs.getString("pin"));
                    pin.setControlNo(String.valueOf(rs.getInt("control_no")));
                    pin.setAmount(rs.getDouble("amount"));
                    pin.setAni(rs.getString("ani"));
                    pin.setInsertDate(String.valueOf(rs.getDate("insert_date")));
                    pin.setActivationDate(String.valueOf(rs.getDate("activation_date")));
                    pin.setRecycleDate(String.valueOf(rs.getDate("recycle_date")));
                    pin.setTransactionCount(rs.getInt("transaction_count"));
                    pin.setBatchID(rs.getInt("BatchID"));
                    pin.setExpirationDate(String.valueOf(rs.getDate("expirationDate")));

                    // Consulta adicional para obtener el estado del pin
                    String sqlPinStatus = "SELECT code FROM PINStatus WHERE id = ?";
                    String state = jdbcTemplate.queryForObject(sqlPinStatus, new Object[]{rs.getInt("pinStatusId")}, String.class);

                    return pin;
                });

                // Agregar los pins obtenidos a la lista
                listPin.addAll(pinsFromDB);
            }

            return listPin;
        } catch (Exception e) {
            // Manejo de excepciones
            throw new RuntimeException("Error al consultar pins: " + e.getMessage(), e);
        }
    }

}
