/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.adamrosenfield.wordswithcrosses.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadReceiver extends BroadcastReceiver {

    private BroadcastReceiver impl;

    public DownloadReceiver()
    {
        int version = android.os.Build.VERSION.SDK_INT;
        if (version >= 9) {
            String receiverClass = "DownloadReceiverGinger";
            try {
                impl = (BroadcastReceiver)
                    Class.forName("com.adamrosenfield.wordswithcrosses.net." + receiverClass)
                    .newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
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
