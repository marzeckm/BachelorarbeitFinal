import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.fxml.*;

public class Main extends Application {
	
	public static Stage stage;
	public static LogicLayer logicLayer;
	
	public static void main(String[] args) {		
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		 Parent fxmlLoader = FXMLLoader.load(getClass().getResource("res/layout_load_project.fxml"));
		 stage = primaryStage;
		 stage.setScene(new Scene(fxmlLoader,450,190));
		 stage.setTitle("Projekt \u00d6ffnen");
		 stage.show();
	}

}
