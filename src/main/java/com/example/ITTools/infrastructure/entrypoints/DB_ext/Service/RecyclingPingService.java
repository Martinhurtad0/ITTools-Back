package com.example.ITTools.infrastructure.entrypoints.DB_ext.Service;

import com.example.ITTools.infrastructure.entrypoints.Audit.Model.RecyclingAudit;
import com.example.ITTools.infrastructure.entrypoints.Audit.Service.AuditService;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.LogTransactionServers;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Pins;
import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Request.RecyclePinsResponse;
import com.example.ITTools.infrastructure.entrypoints.Server.Models.ServerBD_Model;
import com.example.ITTools.infrastructure.entrypoints.Server.Repositories.ServerBD_Repository;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static org.antlr.v4.runtime.misc.Utils.readFile;

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

                pin.setPinStatusId(rs.getInt("pinStatusId"));
                System.out.println("pinStatusId: " + pin.getPinStatusId());

                String sqlPinStatus = "SELECT code FROM PINStatus WHERE id = ?";
                String state = jdbcTemplate.queryForObject(sqlPinStatus, new Object[]{pin.getPinStatusId()}, String.class);
                pin.setState(state);

                return pin;
            });

            return listPin;

        } catch (EmptyResultDataAccessException e) {
            System.out.println("No results found for PIN: " + pinIn);
            return listPin;
        } catch (DataAccessException e) {
            System.err.println("Data access error:" + e.getMessage());
            throw new RuntimeException("Error accessing database: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting pins: " + e.getMessage(), e);
        }
    }



    public RecyclePinsResponse recycleMultiplePins(List<Pins> pinsList, int serverId, String authorizedBy, String ticketNumber, String fileName) {
        List<String> recycledPins = new ArrayList<>();
        List<String> notUpdatedPins = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        List<String> satisfyingMessages = new ArrayList<>();
        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);

        for (Pins pin : pinsList) {
           String recycleDate = pin.getRecycleDate() ;// Inicializa antes del bloque try
            String statusBefore = null;
            String statusAfter = null; // Inicializa antes del bloque try
            String error = ""; // Inicializa antes del bloque try

            try {
                // Obtener detalles del pin desde la base de datos
                List<Pins> pinDetails = listPin(pin.getPin(), serverId);
                if (pinDetails.isEmpty()) {
                    errorMessages.add("The pin " + pin.getPin() + " was not found in the database");
                    error = "Pin not found in database."; // Mensaje de error
                    RecyclingAudit audit = new RecyclingAudit(
                            fileName,
                            pin.getPin(),
                            ticketNumber,
                            pin.getProductId(),
                            pin.getControlNo(),
                            null,
                            null, // El usuario se obtiene automáticamente
                            authorizedBy,
                            null,
                            null,
                            error
                    );
                    auditService.saveRecyclingAudit(audit);
                    notUpdatedPins.add(pin.getPin());
                    continue; // Continuar con el siguiente pin
                }

                Pins pinFromDb = pinDetails.get(0);
                statusBefore = pinFromDb.getState(); // Estado antes de la actualización
                int pinStatusId = pinFromDb.getPinStatusId(); // Obtener el estado del pin por ID


                // Solo actualizar si el estado es igual a 3
                if (pinStatusId == 3) {
                    String sqlUpdate = "UPDATE pins SET activation_date = ?, recycle_date = GETDATE(), pinStatusId = ? " +
                            "WHERE pin = ? AND product_id = ? AND control_no = ?";



                    int updatedRows = jdbcTemplate.update(sqlUpdate,
                            null, // Puede ser null si no se proporciona
                            1,  // Asumiendo que 1 es el estado 'reciclado'
                            pin.getPin(),
                            pin.getProductId(),
                            pin.getControlNo());

                    recycledPins.add(pin.getPin());

                    // Después de la actualización, obtener el nuevo estado
                    statusAfter = "AVALAIBLE"; // Asumimos que el estado después de reciclar es 'reciclado'
                    error="Pin recycled successfully";

                    if (updatedRows > 0) {
                        RecyclingAudit audit = new RecyclingAudit(
                                fileName,
                                pin.getPin(),
                                ticketNumber,
                                pin.getProductId(),
                                pin.getControlNo(),
                                pin.getRecycleDate(),
                                null, // El usuario se obtiene automáticamente
                                authorizedBy,
                                statusBefore,
                                statusAfter,
                                error
                        );
                        auditService.saveRecyclingAudit(audit);
                        satisfyingMessages.add("Pin recycled successfully " + pin.getPin());
                    } else {
                        errorMessages.add("No se actualizó el pin: " + pin.getPin());
                        notUpdatedPins.add(pin.getPin());
                    }
                } else {
                    // Si el estado no es 3, agregar a la lista de pines no actualizados
                    errorMessages.add("The pin " + pin.getPin() + " (Status: " + pinStatusId + ") is not in the 'SOLD' state. It cannot be recycled");
                    notUpdatedPins.add(pin.getPin());
                    error = "Error recycling pin, incorrect status: " + pinStatusId; // Mensaje de error

                    RecyclingAudit audit = new RecyclingAudit(
                            fileName,
                            pin.getPin(),
                            ticketNumber,
                            pin.getProductId(),
                            pin.getControlNo(),
                            pin.getRecycleDate(),
                            null, // El usuario se obtiene automáticamente
                            authorizedBy,
                            statusBefore,
                            statusAfter,
                            error
                    );
                    auditService.saveRecyclingAudit(audit);
                }

            } catch (DataAccessException e) {
                errorMessages.add("Error recycling pin: " + (pin != null ? pin.getPin() : "Unknown") + " - " + e.getMessage());
                notUpdatedPins.add(pin != null ? pin.getPin() : "Desconocido");
                error = "Data access error: " + e.getMessage(); // Mensaje de error

            } catch (Exception e) {
                System.err.println("Unexpected error while recycling pin: " + (pin != null ? pin.getPin() : "Unknown") + " - " + e.getMessage());
                notUpdatedPins.add(pin != null ? pin.getPin() : "Unknown");
                recycledPins.add(pin.getPin());


            }
        }

        return new RecyclePinsResponse(recycledPins, notUpdatedPins, errorMessages, satisfyingMessages);
    }




    public RecyclePinsResponse recyclePinsFromFile(MultipartFile file, int serverId, String authorizedBy, String ticketNumber) {
        List<Pins> pinsList = new ArrayList<>();
        String fileName = file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            if (fileName != null && fileName.endsWith(".xlsx")) {
                pinsList = readPinsFromExcel(inputStream);
            } else if (fileName != null && (fileName.endsWith(".csv") || fileName.endsWith(".txt") || fileName.endsWith(".dat"))) {
                pinsList =readPinsFromFile(inputStream);
            } else {
                throw new IllegalArgumentException("File type not supported. Please use .xlsx, .csv, or .txt.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }

        return recycleMultiplePins(pinsList, serverId, authorizedBy, ticketNumber, fileName);
    }




    public List<Pins> readPinsFromExcel(InputStream inputStream) {
        List<Pins> pinsList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Usar la primera hoja

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar la fila de encabezado

                // Verificar si la fila está vacía en los campos Product ID, PIN y Control No
                Cell productIdCell = row.getCell(0);
                Cell pinCell = row.getCell(1);
                Cell controlNoCell = row.getCell(2);

                if ((productIdCell == null || productIdCell.getCellType() == CellType.BLANK) &&
                        (pinCell == null || pinCell.getCellType() == CellType.BLANK) &&
                        (controlNoCell == null || controlNoCell.getCellType() == CellType.BLANK)) {
                    break; // Detener la lectura si todos los campos están vacíos
                }

                Pins pin = new Pins();

                // Leer Product ID (columna 1)
                if (productIdCell != null) {
                    if (productIdCell.getCellType() == CellType.STRING) {
                        pin.setProductId(productIdCell.getStringCellValue());
                    } else if (productIdCell.getCellType() == CellType.NUMERIC) {
                        pin.setProductId(String.valueOf((int) productIdCell.getNumericCellValue())); // Convertir a STRING
                    }
                } else {
                    System.err.println("The Product ID cell is empty in the row " + row.getRowNum());
                    continue; // Saltar esta fila si el Product ID está vacío
                }

                // Leer PIN (columna 2)
                if (pinCell != null) {
                    if (pinCell.getCellType() == CellType.STRING) {
                        pin.setPin(pinCell.getStringCellValue());
                    } else if (pinCell.getCellType() == CellType.NUMERIC) {
                        pin.setPin(String.valueOf((int) pinCell.getNumericCellValue())); // Convertir a STRING
                    }
                } else {
                    System.err.println("The PIN cell is empty in the row" + row.getRowNum());
                    continue; // Saltar esta fila si el PIN está vacío
                }

                // Leer Control No (columna 3)
                if (controlNoCell != null) {
                    if (controlNoCell.getCellType() == CellType.STRING) {
                        pin.setControlNo(controlNoCell.getStringCellValue());
                    } else if (controlNoCell.getCellType() == CellType.NUMERIC) {
                        pin.setControlNo(String.valueOf((int) controlNoCell.getNumericCellValue())); // Convertir a STRING
                    }
                } else {
                    System.err.println("The Control cell is not empty in the row " + row.getRowNum());
                    continue; // Saltar esta fila si el Control No está vacío
                }

                // Agregar el objeto pin a la lista
                pinsList.add(pin);
            }
        } catch (IOException | EncryptedDocumentException e) {
            throw new RuntimeException("Error reading Excel file: " + e.getMessage(), e);
        }
        return pinsList;
    }


    public List<Pins> readPinsFromFile(InputStream inputStream) {
        List<Pins> pinsList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(","); // Ajustar separador según el formato
                if (columns.length < 3) continue; // Verificar que existan las columnas necesarias

                Pins pin = new Pins();

                pin.setProductId(columns[0].trim()); // Columna 2: Product ID
                pin.setPin(columns[1].trim()); // Columna 1: PIN
                pin.setControlNo(columns[2].trim()); // Columna 3: Control No

                pinsList.add(pin);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV/TXT/DAT file: " + e.getMessage(), e);
        }
        return pinsList;
    }


}
