package com.joblink.model;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private int idUtilisateur;
    private String titre;
    private String message;
    private boolean lue;
    private Timestamp dateCreation;

    public Notification() {
    }

    public Notification(int idUtilisateur, String titre, String message) {
        this.idUtilisateur = idUtilisateur;
        this.titre = titre;
        this.message = message;
        this.lue = false;
        this.dateCreation = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isLue() {
        return lue;
    }

    public void setLue(boolean lue) {
        this.lue = lue;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }
}
