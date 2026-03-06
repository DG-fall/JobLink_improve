package com.joblink.model;

import com.joblink.model.enums.StatutMission;
import java.time.LocalDate;

public class Mission {
    private int idMission;
    private String titre;
    private String description;
    private double budget;
    private LocalDate dateLimite;
    private LocalDate dateLivraisonPrevue;
    private StatutMission statut;
    private int idClient;

    // Constructeur vide
    public Mission() {
    }

    // Constructeur complet (sans ID)
    public Mission(String titre, String description, double budget, LocalDate dateLimite,
                   LocalDate dateLivraisonPrevue, StatutMission statut, int idClient) {
        this.titre = titre;
        this.description = description;
        this.budget = budget;
        this.dateLimite = dateLimite;
        this.dateLivraisonPrevue = dateLivraisonPrevue;
        this.statut = statut;
        this.idClient = idClient;
    }

    // Getters et Setters
    public int getIdMission() {
        return idMission;
    }

    public void setIdMission(int idMission) {
        this.idMission = idMission;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public LocalDate getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDate dateLimite) {
        this.dateLimite = dateLimite;
    }

    public LocalDate getDateLivraisonPrevue() {
        return dateLivraisonPrevue;
    }

    public void setDateLivraisonPrevue(LocalDate dateLivraisonPrevue) {
        this.dateLivraisonPrevue = dateLivraisonPrevue;
    }

    public StatutMission getStatut() {
        return statut;
    }

    public void setStatut(StatutMission statut) {
        this.statut = statut;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }
}
