import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class ActionControllerLoadProjectView implements Initializable{

	@FXML
	private TextField textfieldProjectPath, textfieldJavaMainPath;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (textfieldProjectPath != null) {
			try{
				if (new File("paths.sav").exists()) {
					List<String> lines = Files.readAllLines(Paths.get("paths.sav"), StandardCharsets.US_ASCII);
					textfieldProjectPath.setText(lines.get(0));
					textfieldJavaMainPath.setText(lines.get(1));
				}
			}catch(Exception e) {
				System.err.println("Ein Problem ist aufgetreten: " + e);
			}
		}
	}
	
	@FXML
	public void loadProject() throws IOException {
		String projectPath = textfieldProjectPath.getText(), javaMainPath = textfieldJavaMainPath.getText();
		
		if (!projectPath.equals("") && !javaMainPath.equals("") && new File(projectPath + "/build.gradle").exists() && (javaMainPath.contains(projectPath + "/app/src/main/java") || javaMainPath.contains(projectPath + "\\app\\src\\main\\java")) && javaMainPath.contains(".java") && new File(javaMainPath).exists()) {
			Main.logicLayer = new LogicLayer(projectPath, javaMainPath);
			
			if(!(new File(projectPath + "/NativeWebApp.config").exists())) {
				Main.stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("res/layout_addCodeToJava.fxml"))));
				Main.stage.setTitle("Ben\u00f6tigte Ressourcen & Code Hinzuf\u00fcgen");
			}else {
				Main.stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("res/layout_HtmlEditor.fxml"))));
				Main.stage.setTitle("Native Webapps");
			}
				
			if(!(new File(Main.logicLayer.getAssetsLocation()).exists()))
				new File(Main.logicLayer.getAssetsLocation()).mkdir();
			if(!(new File(Main.logicLayer.getAssetsLocation() + "/sources").exists()))
				new File(Main.logicLayer.getAssetsLocation() + "/sources").mkdir();
		}else {
			new Alert(AlertType.ERROR, "Ein Fehler ist aufgetreten. Bitte beachten Sie, dass sich die gew\u00e4hlte Java-Main im gew\u00e4hlten Projektordner befinden muss.", ButtonType.OK).showAndWait();
		}
	}
	
	@FXML
	public void pickProjectPath() {
		textfieldProjectPath.setText(new DirectoryChooser().showDialog(Main.stage).getPath());
	}
	
	@FXML
	public void pickJavaMainPath() {
		textfieldJavaMainPath.setText(new FileChooser().showOpenDialog(Main.stage).getPath());
	}

	@FXML
	public void findJavaMainAutomatically() {
		for (File file : new File(textfieldProjectPath.getText() + "/app/src/main/java").listFiles()[0].listFiles()[0].listFiles()[0].listFiles())
			if (file.getName().equals("MainActivity.java"))
				textfieldJavaMainPath.setText(file.getPath());
	}

}
