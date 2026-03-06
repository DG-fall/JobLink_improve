package com.joblink.model;

import com.joblink.model.enums.Role;
import java.time.LocalDateTime;

public abstract class Utilisateur {
    protected int idUtilisateur;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String hashMotDePasse; // On stocke le hash, pas le mot de passe clair
    protected LocalDateTime dateInscription;
    protected Role role;

    // Constructeur vide (nécessaire pour le mapping BDD)
    public Utilisateur() {
    }

    // Constructeur complet (sans l'ID car auto-généré par la BDD)
    public Utilisateur(String nom, String prenom, String email, String hashMotDePasse, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.hashMotDePasse = hashMotDePasse;
        this.role = role;
        this.dateInscription = LocalDateTime.now();
    }

    // Getters et Setters (Vous pouvez les générer automatiquement avec Alt+Inser sur IntelliJ)
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHashMotDePasse() { return hashMotDePasse; }
    public void setHashMotDePasse(String hashMotDePasse) { this.hashMotDePasse = hashMotDePasse; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}