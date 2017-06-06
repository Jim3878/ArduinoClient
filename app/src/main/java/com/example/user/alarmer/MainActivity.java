package com.example.user.alarmer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Thread thread;                //執行緒
    private Socket clientSocket;        //客戶端的socket
    private BufferedWriter bw;            //取得網路輸出串流
    private BufferedReader br;            //取得網路輸入串流
    private static boolean isConnected=false;
        //private JSONObject json_write,json_read;        //從java伺服器傳遞與接收資料的json


    Button almBtn;
    Button cntBtn;
    Button btn_disconnect;
    EditText ipET;
    EditText portET;
    TextView stateTV;
    CheckBox alarmCB;
    Handler hr;
    private MediaPlayer player;
    private String msg="hello";
    int cntChk=6000;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());
        stateTV=(TextView)findViewById(R.id.state);
        //stateTV.setText("disconnected");
        cntBtn=(Button)findViewById(R.id.cntBtn);
        cntBtn.setOnClickListener(cont);
        ipET=(EditText)findViewById(R.id.ip_ET);
        ipET.setText("192.168.137.1");
        btn_disconnect=(Button)findViewById(R.id.btn_disconnect);
        btn_disconnect.setOnClickListener(this.disconnect);

        portET=(EditText)findViewById(R.id.host_ET);
        portET.setText("7777");
        almBtn= (Button) findViewById(R.id.almBtn);
        almBtn.setOnClickListener(rec);
        alarmCB=(CheckBox)findViewById(R.id.alarmCB);

         hr = new Handler(){
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case    1:
                        call("長輩跌倒");
                        //oc("長輩跌倒",true);
                        break;
                    case    2:
                        oc("error:"+msg.what,false);
                        alarmCB.setText(getText(R.string.connected));
                        alarmCB.setEnabled(true);
                        break;
                    case    3:
                        oc("error:"+msg.what,false);
                        alarmCB.setEnabled(true);
                        break;
                    case    4://連線成功
                        stateTV.setText(getText(R.string.connected));
                        cntBtn.setText(getText(R.string.connected));
                        cntBtn.setEnabled(false);
                        alarmCB.setEnabled(false);
                        break;
                    case 5://異常中斷
                        stateTV.setText(getText(R.string.disconnected));
                        alarmCB.setEnabled(true);
                        call(getText(R.string.disconnected).toString());
                        cntBtn.setText(getText(R.string.btn_connect));

                        cntBtn.setEnabled(true);
                        break;
                    case 6://中斷連線
                        cntBtn.setEnabled(true);
                        stateTV.setText(getText(R.string.disconnected));
                        cntBtn.setText(getText(R.string.btn_connect));
                        oc(getText(R.string.disconnected).toString(),false);
                        break;
                }
                super.handleMessage(msg);
            }
        };


    }
    private void call(String s){
        Intent ni = new Intent();
        ni.setClass(MainActivity.this,ActivityDialog.class);
        //ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ni.putExtra("KEY_MSG",s);
        this.startActivity(ni);
    }

    private void oc(String r, final boolean isMusic){
        this.onRestart();
        player= MediaPlayer.create(this,R.raw.aoi);
        player.setOnCompletionListener(comL);
        if(isMusic){
            player.start();
        }

        AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
        adb.setMessage(r);


        adb.setPositiveButton("OK,I got this", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(isMusic){
                    player.stop();
                }
            }
        });
        adb.show();

    }


    private OnClickListener disconnect=new OnClickListener() {
        @Override
        public void onClick(View v) {
            isConnected=true;
        }
    };

    /*Connect Server*/
    private  OnClickListener cont=new OnClickListener() {
        @Override
        public void onClick(View v) {
            String ip;
            String temp;
            int port;
            try {
                //ip = ipET.getText().toString();
                //port = Integer.valueOf(portET.getText().toString());
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String temp;
                            final Socket s;
                            InetAddress serverIp = InetAddress.getByName(ipET.getText().toString());
                            s = new Socket(serverIp,Integer.valueOf(portET.getText().toString()) );

                            //連線成功
                            Message m=new Message();
                            m.what=4;
                            hr.sendMessage(m);


                            try{
                                OutputStream os = s.getOutputStream();
                                BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(s.getOutputStream()));


                                InputStream is = s.getInputStream();
                                InputStreamReader reader=new InputStreamReader(is);
                                BufferedReader br=new BufferedReader(reader);

                                DataInputStream din  = new DataInputStream(s.getInputStream());
                                DataOutputStream dout = new DataOutputStream(s.getOutputStream());


                                if(alarmCB.isChecked()){
                                   s.setSoTimeout(cntChk);
                                }
                                while (s.isConnected()&&!isConnected) {
                                    temp=br.readLine();
                                    bw.write("rec:"+temp+"\n");
                                    bw.flush();
                                    if(temp!=null){
                                        if(temp.equals("0")){
                                        m = new Message();
                                        m.what = 1;
                                        hr.sendMessage(m);
                                        }
                                    }

                                }
                                //按下中斷連線
                                bw.close();
                                br.close();
                                s.close();
                                m=new Message();
                                m.what=6;
                                hr.sendMessage(m);
                                isConnected=false;

                            }catch (Exception e){
                                bw.close();
                                br.close();
                                s.close();
                                m=new Message();
                                m.what=5;
                                hr.sendMessage(m);
                            }
                        }catch (Exception e) {
                            Message m=new Message();
                            m.what=5;
                            hr.sendMessage(m);

                        }
                    }
                });
                thread.start();
            }catch (Exception e){
                oc("thread failed",false);
            }

        }
    };
    private OnClickListener rec = new OnClickListener() {
        @Override
        public void onClick(View v) {
            oc("Dad 跌倒",true);
        }
    };
    private MediaPlayer.OnCompletionListener comL =new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            try{
                player.stop();
                player.prepare();
            }catch (Exception e){
                oc("音樂播放失敗",false);
            }
        }
    };

}
