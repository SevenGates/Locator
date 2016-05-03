package mah.sys.locator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Alex on 09-Apr-16.
 */
public class ServerCommunicator {

    public ServerCommunicator() {

    }

    private static final String
            IP_ADDRESS = "81.227.253.254",
            GET_COMPLEX = "GCO,",
            CONFIRM_COMPLEX = "CNF,",
            SEARCH_ROOM = "SER,",
            SEARCH_PROGRAM = "SEP,";
    private static final int
            PORT = 8080;

    public List<String> getComplexes(String text) throws IOException {
        List<String> strings = new ArrayList<>();
        // Skapa socket.
        Socket socket = new Socket(IP_ADDRESS, PORT);

        // Sätt strömmarna.
        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

        // Skicka servern ett meddelande.
        output.writeUTF(GET_COMPLEX + text);
        output.flush();

        Log.w("Test", "Message Sent");
        try {
            // Läs in JSON objekt
            Object obj = input.readObject();
            JSONObject json = new JSONObject(obj.toString());

            // Antal platser
            int nrOfEntries = Integer.parseInt(json.getString("nbrOfPlaces"));
            String complex;
            for (int i = 1; i < nrOfEntries+1; i++) {
                complex = json.getString("place" + i);
                strings.add(complex);
            }
        } catch (ClassNotFoundException | JSONException e) {

        }
        // Stäng socket.
        socket.close();

        return strings;
    }

    public boolean confirmComplex(String text) throws IOException {
        boolean confirm = false;
        // Skapa socket.
        Socket socket = new Socket(IP_ADDRESS, PORT);

        // Sätt strömmarna.
        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        DataInputStream input = new DataInputStream(socket.getInputStream());

        // Skicka servern ett meddelande.
        output.writeUTF(CONFIRM_COMPLEX + text);
        output.flush();

        Log.w("Test", "Message Sent");

        confirm = input.readBoolean();

        // Stäng socket.
        socket.close();

        return confirm;
    }

    public HashMap<String,String> searchRoom(final String searchTerm, final String choosenComplex) throws IOException {
        HashMap<String,String> objects = new HashMap<>();
        try {
            // Skapa socket.
            Socket socket = new Socket(IP_ADDRESS, PORT);

            // Sätt strömmarna.
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            // Skicka servern ett meddelande.
            output.writeUTF(SEARCH_ROOM + searchTerm + "," + choosenComplex);
            output.flush();
            Log.w("Test", "Message Sent");

            Object obj = input.readObject();

            JSONObject json = new JSONObject(obj.toString());

            objects.put("Name",json.getString("name"));
            objects.put("Overhead",json.getString("path"));
            objects.put("MaxFloors",json.getString("floors"));
            objects.put("GoalFloor",json.getString("id"));
            objects.put("FloorMap",json.getString("map"));
            objects.put("RoomId",json.getString("roomid"));
            objects.put("RoomCoor",json.getString("roomCoor"));
            objects.put("DoorCoor", json.getString("doorCoor"));
            objects.put("CorridorCoor",json.getString("corridorCoor"));
            objects.put("nbrOfNodes", json.getString("nbrOfNodes"));
            Iterator<String> interator = json.keys();
            while(interator.hasNext())
            Log.w("JSON", interator.next());
            int nodes = Integer.parseInt(objects.get("nbrOfNodes"));
            for (int i = 1; i < nodes+1; i++) {
                objects.put("node" + i, json.getString("node" + i));
            }
            Log.w("JSON", json.getString("node2"));
            Log.w("JSON", json.getString("node3"));

            Log.w("Test", "Data Parsed");

            // Stäng socket.
            socket.close();


        } catch (JSONException | ClassNotFoundException e) {
            Log.w("Exception", e.toString());
        }

        return objects;
    }
}

//// Skapa en buffert att läsa in bytes i. Med hjälpvariabler.
//ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//int read;
//byte[] data = new byte[16384];
//
//// Läs från inputströmmen och lägg i bufferten.
//while ((read = input.read(data, 0, data.length)) != -1)
//        buffer.write(data, 0, read);
//        buffer.flush();
