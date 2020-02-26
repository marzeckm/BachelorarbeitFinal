import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;

public class ActionControllerHtmlEditorView implements Initializable{

	@FXML
	private ListView<String> listviewHtmlFiles;
	
	@FXML
	private TextArea textareaHtmlEditor;
	
	@FXML
	private RadioButton radiobuttonLoadFromAssets, radiobuttonLoadWebsite;
	
	@FXML
	private ChoiceBox<String> choiceboxHtmlFiles;
	
	@FXML
	private TextField textfieldNewHtmlFileName, textfieldWebsiteUrl;
	
	@FXML
	private ColorPicker colorpickerPrimary, colorpickerPrimaryDark, colorpickerAccent;
	
	@FXML
	private Button buttonSaveWebsite;
	
	@FXML
	private WebView webviewMain;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (Main.logicLayer.getConfigValue("addedNecessaryFiles")) {
			loadWebsitesAndUrls();
		}else {
			choiceboxHtmlFiles.setDisable(true);
			radiobuttonLoadFromAssets.setDisable(true);
			radiobuttonLoadWebsite.setDisable(true);
			textfieldWebsiteUrl.setDisable(true);
			buttonSaveWebsite.setDisable(true);
		}
			
		if(radiobuttonLoadFromAssets != null) {
			ToggleGroup toggleGroup = new ToggleGroup();
			radiobuttonLoadFromAssets.setToggleGroup(toggleGroup);
			radiobuttonLoadWebsite.setToggleGroup(toggleGroup);
			
			colorpickerPrimary.setValue(Color.valueOf(Main.logicLayer.getColorPrimary()));
			colorpickerPrimaryDark.setValue(Color.valueOf(Main.logicLayer.getColorPrimaryDark()));
			colorpickerAccent.setValue(Color.valueOf(Main.logicLayer.getColorAccent()));
			
			Main.logicLayer.writeNecessaryXmlFiles();
			
			loadHtmlFilesToListView();
		}		
	}
	
	@FXML
	public void loadHtmlToHtmlEditor() {
		textareaHtmlEditor.setText("");
		for(String s : Main.logicLayer.readHtmlFile(Main.logicLayer.getAssetsLocation() + "/" + listviewHtmlFiles.getSelectionModel().getSelectedItem()))
			textareaHtmlEditor.setText(textareaHtmlEditor.getText() + s + "\n");
	}
	
	@FXML
	public void createNewHtmlDocumentInAssets() {
		StringBuilder sb = new StringBuilder();
		for (String s : Main.logicLayer.readHtmlFile("res/Template.html"))
		{
		    sb.append(s);
		    sb.append("\n");
		}
		
		if (!textfieldNewHtmlFileName.getText().contentEquals("") && textfieldNewHtmlFileName.getText() != null && !(new File(Main.logicLayer.getAssetsLocation() + "/" + textfieldNewHtmlFileName.getText() + ".html").exists())) {
			try (PrintWriter out = new PrintWriter(Main.logicLayer.getAssetsLocation() + "/" + textfieldNewHtmlFileName.getText() + ".html")) {
				out.println(sb.toString());
			}catch(Exception e) {
				System.out.println("Es ist ein Fehler aufgetreten.");
			};
		}else {
			new Alert(AlertType.ERROR, "Datei konnte nicht erstellt werden, pr\u00fcfen Sie ob die Datei schon existiert, oder auf den Pfad nicht zugegriffen werden kann.", ButtonType.OK).showAndWait();
		}
		textfieldNewHtmlFileName.setText("");
		loadHtmlFilesToListView();
	}
	
	@FXML
	public void saveHtmlToSelectedFile() {
		try (PrintWriter out = new PrintWriter(Main.logicLayer.getAssetsLocation() + "/" + listviewHtmlFiles.getSelectionModel().getSelectedItem())) {
			out.println(textareaHtmlEditor.getText());
		}catch(Exception e) {
			System.out.println("Es ist ein Fehler aufgetreten.");
		};
	}
	
	@FXML
	public void deleteSelectedHtmlFile() {
		new File(Main.logicLayer.getAssetsLocation() + "/" + listviewHtmlFiles.getSelectionModel().getSelectedItem()).delete();
		textareaHtmlEditor.setText("<html><head></head><body contenteditable=\"true\"><font face=\"Times New Roman\"></font face>Sollte dieser HTML-Editor nicht reagieren, stellen Sie sicher, dass eine HTML-Datei und eine g\u00fcltige Schiftart ausgew\u00e4hlt wurden.</body></html>");
		loadHtmlFilesToListView();
	}
	
	@FXML
	public void changeColors() {
		Main.logicLayer.setColorPrimary("#" + colorpickerPrimary.getValue().toString().substring(2, 8));
		Main.logicLayer.setColorPrimaryDark("#" + colorpickerPrimaryDark.getValue().toString().substring(2, 8));
		Main.logicLayer.setColorAccent("#" + colorpickerAccent.getValue().toString().substring(2, 8));
		Main.logicLayer.writeNecessaryXmlFiles();
	}
	
	@FXML
	public void loadSettings() throws IOException {
		Main.stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("res/layout_settings.fxml"))));
	}
	
	@FXML
	public void saveMainUrl() {
		String urlChoiceBox = choiceboxHtmlFiles.getValue();
		String urlTextbox = textfieldWebsiteUrl.getText();
		try {
			if(radiobuttonLoadFromAssets.isSelected() && urlChoiceBox != null && !urlChoiceBox.equals(""))
				Main.logicLayer.setMainUrl("file:///android_asset/" + choiceboxHtmlFiles.getValue());
			if(radiobuttonLoadWebsite.isSelected() && !urlTextbox.equals(""))
				Main.logicLayer.setMainUrl(urlTextbox);
			Main.logicLayer.writeNecessaryXmlFiles();
			loadWebsitesAndUrls();
		}catch(Exception e) {
			System.err.println("Beim Versuch die Main-URL zu speichern, ist ein Fehler aufgetreten: " + e);
		}
	}
	
	// Functions not called by FXML
	
	public void loadHtmlFilesToListView() {
		listviewHtmlFiles.getItems().clear();
		choiceboxHtmlFiles.getItems().clear();
		for (File file : new File(Main.logicLayer.getAssetsLocation()).listFiles((dir, name) -> name.endsWith(".html"))) {
		    if (file.isFile()) {
		    	listviewHtmlFiles.getItems().add(file.getName());
		    	choiceboxHtmlFiles.getItems().add(file.getName());
		    }
		}
	}
	
	@FXML
	public void loadWebsitesAndUrls() {
		if(Main.logicLayer.getMainUrl().contains("file:///android_asset/")) {
			choiceboxHtmlFiles.setValue(Main.logicLayer.getMainUrl().replace("file:///android_asset/", ""));
			String test = Main.logicLayer.getAssetsLocation() + "/" + choiceboxHtmlFiles.getValue();
			webviewMain.getEngine().load("file:" + test);
		}else {
			textfieldWebsiteUrl.setText(Main.logicLayer.getMainUrl());
			webviewMain.getEngine().load(textfieldWebsiteUrl.getText());
			radiobuttonLoadWebsite.setSelected(true);
		}	
	}

}
