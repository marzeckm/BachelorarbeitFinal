/*
    Allgemein.js
*/

function goBack(){
    Android.goBack();
}

function showToast(text){
    Android.showToast(text);
}

function showAlert(title, text){
    Android.showWarning(title, text);
}

function showError(title, text){
    Android.showError(title, text);
}

function systemOutPrintln(text){
    Android.systemOutPrintln(text);
}

function systemErrPrintln(text){
    Android.systemErrPrintln(text);
}

function systemOutPrint(text){
    Android.systemOutPrint(text);
}

function systemErrPrint(text){
    Android.systemErrPrint(text);
}

function getConnectivityStatus(){
    return Android.getConnectivityStatus();
}

function getCurrentLocation(){
    return Android.getCurrentLocation();
}

function takePhoto(){
    return Android.takePhoto();
}

function vibrate(milliseconds){
    Android.vibrate(milliseconds);
}

function vibrateShort(){
    Android.vibrate(100);
}

function vibrateMedium(){
    Android.vibrate(200);
}

function vibrateLong(){
    Android.vibrate(500);
}

function nightModeEnabled(){
    return Android.nightModeEnabled();
}

function setStatusBarColor(colorCode){
    Android.setStatusBarColor(colorCode);
}

function flashLight(value){
    Android.flashlight(value);
}

function writeFile(name, content){
    Android.writeTextToInternalStorage(name, content);
}

function readFile(name){
    return Android.readTextFromInternalStorage(name);
}

function permissionGranted(permissionName){
    return Android.checkPermission(permissionName);
}

function permissionGrantedCamera(){
    return permissionGranted("android.permission.CAMERA");
}

function permissionGrantedInternet(){
    return permissionGranted("android.permission.INTERNET");
}

function permissionGrantedAccessNetworkState(){
    return permissionGranted("android.permission.ACCESS_NETWORK_STATE");
}

function permissionGrantedGPS(){
    return permissionGranted("android.permission.gps");
}

function permissionGrantedAccessFineLocation(){
    return permissionGranted("android.permission.ACCESS_FINE_LOCATION");
}

function permissionGrantedAccessCoarseLocation(){
    return permissionGranted("android.permission.ACCESS_COARSE_LOCATION");
}

function permissionGrantedWriteExternalStorage(){
    return permissionGranted("android.permission.WRITE_EXTERNAL_STORAGE");
}

function permissionGrantedReadExternalStorage(){
    return permissionGranted("android.permission.READ_EXTERNAL_STORAGE");
}

function permissionGrantedVibrate(){
    return permissionGranted("android.permission.VIBRATE");
}

function requestPermission(permission){
    Android.requestPermission(permission);
}

function requestPermissionCamera(){
    requestPermission('android.permission.CAMERA');
}

function requestPermissionAccessFineLocation(){
    requestPermission('android.permission.ACCESS_FINE_LOCATION');
}

function requestPermissionAccessCoarseLocation(){
    requestPermission('android.permission.ACCESS_COARSE_LOCATION');
}

function requestPermissionWriteExternalStorage(){
    requestPermission('android.permission.WRITE_EXTERNAL_STORAGE');
}

function requestPermissionReadExternalStorage(){
    requestPermission('android.permission.READ_EXTERNAL_STORAGE');
}

function pushNotification(notificationTitle, notificationContent){
    Android.pushNotification(notificationTitle, notificationContent);
}

function displayRotationEnabled(value){
    Android.displayRotationEnabled(value);
}

function loadUrl(newUrl){
    callNativeFunction("loadUrl", newUrl)
}

function loadData(newData){
    callNativeFunction("loadData", newData)
}

function setPageNotFound(newUrl){
    callNativeFunction("setPageNotFoundUrl", newUrl)
}

function callNativeFunction(identifyingKeyword){
    window.location.href = "#" + identifyingKeyword;
}

function callNativeFunction(identifyingKeyword, parameters){
    var tempUrl = "#" + identifyingKeyword + "?";
    if(Array.isArray(parameters)){
        for (var i = 0; i < parameters.length; i++){
            if (i < (parameters.length - 1)) 
                tempUrl = tempUrl + parameters[i] + "&";
            else 
                tempUrl = tempUrl + parameters[i];
        }  
    }else{
        tempUrl = tempUrl + parameters;
    }

    window.location.href = tempUrl;
}