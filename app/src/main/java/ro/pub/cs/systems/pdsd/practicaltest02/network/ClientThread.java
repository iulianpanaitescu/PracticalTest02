package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

public class ClientThread extends Thread {

    private String address;
    private int port;
    private String word;
    private TextView responseTextView;

    private Socket socket;

    public ClientThread(String address, int port, String word, TextView responseTextView) {
        this.address = address;
        this.port = port;
        this.word = word;
        this.responseTextView = responseTextView;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            printWriter.println(word);
            printWriter.flush();
            String res = bufferedReader.readLine();
            Log.d("Client ", "Received data: " + res);


            try {
                JSONArray jsonArray = new JSONArray(res);
                JSONObject jsonObject = jsonArray.getJSONObject(0); // assuming you always have at least one element
                JSONArray meaningsArray = ((JSONObject) jsonObject).getJSONArray("meanings");
                JSONObject firstMeaning = meaningsArray.getJSONObject(0); // get the first meaning
                JSONArray definitionsArray = firstMeaning.getJSONArray("definitions");
                JSONObject firstDefinition = definitionsArray.getJSONObject(0); // get the first definition
                String definitionText = firstDefinition.getString("definition"); // get the text of the definition

                // update the TextView
                final String finalDefinitionText = definitionText;
                responseTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        responseTextView.setText(finalDefinitionText);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }


            /*responseTextView.post(new Runnable() {
                @Override
                public void run() {
                    responseTextView.setText(res);
                }
            });*/
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
