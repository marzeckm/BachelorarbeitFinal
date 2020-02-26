import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class LogicLayer {
	private DataLayer dataLayer;
	private String valuesLocation;
	private String assetsLocation;
	
	LogicLayer(String newProjectPath, String newJavaMainPath){
		this.dataLayer = new DataLayer();
		this.dataLayer.setProjectPath(newProjectPath);
		this.dataLayer.setJavaMainPath(newJavaMainPath);
		this.dataLayer.setAcivitiyMainPath(getProjectPath() + "/app/src/main/res/layout/activity_main.xml");
		this.valuesLocation = dataLayer.getProjectPath() + "/app/src/main/res/values";
		this.assetsLocation = dataLayer.getProjectPath() + "/app/src/main/assets";
		writePathsToFile(newProjectPath, newJavaMainPath);
		readNecessaryXmlFiles();
		readConfigFile();
	}
	
	public void setProjectPath(String newProjectPath) {
		this.dataLayer.setProjectPath(newProjectPath);
	}
	
	public String getProjectPath() {
		return this.dataLayer.getProjectPath();
	}
	
	public void setJavaMainPath(String newJavaMainPath) {
		this.dataLayer.setJavaMainPath(newJavaMainPath);
	}
	
	public String getJavaMainPath() {
		return this.dataLayer.getJavaMainPath();
	}
	
	public String getValuesLocation() {
		return this.valuesLocation;
	}
	
	public String getAssetsLocation() {
		return this.assetsLocation;
	}
	
	public String getIconPathNormal() {
		return this.dataLayer.getProjectPath() + "/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png";
	}
	
	public String getIconPathRound() {
		return this.dataLayer.getProjectPath() + "/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png";
	}
	
	public void setPermissions(boolean[] values){
		for (int i = 0; i < dataLayer.getPermissions().size(); i++)
			this.dataLayer.setPermission(dataLayer.getPermissions().get(i), values[i]);
	}
	
	public boolean[] getPermissionStates() {
		return this.dataLayer.getPermissionStates();
	}
	
	public void setUsesFeatures(boolean cameraFeature, boolean gpsFeature) {
		if (cameraFeature)
			if (!this.dataLayer.proofIfAttributeExists(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.camera")) {
					this.dataLayer.appendNewNode(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.camera", "");
			}
		if (gpsFeature) {
			if (!this.dataLayer.proofIfAttributeExists(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location")) {
					this.dataLayer.appendNewNode(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location", "");
					this.dataLayer.setAttributeByOtherAttribute(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location", "android:required", "true");
			}
			if (!this.dataLayer.proofIfAttributeExists(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location.gps")) {
				this.dataLayer.appendNewNode(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location.gps", "");
				this.dataLayer.setAttributeByOtherAttribute(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location.gps", "android:required", "false");
			}
			if (!this.dataLayer.proofIfAttributeExists(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location.network")) {
				this.dataLayer.appendNewNode(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location.network", "");
				this.dataLayer.setAttributeByOtherAttribute(this.dataLayer.getAndroidManifest(), "manifest", "uses-feature", "android:name", "android.hardware.location.network", "android:required", "false");
			}
		}

	}
	
	public void setColorPrimary(String newColor) {
		this.dataLayer.setColor("colorPrimary", newColor);
	}
	
	public String getColorPrimary() {
		return dataLayer.getColor("colorPrimary");
	}
	
	public void setColorPrimaryDark(String newColor) {
		this.dataLayer.setColor("colorPrimaryDark", newColor);
	}
	
	public String getColorPrimaryDark() {
		return dataLayer.getColor("colorPrimaryDark");
	}
	
	public void setColorAccent(String newColor) {
		this.dataLayer.setColor("colorAccent", newColor);
	}
	
	public String getColorAccent() {
		return dataLayer.getColor("colorAccent");
	}
	
	public void setAppTheme(String newAppTheme) {
		dataLayer.setAppTheme(newAppTheme);
	}
	
	public String getAppTheme() {
		return this.dataLayer.getAppTheme();
	}
	
	public void setIconNormal(String imagePath) {
		dataLayer.setIcon(imagePath, "ic_launcher");
	}
	
	public void setIconRound(String imagePath) {
		dataLayer.setIcon(imagePath, "ic_launcher_round");
	}
	
	public ArrayList<String> getJavaMain(){
		return this.dataLayer.getJavaMain();
	}
	
	public void setAppName(String newAppName) {
		this.dataLayer.setString("app_name", newAppName);
	}
	
	public String getAppName() {
		return this.dataLayer.getString("app_name");
	}
	
	public void setMainUrl(String newMainUrl) {
		this.dataLayer.setString("myurl", newMainUrl);
	}
	
	public String getMainUrl() {
		return this.dataLayer.getString("myurl");
	}
	
	public void readNecessaryXmlFiles() {
		this.dataLayer.setAndroidManifest(dataLayer.readXmlFile(this.dataLayer.getProjectPath() + "/app/src/main/AndroidManifest.xml"));
		this.dataLayer.setStyles(dataLayer.readXmlFile(valuesLocation + "/styles.xml"));
		this.dataLayer.setColors(dataLayer.readXmlFile(valuesLocation + "/colors.xml"));
		this.dataLayer.setStrings(dataLayer.readXmlFile(valuesLocation + "/strings.xml"));
		this.dataLayer.setActivityMain(dataLayer.readXmlFile(this.dataLayer.getActivityMainPath()));
		this.dataLayer.setJavaMain(dataLayer.readTextFromFile(dataLayer.getJavaMainPath()));
	}
	
	public void writeNecessaryXmlFiles() {
		this.dataLayer.saveXmlToFile(valuesLocation + "/strings.xml", dataLayer.getStrings());
		this.dataLayer.saveXmlToFile(valuesLocation + "/colors.xml", dataLayer.getColors());
		this.dataLayer.saveXmlToFile(valuesLocation + "/styles.xml", dataLayer.getStyles());
		this.dataLayer.saveXmlToFile(this.dataLayer.getActivityMainPath(), dataLayer.getActivityMain());
		this.dataLayer.saveXmlToFile(dataLayer.getProjectPath() + "/app/src/main/AndroidManifest.xml", dataLayer.getAndroidManifest());
		this.dataLayer.writeTextToFile(dataLayer.getJavaMainPath(), dataLayer.getJavaMain());
	}
	
	public void readConfigFile() {
		this.dataLayer.setProjectConfig(dataLayer.readTextFromFile(this.dataLayer.getProjectPath() + "/NativeWebApp.config"));
	}
	
	public ArrayList<String> readHtmlFile(String fileName) {
		return dataLayer.readTextFromFile(fileName);
	}
	
	public void writePathsToFile(String projectPath, String javaMainPath) {
		dataLayer.writeTextToFile("paths.sav", (projectPath + "\n" + javaMainPath));
	}
	
	public void addToStringsXml() {
		dataLayer.appendNewNode(this.dataLayer.getStrings(), "resources", "string", "name", "myurl", "");
	}

	public void prepareActivityMain(String webViewName) {
		this.dataLayer.removeHelloWorldTextViewFromActivityMain();
		this.dataLayer.addWebViewToActivityMain(webViewName);
	}
	
	public boolean getConfigValue(String configName) {
		for(String line : this.dataLayer.getProjectConfig())
			if(line.contains(configName) && line.contains("true")) return true;
		return false;
	}
	
	public void copyFrameworkClasses(String webViewName) {
		try {
			Files.copy(Paths.get("res/js"), Paths.get(this.dataLayer.getProjectPath() + "/app/src/main/assets/sources/js"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("res/js/Allgemein.js"), Paths.get(this.dataLayer.getProjectPath() + "/app/src/main/assets/sources/js/Allgemein.js"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("res/js/hammer.min.js"), Paths.get(this.dataLayer.getProjectPath() + "/app/src/main/assets/sources/js/hammer.min.js"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("res/js/bootstrap.bundle.js"), Paths.get(this.dataLayer.getProjectPath() + "/app/src/main/assets/sources/js/bootstrap.bundle.js"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("res/js/jquery-3.4.1.min.js"), Paths.get(this.dataLayer.getProjectPath() + "/app/src/main/assets/sources/js/jquery-3.4.1.min.js"), StandardCopyOption.REPLACE_EXISTING);

			Files.copy(Paths.get("res/css"), Paths.get(this.dataLayer.getProjectPath() + "/app/src/main/assets/sources/css"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("res/css/bootstrap.css"), Paths.get(this.dataLayer.getProjectPath() + "/app/src/main/assets/sources/css/bootstrap.css"), StandardCopyOption.REPLACE_EXISTING);

		}catch(Exception e) {
			System.err.println("Ein Fehler beim Kopieren der Ressourcen-Dateien ist aufgetreten: " + e);
		}
		
		ArrayList<String> mainFrameworkClass = this.dataLayer.readTextFromFile("res/JavaScriptJavaConnector.java");
		ArrayList<String> preparedActivityMain = this.dataLayer.readTextFromFile("res/MainActivity.java");
		ArrayList<String> tempActivityMain = this.dataLayer.getJavaMain();
		
		for (String line : tempActivityMain)
			if (line.startsWith("package")) {
				mainFrameworkClass.add(0, line);
				preparedActivityMain.add(0, line);
			}
		
		for (int i = 0; i<preparedActivityMain.size(); i++)
			preparedActivityMain.set(i, preparedActivityMain.get(i).replace("[webViewName]", webViewName));
		
		this.dataLayer.setJavaMain(preparedActivityMain);
		this.dataLayer.writeTextToFile(this.dataLayer.getJavaMainPath().replace("MainActivity.java", "") + "/JavaScriptJavaConnector.java", mainFrameworkClass);
		this.dataLayer.writeTextToFile(this.dataLayer.getJavaMainPath(), preparedActivityMain);

	}
}
