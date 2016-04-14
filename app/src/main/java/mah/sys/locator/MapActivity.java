package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import mah.sys.locator.fragments.BuildingFragment;
import mah.sys.locator.fragments.LoadingFragment;
import mah.sys.locator.fragments.RoomFragment;

public class MapActivity extends FragmentActivity implements Observer, BuildingFragment.BuildingFragmentCommunicator, RoomFragment.RoomFragmentCommunicator {

    // Variabler från sökning.
    private Bitmap
            overheadMap,
            floorMap;
    private int
            goalFloor,
            maxFloor;
    private String buildingName;

    private ServerCommunicator server;

    private FrameLayout fragmentContainer;
    private Button
            btnGoBack,
            btnGoForward;
    private TextView
            txtTopGuide,
            txtBottonGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        Log.w("Test", "Activity Started");
        if (savedInstanceState != null)
            return;

        // Skapa serverkommunikation
        server = new ServerCommunicator();

        // Hämta views
        btnGoBack = (Button) findViewById(R.id.btnGuideBack);
        btnGoForward = (Button) findViewById(R.id.btnGuideForward);
        txtTopGuide = (TextView) findViewById(R.id.txtGuideStep);
        txtBottonGuide = (TextView) findViewById(R.id.txtGuideDesc);

        // Få variabler från Intent.
        Intent intent = getIntent();
        final boolean isRoomSearch = intent.getBooleanExtra("isRoomSearch", true);
        final String searchTerm = intent.getStringExtra("searchTerm");

        // Få variabler från sharedPref
        SharedPreferences settings = getSharedPreferences("mypref", 0);
        final String chosenComplex = settings.getString("chosenComplex", null);

        ObservableRunnable<Object[]> runnable = new ObservableRunnable<Object[]>() {
            @Override
            public void run() {
                if (isRoomSearch) {
                    data = server.searchRoom(searchTerm, chosenComplex);
                    setChanged();
                    notifyObservers();
                    Log.w("Test", "Observers Notified MapActiviy");
                }
            }
        };

        runnable.addObserver(this);
        new Thread(runnable).start();
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
    public void update(Observable observable, Object data) {
        Log.w("Test", "Activity Noted");
        Object[] objects = ((ObservableRunnable<Object[]>) observable).getData();

        overheadMap = getBitmap((byte[]) objects[0]);
        floorMap = getBitmap((byte[]) objects[1]);
        goalFloor = (int) objects[2];
        maxFloor = (int) objects[3];
        buildingName = (String) objects[4];

        BuildingFragment newFragment = new BuildingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_map, newFragment).commit();
        Log.w("Test", "Building Fragment Started");
    }
}
