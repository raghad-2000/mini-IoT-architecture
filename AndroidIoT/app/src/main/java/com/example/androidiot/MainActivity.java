package com.example.androidiot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private EditText editTextIP;
    private EditText editPort;
    private DatagramSocket receiveSocket;
    private Thread listeningThread;
    private volatile boolean listening = true; // Volatile to ensure visibility across threads

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        editTextIP = findViewById(R.id.editTextIP);
        editTextIP.setText("192.168.1.18"); // Example IP, replace with actual dynamic value if needed
        editPort = findViewById(R.id.editPort);
        int defaultPort = 10000;
        editPort.setText(String.valueOf(defaultPort)); // Setting default port value

        findViewById(R.id.buttonNavigateToOrder).setOnClickListener(v -> {
            navigateToOrderActivity();
        });

        findViewById(R.id.receive).setOnClickListener(v -> {
            listenForMessages();
        });

        initReceiveSocket(Integer.parseInt(editPort.getText().toString())); // Dynamically initializing receive socket with user-defined port



        /*setContentView(R.layout.activity_main);

        // Commentaire: Les éléments de l'interface utilisateur sont initialisés ici
         editTextIP = findViewById(R.id.editTextIP);

        // Commentaire: La ligne ci-dessous a été commentée pour éviter de pré-remplir l'adresse IP à des fins de test
        editTextIP.setText("192.168.1.18");
        int port = 10000;
        editPort = findViewById(R.id.editPort);
        editPort.setText(String.valueOf(port));

        findViewById(R.id.buttonNavigateToOrder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receiveSocket != null) receiveSocket.close();
                navigateToOrderActivity();
            }
        });

        InitReceiveSocket();

        findViewById(R.id.receive).setOnClickListener(v -> {
            listenForMessages();
        });*/
    }

    private void navigateToOrderActivity() {
        stopListeningAndCloseSocket();
        Intent intent = new Intent(MainActivity.this, OrderActivity.class);
        intent.putExtra("IP_ADDRESS", editTextIP.getText().toString());
        intent.putExtra("PORT", Integer.parseInt(editPort.getText().toString()));
        startActivity(intent);
    }
    public void initReceiveSocket(int port) {
        try {
            receiveSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    private void listenForMessages() {
        new Thread(() -> {
            byte[] buffer = new byte[512]; // Adjust the buffer size as needed
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (listening) {
                try {
                    // Check if the socket is closed before attempting to receive to avoid SocketException
                    if (receiveSocket.isClosed()) {
                        break; // Exit the loop if the socket is closed
                    }

                    receiveSocket.receive(packet);
                    String receivedData = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                    //String receivedData = new String(packet.getData(), 0, packet.getLength());
                    Log.i("Received", receivedData);
                    // Process the received packet
                    DisplayData(receivedData);

                    // Clear the buffer after processing to avoid reading stale data
                    Arrays.fill(buffer, (byte) 0);

                    // Listen interval: Pause for a specified duration
                    Thread.sleep(1000); // Pause for 1000 milliseconds (1 second)
                } catch (InterruptedException e) {
                    // Handle the case where the sleep is interrupted
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    break; // Optionally, exit the loop if the thread is interrupted
                } catch (Exception e) {
                    if (!listening) {
                        break; // Exit if we're no longer supposed to be listening
                    }
                    e.printStackTrace(); // Log other exceptions for debugging purposes
                }
            }
        }).start();
    }
   /* private void listenForMessages() {
        new Thread(() -> {
            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (listening) {
                try {
                    receiveSocket.receive(packet);
                    if (!listening) break; // Sortie rapide si listening est false après receive
                    DisplayData(packet);
                } catch (Exception e) {
                    if (listening) { // Log les erreurs uniquement si nous sommes censés écouter
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }*/

    public String loadDataOrder() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return sharedPreferences.getString("DataOrder", "TXY"); // Valeur par défaut si non trouvée
    }
    // Commentaire: Modifie l'affichage en fonction de la tram reçue
    public void DisplayData(String receivedData) {
        //String receivedData = new String(data.getData(), 0, data.getLength());
        //Log.i("test", receivedData);
        runOnUiThread(() -> {
            //String receivedData = new String(data.getData(), 0, data.getLength(), "UTF-8");

            //String receivedData = new String(data.getData(), 0, data.getLength(), "UTF-8");
            Log.i("Received Data", receivedData);
            // Mettez à jour votre TextView ou d'autres éléments UI ici
            if(receivedData.length() < 7) return;
            String dataOrder = loadDataOrder();
            if (dataOrder.equals("TXY")) {
                TextView textView1 = findViewById(R.id.textView1);
                textView1.setText("Temperature: " + receivedData.charAt(1) + receivedData.charAt(2) + "°C");

                TextView textView2 = findViewById(R.id.textView2);
                textView2.setText("X: " + receivedData.charAt(4));

                TextView textView3 = findViewById(R.id.textView3);
                textView3.setText("Y: " + receivedData.charAt(6));
            } else {
                TextView textView1 = findViewById(R.id.textView1);
                textView1.setText("Y: " + receivedData.charAt(6));
                TextView textView2 = findViewById(R.id.textView2);
                textView2.setText("X: " + receivedData.charAt(4));
                TextView textView3 = findViewById(R.id.textView3);
                textView3.setText("Temperature: " + receivedData.charAt(1) + receivedData.charAt(2) + "°C");


            }
        });
    }
    private void stopListeningAndCloseSocket() {
        listening = false;
        if (receiveSocket != null) {
            receiveSocket.close(); // Close socket to unblock receive method
        }
        if (listeningThread != null) {
            try {
                listeningThread.join(); // Wait for the listening thread to terminate
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListeningAndCloseSocket();
    }
}
