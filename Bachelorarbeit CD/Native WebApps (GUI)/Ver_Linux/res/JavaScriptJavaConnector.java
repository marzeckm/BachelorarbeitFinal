
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.webkit.*;
import android.widget.Toast;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class JavaScriptJavaConnector {
    //Klassenvariablen-------------------------------------------------------------------------------
    private WebView webView;
    private HashMap<String, CallableFunction> callableFunctions;
    private String currentUrl = "", pageNotFoundUrl = "", lastCallbackValue = "";
    private Context context;

    //Enums -----------------------------------------------------------------------------------------
    enum NodePosition
    {
        beforebegin, afterbegin, beforeend, afterend
    }

    //Konstruktor -----------------------------------------------------------------------------------
    JavaScriptJavaConnector(WebView wv, Context con){
        this.webView = wv;
        this.context = con;
        this.callableFunctions = new HashMap<>();
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                currentUrl = url;
                if (url.contains("#")) splitUrlToCallMethod();
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                //Laden von eigenen "Page Not Found"-Seiten
                if (errorCode == -2) if (!pageNotFoundUrl.equals("")) view.loadUrl(pageNotFoundUrl);
            }
        });
        setJavaScriptEnabled(true);
        //Grundlegende Funktionen der WebView werden per "CallableFunction" im JavaScript zur Verfügung gestellt
        this.addCallableFunction(this, "loadUrl", "loadUrl", new Object[] {"String"});
        this.addCallableFunction(this, "loadData", "loadData", new Object[] {"String"});
        this.addCallableFunction(this, "setPageNotFoundUrl", "setPageNotFoundUrl", new Object[] {"String"});

        //Der WebView wird ein JavaScript-Interface hinzugefügt
        webView.addJavascriptInterface(new MyJavaScriptInterface(con), "Android");
    }

    public String getLastCallbackValue(){
        return this.lastCallbackValue;
    }

    //WebView-Optionen-------------------------------------------------------------------------------
    public void loadUrl(String url){
        this.webView.loadUrl(url);
    }

    public void loadData(String data){
        this.webView.loadData(data, "text/html", "UTF-8");
    }

    public void setDomStorageEnabled(Boolean value){
        this.webView.getSettings().setDomStorageEnabled(value);
    }

    public void setJavaScriptEnabled(Boolean value){
        this.webView.getSettings().setJavaScriptEnabled(value);
    }

    public void setAppCacheEnabled(Boolean value){
        this.webView.getSettings().setAppCacheEnabled(value);
    }

    public void setAllowFileAcess(Boolean value){
        this.webView.getSettings().setAllowFileAccess(value);
    }

    public void allowAccessFromFileURLs(Boolean value){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.webView.getSettings().setAllowUniversalAccessFromFileURLs(value);
            this.webView.getSettings().setAllowFileAccessFromFileURLs(value);
        }
    }

    public void loadCacheElseNetwork(){
        setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }

    public boolean goBack(){
        if (this.webView.canGoBack() && this.currentUrl.contains("#")) {
            this.webView.goBack();
            if (this.webView.canGoBack()){
                this.webView.goBack();
                return true;
            } else return false;
        }else if(this.webView.canGoBack()){
            this.webView.goBack();
            return true;
        }else{
            return false;
        }
    }

    //Speicherzugriff--------------------------------------------------------------------------------
    public void writeFile(Context context, String fileName, String value){
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, MODE_PRIVATE);
            fileOutputStream.write(value.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println("Beim Schreiben der Datei trat ein Fehler auf: " + e);
        }
    }

    public String readFile(Context context, String fileName) {
        String text = "", line;

        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();


            if ((line = bufferedReader.readLine()) != null) {
                if (text.equals("")) text = line;
                else text = text + "\n" + line;
            }
        } catch (Exception e) {
            System.err.println("Beim Lesen der Datei trat ein Fehler auf: " + e);
        }
        return text;
    }

    //Framework-Funktionen --------------------------------------------------------------------------
    public void executeJavaScript(final String cmd){
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(cmd, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            lastCallbackValue = s;
                        }
                    });
                } else {
                    loadUrl("javascript:" + cmd + "; void(0);");
                }
                currentUrl = url;
                if (url.contains("#")) splitUrlToCallMethod();
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (errorCode == -2) if (!pageNotFoundUrl.equals("")) view.loadUrl(pageNotFoundUrl);
            }
        });
    }

    public void setPageNotFoundUrl(String newPageNotFoundUrl){
        this.pageNotFoundUrl = newPageNotFoundUrl;
    }

    private String getHtmlElementById(String id){
        return ("document.getElementById('" + id + "')");
    }

    private String getHtmlElementsByClass(String classname){
        return ("document.getElementsByClassName('"+ classname + "')");
    }

    private String getHtmlElementsByTagName(String tagname){
        return ("document.getElementsByTagName('"+ tagname + "')");
    }

    private void doSomethingByClassName(String classname, String function){
        executeJavaScript("var availableClasses = " + getHtmlElementsByClass(classname) + "; [].forEach.call(availableClasses, function (availableClass) {availableClass." +  function + "})");
    }

    private void doSomethingByTagName(String tagname, String function){
        executeJavaScript("var availableTags = " + getHtmlElementsByTagName(tagname) + "; [].forEach.call(availableTags, function (availableTag) {availableTag." +  function + "})");
    }

    private String makeStyleAttributeValid(String attribute){
        int i = attribute.indexOf("-");

        if (i != -1)
            attribute = attribute.substring(0, i) + attribute.substring(i+1, i+2).toUpperCase() + attribute.substring(i+2);

        return attribute;
    }

    public void setCssById(String id, String attribute, String value){
        executeJavaScript(getHtmlElementById(id) + ".style." + makeStyleAttributeValid(attribute) + " = '" + value + "';");
    }

    public void setCssByClass(String classname, String attribute, String value){
        doSomethingByClassName(classname, "style." + makeStyleAttributeValid(attribute) + " = '" + value + "';");
    }

    public void setCssByTagName(String tagname, String attribute, String value){
        doSomethingByTagName(tagname, "style." + makeStyleAttributeValid(attribute) + " = '" + value + "';");
    }

    public void setHtmlAttributeById(String id, String attributeName, String value){
        executeJavaScript(getHtmlElementById(id) + "." + attributeName + " = '" + value + "';");
    }

    public void setHtmlAttributeByClass(String classname, String attributeName,  String value){
        doSomethingByClassName(classname, attributeName + " = '" + value + "';");
    }

    public void setHtmlAttributeByTagName(String tagname, String attributeName,  String value){
        doSomethingByTagName(tagname, attributeName + " = '" + value + "';");
    }

    public void setInnerHtmlById(String id, String value){
        setHtmlAttributeById(id, "innerHTML", value);
    }

    public void setInnerHtmlByClass(String classname, String value){
        setHtmlAttributeByClass(classname, "innerHTML", value);
    }

    public void setInnerHtmlByTagName(String tagname, String value){
        setHtmlAttributeByTagName(tagname, "innerHTML", value);    }

    public void setImageSourceById(String id, String url){
        setHtmlAttributeById(id, "src", url);
    }

    public void setImageSourceByClass(String classname, String url){
        setHtmlAttributeByClass(classname, "src", url);
    }

    public void appendNodeById(String motherNodeId, HtmlNode htmlNode, NodePosition nodePosition){
        executeJavaScript(getHtmlElementById(motherNodeId) + ".insertAdjacentHTML('" + nodePosition + "', '" + htmlNode.get() + "');");
    }

    public void appendNodeByClass(String motherNodeClassName, HtmlNode htmlNode, NodePosition nodePosition){
        doSomethingByClassName(motherNodeClassName, "insertAdjacentHTML('" + nodePosition + "', '" + htmlNode.get() + "');");
    }

    public void appendNodeByTagName(String motherNodeTagName, HtmlNode htmlNode, NodePosition nodePosition){
        doSomethingByTagName(motherNodeTagName, "insertAdjacentHTML('" + nodePosition + "', '" + htmlNode.get() + "');");
    }

    public void replaceNodeById(String nodeToReplaceId, HtmlNode htmlNode){
        setInnerHtmlById(nodeToReplaceId, htmlNode.get());
    }

    public void replaceNodeByClass(String nodeToReplaceClass, HtmlNode htmlNode){
        setInnerHtmlByClass(nodeToReplaceClass, htmlNode.get());
    }

    public void replaceNodeByTagName(String nodeToReplaceTagName, HtmlNode htmlNode){
        setInnerHtmlByClass(nodeToReplaceTagName, htmlNode.get());
    }

    public void removeNodeById(String id){
        executeJavaScript(getHtmlElementById(id) + ".remove();");
    }

    public void removeNodeByClass(String classname){
        executeJavaScript( "var availableClasses = " + getHtmlElementsByClass(classname) + "; while(availableClasses.length > 0){availableClasses[0].remove();}");
    }

    public void removeNodeByTagName(String tagname){
        executeJavaScript( "var availableTags = " + getHtmlElementsByTagName(tagname) + "; while(availableTags.length > 0){availableTags[0].remove();}");
    }

    //Funktionen "Callable Functions" ---------------------------------------------------------------

    public void addCallableFunction(Object classObject, String methodName, String keyword){
        this.callableFunctions.put(keyword, new CallableFunction(classObject, methodName, keyword));
    }

    public void addCallableFunction(Object classObject, String methodName, String keyword, Object[] arguments){
        this.callableFunctions.put(keyword, new CallableFunction(classObject, methodName, keyword, arguments));
    }

    public void removeCallableFunctions(String key){
        this.callableFunctions.remove(key);
    }

    private void proofCallability(String proofingKeyword, Object[] arguments){
        if (this.callableFunctions.containsKey(proofingKeyword)){
            this.callableFunctions.get(proofingKeyword).setArguments(arguments);
            this.callableFunctions.get(proofingKeyword).invokeMethod();
        }
    }

    private void splitUrlToCallMethod(){
        String callableFunctionIdentifier;
        Object[] callableFunctionParameters = new Object[0];
        if(this.currentUrl.contains("#")) {
            String tempSubString = this.currentUrl.substring(this.currentUrl.indexOf('#') + 1);
            if (tempSubString.contains("?")) {
                callableFunctionIdentifier = tempSubString.substring(0, tempSubString.indexOf('?'));
                tempSubString = tempSubString.substring(tempSubString.indexOf('?') + 1);
                callableFunctionParameters = proofTypes(tempSubString.split("&"));
            }else{
                callableFunctionIdentifier = tempSubString;
            }
            proofCallability(callableFunctionIdentifier, callableFunctionParameters);
        }
    }

    private Object[] proofTypes(Object[] callableFunctionsParameters){
        Object[] tempParameters = new Object[callableFunctionsParameters.length];
        for (int i = 0; i < callableFunctionsParameters.length; i++ ){
            Object object = callableFunctionsParameters[i];
            if (object.equals("true") || object.equals("false")){
                tempParameters[i] = ((boolean)Boolean.parseBoolean((String) object));
            }else{
                try{
                    tempParameters[i] = Integer.parseInt((String)object);
                }catch(Exception e){
                    try{
                        tempParameters[i] = Float.parseFloat((String)object);
                    }catch(Exception e2){
                        tempParameters[i] = object;
                    }
                }
            }
        }
        return tempParameters;
    }

    //Native Funktionen, die durch den JavaScript-Code erreichbar sind ------------------------------
    class MyJavaScriptInterface {
        private Context context;

        MyJavaScriptInterface(Context context){
            this.context = context;
        }

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public void systemOutPrintln(String message) {
            System.out.println(message);
        }

        @JavascriptInterface
        public void systemErrPrintln(String message) {
            System.err.println(message);
        }

        @JavascriptInterface
        public void systemOutPrint(String message) {
            System.out.print(message);
        }

        @JavascriptInterface
        public void systemErrPrint(String message) {
            System.err.print(message);
        }

        @JavascriptInterface
        public void showWarning(String warningTitle, String warningText){
            new AlertDialog.Builder(context)
                    .setTitle(warningTitle)
                    .setMessage(warningText)
                    .setPositiveButton(android.R.string.yes, null)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }

        @JavascriptInterface
        public void showError(String errorTitle, String errorText){
            new AlertDialog.Builder(context)
                    .setTitle(errorTitle)
                    .setMessage(errorText)
                    .setPositiveButton(android.R.string.yes, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        @JavascriptInterface
        public String nightModeEnabled(){
            int nightModeFlags = this.context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    return "UI_MODE_NIGHT_YES";
                case Configuration.UI_MODE_NIGHT_NO:
                    return "UI_MODE_NIGHT_NO";
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    return "UI_MODE_NIGHT_UNDEFINED";
            }
            return "null";
        }

        @JavascriptInterface
        public void displayRotationEnabled(boolean value){
            if (value)
                ((Activity) this.context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            else
                ((Activity) this.context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        @JavascriptInterface
        public String getConnectivityStatus() {
            ConnectivityManager cm = (ConnectivityManager) this.context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (null != activeNetwork) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                        return "TYPE_WIFI";

                    if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                        return "TYPE_MOBILE";
                }
                return "TYPE_NOT_CONNECTED";
            }
            return "Type_Error";
        }

        @JavascriptInterface
        public boolean checkPermission(String permission){
            return ContextCompat.checkSelfPermission(this.context, permission) == PackageManager.PERMISSION_GRANTED;
        }

        @JavascriptInterface
        public void requestPermission(String permission){
            if (!checkPermission(permission))
                ActivityCompat.requestPermissions((Activity) this.context, new String[]{permission}, 100);
        }

        @JavascriptInterface
        public String getCurrentLocation(){
            Toast.makeText(this.context, "Die Anwendung möchte Ihren Standort abfragen.", Toast.LENGTH_SHORT).show();
            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            if (checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) && checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION )) {
                try {
                    LocationManager lm = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new MyLocationListener(this.context));
                    System.out.println(location.getLatitude() + "," + location.getLongitude());
                    return (location.getLatitude() + "," + location.getLongitude());
                }catch(Exception e){
                    return "null";
                }
            }
            return "null";
        }

        @JavascriptInterface
        public String takePhoto(){
            requestPermission(Manifest.permission.CAMERA);
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (checkPermission(android.Manifest.permission.CAMERA) && checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) && checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                String photoPathAndFileName = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/DCIM/Camera/" + "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoPathAndFileName)));

                ((Activity) this.context).startActivityForResult(intent, 100);
                return photoPathAndFileName;
            }
            return "null";
        }

        @JavascriptInterface
        public void vibrate(int milliseconds){
            Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(milliseconds);
            }
        }

        @JavascriptInterface
        public void pushNotification(String notificationTitle, String notificationContent){
            NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel("NativeWebApp_ID", "NativeWebApp", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription("NativeWebApp-Notification");
                notificationManager.createNotificationChannel(notificationChannel);
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.context.getApplicationContext(), "NativeWebApp_ID")
                    .setSmallIcon(R.mipmap.ic_launcher) // Icon der Benachrichtigung
                    .setContentTitle(notificationTitle) // Benachrichtigungstitel
                    .setContentText(notificationContent)// Benachrichtigungsinhalt
                    .setAutoCancel(true); // Benachrichtigung wird nach Klick entfernt
            Intent intent = new Intent(this.context.getApplicationContext(), ((Activity) this.context).getClass());
            PendingIntent pi = PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pi);
            notificationManager.notify(0, notificationBuilder.build());
        }

        @JavascriptInterface
        public void flashlight(boolean value) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CameraManager cameraManager = (CameraManager) this.context.getSystemService(Context.CAMERA_SERVICE);

                try {
                    String cameraId = cameraManager.getCameraIdList()[0];
                    cameraManager.setTorchMode(cameraId, value);
                } catch (CameraAccessException e) {
                    System.err.println("Fehler: " + e);
                }
            }else{
                Toast.makeText(this.context, "Feature wird von Android-Version nicht unterstützt", Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void writeTextToInternalStorage(String fileName, String content){
            writeFile(this.context, fileName, content);
        }

        @JavascriptInterface
        public String readTextFromInternalStorage(String fileName){
            return readFile(this.context, fileName);
        }

        @JavascriptInterface
        public void setStatusBarColor(String color){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                ((Activity)this.context).getWindow().setStatusBarColor(Color.parseColor(color));
        }

    }

    private class MyLocationListener implements LocationListener {
        private Context context;

        MyLocationListener(Context context){
            this.context = context;
        }

        public void onLocationChanged(Location location) {
            if (location != null) {
                System.out.println("Standort wurde ermittelt!");
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(this.context, "Error onProviderDisabled", Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(this.context, "onProviderEnabled", Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(this.context, "onStatusChanged", Toast.LENGTH_LONG).show();
        }
    }
}

//Klasse der Funktionen die über Fragmente und Abfragen aufgerufen werden können --------------------
class CallableFunction{

    Object classObject;
    String methodName, keyword;
    Object[] arguments = new Object[0];

    CallableFunction(Object classObject, String methodName, String keyword){
        this.classObject = classObject;
        this.methodName = methodName;
        this.keyword = keyword;
    }

    CallableFunction(Object classObject, String methodName, String keyword, Object[] arguments){
        this.classObject = classObject;
        this.methodName = methodName;
        this.keyword = keyword;
        this.arguments = arguments;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setArguments(Object[] newArguments){
        this.arguments = newArguments;
    }

    public void invokeMethod() {
        try {
            java.lang.reflect.Method method = this.classObject.getClass().getDeclaredMethod(this.methodName, getArgumentClasses());
            method.invoke(classObject, this.arguments);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private Class<?>[] getArgumentClasses(){
        Class<?>[] argumentClasses = new Class<?>[this.arguments.length];
        for(int i = 0; i < argumentClasses.length; i++){
            argumentClasses[i] = this.arguments[i].getClass();
        }

        return argumentClasses;
    }
}

//Klasse zur einfachen Erstellung von HTML-Knoten in Java -------------------------------------------
class HtmlNode {
    private String tagname, innerHtml;
    private HashMap<String, String> cssRules, attributes;
    private ArrayList<HtmlNode> childNodes;

    HtmlNode(String tagname){
        standardNode(tagname);
    }

    HtmlNode(String tagname, String innerHtml){
        standardNode(tagname);
        this.innerHtml = innerHtml;
    }

    public String get() {
        String temp = "<" + this.tagname;

        for (Map.Entry<String, String> entry : this.attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            temp = temp.concat(" " + key + "=\"" + value + "\"");
        }

        if (this.cssRules.size() > 0) {
            temp = temp.concat(" style=\"");

            for (Map.Entry<String, String> entry : this.cssRules.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                temp = temp.concat(key + ": " + value + "; ");
            }

            temp = temp.concat("\"");
        }

        if (this.innerHtml.contentEquals("") && this.getChildNodes().size() < 1) {
            temp = temp.concat("/>");
        }else {
            temp = temp.concat(">" + this.innerHtml);
            for (HtmlNode tempNode : this.childNodes)
                temp = temp.concat(tempNode.get());
            temp = temp.concat("</" + this.tagname + ">");
        }

        return temp;
    }

    private void standardNode(String tagname) {
        this.tagname = tagname;
        this.innerHtml = "";
        this.attributes = new HashMap<>();
        this.cssRules = new HashMap<>();
        this.childNodes = new ArrayList<>();
    }

    public void setTagName(String newTagName) {
        this.tagname = newTagName;
    }

    public String getTagName() {
        return this.tagname;
    }

    public void setInnerHTML(String newInnerHtml) {
        this.innerHtml = newInnerHtml;
    }

    public String getInnerHtml() {
        return this.innerHtml;
    }

    public void setAttribute(String attributeName, String attributeContent) {
        this.attributes.put(attributeName, attributeContent);
    }

    public String getAttribute(String attributeName) {
        return this.attributes.get(attributeName);
    }

    public void setCssAttribute(String cssAttributeName, String cssAttributeContent) {
        this.cssRules.put(cssAttributeName, cssAttributeContent);
    }

    public String getCssAttribute(String cssAttributeName) {
        return this.cssRules.get(cssAttributeName);
    }

    public void appendChild(HtmlNode childNode) {
        this.childNodes.add(childNode);
    }

    public void removeChild(int id){
        this.childNodes.remove(id);
    }

    public void removeChild(HtmlNode childNode){
        this.childNodes.remove(childNode);
    }

    public ArrayList<HtmlNode> getChildNodes() {
        return this.childNodes;
    }

    public void setId(String id) {
        this.setAttribute("id", id);
    }

    public String getId() {
        return this.getAttribute("id");
    }

    public void setHtmlClass(String classname) {
        this.setAttribute("class", classname);
    }

    public String getHtmlClass() {
        return this.getAttribute("class");
    }
}

