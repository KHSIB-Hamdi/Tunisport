/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entités;

import java.util.Date;

/**
 *
 * @author ASUS
 */
public class MatchF {
    private int id;
    private String equipeA;
    private String equipeB;
    private Date date;
    private String type;
    private String stade;
    private String tournoi;
    private int resultatA;
    private int resultatB;
    private int prix;
    private String image;
    private String image2;

    public MatchF(int id, String equipeA, String equipeB, Date date, String type, String stade, String tournoi, int resultatA, int resultatB, int prix, String image, String image2) {
        this.id = id;
        this.equipeA = equipeA;
        this.equipeB = equipeB;
        this.date = date;
        this.type = type;
        this.stade = stade;
        this.tournoi = tournoi;
        this.resultatA = resultatA;
        this.resultatB = resultatB;
        this.prix = prix;
        this.image = image;
        this.image2 = image2;
    }

    public MatchF(String equipeA, String equipeB, Date date, String type, String stade, String tournoi, int resultatA, int resultatB, int prix, String image, String image2) {
        this.equipeA = equipeA;
        this.equipeB = equipeB;
        this.date = date;
        this.type = type;
        this.stade = stade;
        this.tournoi = tournoi;
        this.resultatA = resultatA;
        this.resultatB = resultatB;
        this.prix = prix;
        this.image = image;
        this.image2 = image2;
    }

    public MatchF(String equipeA) {
        this.equipeA = equipeA;
    }

    

    

    public MatchF() {
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEquipeA() {
        return equipeA;
    }

    public String getEquipeB() {
        return equipeB;
    }

    public Date getDate() {
        return date;
    }

    
    public String getType() {
        return type;
    }

    public String getStade() {
        return stade;
    }

    public String getTournoi() {
        return tournoi;
    }

    public int getResultatA() {
        return resultatA;
    }

    public int getResultatB() {
        return resultatB;
    }

    public int getPrix() {
        return prix;
    }

    public String getImage() {
        return image;
    }

    public String getImage2() {
        return image2;
    }

    
    
    
    
    public void setEquipeA(String equipeA) {
        this.equipeA = equipeA;
    }

    public void setEquipeB(String equipeB) {
        this.equipeB = equipeB;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    

    

    public void setType(String type) {
        this.type = type;
    }

    public void setStade(String stade) {
        this.stade = stade;
    }

    public void setTournoi(String tournoi) {
        this.tournoi = tournoi;
    }

    public void setResultatA(int resultatA) {
        this.resultatA = resultatA;
    }

    public void setResultatB(int resultatB) {
        this.resultatB = resultatB;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }
    
    

    @Override
    public String toString() {
        return "MatchF{" + "id=" + id + ", equipeA=" + equipeA + ", equipeB=" + equipeB + ", type=" + type + ", stade=" + stade + ", tournoi=" + tournoi + ", resultatA=" + resultatA + ", resultatB=" + resultatB + ", prix=" + prix + '}';
    }

   
}
