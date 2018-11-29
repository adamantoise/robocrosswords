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

package com.adamrosenfield.wordswithcrosses;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import com.adamrosenfield.wordswithcrosses.versions.AndroidVersionUtils;

public class HTMLActivity extends Activity {
    protected AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utils.finishOnHomeButton(this);

        setContentView(R.layout.html_view);

        WebView webview = (WebView) findViewById(R.id.webkit);
        Uri u = getIntent().getData();
        webview.loadUrl(u.toString());

        Button done = (Button) findViewById(R.id.closeButton);
        done.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HTMLActivity.this.finish();
                }
            });
    }
}
