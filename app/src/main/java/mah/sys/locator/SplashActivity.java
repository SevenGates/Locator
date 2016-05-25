package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class SplashActivity extends AppCompatActivity implements View.OnClickListener, Observer{

    // Views
    private AppCompatButton btnChoose;
    private DelayAutoCompleteTextView searchField;
    private TextView txtError;
    private TransparentProgressDialog loading;
    private Spinner spinnerComplex;

    // Server Com
    private ServerCommunicator server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ladda förra instance om den finns.
        if (savedInstanceState != null)
            return;

        // Ladda från tidigare valt complex.
        SharedPreferences settings = getSharedPreferences("mypref",0);
        String chosenComplex = settings.getString("chosenComplex","NO_COMPLEX");
        Log.w("Debug", chosenComplex);
        if(!chosenComplex.equals("NO_COMPLEX") ){
            startActivity(new Intent(this,SearchActivity.class));
        }

        // Hitta view.
        spinnerComplex = (Spinner)findViewById(R.id.spinnerComplex);

        // Server uppkoppling.
        server = new ServerCommunicator();

        // Skapa ny runnable för hämta complex i annan tråd.
        ObservableRunnable<List<String>> runnable = new ObservableRunnable<List<String>>() {
            @Override
            public void run() {
                try {
                    // Serveranrop.
                    data = server.getComplexes("Ma");
                    setChanged();
                    notifyObservers();
                } catch (final Exception e) {
                    // Vid fel, ändra feltexten m.h.a UI-tråden.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.w("Debug", e.toString());
                            txtError.setText(getErrorText(e.getClass().toString().substring(6)));
                        }
                    });
                }
            }
        };

        // Hämta complex.
        runnable.addObserver(this);
        new Thread(runnable).start();



        // Ladda knapp.
        btnChoose = (AppCompatButton)findViewById(R.id.buttonChoose);
        btnChoose.setOnClickListener(this);

        // Färga knapp.
        ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{ContextCompat.getColor(this, R.color.buttonColor)});
        btnChoose.setSupportBackgroundTintList(csl);

        // Laddda feltext.
        txtError = (TextView)findViewById(R.id.txtErrorSplash);
    }

    /**
     * Kallas av NotifyObservers i Observable
     * @param observable
     * @param data
     */
    @Override
    public void update(final Observable observable, Object data) {
            /*
            boolean confirmed = ((ObservableRunnable<Boolean>) observable).getData();
            if (confirmed) { // Om plats finns i DB.
                // Spara platsen i telefon-minne.
                SharedPreferences settings = getSharedPreferences("mypref", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("chosenComplex", spinnerComplex.getSelectedItem().toString());
                editor.commit();

                Log.w("Debug", spinnerComplex.getSelectedItem().toString() + " Stored!");

                // Byta aktivitet.
                startActivity(new Intent(this, SearchActivity.class));
            } else { // Om plats inte finns i DB.
                txtError.setText(R.string.error_no_complex);
            }*/

            Log.w("Debug", "Value returned");
            // Instansiera Adapter till spinner.
        List<String> items = ((ObservableRunnable<List<String>>) observable).getData();
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, items);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinnerComplex.setAdapter(spinnerArrayAdapter);
                spinnerComplex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}
                    public void onNothingSelected(AdapterView<?> parent) {}});
            }
        });

    }

    /**
     * OnClick-Listener
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == btnChoose) {
            // Ta bort felmeddelanden.
            txtError.setText("");

            // Spara platsen i telefon-minne.
            SharedPreferences settings = getSharedPreferences("mypref", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("chosenComplex", spinnerComplex.getSelectedItem().toString());
            editor.commit();

            Log.w("Debug", spinnerComplex.getSelectedItem().toString() + " Stored!");

            // Byta aktivitet.
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);

            /*
            // Visa laddningsskärm.
            loading.show();

            // Konfirmera valet med server.
            final String text = spinnerComplex.getSelectedItem().toString();

            Log.w("Debug", "Trying: " + spinnerComplex.getSelectedItem().toString());

            ObservableRunnable<Boolean> runnable = new ObservableRunnable<Boolean>() {
                @Override
                public void run() {
                    try {
                        // Serveranrop.
                        data = server.confirmComplex(text);
                        setChanged();
                        notifyObservers();
                    } catch (final Exception e) {
                        // Vid fel, ändra feltexten m.h.a UI-tråden.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtError.setText(getErrorText(e.getClass().toString().substring(6)));
                            }
                        });
                    } finally {
                        // Ta bort laddnignsskärm.
                        loading.dismiss();
                    }
                }
            };
            // Kör serverkommunikationen på ny tråd.
            runnable.addObserver(this);
            new Thread(runnable).start();

            */
        }
    }

    /**
     * Används för att söka i DB efter platser.
     * @param searchString Sträng att söka på i DB.
     * @return En lista på platser från DB.
     */
    public List<String> getComplexes(String searchString) {
        List<String> strings = null;
        try {
            // Serveranrop.
            strings = server.getComplexes(searchString);

            // Ta bort felmeddelanden. Denna metoden kallas ändast via en tråd. Så använd UI-tråden för detta.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtError.setText("");
                }
            });
        } catch (final Exception e) {
            // Vid fel, ändra feltexten m.h.a UI-tråden.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtError.setText(getErrorText(e.getClass().toString().substring(6)));
                }
            });
        }
        return strings;
    }

    /**
     * Genererar felmeddelanden.
     * @param error Felet som kastas.
     * @return En sträng att visa för användaren.
     */
    private String getErrorText(String error) {
        switch (error) {
            // EOF-inträffar när sökfältet är tomt, ska inte visa något fel.
            case "java.io.EOFException":
                return "";
            case "java.net.ConnectException":
            case "java.net.SocketTimeoutException":
                return getResources().getString(R.string.error_offline);
            // Ett oväntat fel inträffades. Detta bör undvikas och skapas egna fel för.
            default:
                return getResources().getString(R.string.error_unknown);
        }
    }
}
