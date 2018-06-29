package com.electromancy.max.spacenerdscommunicator;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class CommunicatorActivity extends AppCompatActivity {

    private InetAddress inetAddress = InetAddress.getByName("192.168.0.113");
    private int port = 8080;
    private  TextView voiceText;

    public CommunicatorActivity() throws UnknownHostException {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicator);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView serverStatus = findViewById(R.id.serverStatus);
        serverStatus.setText("Sending messages to " + inetAddress +":" + port);

        FloatingActionButton fab = findViewById(R.id.fab);
        voiceText = findViewById(R.id.voiceText);
        fab.setOnClickListener(talkListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_communicator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_configure_ip) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter new IP address, currently: " + inetAddress.toString() + ":"+ port);

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        inetAddress= InetAddress.getByName(input.getText().toString());
                        TextView serverStatus = findViewById(R.id.serverStatus);
                        serverStatus.setText("Sending messages to " + inetAddress +":" + port);
                    } catch (Exception e) {
                        Toast t = Toast.makeText(getApplicationContext(),
                                "Oops! You have entered an incorrect address.",
                                Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK && null != data) {
                    SendTextTask task = new SendTextTask();
                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    System.out.println(text);
                    task.execute(text.get(0));
                    voiceText.setText(text.get(0));
                }
                break;
            }

        }
    }

    View.OnClickListener talkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

            try {
                startActivityForResult(intent, 1);
                voiceText.setText("");
            } catch (ActivityNotFoundException a) {
                Toast t = Toast.makeText(getApplicationContext(),
                        "Oops! Your device doesn't support Speech to Text",
                        Toast.LENGTH_SHORT);
                t.show();
            }

        }
    };



    //Async task to send text to server
    private class SendTextTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            try {
                String newline = "\n";
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(inetAddress, port),1000    );

                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(strings[0].getBytes());
                outputStream.write(newline.getBytes());

                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
                return "There was a problem sending your message: " + e.getMessage();
            }

            return "Your message was successfully sent to the server";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast t = Toast.makeText(getApplicationContext(),
                    result,
                    Toast.LENGTH_LONG);
            t.show();
        }
    }

}