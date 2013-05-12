package com.adamrosenfield.wordswithcrosses.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadReceiver extends BroadcastReceiver {

    private BroadcastReceiver impl;

    public DownloadReceiver()
    {
        if (android.os.Build.VERSION.SDK_INT >= 9) {
            try {
                BroadcastReceiver built = (BroadcastReceiver)
                    Class.forName("com.adamrosenfield.wordswithcrosses.net.DownloadReceiverGinger")
                    .newInstance();
                impl = built;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if (impl == null) {
            impl = new DownloadReceiverNoop();
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        impl.onReceive(ctx, intent);
    }
}
