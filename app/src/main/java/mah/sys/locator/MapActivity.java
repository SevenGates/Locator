package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import mah.sys.locator.fragments.BuildingFragment;
import mah.sys.locator.fragments.LevelFragment;
import mah.sys.locator.fragments.LoadingFragment;
import mah.sys.locator.fragments.RoomFragment;

public class MapActivity extends AppCompatActivity implements  View.OnClickListener, Observer, BuildingFragment.BuildingFragmentCommunicator, RoomFragment.RoomFragmentCommunicator, LevelFragment.LevelFragmentCommunicator {

    // Variabler från sökning.
    private Bitmap
            overheadMap,
            floorMap;
    private int
            goalFloor,
            maxFloor,
            roomX,
            roomY,
            doorX,
            doorY,
            corridorX,
            corridorY;
    private int[][] path;
    private String
            buildingName,
            roomName;

    // Server Com
    private ServerCommunicator server;

    // Views
    private AppCompatButton
            btnGoBack,
            btnGoForward;
    private TextView
            txtTopGuide,
            txtBottomGuide;

    // Activityn börjar med att ladda.
    private boolean loading = true;

    // Swipe hjälp-variabler.
    private final int MIN_SWIPE_DISTANCE = 200;
    private float touchX1, touchX2;

    // Framents med hjälp-variabler.
    private final Fragment[] FRAGMENTS = { new BuildingFragment(), new LevelFragment(), new RoomFragment()};
    private int currentFragmentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Ladda förra instance om den finns.
        if (savedInstanceState != null)
            return;

        // Index börjar på 0. Detta ska vara BuildingFragment.
        currentFragmentIndex = 0;

        // Skapa serverkommunikation.
        server = new ServerCommunicator();

        // Tildela views efter ID.
        btnGoBack = (AppCompatButton)findViewById(R.id.btnGuideBack);
        btnGoForward = (AppCompatButton)findViewById(R.id.btnGuideForward);
        txtTopGuide = (TextView) findViewById(R.id.txtGuideStep);
        txtBottomGuide = (TextView) findViewById(R.id.txtGuideDesc);

        // Färga knappar TODO: Detta är inte snyggt, fixa detta?
        ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.buttonColor)});
        btnGoBack.setSupportBackgroundTintList(csl);
        btnGoForward.setSupportBackgroundTintList(csl);

        // Sätt Listeners.
        btnGoBack.setOnClickListener(this);
        btnGoForward.setOnClickListener(this);

        // Få variabler från Intent. Sökterm + sökningstyp.
        Intent intent = getIntent();
        final boolean isRoomSearch = intent.getBooleanExtra("isRoomSearch", true);
        final String searchTerm = intent.getStringExtra("searchTerm");

        // Få variabler från sharedPref. Här sparas platsen man sökt på.
        SharedPreferences settings = getSharedPreferences("mypref", 0);
        final String chosenComplex = settings.getString("chosenComplex", null);

        // Skapa ny runnable för sökning i annan tråd.
        ObservableRunnable<HashMap<String,String>> runnable = new ObservableRunnable<HashMap<String,String>>() {
            @Override
            public void run() {
                if (isRoomSearch) {
                    try {
                        // Serveranrop.
                        data = server.searchRoom(searchTerm, chosenComplex);
                        setChanged();
                        notifyObservers();
                    } catch (IOException | SearchErrorException e) {
                        // Fel påträffades. Gå tillbacks till sökning med fel-meddelande.
                        Intent newIntent = new Intent(getApplicationContext(), SearchActivity.class);
                        newIntent.putExtra("Error", e);
                        startActivity(newIntent);
                    }
                }
            }
        };

        // Kör sökning.
        runnable.addObserver(this);
        new Thread(runnable).start();

        // Dölj frammåtknappen under laddningen.
        btnGoForward.setVisibility(View.GONE);

        // Starta laddningsfragment.
        LoadingFragment startFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_map, startFragment).commit();
        Log.w("Test", "Fragment Started");
    }

    // region public getFunctions
    @Override
    public Bitmap getOverheadMap() {
        return overheadMap;
    }

    @Override
    public int[][] getPath() {
        return path;
    }

    @Override
    public String getRoomName() {
        return roomName;
    }

    @Override
    public String getBuildingName() {
        return buildingName;
    }

    public Bitmap getFloorMap() {
        return floorMap;
    }

    @Override
    public int getRoomX() {
        return roomX;
    }

    @Override
    public int getRoomY() {
        return roomY;
    }

    @Override
    public int getDoorX() {
        return doorX;
    }

    @Override
    public int getDoorY() {
        return doorY;
    }

    @Override
    public int getCorridorX() {
        return corridorX;
    }

    @Override
    public int getCorridorY() {
        return corridorY;
    }

    @Override
    public int getGoalFloor() {
        return goalFloor;
    }
    //endregion

    /**
     * Gör om en byteArray till en bitmap.
     * @param bytes ByteArray som ska göras till bitmap.
     * @return Bitmap från byteArray.
     */
    private Bitmap getBitmap(byte[] bytes) {
        // Avkoda bytearrayen till en bild.
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }

    /**
     * Ändrar instruktionerna i appen.
     * @param top Text som ska stå i toppen.
     * @param bottom Text som ska stå i botten.
     */
    @Override
    public void setInstructions(String top, String bottom) {
        txtTopGuide.setText(top);
        txtBottomGuide.setText(bottom);
    }

    /**
     * Uppdaterar texten som står i knapparna.
     */
    private void updateButtonText() {
        // Hämta text som kan stå på knappar.
        String
                back = getResources().getString(R.string.arrowLeft),
                forward = getResources().getString(R.string.arrowRight),
                cancel = getResources().getString(R.string.btnText_cancel),
                finish = getResources().getString(R.string.btnText_finish);

        // Backbutton update.
        if(currentFragmentIndex == 0)
            btnGoBack.setText(cancel);
        else
            btnGoBack.setText(back);

        // Forwardbutton update.
        if(currentFragmentIndex == FRAGMENTS.length - 1) // Sista fragment
            btnGoForward.setText(finish);
        else
            btnGoForward.setText(forward);
    }

    /**
     * Bytar fragment i arrayen.
     * @param newFragmentIndex Index av det nya fragmentet.
     */
    private void switchFragment(int newFragmentIndex) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_map, FRAGMENTS[newFragmentIndex]).commit();
    }

    /**
     * OnClick-Listener
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == btnGoBack) {
            // Om första fragmenten, avbryt vägvisning.
            if(currentFragmentIndex <= 0) {
                startActivity(new Intent(this, SearchActivity.class));
            }
            // Annars gå frammåt i fragment-arrayen.
            else {
                switchFragment(--currentFragmentIndex);
                updateButtonText();
            }
        }
        else if (v == btnGoForward) {
            // Om sista fragmenten, klar med vägvisning.
            if(currentFragmentIndex >= FRAGMENTS.length - 1) {
                startActivity(new Intent(this, SearchActivity.class));
            }
            // Annars gå backåt i fragment-arrayen.
            else {
                switchFragment(++currentFragmentIndex);
                updateButtonText();
            }
        }
    }

    /**
     * OnTouch-Listener för swipes
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            // När fingret sätts ner.
            case MotionEvent.ACTION_DOWN:
                touchX1 = event.getX();
                break;
            // När fingret lämnar skärmen.
            case MotionEvent.ACTION_UP:
                touchX2 = event.getX();

                // Räkna ut delta mellan start och slutpunkt. i x-led.
                float delta = touchX2 - touchX1;

                // Om sträckan är större än minimum för swipe är det en swipe.
                if(Math.abs(delta) > MIN_SWIPE_DISTANCE) {
                    // Om appen laddar går det inte att swipea.
                    if(loading)
                        break;
                    // Backåt-swipe.
                    if(touchX2 > touchX1 && currentFragmentIndex > 0) {
                        switchFragment(--currentFragmentIndex);
                        updateButtonText();
                    }
                    // Frammåt-swipe.
                    else if(touchX1 > touchX2 && currentFragmentIndex < FRAGMENTS.length - 1) {
                        switchFragment(++currentFragmentIndex);
                        updateButtonText();
                    }
                }
            break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Kallas av NotifyObservers i Observable
     * @param observable
     * @param data
     */
    @Override
    public void update(Observable observable, Object data) {
        // Denna klassen ska bara lyssna på ObservableRunnable<HashMap<String,String>>
        HashMap<String,String> objects = ((ObservableRunnable<HashMap<String,String>>) observable).getData();

        // Hämta alla variabler från Hashmapen.
            goalFloor = Integer.valueOf(objects.get("GoalFloor").replaceAll("\\D+", ""));
            maxFloor = Integer.valueOf(objects.get("MaxFloors"));
            buildingName = objects.get("Name");
            roomName = objects.get("RoomId");

            // Koordinater
            String
                roomCoords = objects.get("RoomCoor"),
                doorCoords = objects.get("DoorCoor"),
                corridorCoors = objects.get("CorridorCoor");
            roomX = Integer.parseInt(roomCoords.split("\\.")[0]);
            roomY = Integer.parseInt(roomCoords.split("\\.")[1]);
            doorX = Integer.parseInt(doorCoords.split("\\.")[0]);
            doorY = Integer.parseInt(doorCoords.split("\\.")[1]);
            corridorX = Integer.parseInt(corridorCoors.split("\\.")[0]);
            corridorY = Integer.parseInt(corridorCoors.split("\\.")[1]);

            // Path
            int nbrOfNodes = Integer.parseInt(objects.get("nbrOfNodes"));
            Log.w("Test", Integer.toString(nbrOfNodes));
            path = new int[nbrOfNodes][2];
            String node;
            for(int i = 1; i < nbrOfNodes+1; i++){
                node = objects.get("node"+i);
                path[i-1][0] = Integer.parseInt(node.split("\\.")[0]);
                path[i-1][1] = Integer.parseInt(node.split("\\.")[1]);
            }

        // Hämta båda bytearrayer och decodea dem till Bitmaps
        byte[] overheadBytes = Base64.decode(objects.get("Overhead"),Base64.DEFAULT);
        overheadMap = getBitmap(overheadBytes);

        byte[] floorMaps = Base64.decode(objects.get("FloorMap"), Base64.DEFAULT);
        floorMap = getBitmap(floorMaps);

        // Starta den första fragmenten.
        switchFragment(currentFragmentIndex);

        // Visa frammåt-knappen.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGoForward.setVisibility(View.VISIBLE);
            }
        });

        // Laddar inte längre.
        loading = false;
    }
}
