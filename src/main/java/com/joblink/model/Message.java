package com.joblink.model;

import java.time.LocalDateTime;

public class Message {
    private int idMessage;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private int idExpediteur;
    private int idDestinataire;

    // Constructeur vide
    public Message() {
    }

    // Constructeur complet (sans ID)
    public Message(String contenu, LocalDateTime dateEnvoi, int idExpediteur, int idDestinataire) {
        this.contenu = contenu;
        this.dateEnvoi = dateEnvoi;
        this.idExpediteur = idExpediteur;
        this.idDestinataire = idDestinataire;
    }

    // Getters et Setters
    public int getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(int idMessage) {
        this.idMessage = idMessage;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public int getIdExpediteur() {
        return idExpediteur;
    }

    public void setIdExpediteur(int idExpediteur) {
        this.idExpediteur = idExpediteur;
    }

    public int getIdDestinataire() {
        return idDestinataire;
    }

    public void setIdDestinataire(int idDestinataire) {
        this.idDestinataire = idDestinataire;
    }
}
