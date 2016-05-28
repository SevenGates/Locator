package mah.sys.locator.fragments;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import mah.sys.locator.FragmentCommunicator;
import mah.sys.locator.R;
import mah.sys.locator.ZoomableImageView;

/**
 * Created by Alex on 14-Apr-16.
 */
public class RoomFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // Views
    private ZoomableImageView imgViewRoomMap;
    private Spinner sprPathSelector;

    // Callback
    private RoomFragmentCommunicator callback;

    // Variabler för vägvisning.
    private Bitmap floorMap;
    private int
            roomX,
            roomY,
            doorX,
            doorY,
            corridorX,
            corridorY;
    private int[][][] path;

    // Bild
    private Canvas canvas;
    private Bitmap image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_room, container, false);

        // Hämta callback.
        try {
            callback = (RoomFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Exception", getActivity().toString() + " måste ärva RoomFragmentCommunicator");
        }

        callback.activateForwardButton();

        // Hämta variabler.
        floorMap = callback.getFloorMap();
        roomX = callback.getRoomX();
        roomY = callback.getRoomY();
        doorX = callback.getDoorX();
        doorY = callback.getDoorY();
        corridorX = callback.getCorridorX();
        corridorY = callback.getCorridorY();
        path = callback.getPath();

        // Hitta view.
        imgViewRoomMap = (ZoomableImageView) v.findViewById(R.id.imgViewRoomMap);
        sprPathSelector = (Spinner)v.findViewById(R.id.sprPathSelector);
        sprPathSelector.setSaveEnabled(false);

        // Hämta vägnamn.
        String[] items = callback.getPathNames();

        // Instansiera Adapter till spinner.
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, items);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprPathSelector.setAdapter(spinnerArrayAdapter);
        sprPathSelector.setOnItemSelectedListener(this);
        sprPathSelector.invalidate();
        sprPathSelector.setSelection(callback.getPathNbr());

        // Rita linje och märk ut sal på kartan.
        drawLineToRoom(callback.getPathNbr());

        // Sätt instruktioner.
        String
                topText = getResources().getString(R.string.guide_room_top),
                bottomText = getResources().getString(R.string.guide_room_bottom) + " " + callback.getRoomName();
        callback.setInstructions(topText, bottomText);

        Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.toast_zoom), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,250);
        toast.show();

        return v;
    }

    /**
     * Ritar ett sträck mellan startnoden och salen via alla checkpoints.
     * Märker även ut salern med en cirkel.
     */
    private void drawLineToRoom(int pathNr) {
        // Skapa ny bild som går rita på.
        image = floorMap.copy(floorMap.getConfig(),true);
        canvas = new Canvas(image);

        // Skapa pencil.
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#BF360C"));
        paint.setStrokeWidth(5);

        // Hjälpvariabler för att öka läsligheten av nedanstående algoritm.
        int[][] chosenPath = path[pathNr];
        int
            length = chosenPath.length,
            X = 0,
            Y = 1;

        // Rita ut vägen mellan noder.
        for(int i = 0; i < length-1; i++)
            canvas.drawLine(chosenPath[i][X],chosenPath[i][Y],chosenPath[i+1][X],chosenPath[i+1][Y], paint);

        // Rita från sista noden till korridor -> dörr -> rum
        canvas.drawLine(chosenPath[length - 1][X], chosenPath[length - 1][Y], corridorX, corridorY, paint);
        canvas.drawLine(corridorX, corridorY, doorX, doorY, paint);
        canvas.drawLine(doorX, doorY, roomX, roomY, paint);

        // Rita cirklar vid start och stop.
        canvas.drawCircle(roomX, roomY, 13, paint);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(chosenPath[0][X], chosenPath[0][Y], 10, paint);

        // Sätt ut den nya bilden.
        imgViewRoomMap.setImageBitmap(image);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Rita om vägen.
        Log.w("Log","Redrawing for path " + position);
        callback.setPathNbr(position);
        drawLineToRoom(callback.getPathNbr());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.w("Log", "Nothing");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        floorMap = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        floorMap = null;
    }
    /**
     * Kommunikation med activity.
     */
    public interface RoomFragmentCommunicator extends FragmentCommunicator {
        Bitmap getFloorMap();
        String getRoomName();
        int getRoomX();
        int getRoomY();
        int getDoorX();
        int getDoorY();
        int getCorridorX();
        int getCorridorY();
        int[][][] getPath();
        String[] getPathNames();
        int getPathNbr();
        void setPathNbr(int path);
    }
}
