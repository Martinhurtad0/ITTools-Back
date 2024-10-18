package com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.Pins;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.lang.String.valueOf;

@Repository
public class PinRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Pins> findPinsByPinAndServerId(String pinIn, int serverId, JdbcTemplate jdbcTemplate) {
        String sqlPins = "SELECT * FROM pins WHERE pin = ?";
        return jdbcTemplate.query(sqlPins, new Object[]{pinIn}, (rs, rowNum) -> {
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

            // Obtén el estado del pin
            int pinStatusId = rs.getInt("pinStatusId");
            String sqlPinStatus = "SELECT code FROM PINStatus WHERE id = ?";
            String state = jdbcTemplate.queryForObject(sqlPinStatus, new Object[]{pinStatusId}, String.class);
            pin.setState(state);

            return pin;
        });
    }



    public boolean updatePin(Pins pinIn) {
        String sql = "UPDATE pins SET activation_date = ?, recycle_date = GETDATE(), pinStatusId = ? " +
                "WHERE pin = ? AND product_id = ? AND control_no = ?";
        return jdbcTemplate.update(sql,
                pinIn.getActivationDate(),
                1,  // pinStatusId
                pinIn.getPin(),
                pinIn.getProductId(),
                pinIn.getControlNo()) > 0;
    }



    public List<Pins> findPinsByControlNo(String controlNo) {
        String sqlPins = "SELECT * FROM pins WHERE control_no = ?";
        return jdbcTemplate.query(sqlPins, new Object[]{controlNo}, (rs, rowNum) -> {
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
            return pin;
        });
    }
}
