package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class SplashActivity extends AppCompatActivity implements View.OnClickListener, Observer {

    private AppCompatButton btnChoose;
    private DelayAutoCompleteTextView searchField;
    private ServerCommunicator server;
    private TextView txtError;

    private TransparentProgressDialog loading;


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

        server = new ServerCommunicator();

        // Ladda knapp.
        btnChoose = (AppCompatButton)findViewById(R.id.buttonChoose);
        btnChoose.setOnClickListener(this);

        // Färga knapp. TODO: Detta är inte snyggt, fixa detta?
        ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.buttonColor)});
        btnChoose.setSupportBackgroundTintList(csl);

        // Laddda feltext.
        txtError = (TextView)findViewById(R.id.txtErrorSplash);

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
                // Ta bort felmeddelanden.
                txtError.setText("");

                String complex = (String)adapterView.getItemAtPosition(position);
                searchField.setText(complex);
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
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
            txtError.setText(R.string.error_no_complex);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnChoose) {
            // Ta bort felmeddelanden.
            txtError.setText("");

            // Konfirmera valet med server.
            final String text = searchField.getText().toString();
            loading.show();
            ObservableRunnable<Boolean> runnable = new ObservableRunnable<Boolean>() {
                @Override
                public void run() {
                    try {
                        data = server.confirmComplex(text);
                        setChanged();
                        notifyObservers();
                    } catch (final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtError.setText(getErrorText(e.getClass().toString().substring(6)));
                            }
                        });
                    } finally {
                        loading.dismiss();
                    }
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

            // Ta bort felmeddelanden.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtError.setText("");
                }
            });
        } catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtError.setText(getErrorText(e.getClass().toString().substring(6)));
                }
            });
        }
        return strings;
    }

    private String getErrorText(String error) {
        Log.w("Test", error);
        switch (error) {
            case "java.io.EOFException":
                return "";
            case "java.net.ConnectException":
            case "java.net.SocketTimeoutException":
                return getResources().getString(R.string.error_offline);
            default:
                return getResources().getString(R.string.error_unknown);
        }
    }
}
