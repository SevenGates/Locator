package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnChoose;
    DelayAutoCompleteTextView searchField;

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
    public void onClick(View v) {
        if (v == btnChoose) {
            // Konfirmera valet med server.
            String text = searchField.getText().toString();
            if(ServerCommunicator.confirmComplex(text)) {
                // Spara valet på mobilen.
                SharedPreferences settings = getSharedPreferences("mypref",0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("choosenComplex", text);
                editor.commit();

                // Byta aktivitet.
                startActivity(new Intent(this, SearchActivity.class));
            } else {
                //TODO: FIX ERROR MSG
            }
        }
    }
}
