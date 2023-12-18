/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tunisport;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import Entités.MatchF;
import services.MatchFCRUD;

/**
 *
 * @author ASUS
 */
public class Tunisport {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
       MatchF m = new MatchF();
    

      
       
       MatchFCRUD sm= new MatchFCRUD();


        try {
       //     
            sm.add(m);
            

           
            
           
            System.out.println(" ***** affichage match************");
            List<MatchF> l = new ArrayList<>();
            l = sm.read();
            for( MatchF M : l){
                            System.out.println(M.toString());

            }

           
           
           System.out.println(sm.read());
           
           System.out.println(" ***** supp ************");
           
           System.out.println(sm.read());
 
        } catch (SQLException ex) {
            Logger.getLogger(Tunisport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
