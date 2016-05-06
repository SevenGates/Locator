package mah.sys.locator.fragments;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import mah.sys.locator.FragmentCommunicator;
import mah.sys.locator.R;
import mah.sys.locator.ZoomableImageView;

/**
 * Created by Alex on 14-Apr-16.
 */
public class RoomFragment extends Fragment {

    // Views
    private ZoomableImageView imgViewRoomMap;

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
    private int[][] path;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room, container, false);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        // Hämta callback.
        try {
            callback = (RoomFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Test", getActivity().toString() + " måste ärva RoomFragmentCommunicator");
        }

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
        imgViewRoomMap = (ZoomableImageView) getView().findViewById(R.id.imgViewRoomMap);

        // Rita linje och märk ut sal på kartan.
        drawLineToRoom();

        // Visa bilden.
        imgViewRoomMap.setImageBitmap(floorMap);

        // Sätt instruktioner.
        String
                topText = getResources().getString(R.string.guide_room_top),
                bottomText = getResources().getString(R.string.guide_room_bottom) + " " + callback.getRoomName();
        callback.setInstructions(topText,bottomText);
    }

    /**
     * Ritar ett sträck mellan startnoden och salen via alla checkpoints.
     * Märker även ut salern med en cirkel.
     */
    private void drawLineToRoom() {
        Bitmap image = floorMap.copy(floorMap.getConfig(),true);
        Canvas canvas = new Canvas(image);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#BF360C"));
        paint.setStrokeWidth(5);

        for(int i = 0; i < path.length-1; i++)
            canvas.drawLine(path[i][0],path[i][1],path[i+1][0],path[i+1][1], paint);
        canvas.drawLine(path[path.length-1][0],path[path.length-1][1],corridorX,corridorY, paint);
        canvas.drawLine(corridorX, corridorY, doorX, doorY, paint);
        canvas.drawLine(doorX, doorY, roomX, roomY, paint);

        canvas.drawCircle(roomX, roomY, 12, paint);
        floorMap = image;
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
        int[][] getPath();
    }
}
