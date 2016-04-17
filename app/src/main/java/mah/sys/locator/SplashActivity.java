package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class SplashActivity extends AppCompatActivity implements View.OnClickListener, Observer {

    private Button btnChoose;
    private DelayAutoCompleteTextView searchField;
    private ServerCommunicator server;

    private TransparentProgressDialog loading;
    private Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

/*      TODO: FIXA DETTA
        // Ladda från tidigare valt complex.
        SharedPreferences settings = getSharedPreferences("mypref",0);
        String choosenComplex = settings.getString("choosenComplex","NO_COMPLEX");
        if(!choosenComplex.equals("NO_COMPLEX") ){
            startActivity(new Intent(this,SearchActivity.class));
        }
    */
        loading = new TransparentProgressDialog(this);
        runnable = new Runnable() {
            @Override
            public void run() {
                if(loading.isShowing())
                    loading.dismiss();
            }
        };

        server = new ServerCommunicator();

        // Ladda Knapp.
        btnChoose = (Button)findViewById(R.id.buttonChoose);
        btnChoose.setOnClickListener(this);

        // Ladda sökfält.
        searchField = (DelayAutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        searchField.setThreshold(2);
        searchField.setAdapter(new ComplexAutoCompleteAdapter(this));
        searchField.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));

        // Sökfält-Listener.
        searchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String complex = (String)adapterView.getItemAtPosition(position);
                searchField.setText(complex);
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        loading.dismiss();
        boolean confirmed = ((ObservableRunnable<Boolean>)observable).getData();
        if(confirmed) {
            SharedPreferences settings = getSharedPreferences("mypref",0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("chosenComplex", searchField.getText().toString());
            editor.commit();
            Log.w("Test", searchField.getText().toString() + " stored!");

            // Byta aktivitet.
            startActivity(new Intent(this, SearchActivity.class));
        } else {
            //TODO: FIX ERROR MSG
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnChoose) {
            // Konfirmera valet med server.
            final String text = searchField.getText().toString();
            loading.show();
            ObservableRunnable<Boolean> runnable = new ObservableRunnable<Boolean>() {
                @Override
                public void run() {
                    try {
                        data = server.confirmComplex(text);
                    } catch (IOException e) {
                        Log.w("Test", "Connection Error!");
                        // TODO: Error msg, måste fixas i activity, inte från tråden.
                    }
                    setChanged();
                    notifyObservers();
                }
            };
            runnable.addObserver(this);
            new Thread(runnable).start();
        }
    }

    public List<String> getComplexes(String searchString) {
        List<String> strings = null;
        try {
            strings = server.getComplexes(searchString);
        } catch (IOException e) {
            Log.w("Test", "Connection Error!");
            // TODO: Error msg
        }
        return strings;
    }
}
