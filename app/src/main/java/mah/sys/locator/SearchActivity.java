package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    // Skapa variabler
    private AppCompatButton
        btnSearchRoom,
        btnSearchProg,
        btnChangePlace;
    private EditText
        textSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Tildela views efter ID.
        btnSearchProg = (AppCompatButton)findViewById(R.id.buttonSearchProg);
        btnSearchRoom = (AppCompatButton)findViewById(R.id.buttonSearchRoom);
        btnChangePlace = (AppCompatButton)findViewById(R.id.buttonChangePlace);
        textSearch = (EditText)findViewById(R.id.editTextSearch);

        // Färga knappar TODO: Detta är inte snyggt, fixa detta?
        ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.buttonColor)});
        btnSearchProg.setSupportBackgroundTintList(csl);
        btnSearchRoom.setSupportBackgroundTintList(csl);
        btnChangePlace.setSupportBackgroundTintList(csl);

        // Lägg till clickListener.
        btnSearchProg.setOnClickListener(this);
        btnSearchRoom.setOnClickListener(this);
        btnChangePlace.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v == btnSearchRoom) {
            // Byta till map aktiviteten med en sal-sökning.
            Intent intent = createIntent();
            intent.putExtra("isRoomSearch", true);
            startActivity(intent);
        }

        if(v == btnSearchProg) {
            // TODO: IMPLEMENT (COULD)
            /*
            Intent intent = createIntent();
            intent.putExtra("isRoomSearch", false);
            startActivity(intent);
            */
        }

        if(v == btnChangePlace) {
            // Ta bort den gamla sparade platsen.
            SharedPreferences settings = getSharedPreferences("mypref",0);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove("choosenComplex");

            // Byta aktivitet.
            startActivity(new Intent(this,SplashActivity.class));
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

        return intent;
    }
}
