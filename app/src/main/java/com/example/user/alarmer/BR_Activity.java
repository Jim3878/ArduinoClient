package com.example.user.alarmer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DateFormat;

/**
 * Created by user on 2017/6/3.
 */

public class BR_Activity extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String msg= intent.getStringExtra("KEY_MSG");

        Intent ni = new Intent();
        ni.setClass(context,ActivityDialog.class);
        ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ni.putExtra("KEY_MSG",msg);
        context.startActivity(ni);
    }
}
