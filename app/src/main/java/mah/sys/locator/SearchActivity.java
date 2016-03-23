package mah.sys.locator;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    // Skapa variabler
    private Button
        btnSearchRoom,
        btnSearchProg;
    private EditText
        textSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Tildela views efter ID.
        btnSearchProg = (Button)findViewById(R.id.buttonSearchProg);
        btnSearchRoom = (Button)findViewById(R.id.buttonSearchRoom);
        textSearch = (EditText)findViewById(R.id.editTextSearch);

        // Lägg till blickListener.
        btnSearchProg.setOnClickListener(this);
        btnSearchRoom.setOnClickListener(this);
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
    }

    /**
     * Skapar ett nytt intent för MapActivity.
     * @return Intent för MapActivity.
     */
    private Intent createIntent() {
        Intent intent = new Intent(this,MapActivity.class);

        // Vad användaren sökt på.
        String searchTerm = textSearch.getText().toString();
        intent.putExtra("searchTerm",searchTerm);

        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
