package com.joblink.model;

import com.joblink.model.enums.StatutProposition;
import java.time.LocalDateTime;

public class Proposition {
    private int idProposition;
    private double montantPropose;
    private String message;
    private LocalDateTime dateProposition;
    private StatutProposition statut;
    private int idMission;
    private int idFreelance;

    // Constructeur vide
    public Proposition() {
    }

    // Constructeur complet (sans ID)
    public Proposition(double montantPropose, String message, LocalDateTime dateProposition,
                       StatutProposition statut, int idMission, int idFreelance) {
        this.montantPropose = montantPropose;
        this.message = message;
        this.dateProposition = dateProposition;
        this.statut = statut;
        this.idMission = idMission;
        this.idFreelance = idFreelance;
    }

    // Getters et Setters
    public int getIdProposition() {
        return idProposition;
    }

    public void setIdProposition(int idProposition) {
        this.idProposition = idProposition;
    }

    public double getMontantPropose() {
        return montantPropose;
    }

    public void setMontantPropose(double montantPropose) {
        this.montantPropose = montantPropose;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateProposition() {
        return dateProposition;
    }

    public void setDateProposition(LocalDateTime dateProposition) {
        this.dateProposition = dateProposition;
    }

    public StatutProposition getStatut() {
        return statut;
    }

    public void setStatut(StatutProposition statut) {
        this.statut = statut;
    }

    public int getIdMission() {
        return idMission;
    }

    public void setIdMission(int idMission) {
        this.idMission = idMission;
    }

    public int getIdFreelance() {
        return idFreelance;
    }

    public void setIdFreelance(int idFreelance) {
        this.idFreelance = idFreelance;
    }
}
