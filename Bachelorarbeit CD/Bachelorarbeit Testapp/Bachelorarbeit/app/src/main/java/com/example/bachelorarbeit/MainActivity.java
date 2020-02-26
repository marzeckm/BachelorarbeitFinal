package com.example.bachelorarbeit;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.webkit.WebView;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    JavaScriptJavaConnector jsConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Don't change code from here on
        jsConnector = new JavaScriptJavaConnector((WebView)findViewById(R.id.webView1), this);
        jsConnector.loadUrl(getString(R.string.myurl));
        jsConnector.addCallableFunction(this, "onBackPressed", "onBackPressed");
        //Add your own Code after this
        jsConnector.addCallableFunction(this,"setStatusBarColor", "setStatusBarColor", new Object[] {"String"});
    }

    @Override
    public void onBackPressed(){
        if(!jsConnector.goBack()) super.onBackPressed();
    }

    public void setStatusBarColor(String color){
        if (Build.VERSION.SDK_INT >= 21)
            this.getWindow().setStatusBarColor(Color.parseColor(color));
    }
}
