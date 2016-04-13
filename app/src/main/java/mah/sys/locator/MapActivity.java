package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import java.util.Observable;
import java.util.Observer;
import mah.sys.locator.fragments.BuildingFragment;
import mah.sys.locator.fragments.LoadingFragment;

public class MapActivity extends FragmentActivity implements Observer, BuildingFragment.BuildingFragmentCommunicator {

    // Variabler från sökning.
    private Bitmap
        overheadMap,
        floorMap;
    private int
        goalFloor,
        maxFloor;
    private ServerCommunicator server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.w("Test", "Activity Started");

        server = new ServerCommunicator();

        // Få variabler från Intent.
        Intent intent = getIntent();
        final boolean isRoomSearch = intent.getBooleanExtra("isRoomSearch", true);
        final String searchTerm = intent.getStringExtra("searchTerm");

        // Få variabler från sharedPref
        SharedPreferences settings = getSharedPreferences("mypref", 0);
        final String chosenComplex = settings.getString("chosenComplex", null);

        GetAndSetObjectsRunnable runnable = new GetAndSetObjectsRunnable(isRoomSearch,searchTerm,chosenComplex);
        runnable.addObserver(this);
        Thread loadDataThread = new Thread(runnable);
        Log.w("Test", "Thread Created");
        loadDataThread.start();
        Log.w("Test", "Thread Started");
        LoadingFragment startFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,startFragment).commit();
        Log.w("Test", "Fragment Started");
    }

    private Bitmap getBitmap(byte[] bytes) {
        // Avkoda bytearrayen till en bild.
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Log.w("BitmapTest", String.valueOf(bitmap.getWidth()));
        return bitmap;
    }

    @Override
    public Bitmap fetchOverheadMap() {
        return overheadMap;
    }

    public Bitmap getFloorMap() {
        return floorMap;
    }

    public int getGoalFloor() {
        return goalFloor;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    @Override
    public void update(Observable observable, Object data) {
        Object[] objects = ((GetAndSetObjectsRunnable)observable).getObjects();

        overheadMap = getBitmap((byte[])objects[0]);
        floorMap = getBitmap((byte[])objects[1]);
        goalFloor = (int)objects[2];
        maxFloor = (int)objects[3];

        BuildingFragment startFragment = new BuildingFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,startFragment).commit();
    }

    private class GetAndSetObjectsRunnable extends Observable implements Runnable {

        private Object[] objects = new Object[4];
        private boolean isRoomSearch;
        private String
            searchTerm,
            chosenComplex;

        public GetAndSetObjectsRunnable(boolean isRoomSearch, String searchTerm, String choosenComplex) {
            this.isRoomSearch = isRoomSearch;
            this.searchTerm = searchTerm;
            this.chosenComplex = choosenComplex;
        }
        @Override
        public void run() {
            if(isRoomSearch) {
                objects = server.searchRoom(searchTerm,chosenComplex);
                setChanged();
                notifyObservers();
                Log.w("Test", "Observers Notified");
            }
        }

        public Object[] getObjects() {
           return objects;
        }

    }
}
