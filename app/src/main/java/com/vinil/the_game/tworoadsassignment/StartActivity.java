package com.vinil.the_game.tworoadsassignment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.regex.Pattern;

public class StartActivity extends AppCompatActivity {

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        final EditText ipAddressEditText = (EditText)findViewById(R.id.ipAddressEditText);
        final EditText portNumberEditText = (EditText)findViewById(R.id.portNumberEditText);
        Button startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String ip = ipAddressEditText.getText().toString().trim();
                final String port = portNumberEditText.getText().toString().trim();
                boolean isIPValid = checkIfIPValid(ip);
                boolean isPortValid = checkIfPortValid(port);
                if(isIPValid&&isPortValid){
                    AsyncTask asyncTask = new AsyncTask() {
                        boolean exists = false;
                        ProgressDialog dialog = new ProgressDialog(StartActivity.this);

                        @Override
                        protected void onPreExecute() {
                            dialog.setMessage("Please wait");
                            dialog.show();
                        }

                        @Override
                        protected Object doInBackground(Object[] params) {
                            try {
                                SocketAddress sockaddr = new InetSocketAddress(ip,Integer.parseInt(port));
                                // Create an unbound socket
                                Socket sock = new Socket();

                                // This method will block no more than timeoutMs.
                                // If the timeout occurs, SocketTimeoutException is thrown.
                                int timeoutMs = 2000;   // 2 seconds
                                sock.connect(sockaddr, timeoutMs);
                                exists = true;
                            }catch(SocketTimeoutException e){
                                e.printStackTrace();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            if(dialog.isShowing()){
                                dialog.dismiss();
                            }
                            if(exists==true) {
                                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                intent.putExtra("url", "http://" + ip + ":" + port);
                                startActivity(intent);
                            }else{
                                Toast.makeText(StartActivity.this,"Not reachable! Please try again later!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    asyncTask.execute();
                }else{
                    Toast.makeText(StartActivity.this,"Please enter valid IP/Port!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public boolean checkIfIPValid(String ip){
        return PATTERN.matcher(ip).matches();
    }

    public boolean checkIfPortValid(String port){
        int portInt = Integer.parseInt(port);
        if(portInt>1023 && portInt<65536) {
            return true;
        }
        else{
            return false;
        }
    }

}
