/**
 * This file is part of Words With Crosses.
 *
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

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

public class OpenURLPreference extends Preference
{
    private String url = "";

    public OpenURLPreference(Context context)
    {
        super(context);
    }

    public OpenURLPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs)
    {
        TypedArray styledAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.OpenURLPreference);

        try
        {
            setUrl(styledAttrs.getString(R.styleable.OpenURLPreference_url));
        }
        finally
        {
            styledAttrs.recycle();
        }
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    @Override
    protected void onClick()
    {
        Uri uri = Uri.parse(url);
        Intent intent;

        // Open the URI in either our HTML activity for local files, or in
        // the user's browser for other URL schemes
        if (uri.getScheme().equals("file"))
        {
            intent = new Intent(Intent.ACTION_VIEW, uri, getContext(), HTMLActivity.class);
        }
        else
        {
            intent = new Intent(Intent.ACTION_VIEW, uri);
        }

        getContext().startActivity(intent);
    }
}
