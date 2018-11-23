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

import com.au.planningalerts.server.Alert;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


/**
 * Google map Alert markers are coloured/differentiated according to the Alert age/date.
 * <p>
 * This class is responsible for selecting the marker icon colour
 * based on the Alert issue/received date.
 * <p>
 * Colours range across a spectrum of 60 hues over a period of the last 3 years -
 * with Red representing the most recent alerts and  orange and yellow for the older ones.
 * <p>
 * Alerts 3 years (36 months) or older are shown as pale yellow. Why? In most cases the user
 * is interested in the more recent alerts i.e whats happening now vs older (usually completed)
 * applications.
 */
public class MapMarkerGenerator {


    /**
     * Calculate the Alert Hue
     *
     * @param a
     * @return
     */
    public static float getHueColour(Alert a) {

        Date d = a.getDate_received();

        if (d == null) {
            d = a.getDate_scrapped();
        }

        if (d == null) {
            return BitmapDescriptorFactory.HUE_RED;
        }
        long diffInMillis = System.currentTimeMillis() - d.getTime();

        long days = MILLISECONDS.toDays(diffInMillis);

        // Approximate given we don't have Java 8 date operations here
        long months = days / 30;
        months = Math.min(36, months);
        float adj = (months / (float) 36) * 60;

        return adj;


    }


    /**
     * Create Alert Marker Options for a customised L&F marker
     *
     * @param a
     * @return
     */
    public static MarkerOptions createMarkerOptions(Alert a) {

        MarkerOptions o = new MarkerOptions();
        o = o.position(a.getLocation());
        o = o.title(a.getCouncil_reference());
        // marker is colour coded red to yellow according to application date/age.
        // All older alerts more than ~36  months old are shown as a pale yellow
        float hue = getHueColour(a);
        float z = ((60.0f - hue) / 60.0f); // limit hue range from red to yellow
        // Adjust Z to ensure more recent alerts are on top of older ones
        // for the same address
        o = o.zIndex(z);

        // Set standard hue icon
        BitmapDescriptor d = BitmapDescriptorFactory.defaultMarker(hue);
        o = o.icon(d);

        return o;
    }


}
