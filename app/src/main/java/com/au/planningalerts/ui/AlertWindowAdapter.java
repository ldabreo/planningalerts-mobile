/*
 * Copyright 2018 L.D'Abreo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.au.planningalerts.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.au.planningalerts.R;
import com.au.planningalerts.server.Alert;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Map marker window contents supplier.
 * <p>
 * Provides the UI Info Window contents when the user clicks/selects a Map Marker.
 * <p>
 * See {@link com.google.android.gms.maps.GoogleMap.InfoWindowAdapter}
 */

public class AlertWindowAdapter implements GoogleMap.InfoWindowAdapter {

    protected Activity mActivity;


    public AlertWindowAdapter(Activity a) {
        mActivity = a;
    }

    // This defines the contents within the info window for the marker
    @Override
    public View getInfoContents(Marker marker) {
        // Getting view from the layout file

        if (marker.getTag() == null) { // Not an alert marker
            return null;
        }
        // Show alert council reference (aka name) in marker window
        Alert a = (Alert) marker.getTag();

        // Window layout defined in xml.

        @SuppressLint("InflateParams")
        View v = mActivity.getLayoutInflater().inflate(R.layout.alert_marker_view, null);
        TextView tv = v.findViewById(R.id.alertLink);
        String info = a.getCouncil_reference();


        tv.setText(info);


        return v;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }


}
