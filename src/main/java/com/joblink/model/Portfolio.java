package com.joblink.model;

public class Portfolio {
    private int idPortfolio;
    private String titre;
    private String description;
    private int idFreelance;

    // Constructeur vide
    public Portfolio() {
    }

    // Constructeur complet (sans ID)
    public Portfolio(String titre, String description, int idFreelance) {
        this.titre = titre;
        this.description = description;
        this.idFreelance = idFreelance;
    }

    // Getters et Setters
    public int getIdPortfolio() {
        return idPortfolio;
    }

    public void setIdPortfolio(int idPortfolio) {
        this.idPortfolio = idPortfolio;
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

    public int getIdFreelance() {
        return idFreelance;
    }

    public void setIdFreelance(int idFreelance) {
        this.idFreelance = idFreelance;
    }
}
