package com.joblink.model;

import java.time.LocalDateTime;

public class Evaluation {
    private int idEvaluation;
    private int note;
    private String commentaire;
    private LocalDateTime dateEvaluation;
    private int idMission;
    private int idClient;
    private int idFreelance;

    // Constructeur vide
    public Evaluation() {
    }

    // Constructeur complet (sans ID)
    public Evaluation(int note, String commentaire, LocalDateTime dateEvaluation,
                      int idMission, int idClient, int idFreelance) {
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
        this.idMission = idMission;
        this.idClient = idClient;
        this.idFreelance = idFreelance;
    }

    // Getters et Setters
    public int getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(int idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateEvaluation() {
        return dateEvaluation;
    }

    public void setDateEvaluation(LocalDateTime dateEvaluation) {
        this.dateEvaluation = dateEvaluation;
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
