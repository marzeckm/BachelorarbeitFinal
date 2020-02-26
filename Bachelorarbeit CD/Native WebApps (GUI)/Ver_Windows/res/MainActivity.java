
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebView;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    JavaScriptJavaConnector jsConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Dont change code from here
        jsConnector = new JavaScriptJavaConnector((WebView)findViewById(R.id.[webViewName]), this);
        jsConnector.loadUrl(getString(R.string.myurl));
        jsConnector.addCallableFunction(this, "onBackPressed", "onBackPressed");
        //Add your own Code after this

    }

    @Override
    public void onBackPressed(){
        if(!jsConnector.goBack()) super.onBackPressed();
    }
}
