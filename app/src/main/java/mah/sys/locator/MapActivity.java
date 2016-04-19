package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import mah.sys.locator.fragments.BuildingFragment;
import mah.sys.locator.fragments.LoadingFragment;
import mah.sys.locator.fragments.RoomFragment;

public class MapActivity extends FragmentActivity implements Observer, BuildingFragment.BuildingFragmentCommunicator, RoomFragment.RoomFragmentCommunicator, View.OnClickListener {

    // Variabler från sökning.
    private Bitmap
            overheadMap,
            floorMap;
    private int
            goalFloor,
            maxFloor;
    private double
            roomCoords,
            doorCoords,
            corridorCoords;

    private String
            buildingName,
            roomName;

    private ServerCommunicator server;

    private FrameLayout fragmentContainer;
    private Button
            btnGoBack,
            btnGoForward;
    private TextView
            txtTopGuide,
            txtBottonGuide;

    private final Fragment[] FRAGMENTS = { new BuildingFragment(), new RoomFragment()};
    private int currentFragmentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        Log.w("Test", "Activity Started");
        if (savedInstanceState != null)
            return;

        // Index börjar på 0.
        currentFragmentIndex = 0;

        // Skapa serverkommunikation.
        server = new ServerCommunicator();

        // Hämta views.
        btnGoBack = (Button) findViewById(R.id.btnGuideBack);
        btnGoForward = (Button) findViewById(R.id.btnGuideForward);
        txtTopGuide = (TextView) findViewById(R.id.txtGuideStep);
        txtBottonGuide = (TextView) findViewById(R.id.txtGuideDesc);

        // Sätt Listeners.
        btnGoBack.setOnClickListener(this);
        btnGoForward.setOnClickListener(this);

        // Få variabler från Intent.
        Intent intent = getIntent();
        final boolean isRoomSearch = intent.getBooleanExtra("isRoomSearch", true);
        final String searchTerm = intent.getStringExtra("searchTerm");

        // Få variabler från sharedPref.
        SharedPreferences settings = getSharedPreferences("mypref", 0);
        final String chosenComplex = settings.getString("chosenComplex", null);

        // Gör sökning.
        ObservableRunnable<HashMap<String,String>> runnable = new ObservableRunnable<HashMap<String,String>>() {
            @Override
            public void run() {
                if (isRoomSearch) {
                    try {
                        data = server.searchRoom(searchTerm, chosenComplex);
                        setChanged();
                        notifyObservers();
                        Log.w("Test", "Observers Notified MapActiviy");
                    } catch (IOException e) {
                        Log.w("Test", "Connection Error!");
                        // TODO: Error msg, måste fixas i activity, inte från tråden.
                    }
                }
            }
        };

        runnable.addObserver(this);
        new Thread(runnable).start();

        // Stäng av frammåtknappen under laddningen.
        btnGoBack.setEnabled(true);
        btnGoForward.setEnabled(false);

        // Starta laddningsfragment.
        LoadingFragment startFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_map, startFragment).commit();
        Log.w("Test", "Fragment Started");
    }

    private Bitmap getBitmap(byte[] bytes) {
        // Avkoda bytearrayen till en bild.
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Log.w("BitmapTest", String.valueOf(bitmap.getWidth()));
        return bitmap;
    }

    @Override
    public Bitmap getOverheadMap() {
        return overheadMap;
    }

    @Override
    public String getBuildingName() {
        return buildingName;
    }

    @Override
    public void setIntructions(String top, String bottom) {
        txtTopGuide.setText(top);
        txtBottonGuide.setText(bottom);
    }

    public Bitmap getFloorMap() {
        return floorMap;
    }

    @Override
    public double getRoomCoords() {
        return 0.0;
    }

    @Override
    public double getDoorCoords() {
        return 0;
    }

    @Override
    public double getCorridorCoords() {
        return 0;
    }

    @Override
    public void onClick(View v) {
        if (v == btnGoBack) {
            if(currentFragmentIndex <= 0) {
                //TODO: Cancel Search
            }
            else
                switchFragment(--currentFragmentIndex);
        }
        else if (v == btnGoForward) {
            if(currentFragmentIndex >= FRAGMENTS.length - 1) {
                //TODO: Finish Search
            }
            else
                switchFragment(++currentFragmentIndex);
        }
    }

    private void switchFragment(int newFragmentIndex) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_map, FRAGMENTS[newFragmentIndex]).commit();
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.w("Test", "Activity Noted");
        HashMap<String,String> objects = ((ObservableRunnable<HashMap<String,String>>) observable).getData();
        // Hämta alla variabler från Hashmapen.
        goalFloor = Integer.valueOf(objects.get("GoalFloor").replaceAll("\\D+", ""));
        maxFloor = Integer.valueOf(objects.get("MaxFloors"));
        roomCoords = Double.valueOf(objects.get("RoomCoor"));
        doorCoords = Double.valueOf(objects.get("DoorCoor"));
        corridorCoords = Double.valueOf(objects.get("CorridorCoor"));
        buildingName = objects.get("Name");
        roomName = objects.get("roomId");

        // Hämta båda bilder och decodea dem till Bitmaps
        byte[] overheadBytes = Base64.decode(objects.get("Overhead"),Base64.DEFAULT);
        overheadMap = getBitmap(overheadBytes);

        byte[] floorMaps = Base64.decode(objects.get("FloorMap"), Base64.DEFAULT);
        floorMap = getBitmap(floorMaps);
        // Starta den första fragmenten.
        switchFragment(currentFragmentIndex);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGoForward.setEnabled(true);
            }
        });

        Log.w("Test", "Building Fragment Started");
    }
}
