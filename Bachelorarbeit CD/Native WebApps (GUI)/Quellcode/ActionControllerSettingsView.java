import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class ActionControllerSettingsView implements Initializable{
	@FXML
	private TextField textfieldAppName, textfieldJavaMainPathMain, textfieldProjectPathMain;
	
	@FXML
	private CheckBox checkboxPermissionInternet, checkboxPermissionNetworkStatus, checkboxPermissionCamera, checkboxPermissionVibrate, checkboxPermissionGPSCoarseLoc, checkboxPermissionGPSFineLoc, checkboxPermissionWriteExternalStorage, checkboxPermissionReadExternalStorage, checkboxPermissionFlashlight;
	
	@FXML
	private ImageView imageviewIconNormal, imageviewIconRound;
	
	@FXML
	private ChoiceBox<String> choiceboxTheme1, choiceboxTheme2;
	
	private ArrayList<CheckBox> permissionCheckboxes = new ArrayList<>(); 
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		permissionCheckboxes.add(checkboxPermissionInternet);
		permissionCheckboxes.add(checkboxPermissionNetworkStatus);
		permissionCheckboxes.add(checkboxPermissionVibrate);
		permissionCheckboxes.add(checkboxPermissionCamera);
		permissionCheckboxes.add(checkboxPermissionFlashlight);
		permissionCheckboxes.add(checkboxPermissionWriteExternalStorage);
		permissionCheckboxes.add(checkboxPermissionReadExternalStorage);
		permissionCheckboxes.add(checkboxPermissionGPSCoarseLoc);
		permissionCheckboxes.add(checkboxPermissionGPSFineLoc);

		try {
			//load Textfields
			
			if (textfieldAppName != null) {
				textfieldAppName.setText(Main.logicLayer.getAppName());
				textfieldJavaMainPathMain.setText(Main.logicLayer.getJavaMainPath());
				textfieldProjectPathMain.setText(Main.logicLayer.getProjectPath());
				
				boolean[] permissionStates =  Main.logicLayer.getPermissionStates();
				for (int i = 0; i < this.permissionCheckboxes.size(); i++)
					this.permissionCheckboxes.get(i).setSelected(permissionStates[i]);
				
				imageviewIconNormal.setImage(new Image(new File(Main.logicLayer.getIconPathNormal()).toURI().toString()));
				imageviewIconRound.setImage(new Image(new File(Main.logicLayer.getIconPathRound()).toURI().toString()));
				
				String[] tempAppTheme = Main.logicLayer.getAppTheme().split("\\.");
				choiceboxTheme1.setItems(FXCollections.observableArrayList("Light", "DayNight"));
				choiceboxTheme2.setItems(FXCollections.observableArrayList("", "NoActionBar", "DialogWhenLarge", "Dialog", "Dialog.Alert", "DarkActionBar", "Dialog.MinWidth"));
				
				if (tempAppTheme.length == 3) {
					choiceboxTheme1.setValue(tempAppTheme[2]);
					choiceboxTheme2.setValue("");
				}else if(tempAppTheme.length == 4) {
					choiceboxTheme1.setValue(tempAppTheme[2]);
					choiceboxTheme2.setValue(tempAppTheme[3]);
				}else {
					System.err.println("Ein Fehler ist aufgetreten!");
				}
			}
		}catch(Exception e){
			System.out.println("Beim Initialisieren ist ein Fehler aufgetreten: " + e);
		}
	}
	
	@FXML
	public void savePathChange() {
		String projectPath = textfieldProjectPathMain.getText(), javaMainPath = textfieldJavaMainPathMain.getText();

		if (!projectPath.equals("") && !javaMainPath.equals("") && new File(projectPath + "/build.gradle").exists() && javaMainPath.contains(projectPath + "/app/src/main/java") && javaMainPath.contains(".java") && new File(javaMainPath).exists()) {
			Main.logicLayer.setProjectPath(projectPath);
			Main.logicLayer.setJavaMainPath(javaMainPath);
		}
	}
	
	@FXML
	public void cancelPathChange() {
		textfieldProjectPathMain.setText(Main.logicLayer.getProjectPath());
		textfieldJavaMainPathMain.setText(Main.logicLayer.getJavaMainPath());
	}
	
	@FXML
	public void saveAllChanges() {
		Main.logicLayer.setAppName(textfieldAppName.getText());
		
		if (choiceboxTheme2.getValue().equals("")) {
			Main.logicLayer.setAppTheme("Theme.AppCompat." + choiceboxTheme1.getValue());
		}else {
			Main.logicLayer.setAppTheme("Theme.AppCompat." + choiceboxTheme1.getValue() + "." + choiceboxTheme2.getValue());
		}
		
		boolean[] newPermissionStates = new boolean[this.permissionCheckboxes.size()];
		for (int i = 0; i < this.permissionCheckboxes.size(); i++) newPermissionStates[i] = this.permissionCheckboxes.get(i).isSelected();
			
		Main.logicLayer.setPermissions(newPermissionStates);
		
		Main.logicLayer.setUsesFeatures( checkboxPermissionCamera.isSelected(), (checkboxPermissionGPSCoarseLoc.isSelected() || checkboxPermissionGPSFineLoc.isSelected()));
		
		Main.logicLayer.writeNecessaryXmlFiles();
	}
	
	@FXML
	public void setIconNormal() {
		Main.logicLayer.setIconNormal(new FileChooser().showOpenDialog(Main.stage).getPath());
		imageviewIconNormal.setImage(new Image(new File(Main.logicLayer.getIconPathNormal()).toURI().toString()));
	}
	
	@FXML
	public void setIconRound() {
		Main.logicLayer.setIconRound(new FileChooser().showOpenDialog(Main.stage).getPath());
		imageviewIconRound.setImage(new Image(new File(Main.logicLayer.getIconPathRound()).toURI().toString()));
	}
	
	@FXML
	public void loadHtmlEditor() throws IOException {
		Main.stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("res/layout_HtmlEditor.fxml"))));
	}
}
