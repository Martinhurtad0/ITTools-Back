package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import lombok.Getter;
import lombok.Setter;

public class Pins {
   @Getter @Setter
        private String pin;
    @Getter @Setter
        private String productId;
    @Getter @Setter
        private String controlNo;
    @Getter @Setter
        private double amount;
    @Getter @Setter// Asegúrate de que el valor en JSON sea un número
        private String ani;
    @Getter @Setter
        private String insertDate;
    @Getter @Setter
        private String activationDate;
    @Getter @Setter
        private String recycleDate;
    @Getter @Setter
        private int transactionCount;// Asegúrate de que el valor en JSON sea un número
    @Getter @Setter
        private int batchID; // Asegúrate de que el valor en JSON sea un número
    @Getter @Setter
        private String expirationDate;
    @Getter @Setter
    private String state;
    @Getter @Setter
    private int pinStatusId;

        // Getters y Setters



}
