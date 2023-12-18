/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entités;

/**
 *
 * @author ASUS
 */
public class Equipe {
    int id;
    String nom;
    String slug;
    int classement;
    int points;

    public Equipe(int id, String nom, String slug, int classement, int points) {
        this.id = id;
        this.nom = nom;
        this.slug = slug;
        this.classement = classement;
        this.points = points;
    }

    public Equipe() {
    }

    public Equipe(String nom, String slug, int classement, int points) {
        this.nom = nom;
        this.slug = slug;
        this.classement = classement;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getClassement() {
        return classement;
    }

    public void setClassement(int classement) {
        this.classement = classement;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    
    
}
