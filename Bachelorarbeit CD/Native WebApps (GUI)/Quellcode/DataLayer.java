import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataLayer {
	private String projectPath, javaMainPath, activityMainPath;
	private ArrayList<String> javaMain, projectConfig, permissions;
	private Document androidManifest, styles, colors, strings, activityMain;
	
	DataLayer(){
		permissions = new ArrayList<String>();
		permissions.add("android.permission.INTERNET");
		permissions.add("android.permission.ACCESS_NETWORK_STATE");
		permissions.add("android.permission.VIBRATE");
		permissions.add("android.permission.CAMERA");
		permissions.add("android.permission.FLASHLIGHT");
		permissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
		permissions.add("android.permission.READ_EXTERNAL_STORAGE");
		permissions.add("android.permission.ACCESS_COARSE_LOCATION");
		permissions.add("android.permission.ACCESS_FINE_LOCATION");
	}

	//----------------------------------------------------------------------------
	// XML-Functions
	//----------------------------------------------------------------------------

	public Document readXmlFile(String filePath) {
		Document doc = null;
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(filePath);
			//Tidy-up Code
			doc.getDocumentElement().normalize();
			XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//text()[normalize-space(.) = '']");
			NodeList blankTextNodes = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);
			//End of Tidy-Up Code
			for (int i = 0; i < blankTextNodes.getLength(); i++) {
			     blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
			}
			
		} catch (Exception e) {
			System.err.println("Beim Lesen der XML-Datei ist ein Fehler aufgetreten: " + e);
		}
		
		return doc;
	}
	
	public Document appendNewNode(Document tempDoc, String motherNodeName, String childNodeName, String attributeName, String attributeValue, String nodeContent) {
		Node resources = tempDoc.getElementsByTagName(motherNodeName).item(0);
		Node newNode = tempDoc.createElement(childNodeName);
		((Element) newNode).setAttribute(attributeName, attributeValue);
		newNode.setTextContent(nodeContent);
		resources.appendChild(newNode);
		
		return tempDoc;
	}
	
	public String getContentFromNode(Document tempDoc, String motherNodeName, String childNodeName, String attributeName, String expectedAttribute) {
		NodeList tempNodeList = tempDoc.getElementsByTagName(motherNodeName).item(0).getChildNodes();
		for (int i = 0; i < tempNodeList.getLength(); i++)
			if (tempNodeList.item(i).getNodeName().equals(childNodeName))
				if (((Element) tempNodeList.item(i)).getAttribute(attributeName).equals(expectedAttribute)) 
					return tempNodeList.item(i).getTextContent();
		return "";
	}
	
	public String getAttributeFromNode(Document tempDoc, String motherNodeName, String nodeName, String attributeName) {
		NodeList tempNodeList = tempDoc.getElementsByTagName(motherNodeName).item(0).getChildNodes();
		for (int i = 0; i < tempNodeList.getLength(); i++)
			if (tempNodeList.item(i).getNodeName().equals(nodeName))
				 return ((Element) tempNodeList.item(i)).getAttribute(attributeName);
		return "";
	}
	
	public String getAttributeFromOtherNodeAttribute(Document tempDoc, String motherNodeName, String nodeName, String otherAttributeName, String otherAttributeContent, String neededAttributeName) {
		NodeList tempNodeList = tempDoc.getElementsByTagName(motherNodeName).item(0).getChildNodes();
		for (int i = 0; i < tempNodeList.getLength(); i++)
			if (tempNodeList.item(i).getNodeName().equals(nodeName) && ((Element) tempNodeList.item(i)).getAttribute(otherAttributeName).contentEquals(otherAttributeContent))
				 return ((Element) tempNodeList.item(i)).getAttribute(neededAttributeName);
		return "";
	}
	
	public void setContentByAttribute(Document tempDoc, String motherNodeName, String nodeName, String attributeName, String attributeContent, String newContent) {
		NodeList tempNodeList = tempDoc.getElementsByTagName(motherNodeName).item(0).getChildNodes();
		for (int i = 0; i < tempNodeList.getLength(); i++)
			if (tempNodeList.item(i).getNodeName().equals(nodeName) && ((Element) tempNodeList.item(i)).getAttribute(attributeName).equals(attributeContent))
				 tempNodeList.item(i).setTextContent(newContent);
	}
	
	public void setAttributeByOtherAttribute(Document tempDoc, String motherNodeName, String nodeName, String attributeName, String attributeContent, String newAttributeName, String newAttributeContent) {
		NodeList tempNodeList = tempDoc.getElementsByTagName(motherNodeName).item(0).getChildNodes();
		for (int i = 0; i < tempNodeList.getLength(); i++)
			if (tempNodeList.item(i).getNodeName().equals(nodeName) && ((Element) tempNodeList.item(i)).getAttribute(attributeName).equals(attributeContent))
				 ((Element) tempNodeList.item(i)).setAttribute(newAttributeName, newAttributeContent);
	}
	
	public boolean proofIfAttributeExists(Document tempDoc, String motherNodeName, String childNodeName, String attributeName, String attributeContent) {
		NodeList tempNodeList = tempDoc.getElementsByTagName(motherNodeName).item(0).getChildNodes();
		for (int i = 0; i < tempNodeList.getLength(); i++) {
			Node tempNode = tempNodeList.item(i);
			if (tempNode.getNodeName().equals(childNodeName) && ((Element) tempNode).getAttribute(attributeName).contentEquals(attributeContent))
				return true;
		}
		return false;
	}
	
	public void removeNodeByAttribute(Document tempDoc, String motherNodeName, String childNodeName, String attributeName, String attributeContent) {
		NodeList tempNodeList = tempDoc.getElementsByTagName(motherNodeName).item(0).getChildNodes();
		for (int i = 0; i < tempNodeList.getLength(); i++) {
			Node tempNode = tempNodeList.item(i);
			if (tempNode.getNodeName().equals(childNodeName) && ((Element) tempNode).getAttribute(attributeName).contentEquals(attributeContent))
				tempNode.getParentNode().removeChild(tempNode);	
		}
	}
	
	public void saveXmlToFile(String filePath, Document tempDoc) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(tempDoc);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult result = new StreamResult(new File(filePath));
			transformer.transform(source, result);
		}catch(Exception e) {
			System.err.println(e);
		}
	}
	
	//----------------------------------------------------------------------------
	//Textdateien Lesen/Schreiben
	//----------------------------------------------------------------------------

	public ArrayList<String> readTextFromFile(String fileLocation){
		ArrayList<String> tempArrayList = new ArrayList<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileLocation));
			String tempString;
			
			while ((tempString = br.readLine()) != null) 
			    tempArrayList.add(tempString);
			
			br.close();
		} catch (Exception e) {
			System.err.println("Ein Fehler ist beim Lesen der Datei aufgetreten: " + e);
		} 
		
		return tempArrayList;
	}
	
	public void writeTextToFile(String fileLocation, ArrayList<String> value) {
		BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(fileLocation)));
            for (String line: value)
                writer.write(line + "\n");
            
        } catch (Exception e) {
        	System.err.println("Ein Fehler ist beim Schreiben in die Datei aufgetreten: " + e);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            	System.err.println("Ein Fehler ist beim Schlie\u00dfen des BufferedWriters aufgetreten: " + e);
            }
        }
	}
	
	//----------------------------------------------------------------------------
	//Grundlegende Setter/Getter-Methoden
	//----------------------------------------------------------------------------
	
	public void setProjectPath(String newProjectPath) {
		this.projectPath = newProjectPath;
	}
	
	public String getProjectPath() {
		return this.projectPath;
	}
	
	public void setJavaMainPath(String newJavaMainPath) {
		this.javaMainPath = newJavaMainPath; 
	}
	
	public String getJavaMainPath() {
		return this.javaMainPath;
	}
	
	public void setAcivitiyMainPath(String newActivityMainPath) {
		this.activityMainPath = newActivityMainPath; 
	}
	
	public String getActivityMainPath() {
		return this.activityMainPath;
	}
	
	public void setAndroidManifest(Document newAndroidManifest) {
		this.androidManifest = newAndroidManifest;
	}
	
	public Document getAndroidManifest(){
		return this.androidManifest;
	}
	
	public void setStyles(Document newStyles) {
		this.styles = newStyles;
	}
	
	public Document getStyles(){
		return this.styles;
	}
	
	public void setColors(Document newColors) {
		this.colors = newColors;
	}
	
	public Document getColors(){
		return this.colors;
	}
	
	public void setStrings(Document newStrings) {
		this.strings = newStrings;
	}
	
	public Document getStrings(){
		return this.strings;
	}
	
	public void setActivityMain(Document newActivityMain) {
		this.activityMain = newActivityMain;
	}
	
	public Document getActivityMain(){
		return this.activityMain;
	}
	
	public void setJavaMain(ArrayList<String> newJavaMain) {
		this.javaMain = newJavaMain;
	}
	
	public ArrayList<String> getJavaMain(){
		return this.javaMain;
	}
	
	public void setProjectConfig(ArrayList<String> newProjectConfig) {
		this.projectConfig = newProjectConfig;
	}
	
	public ArrayList<String> getProjectConfig(){
		return this.projectConfig;
	}
	
	public ArrayList<String> getPermissions(){
		return this.permissions;
	}
	
	//----------------------------------------------------------------------------
	//Andere Funktionen
	//----------------------------------------------------------------------------
	
	public void setPermission(String permission, boolean value) {
		boolean isStatus = proofIfAttributeExists(this.androidManifest, "manifest", "uses-permission", "android:name", permission);
		if (isStatus && !value)
			this.removeNodeByAttribute(this.androidManifest, "manifest", "uses-permission", "android:name", permission);
		if (!isStatus && value)
			this.appendNewNode(this.androidManifest, "manifest", "uses-permission", "android:name", permission, "");
	}
	
	public boolean[] getPermissionStates() {
		boolean[] tempBooleanArray = new boolean[permissions.size()];
		for (int i = 0; i < permissions.size(); i++)
			tempBooleanArray[i] = this.proofIfAttributeExists(this.androidManifest, "manifest", "uses-permission", "android:name", this.permissions.get(i));
		return tempBooleanArray;
	}
	
	public void setIcon(String imagePath, String iconFileName) {
		String iconPath = this.getProjectPath() + "/app/src/main/res";
		try {
			Image image = ImageIO.read(new File(imagePath));
			//Save Icons
		    ImageIO.write(convertImageToBufferedImage(image.getScaledInstance(192, 192, java.awt.Image.SCALE_SMOOTH)), "png", new File(iconPath + "/mipmap-xxxhdpi/" + iconFileName + ".png"));
		    ImageIO.write(convertImageToBufferedImage(image.getScaledInstance(144, 144, java.awt.Image.SCALE_SMOOTH)), "png", new File(iconPath + "/mipmap-xxhdpi/" + iconFileName + ".png"));
		    ImageIO.write(convertImageToBufferedImage(image.getScaledInstance(96, 96, java.awt.Image.SCALE_SMOOTH)), "png", new File(iconPath + "/mipmap-xhdpi/" + iconFileName + ".png"));
		    ImageIO.write(convertImageToBufferedImage(image.getScaledInstance(72, 72, java.awt.Image.SCALE_SMOOTH)), "png", new File(iconPath + "/mipmap-hdpi/" + iconFileName + ".png"));
		    ImageIO.write(convertImageToBufferedImage(image.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH)), "png", new File(iconPath + "/mipmap-mdpi/" + iconFileName + ".png"));
		} catch (IOException e) {
			System.out.println("Bei dem Versuch ein Bild zu Laden ist ein Problem aufgetreten: " + e);
		}
	}
	
	public String getColor(String colorType) {
		return this.getContentFromNode(this.colors, "resources", "color", "name", colorType);
	}
	
	public void setColor(String colorType, String newColor) {
		this.setContentByAttribute(this.colors, "resources", "color", "name", colorType, newColor);
	}
	
	public String getString(String stringName) {
		return this.getContentFromNode(this.strings, "resources", "string", "name", stringName);
	}
	
	public void setString(String stringName, String newString) {
		this.setContentByAttribute(this.strings, "resources", "string", "name", stringName, newString);
	}
	
	public String getAppTheme() {
		return this.getAttributeFromOtherNodeAttribute(this.styles, "resources", "style", "name", "AppTheme", "parent");
	}
	
	public void setAppTheme(String newTheme) {
		this.setAttributeByOtherAttribute(this.styles, "resources", "style", "name", "AppTheme", "parent", newTheme);
	}
	
	public void addCodeToJavaMain(int lineId, String code) {
		this.javaMain.add(lineId, code);
	}
	
	public void removeCodeFromJava(int startLineId, int endLineId) {
		for (int i = startLineId; i <= endLineId; i++) {
			this.javaMain.remove(i);
		}
	}
	
	public static BufferedImage convertImageToBufferedImage(Image image) {
	    BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    Graphics bg = bufferedImage.getGraphics();
	    bg.drawImage(image, 0, 0, null);
	    bg.dispose();
	    return bufferedImage;
	}
	
	public void writeTextToFile(String fileName, String text) {
		try (PrintWriter out = new PrintWriter(fileName)) {
	    	out.println(text);
	    	out.close();
		}catch(Exception e) {
			System.err.println("Ein Fehler ist aufgetreten: " + e);
		}
	}
	
	//----------------------------------------------------------------------------
	//Funktionen bei Initialisierung
	//----------------------------------------------------------------------------
	
	public void removeHelloWorldTextViewFromActivityMain() {
		this.removeNodeByAttribute(this.activityMain, "androidx.constraintlayout.widget.ConstraintLayout", "TextView", "android:text", "Hello World!");
	}
	
	public void addWebViewToActivityMain(String webViewName) {
		String motherNode = "androidx.constraintlayout.widget.ConstraintLayout";
		this.appendNewNode(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "android:layout_width", "0dp");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "android:layout_height", "0dp");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "android:usesCleartextTraffic", "true");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "app:layout_constraintBottom_toBottomOf", "parent");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "app:layout_constraintEnd_toEndOf", "parent");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "app:layout_constraintHorizontal_bias", "1.0");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "app:layout_constraintStart_toStartOf", "parent");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "app:layout_constraintTop_toTopOf", "parent");
		this.setAttributeByOtherAttribute(this.activityMain, motherNode, "WebView", "android:id", "@+id/" + webViewName, "app:layout_constraintVertical_bias", "1.0");
	}
}
