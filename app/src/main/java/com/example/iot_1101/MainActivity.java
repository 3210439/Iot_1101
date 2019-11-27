package com.example.iot_1101;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Button sendBtn;
    EditText msgEdit;
    TextView textView, shower_txt;

    private ImageView bulb1_on ,bulb2_on, bulb3_on ,bulb4_on, bulb5_on , door1_close;
    private ImageView working_shower, closed_wnd1, closed_wnd2;

    private String[] words;
    private boolean clicked;
    private boolean auto_on;
    boolean flagConnection = true;
    boolean isConnected = false;
    boolean flagRead = true;


    Handler writeHandler;

    //add~~~~~~~~~~~~~~~~~~~

    Socket socket;
    BufferedInputStream bin;
    BufferedOutputStream bout;

    SocketThread st;
    ReadThread rt;
    WriteThread wt;

    String serverIp="192.168.0.107";
    int serverPort=9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendBtn = findViewById(R.id.lab1_send_btn);
        sendBtn.setOnClickListener(this);
        msgEdit = findViewById(R.id.lab1_send_text);
        textView = findViewById(R.id.textview);

        bulb1_on = findViewById(R.id.bulb1_on);
        bulb2_on = findViewById(R.id.bulb2_on);
        bulb3_on = findViewById(R.id.bulb3_on);
        bulb4_on = findViewById(R.id.bulb4_on);
        bulb5_on = findViewById(R.id.bulb5_on);
        door1_close = findViewById(R.id.door1_close);
        working_shower = findViewById(R.id.working_shower);
        shower_txt = findViewById(R.id.shower_txt);
        closed_wnd1 = findViewById(R.id.window1_close);
        closed_wnd2 = findViewById(R.id.window2_close);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.choose_opt:
                if(clicked == false && isConnected ==true){
                    clicked = true;
                    Message msg=new Message();
                    msg.obj="500";
                    writeHandler.sendMessage(msg);
                    if(auto_on) {
                        Toast.makeText(getApplicationContext(), "수동으로 변경 합니다.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {

                        Toast.makeText(getApplicationContext(), "자동으로 변경 합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        if (!msgEdit.getText().toString().trim().equals("")) {
            Message msg=new Message();
            msg.obj=msgEdit.getText().toString();
            writeHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        st=new SocketThread();
        st.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        flagConnection = false;
        isConnected = false;

        if (socket != null) {
            flagRead = false;
            if(writeHandler != null)
                writeHandler.getLooper().quit();
            try {
                if(bout !=null)
                    bout.close();
                if(bin != null)
                    bin.close();
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void showToast(String message){
        Toast toast=Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    Handler mainHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==10){
                //connection ok~~
                showToast("connection ok~~");
            }else if(msg.what==20){
                //connection fail~~~
                showToast("connection fail~~");
            }else if(msg.what==100){
                //message read....
                textView.setText((String)msg.obj);
                clicked = false;
                try {
                    String str1 = (String) msg.obj;
                    words = str1.split("\\s");

                    if (words[0] == "1")
                        bulb1_on.setVisibility(View.VISIBLE);
                    else
                        bulb1_on.setVisibility(View.INVISIBLE);

                    if (words[1] == "1")
                        bulb2_on.setVisibility(View.VISIBLE);
                    else
                        bulb2_on.setVisibility(View.INVISIBLE);

                    if (words[2] == "1")
                        bulb3_on.setVisibility(View.VISIBLE);
                    else
                        bulb3_on.setVisibility(View.INVISIBLE);

                    if (words[3] == "1")
                        bulb4_on.setVisibility(View.VISIBLE);
                    else
                        bulb4_on.setVisibility(View.INVISIBLE);

                    if (words[4] == "1")
                        bulb5_on.setVisibility(View.VISIBLE);
                    else
                        bulb5_on.setVisibility(View.INVISIBLE);

                    if (words[5] == "1")
                        closed_wnd1.setVisibility(View.INVISIBLE);
                    else
                        closed_wnd1.setVisibility(View.VISIBLE);

                    if (words[6] == "1")
                        closed_wnd2.setVisibility(View.INVISIBLE);
                    else
                        closed_wnd2.setVisibility(View.VISIBLE);

                    if (words[7] == "1")
                        door1_close.setVisibility(View.INVISIBLE);
                    else
                        door1_close.setVisibility(View.VISIBLE);

                    if (words[8] == "1") {
                        if(words[9].equals("100")){
                            working_shower.setVisibility(View.VISIBLE);
                            shower_txt.setText("목욕물 완료");
                        }
                        else {
                            working_shower.setVisibility(View.INVISIBLE);
                            shower_txt.setText("받는중");
                            Toast.makeText(getApplicationContext(), words[9] + "%", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        working_shower.setVisibility(View.VISIBLE);
                            shower_txt.setText("욕조물 받기");
                    }
                    if (words[9] == "1") { // 창문 열고 닫기를 자동으로 할지 수동으로 할지 선택
                        // auto
                        auto_on = true;
                    }
                    else
                    {
                        // menual
                        auto_on = false;
                    }



                }catch (Exception e)
                {

                }


            }else if(msg.what==200){
                //message write...
                msgEdit.setText("");
            }
        }
    };

    public void bulb1_on_click(View view) {
        if(clicked == false && isConnected ==true){
            clicked = true;
            Message msg=new Message();
            msg.obj =  "101";
            writeHandler.sendMessage(msg);
        }
    }

    public void bulb2_on_click(View view) {
        if(clicked == false && isConnected ==true){
            clicked = true;
            Message msg=new Message();
            msg.obj="102";
            writeHandler.sendMessage(msg);
        }}

    public void bulb3_on_click(View view) {
        if(clicked == false && isConnected ==true){
            clicked = true;
            Message msg=new Message();
            msg.obj="103";
            writeHandler.sendMessage(msg);
        }}

    public void bulb4_on_click(View view) {
        if(clicked == false && isConnected ==true){
            clicked = true;
            Message msg=new Message();
            msg.obj="104";
            writeHandler.sendMessage(msg);
        }}

    public void bulb5_on_click(View view) {
        if(clicked == false && isConnected ==true){
            clicked = true;
            Message msg=new Message();
            msg.obj="105";
            writeHandler.sendMessage(msg);
        }}

    public void door1_open_click(View view) {
        if(clicked == false && isConnected ==true){
            clicked = true;
            Message msg=new Message();
            msg.obj="201";
            writeHandler.sendMessage(msg);
        }}

    public void window1_click(View view) {
        if(clicked == false && isConnected ==true && auto_on){
            clicked = true;
            Message msg=new Message();
            msg.obj="301";
            writeHandler.sendMessage(msg);
        }
    }

    public void window2_click(View view) {
        if(clicked == false && isConnected ==true && auto_on){
            clicked = true;
            Message msg=new Message();
            msg.obj="302";
            writeHandler.sendMessage(msg);
        }
    }

    public void shower_click(View view) {
        if(clicked == false && isConnected ==true){
            clicked = true;
            Message msg=new Message();
            msg.obj="203";
            writeHandler.sendMessage(msg);
        }
    }


    class SocketThread extends Thread {

        public void run() {

            //add~~~~~~~~~~~~~~~~~
            while (flagConnection){
                try{
                    if(!isConnected){
                        socket=new Socket();
                        SocketAddress remoteAddr=new InetSocketAddress(serverIp, serverPort);
                        socket.connect(remoteAddr, 10000);

                        bout=new BufferedOutputStream(socket.getOutputStream());
                        bin=new BufferedInputStream(socket.getInputStream());

                        if(rt != null){
                            flagRead=false;
                        }
                        if(wt != null){
                            writeHandler.getLooper().quit();
                        }

                        wt=new WriteThread();
                        wt.start();
                        rt=new ReadThread();
                        rt.start();

                        isConnected=true;

                        Message msg=new Message();
                        msg.what=10;
                        mainHandler.sendMessage(msg);
                    }else {
                        SystemClock.sleep(10000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    SystemClock.sleep(10000);
                }
            }

        }
    }

    class WriteThread extends Thread {

        @Override
        public void run() {
            //add~~~~~~~~~~
            Looper.prepare();
            writeHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    try{
                        bout.write(((String)msg.obj).getBytes());
                        bout.flush();

                        Message message=new Message();
                        message.what=200;
                        message.obj=msg.obj;
                        mainHandler.sendMessage(message);
                    }catch (Exception e){
                        e.printStackTrace();
                        isConnected=false;
                        writeHandler.getLooper().quit();
                        try{
                            flagRead=false;
                        }catch (Exception e1){}
                    }
                }
            };
            Looper.loop();
        }
    }

    class ReadThread extends Thread {

        @Override
        public void run() {
            //add~~~~~~~~~~~~~~~~~~~

            byte[] buffer=null;
            while(flagRead){
                buffer=new byte[1024];
                try{
                    String message=null;
                    int size=bin.read(buffer); // 서버로부터 데이터를 읽음과 동시에 버퍼 크기를 받아온다.
                    if(size>0){
                        message=new String(buffer, 0, size, "utf-8");
                        if(message != null && !message.equals("")){
                            Message msg=new Message();
                            msg.what=100;
                            msg.obj=message;
                            mainHandler.sendMessage(msg);
                        }
                    }else {
                        flagRead=false;
                        isConnected=false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    flagRead=false;
                    isConnected=false;
                }
            }
            Message msg=new Message();
            msg.what=20;
            mainHandler.sendMessage(msg);
        }

    }


}

