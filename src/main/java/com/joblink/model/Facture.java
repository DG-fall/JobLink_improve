package com.joblink.model;

import java.time.LocalDateTime;

public class Facture {
    private int idFacture;
    private double montantTotal;
    private LocalDateTime dateEmission;
    private int idMission;
    private int idClient;
    private int idFreelance;

    // Constructeur vide
    public Facture() {
    }

    // Constructeur complet (sans ID)
    public Facture(double montantTotal, LocalDateTime dateEmission, int idMission,
                   int idClient, int idFreelance) {
        this.montantTotal = montantTotal;
        this.dateEmission = dateEmission;
        this.idMission = idMission;
        this.idClient = idClient;
        this.idFreelance = idFreelance;
    }

    // Getters et Setters
    public int getIdFacture() {
        return idFacture;
    }

    public void setIdFacture(int idFacture) {
        this.idFacture = idFacture;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public LocalDateTime getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDateTime dateEmission) {
        this.dateEmission = dateEmission;
    }

    public int getIdMission() {
        return idMission;
    }

    public void setIdMission(int idMission) {
        this.idMission = idMission;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdFreelance() {
        return idFreelance;
    }

    public void setIdFreelance(int idFreelance) {
        this.idFreelance = idFreelance;
    }
}
