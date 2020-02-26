import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class ActionControllerAddNecessaryCode implements Initializable{

	@FXML
	private TextField textfieldWebViewName;
	
	@FXML
	private CheckBox checkboxAddNecessaryData;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
	}
	
	@FXML
	public void addNecessaryCode() throws IOException {
			
		if(checkboxAddNecessaryData.isSelected() && !textfieldWebViewName.getText().contentEquals("")) {
			Main.logicLayer.copyFrameworkClasses(textfieldWebViewName.getText());
			Main.logicLayer.prepareActivityMain(textfieldWebViewName.getText());
			Main.logicLayer.addToStringsXml();
			
			writeConfigFile(textfieldWebViewName.getText(), checkboxAddNecessaryData.isSelected());
			
			Main.logicLayer.writeNecessaryXmlFiles();
			Main.stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("res/layout_HtmlEditor.fxml"))));
			Main.stage.setTitle("Native Webapps");
		}
	}
	
	@FXML
	public void dontAddNecessaryCode() {
		writeConfigFile("null", false);
	}
	
	//Methods not used by FXML itself
	
	public void writeConfigFile(String webViewName, boolean addedNecessaryFiles) {
		BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(Main.logicLayer.getProjectPath() + "/NativeWebApp.config")));
            writer.write("addedNecessaryFiles=" + addedNecessaryFiles);
            writer.write("\nWebViewName=" + webViewName);
        } catch (Exception e) {
            System.err.println("Beim Speichern der Konfigurationsdatei ist ein Fehler aufgetreten: " + e);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            	System.err.println("Beim Schlie\u00dfen des BufferedWriter's ist ein Fehler augetreten: " + e);
            }
        }
        Main.logicLayer.readConfigFile();
	}

}
