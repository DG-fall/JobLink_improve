package com.joblink.model;

import com.joblink.model.enums.Role;
import java.time.LocalDateTime;

public class Freelance extends Utilisateur {
    private double tarifJournalier;

    // Constructeur vide
    public Freelance() {
        super();
    }

    // Constructeur complet (sans ID)
    public Freelance(String nom, String prenom, String email, String hashMotDePasse,
                     double tarifJournalier) {
        super(nom, prenom, email, hashMotDePasse, Role.FREELANCE);
        this.tarifJournalier = tarifJournalier;
    }

    // Getters et Setters
    public double getTarifJournalier() {
        return tarifJournalier;
    }

    public void setTarifJournalier(double tarifJournalier) {
        this.tarifJournalier = tarifJournalier;
    }
}
