package com.joblink.model;

public class Fichier {
    private int idFichier;
    private String nomFichier;
    private String cheminFichier;
    private String typeFichier;
    private Integer idPortfolio; // Nullable
    private Integer idMission;   // Nullable

    // Constructeur vide
    public Fichier() {
    }

    // Constructeur complet (sans ID)
    public Fichier(String nomFichier, String cheminFichier, String typeFichier,
                   Integer idPortfolio, Integer idMission) {
        this.nomFichier = nomFichier;
        this.cheminFichier = cheminFichier;
        this.typeFichier = typeFichier;
        this.idPortfolio = idPortfolio;
        this.idMission = idMission;
    }

    // Getters et Setters
    public int getIdFichier() {
        return idFichier;
    }

    public void setIdFichier(int idFichier) {
        this.idFichier = idFichier;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public String getTypeFichier() {
        return typeFichier;
    }

    public void setTypeFichier(String typeFichier) {
        this.typeFichier = typeFichier;
    }

    public Integer getIdPortfolio() {
        return idPortfolio;
    }

    public void setIdPortfolio(Integer idPortfolio) {
        this.idPortfolio = idPortfolio;
    }

    public Integer getIdMission() {
        return idMission;
    }

    public void setIdMission(Integer idMission) {
        this.idMission = idMission;
    }
}
