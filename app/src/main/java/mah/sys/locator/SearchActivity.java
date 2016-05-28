package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Locale;
import android.content.Context;
import android.widget.Toast;


public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    // Views
    private AppCompatButton
        btnSearchRoom,
        btnSearchProg,
        btnChangePlace;
    private EditText
        textSearch;
    private TextView
        txtError,
        txtComplex;

    private ImageButton
        btnSwe,
        btnEng;

    private Toast errorToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Ladda förra instance om den finns.
        if (savedInstanceState != null)
            return;

        // Tildela views efter ID.
        btnSearchProg = (AppCompatButton)findViewById(R.id.buttonSearchProg);
        btnSearchRoom = (AppCompatButton)findViewById(R.id.buttonSearchRoom);
        btnChangePlace = (AppCompatButton)findViewById(R.id.buttonChangePlace);
        textSearch = (EditText)findViewById(R.id.editTextSearch);
        txtError = (TextView)findViewById(R.id.txtErrorSearch);
        txtComplex = (TextView)findViewById(R.id.txtAppPlace);
        btnSwe = (ImageButton)findViewById(R.id.btnSwe);
        btnEng = (ImageButton)findViewById(R.id.btnEng);

        // Om Activityn kallas vid fel, visa felmeddelandet.
        try{
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            Exception exception = (Exception)bundle.get("Error");
            // Om custom-exception är meddelandet redan i Exceptionen.
            if(exception instanceof SearchErrorException)
                errorToast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
            else
                errorToast.makeText(this, getErrorText(exception.getClass().toString().substring(6)), Toast.LENGTH_LONG).show();

        } catch (NullPointerException e) {
            // Ingen feltext att ladda, bra!
        }

        // Ladda valt complex.
        SharedPreferences settings = getSharedPreferences("mypref",0);
        String choosenComplex = settings.getString("chosenComplex","");
        txtComplex.setText(choosenComplex);

        // Färga knappar
        ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{ContextCompat.getColor(this, R.color.buttonColor)});
        btnSearchProg.setSupportBackgroundTintList(csl);
        btnSearchRoom.setSupportBackgroundTintList(csl);
        btnChangePlace.setSupportBackgroundTintList(csl);

        // Lägg till clickListener.
        btnSearchProg.setOnClickListener(this);
        btnSearchRoom.setOnClickListener(this);
        btnChangePlace.setOnClickListener(this);

        btnSwe.setOnClickListener(this);
        btnEng.setOnClickListener(this);

        errorToast = new Toast(this);
    }

    /**
     * OnClick-Listener
     * @param v
     */
    @Override
    public void onClick(View v){

        if(v == btnSearchRoom) {
            // Byta till map aktiviteten via en sal-sökning.
            Intent intent = createIntent();
            intent.putExtra("isRoomSearch", true);
            startActivity(intent);
        }

        if(v == btnSearchProg) {
            Intent intent = createIntent();
            intent.putExtra("isRoomSearch", false);
            startActivity(intent);
        }

        if(v == btnChangePlace) {
            // Ta bort den gamla sparade platsen från mobil-minne.
            SharedPreferences settings = getSharedPreferences("mypref",0);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove("chosenComplex");
            editor.commit();

            //Byta aktivitet till splash.
            finish();
        }

        // funktion för att byta språk till engelska
        if( v== btnEng){
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());

            Intent intent = new Intent(SearchActivity.this, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        // funktion för att byta språk till svenska
        if(v==btnSwe){
            Locale locale = new Locale("se");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());

            Intent intent = new Intent(SearchActivity.this, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }
    /**
     * Skapar ett nytt intent för MapActivity.
     * @return Intent för MapActivity.
     */
    private Intent createIntent() {
        Intent intent = new Intent(this,MapActivity.class);
        // Vad användaren sökt på. Filtrera ut tecken.
        String searchTerm = textSearch.getText().toString().replaceAll("[^a-öA-Ö0-9]+","");
        intent.putExtra("searchTerm",searchTerm);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("Test","Destroy");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("Test", "Pause");
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
