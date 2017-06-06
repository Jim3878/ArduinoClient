package com.example.user.alarmer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

/**
 * Created by user on 2017/6/3.
 */

public class ActivityDialog extends Activity {
    private MediaPlayer player;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

       Intent intent = getIntent();
        String msg = intent.getStringExtra("KEY_MSG");

        AlertDialog.Builder adb=new AlertDialog.Builder(this);
        adb.setMessage(msg);

        player= MediaPlayer.create(this,R.raw.aoi);
        player.setOnCompletionListener(comL);
        player.start();


        adb.setPositiveButton("OK,I got this", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                player.stop();
                finish();
            }
        });
        adb.show();
    }

    private MediaPlayer.OnCompletionListener comL =new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            try{
                player.stop();
                player.prepare();
            }catch (Exception e){
            }
        }
    };
}
