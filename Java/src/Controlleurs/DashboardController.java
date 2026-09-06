/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlleurs;

import Entités.MatchF;
import Utils.DataBase;
import Views.getData;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.MatchFCRUD;

/**
 * FXML Controller class
 *
 * @author ASUS
 */
public class DashboardController implements Initializable {

    @FXML
    private Button close;
    @FXML
    private Button minimize;
    @FXML
    private TableView<MatchF> match_table;
    @FXML
    private TableColumn<MatchF, String> match_col_id;
    @FXML
    private TableColumn<MatchF, String> eqA_col;
    @FXML
    private TableColumn<MatchF, String> eqB_col;
    @FXML
    private TableColumn<MatchF, String> date_col;
    @FXML
    private TableColumn<MatchF, String> type_col;
    @FXML
    private TableColumn<MatchF, String> stade_col;
    @FXML
    private TableColumn<MatchF, String> tournoi_col;
    @FXML
    private TableColumn<MatchF, String> rA_col;
    @FXML
    private TableColumn<MatchF, String> rB_col;
    @FXML
    private TableColumn<MatchF, String> prix_col;
    @FXML
    private TableColumn<MatchF, String> image_col;
    private TextField tfeqA;
    private TextField tfeqB;
    private TextField tftype;
    private TextField tfstade;
    private TextField tftournoi;
    @FXML
    private TextField tfrA;
    @FXML
    private TextField tfrB;
    @FXML
    private DatePicker dpdate;
    @FXML
    private ImageView ivimage;
    @FXML
    private Button btnimporter;
    @FXML
    private Button btnajouter;
    @FXML
    private Button btnsupprimer;
    @FXML
    private Button btnmodifier;
    @FXML
    private AnchorPane main_form;
    @FXML
    private Button btnhome;
    @FXML
    private Button btnmatchs;
    @FXML
    private AnchorPane home_form;
    @FXML
    private AnchorPane match_form;
    @FXML
    private TextField tfprix;
    @FXML
    private ImageView ivimage2;
    @FXML
    private Button btnimporter2;
    private ComboBox<String> combobox;
    ObservableList<String> optionstype = FXCollections.observableArrayList();
    ObservableList<String> optionstype1 = FXCollections.observableArrayList();
    ObservableList<String> optionstype2 = FXCollections.observableArrayList();
    ObservableList<String> optionstype3 = FXCollections.observableArrayList();
    ObservableList<String> optionstype4 = FXCollections.observableArrayList();
    Map<String, Integer> maMap = new HashMap<String, Integer>();
    Map<String, Integer> maMap1 = new HashMap<String, Integer>();
    Map<String, Integer> maMap2 = new HashMap<String, Integer>();
    Map<String, Integer> maMap3 = new HashMap<String, Integer>();
    Map<String, Integer> maMap4 = new HashMap<String, Integer>();
    
    private Connection con;
    private Statement ste;
    private PreparedStatement pst ;
    private ResultSet res ;
    @FXML
    private ComboBox<String> cbeqA;
    @FXML
    private ComboBox<String> cbeqB;
    @FXML
    private ComboBox<String> cbstade;
    @FXML
    private ComboBox<String> cbtournoi;
    @FXML
    private ComboBox<String> cbtype;
    @FXML
    private TextField tfid;
    @FXML
    private Button btneffacer;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            MatchShowListData();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        fillcomboEquipeA();
        cbeqA.setItems(optionstype);
        fillcomboEquipeB();
        cbeqB.setItems(optionstype1);
        fillcomboTypeMatch();
        cbtype.setItems(optionstype2);
        fillcomboStade();
        cbstade.setItems(optionstype3);
        fillcomboTournoi();
        cbtournoi.setItems(optionstype4);
        
    }

        
    public void fillcomboEquipeA() {
       
        con = DataBase.getInstance().getConnection();
        try {
            Statement st = con.createStatement();
            String req = "select * from equipe";
            
            
            ResultSet rs = st.executeQuery(req);
           
            while (rs.next()) {
                maMap.put(rs.getString("nom"), rs.getInt("id"));
                       
                optionstype.add(rs.getString("nom"));
            }

        } catch (SQLException ex) {

        }   
    }
    
    public void fillcomboEquipeB() {
       
        con = DataBase.getInstance().getConnection();
        try {
            Statement st = con.createStatement();
            String req = "select * from equipe";
            
            
            ResultSet rs = st.executeQuery(req);
           
            while (rs.next()) {
                maMap1.put(rs.getString("nom"), rs.getInt("id"));
                       
                optionstype1.add(rs.getString("nom"));
            }

        } catch (SQLException ex) {

        }   
    }
    
    public void fillcomboTypeMatch() {
       
        con = DataBase.getInstance().getConnection();
        try {
            Statement st = con.createStatement();
            String req = "select * from type_match";
            
            
            ResultSet rs = st.executeQuery(req);
           
            while (rs.next()) {
                maMap2.put(rs.getString("nom"), rs.getInt("id"));
                       
                optionstype2.add(rs.getString("nom"));
            }

        } catch (SQLException ex) {

        }   
    }
    
    public void fillcomboStade() {
       
        con = DataBase.getInstance().getConnection();
        try {
            Statement st = con.createStatement();
            String req = "select * from stade";
            
            
            ResultSet rs = st.executeQuery(req);
           
            while (rs.next()) {
                maMap3.put(rs.getString("nom"), rs.getInt("id"));
                       
                optionstype3.add(rs.getString("nom"));
            }

        } catch (SQLException ex) {

        }   
    }
    
    
    public void fillcomboTournoi() {
       
        con = DataBase.getInstance().getConnection();
        try {
            Statement st = con.createStatement();
            String req = "select * from tournoi";
            
            
            ResultSet rs = st.executeQuery(req);
           
            while (rs.next()) {
                maMap4.put(rs.getString("nom"), rs.getInt("id"));
                       
                optionstype4.add(rs.getString("nom"));
            }

        } catch (SQLException ex) {

        }   
    }
    
    @FXML
    private void close(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void minimize(ActionEvent event) {
        Stage stage = (Stage) main_form.getScene().getWindow();
        stage.setIconified(true);
    }


    @FXML
    private void switchform(ActionEvent event) {
        if (event.getSource() == btnhome) {
            home_form.setVisible(true);
            match_form.setVisible(false);
            
            btnhome.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            btnmatchs.setStyle("-fx-background-color:transparent");

        } else if (event.getSource() == btnmatchs) {
            home_form.setVisible(false);
            match_form.setVisible(true);
            
            btnmatchs.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            btnhome.setStyle("-fx-background-color:transparent");

        } 
    }
    
    
    @FXML
    private void reset(ActionEvent event) {
        cbeqA.setValue(null);
        cbeqB.setValue(null);
        dpdate.setValue(null);
        cbtype.setValue(null);
        cbstade.setValue(null);
        cbtournoi.setValue(null);
        tfrA.setText("");
        tfrB.setText("");
        tfprix.setText("");
        ivimage.setImage(null);
        ivimage2.setImage(null);
        getData.path = "";
    }
    
    
    
    @FXML
    private void supprimerMatch(ActionEvent event) throws SQLException {
        
    MatchF m = match_table.getSelectionModel().getSelectedItem();
    
    if (m != null) {
        MatchFCRUD us = new MatchFCRUD();
        us.delete(m.getId());
        RefreshMatchShowListData();
    } else {
        System.out.println("Aucun match sélectionné");
    }
    
    }

    @FXML
    private void insererImage(ActionEvent event) {
        FileChooser open = new FileChooser();
        File file = open.showOpenDialog(main_form.getScene().getWindow());

        if (file != null) {
            getData.path = file.getAbsolutePath();

            Image image = new Image(file.toURI().toString(), 101, 127, false, true);
            ivimage.setImage(image);
            
        }
    }
    
    @FXML
    private void insererImage2(ActionEvent event) {
        FileChooser open = new FileChooser();
        File file = open.showOpenDialog(main_form.getScene().getWindow());

        if (file != null) {
            getData.path = file.getAbsolutePath();

            Image image = new Image(file.toURI().toString(), 101, 127, false, true);
            ivimage2.setImage(image);
            
        }
    }
    
    public ObservableList<MatchF> MatchListData() throws SQLException {

        return new MatchFCRUD().read();
    }
    
    private ObservableList<MatchF> MatchList;
    
    public void MatchShowListData() throws SQLException {
        MatchList = MatchListData();

        match_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        eqA_col.setCellValueFactory(new PropertyValueFactory<>("equipeA"));
        eqB_col.setCellValueFactory(new PropertyValueFactory<>("equipeB"));
        date_col.setCellValueFactory(new PropertyValueFactory<>("date"));
        type_col.setCellValueFactory(new PropertyValueFactory<>("type"));
        stade_col.setCellValueFactory(new PropertyValueFactory<>("stade"));
        tournoi_col.setCellValueFactory(new PropertyValueFactory<>("tournoi"));
        rA_col.setCellValueFactory(new PropertyValueFactory<>("resultatA"));
        rB_col.setCellValueFactory(new PropertyValueFactory<>("resultatB"));
        prix_col.setCellValueFactory(new PropertyValueFactory<>("prix"));
        image_col.setCellValueFactory(new PropertyValueFactory<>("image"));
        

        match_table.setItems(MatchList);

    }
    
    public void RefreshMatchShowListData() throws SQLException {
        MatchList.clear();
        MatchList = MatchListData();

        match_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        eqA_col.setCellValueFactory(new PropertyValueFactory<>("equipeA"));
        eqB_col.setCellValueFactory(new PropertyValueFactory<>("equipeB"));
        date_col.setCellValueFactory(new PropertyValueFactory<>("date"));
        type_col.setCellValueFactory(new PropertyValueFactory<>("type"));
        stade_col.setCellValueFactory(new PropertyValueFactory<>("stade"));
        tournoi_col.setCellValueFactory(new PropertyValueFactory<>("tournoi"));
        rA_col.setCellValueFactory(new PropertyValueFactory<>("resultatA"));
        rB_col.setCellValueFactory(new PropertyValueFactory<>("resultatB"));
        prix_col.setCellValueFactory(new PropertyValueFactory<>("prix"));
        image_col.setCellValueFactory(new PropertyValueFactory<>("image"));
        

        match_table.setItems(MatchList);

    }
    
    @FXML
    public void MatchSelect() {
        MatchF m = match_table.getSelectionModel().getSelectedItem();
        int num = match_table.getSelectionModel().getSelectedIndex();

        if ((num - 1) < -1) {
            return;
        }

        tfid.setText(String.valueOf(m.getId()));
        cbeqA.setValue(m.getEquipeA());
        cbeqB.setValue(m.getEquipeB());
        
        cbtype.setValue(m.getType());
        cbstade.setValue(m.getStade());
        cbtournoi.setValue(m.getTournoi());
        tfrA.setText(String.valueOf(m.getResultatA()));
        tfrB.setText(String.valueOf(m.getResultatB()));
        tfprix.setText(String.valueOf(m.getPrix()));
    
        getData.path = m.getImage();

        String uri = "file:" + m.getImage();

        Image image = new Image(uri, 101, 127, false, true);
        ivimage.setImage(image);
        
        getData.path = m.getImage2();

        String uri2 = "file:" + m.getImage2();

        Image image2 = new Image(uri2, 101, 127, false, true);
        ivimage2.setImage(image2);
    }
    
    

    @FXML
    private void modifierMatch(ActionEvent event) {
        String uri = getData.path;
        uri = uri.replace("\\", "\\\\");
        
        String uri2 = getData.path;
        uri2 = uri2.replace("\\", "\\\\");

        

        String req = "UPDATE match_f SET equipe_a_id = '"
                + maMap.get(cbeqA.getValue()) + "', equipe_b_id = '"
                + maMap1.get(cbeqB.getValue()) + "', date_match = '"
                + Date.valueOf(dpdate.getValue()) + "', type_match_id = '"
                + maMap2.get(cbtype.getValue()) + "', stade_id = '"
                + maMap3.get(cbstade.getValue())+ "', tournoi_id = '"
                + maMap4.get(cbtournoi.getValue())+ "', resultat_a = '"
                + tfrA.getText()+ "', resultat_b = '"
                + tfrB.getText()+ "', prix = '"
                + tfprix.getText() + "', image = '"
                + uri + "', image2 = '" + uri2 + "' WHERE id ='"
                + tfid.getText() + "'";

       
            con  = DataBase.getInstance().getConnection();
         try {
            Alert alert;
            if (cbeqA.getValue().isEmpty()
                    || cbeqB.getValue().isEmpty()
                    || cbtournoi.getValue().isEmpty()
                    || cbstade.getValue().isEmpty()
                    || cbtype.getValue().isEmpty()
                    || tfrA.getText().isEmpty()
                    || tfrA.getText().isEmpty()
                    || tfprix.getText().isEmpty()
                    || getData.path == null || getData.path == "") {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Message d'erreur");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez remplir tous les champs");
                alert.showAndWait();
            } else {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Message de confirmation");
                alert.setHeaderText(null);
                alert.setContentText("Voulez vous modifier le match d'ID: " + tfid.getText() + "?");
                Optional<ButtonType> option = alert.showAndWait();

                if (option.get().equals(ButtonType.OK)) {
                    Statement st = con.createStatement();
                    st.executeUpdate(req);

                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Updated!");
                    alert.showAndWait();

                    RefreshMatchShowListData();
                    
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    @FXML
    private void ajouterMatch(ActionEvent event) throws SQLException {
        
        String uri = getData.path;
        uri = uri.replace("\\", "\\\\");
        
        String uri2 = getData.path;
        uri2 = uri2.replace("\\", "\\\\");
        
        LocalDate selectedDate = dpdate.getValue();
        LocalDate today = LocalDate.now();
        
            MatchF m = new MatchF(
                    (maMap.get(cbeqA.getValue()).toString()),
                    (maMap1.get(cbeqB.getValue()).toString()),
                    Date.valueOf(dpdate.getValue()),
                    (maMap2.get(cbtype.getValue()).toString()),
                    (maMap3.get(cbstade.getValue()).toString()),
                    (maMap4.get(cbtournoi.getValue()).toString()),
                    Integer.parseInt(tfrA.getText()),
                    Integer.parseInt(tfrB.getText()),
                    Integer.parseInt(tfprix.getText()),
                    uri,
                    uri2);
            try{
                Alert alert2;
            if (    cbeqA.getValue().isEmpty()
                    || cbeqB.getValue().isEmpty()
                    || cbtournoi.getValue().isEmpty()
                    || cbstade.getValue().isEmpty()
                    || cbtype.getValue().isEmpty()
                    || tfrA.getText().isEmpty()
                    || tfrA.getText().isEmpty()
                    || tfprix.getText().isEmpty()
                    || getData.path == null || getData.path == ""){
                    alert2 = new Alert(Alert.AlertType.ERROR);
                    alert2.setTitle("Message d'erreur");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Veuillez remplir tous les champs!");
                    alert2.showAndWait();
                 
            }
            else if (Integer.parseInt(tfrA.getText()) < 0){
                alert2 = new Alert(Alert.AlertType.ERROR);
                    alert2.setTitle("Message d'erreur");
                    alert2.setHeaderText(null);
                    alert2.setContentText("La valeur du résultat de l'équipe à domicile est negative!");
                    alert2.showAndWait();
            }
            else if (selectedDate == null || selectedDate.isBefore(today)){
                alert2 = new Alert(Alert.AlertType.ERROR);
                    alert2.setTitle("Message d'erreur");
                    alert2.setHeaderText(null);
                    alert2.setContentText("La date doit être au minimum aujourd'hui!");
                    alert2.showAndWait();
            }
            else {
                alert2 = new Alert(Alert.AlertType.CONFIRMATION);
                alert2.setTitle("Message de confirmation");
                alert2.setHeaderText(null);
                alert2.setContentText("Voulez vous ajouter ce match?");
                Optional<ButtonType> option = alert2.showAndWait();

                if (option.get().equals(ButtonType.OK)) {
                    MatchFCRUD pc = new MatchFCRUD();
                    pc.add(m);

                    alert2 = new Alert(Alert.AlertType.INFORMATION);
                    alert2.setTitle("Information Message");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Match ajouté avec succès!");
                    alert2.showAndWait();

                    RefreshMatchShowListData();
                    
                }
            }
            }
            catch (SQLException ex) {
                    System.out.println(ex.getMessage());;
                }
            
            
           
            
        
    }

    
    
    
    
    

    

    
    
    
    
    
       
    
    
}
