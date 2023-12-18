/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import Entités.MatchF;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import Utils.DataBase;
import Interfaces.InterfaceMatch;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class MatchFCRUD implements InterfaceMatch<MatchF>{
    private Connection con;
    private Statement ste;
    private PreparedStatement pst ;
    private ResultSet res ;
    
    public MatchFCRUD() {
        con = DataBase.getInstance().getConnection();
        
    }

    
    
    
 @Override
     public void add(MatchF m) throws SQLException{
        
        Statement st = con.createStatement();
          String req =" insert into match_f (id, equipe_a_id ,  equipe_b_id, date_match, type_match_id, stade_id, tournoi_id, resultat_a, resultat_b, prix, image, image2)"+ "values ('" +m.getId()+ "' , '"+m.getEquipeA()+" ' ,'"+m.getEquipeB()+" ' ,'"+m.getDate()+" ' , '"+m.getType() + " ' , '"+m.getStade() + " ' , '"+m.getTournoi() + " ' , '"+m.getResultatA() + " ' , '"+m.getResultatB() +  "' , '"+m.getPrix()+  "' , '"+m.getImage()+  "' , '"+m.getImage2()+"')"; 
    st.executeUpdate(req);
    System.out.println("match ajouté");



    }

    @Override
    public ObservableList<MatchF> read() throws SQLException{
    ObservableList<MatchF> ls = FXCollections.observableArrayList();
    
    Statement st = con.createStatement();
    String req = "select * from match_f ";
    /*Statement st1 = con.createStatement();
    Statement st2 = con.createStatement();
    Statement st3 = con.createStatement();
    Statement st4 = con.createStatement();
    
    String req1 = "select * from equipe";
    String req2 = "select * from type_match";
    String req3 = "select * from stade";
    String req4 = "select * from tournoi";*/
    ResultSet rs = st.executeQuery(req);
    /*ResultSet rs1 = st1.executeQuery(req1);
    ResultSet rs2 = st2.executeQuery(req2);
    ResultSet rs3 = st3.executeQuery(req3);
    ResultSet rs4 = st4.executeQuery(req4);*/
    
     
    while(rs.next() ){
        
        int id = rs.getInt("id");
        String equipeA = rs.getString("equipe_a_id");
        String equipeB = rs.getString("equipe_b_id");
        Date date = rs.getDate("date_match");
        String type = rs.getString("type_match_id");
        String stade = rs.getString("stade_id");
        String tournoi = rs.getString("tournoi_id");
        int resultatA = rs.getInt("resultat_a");
        int resultatB = rs.getInt("resultat_b");
        int prix = rs.getInt("prix");
        String image = rs.getString("image");
        String image2 = rs.getString("image2");
        

        MatchF m = new MatchF(id, equipeA ,  equipeB,date ,type, stade, tournoi, resultatA, resultatB, prix, image, image2);
        ls.add(m);
        
    }
    
    return ls;

    }

   
    public void update(MatchF m) throws SQLException {
        try {
            PreparedStatement pt = con.prepareStatement("UPDATE match_f SET equipe_a_id = '" + m.getEquipeA()+ "', equipe_b_id = '"+m.getEquipeB()+ "', date_match = '"+m.getDate()+ "', type_match_id = '" + m.getType()+ "', stade_id = '" + m.getStade()+ "', tournoi_id = '" + m.getTournoi()+ "', resultat_a = '" + m.getResultatA()+ "', resultat_b = '" + m.getResultatB()+ "', prix = '" + m.getPrix()+ "', image = '" + m.getImage()+ "', image2 = '" + m.getImage2() + "' WHERE id = " + m.getId()); 
            pt.executeUpdate();
            System.out.println("match modifié !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }


    }

    
    public void delete(int id) throws SQLException {
        try {
            PreparedStatement pt = con.prepareStatement("DELETE FROM match_f WHERE id = " + id);
            
            pt.executeUpdate();
            System.out.println("match supprimé !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }    
    }
    
    

    
   
    
    

    
    
    
    
    
    
    
   
 
    
}
