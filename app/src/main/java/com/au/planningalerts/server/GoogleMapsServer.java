/*
 *
 *  * Copyright (C) 2018 Planning Alerts
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.au.planningalerts.server;

import android.content.res.Resources;

import com.au.planningalerts.BuildConfig;
import com.au.planningalerts.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Class  handles Google Geocoding interactions. Location data is returned as a JSON payload.
 * <p>
 * See the {@link IGoogleMapsServer} interface.
 */
public class GoogleMapsServer extends JsonServerAdapter<LatLng> implements IGoogleMapsServer {


    protected String mServerUrl;
    protected int mConnectTimeout;
    protected int mReadTimeout;

    public GoogleMapsServer(Resources resources) {
        this.mServerUrl = resources.getString(R.string.google_geocoding_url);
        this.mConnectTimeout = Integer.parseInt(resources.getString(R.string.google_geocoding_connecttimeout));
        this.mReadTimeout = Integer.parseInt(resources.getString(R.string.google_geocoding_readtimeout));
    }


    @Override
    public LatLng toLocation(String addressFreeForm) throws Exception {


        HttpsURLConnection con = null;
        URL url = null;
        InputStream ins = null;
        String json = null;
        try {

            // get url
            String encoded = mServerUrl +
                    "?address=" + addressFreeForm.replace(' ', '+') +
                    "&key=" + BuildConfig.google_maps_key;


            // open connection
            url = openURL(encoded);
            con = (HttpsURLConnection) url.openConnection();
            con.setConnectTimeout(mConnectTimeout);
            con.setReadTimeout(mReadTimeout);

            int responseCode = con.getResponseCode();
            ins = con.getInputStream();

            // read results
            json = readStream(ins);

            return fromJSON(json);

        } catch (Exception e) {
            throw e;
        } finally {

            // close resources
            if (con != null) {
                con.disconnect();
            }
        }


    }


    @Override
    protected LatLng fromJSON(String json) throws Exception {

        JSONObject response = new JSONObject(json);

        if (response.has("error_message")) {
            throw new Exception(response.getString("error_message"));
        }

        if (response.has("status")) {
            if (response.getString("status").equals("ZERO_RESULTS")) {
                throw new Exception("Unable to geocode");
            }
        }
        JSONArray results = response.getJSONArray("results");
        JSONObject entry = results.getJSONObject(0);
        JSONObject geometry = entry.getJSONObject("geometry");
        JSONObject location = geometry.getJSONObject("location");

        double lat = location.getDouble("lat");
        double lng = location.getDouble("lng");

        LatLng ll = new LatLng(lat, lng);
        return ll;


    }

    /**
     * Made a one-liner to assist with URL mocking.
     *
     * @param urlString the URL strring
     * @return a URL
     * @throws MalformedURLException
     */
    public URL openURL(String urlString) throws MalformedURLException {
        return new URL(urlString);
    }

    /**
     * Made a one-liner to assist with URL mocking.
     *
     * @param context base
     * @param spec    tail
     * @return a URL
     * @throws MalformedURLException
     */
    public URL openUrlWithContext(URL context, String spec) throws MalformedURLException {
        return new URL(context, spec);
    }
}
