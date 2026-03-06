package com.joblink.model;

import com.joblink.model.enums.Role;
import java.time.LocalDateTime;

public class Client extends Utilisateur {
    private String nomEntreprise;
    private String adresse;

    // Constructeur vide
    public Client() {
        super();
    }

    // Constructeur complet (sans ID)
    public Client(String nom, String prenom, String email, String hashMotDePasse,
                  String nomEntreprise, String adresse) {
        super(nom, prenom, email, hashMotDePasse, Role.CLIENT);
        this.nomEntreprise = nomEntreprise;
        this.adresse = adresse;
    }

    // Getters et Setters
    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
