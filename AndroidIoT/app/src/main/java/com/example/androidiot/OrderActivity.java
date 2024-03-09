package com.example.androidiot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class OrderActivity extends AppCompatActivity {

    private DatagramSocket sendSocket;
    private InetAddress address;
    private String ipAddress;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        InitSendSocket();
        ipAddress = getIntent().getStringExtra("IP_ADDRESS");
        port = getIntent().getIntExtra("PORT", 10000); // Utiliser 10000 comme port par défaut si non spécifié


        findViewById(R.id.buttonYXT).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = "YXT";
                SendData(data, port, ipAddress);
            }
        });

        findViewById(R.id.navigateToHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderActivity.this, MainActivity.class);
                // L'ajout de FLAG_ACTIVITY_CLEAR_TOP effacera l'historique des activités précédentes
                // Cela signifie que si l'utilisateur appuie sur le bouton de retour, il ne reviendra pas à OrderActivity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // Commentaire: Utilisation d'une expression lambda pour simplifier le code
        findViewById(R.id.buttonTXY).setOnClickListener(v -> {
            String data = "TXY";
            SendData(data, port, ipAddress);
        });
    }

    // Commentaire: Envoi X fois la data
    public void SendData(final String Sdata, final int port, final String addressText) {
        new Thread(() -> {
            try {
                address = InetAddress.getByName(addressText);
                byte[] data = Sdata.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                sendSocket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void InitSendSocket() {
        if (sendSocket == null || sendSocket.isClosed()) {
            try {
                sendSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sendSocket != null) sendSocket.close();
    }

}
