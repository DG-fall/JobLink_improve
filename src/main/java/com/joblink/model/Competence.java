package com.joblink.model;

public class Competence {
    private int idCompetence;
    private String nom;

    // Constructeur vide
    public Competence() {
    }

    // Constructeur complet (sans ID)
    public Competence(String nom) {
        this.nom = nom;
    }

    // Getters et Setters
    public int getIdCompetence() {
        return idCompetence;
    }

    public void setIdCompetence(int idCompetence) {
        this.idCompetence = idCompetence;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
