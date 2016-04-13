package mah.sys.locator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
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

    public List<String> getComplexes() {
        List<String> list = new ArrayList<String>();
        list.add("Malmö Högskola");
        list.add("Lunds Universitet");
        list.add("Malmö Akutmottagningen");
        list.add("Malmö Arena");
        list.add("Malmgruvan i Kiruna");
        list.add("Halmö Stadsbibliotek");
        return list;
        /*List<String> strings = new ArrayList<String>();
        try {
            // Skapa socket.
            Socket socket = new Socket(IP_ADDRESS, PORT);

            // Sätt strömmarna.
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream input = new DataInputStream(socket.getInputStream());

            // Skicka servern ett meddelande.
            output.writeUTF(GET_COMPLEX);
            output.flush();

            Log.w("Test", "Message Sent");

            int nrOfEntries = input.readInt();
            String complex;
            for (int i = 0; i < nrOfEntries; i++) {
                complex = input.readUTF();
                strings.add(complex);
            }
            // Stäng socket.
            socket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return confirm;*/
    }

    public boolean confirmComplex(String text) {
        return true;
        /*
        boolean confirm = false;
        try {
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


        } catch (IOException e) {
            e.printStackTrace();
        }

        return confirm;
        */
    }

    public Object[] searchRoom(final String searchTerm, final String choosenComplex) {
        Object[] objects = new Object[4];
        try {
            // Skapa socket.
            Socket socket = new Socket(IP_ADDRESS, PORT);

            // Sätt strömmarna.
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream input = new DataInputStream(socket.getInputStream());

            // Skicka servern ett meddelande.
            output.writeUTF(SEARCH_ROOM + searchTerm + "," + choosenComplex);
            output.flush();

            Log.w("Test", "Message Sent");

            // Skapa hjälpvariabler
            int arrayLength;
            byte[] bytes;

            // Första bitmapen
            arrayLength = input.readInt();
            bytes = new byte[arrayLength];
            if (arrayLength > 0)
                input.readFully(bytes);
            objects[0] = bytes;

            Log.w("Test", "Första Bitmap läst");

            // Andra bitmapen
            arrayLength = input.readInt();
            bytes = new byte[arrayLength];
            if (arrayLength > 0)
                input.readFully(bytes);
            objects[1] = bytes;

            Log.w("Test", "Andra Bitmap läst");

            // Mål våning
            objects[2] = input.readInt();

            Log.w("Test", "Första int läst");

            // Max våningar
            objects[3] = input.readInt();

            Log.w("Test", "Andra int läst");

            // Stäng socket.
            socket.close();


        } catch (IOException e) {
            e.printStackTrace();
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
