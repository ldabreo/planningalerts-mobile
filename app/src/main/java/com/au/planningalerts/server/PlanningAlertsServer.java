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

package com.au.planningalerts.server;


import android.content.res.Resources;
import android.util.Log;

import com.au.planningalerts.BuildConfig;
import com.au.planningalerts.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Class that handles all interactions with the the PlanningAlerts Server.
 * <p>
 * The client api is defined by the interface {@link JsonServerAdapter} interface,
 * which defines search operations for finding development planning allplications and alerts.
 * <p>
 * The search criteria is defined by {@link AlertSearchCriteria}.
 * <p>
 * Data is returned as an array of {@link Alert}
 */
public class PlanningAlertsServer extends JsonServerAdapter<Alert[]> implements IPlanningAlertsServer {

    protected String mServerUrl;
    protected int mConnectTimeout;
    protected int mReadTimeout;

    public PlanningAlertsServer(Resources resources) {

        this.mServerUrl = resources.getString(R.string.planningalerts_url);
        this.mConnectTimeout = Integer.parseInt(resources.getString(R.string.planningalerts_connecttimeout));
        this.mReadTimeout = Integer.parseInt(resources.getString(R.string.planningalerts_readtimeout));
    }

    @Override
    public Alert[] findAlerts(AlertSearchCriteria filter) throws Exception {
        return fetchAlerts(filter);
    }


    @Override
    public Alert[] findAlerts(LatLng location) throws Exception {
        AlertSearchCriteria filter = new AlertSearchCriteria(location);
        return findAlerts(filter);
    }


    /**
     * Fetch alert data from the server, given the supplied search criteria.
     *
     * @param filter defines search criteria
     * @return an Array of Alerts
     * @throws Exception
     */
    protected Alert[] fetchAlerts(AlertSearchCriteria filter) throws Exception {

        // check filter is not null


        HttpsURLConnection con = null;
        URL url = null;
        InputStream ins = null;
        String json = null;
        try {

            // get url
            String encoded = mServerUrl +
                    "?key=" + BuildConfig.planningalerts_key + "&" +
                    filter.toQueryParameters(1);

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
            Log.e(getClass().getSimpleName(), "Problem accessing url:" + url, e.getCause());
            throw e;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

    }

    @Override
    protected Alert[] fromJSON(String json) throws Exception {

        List<Alert> alerts = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Alert alert = Alert.fromJSON(obj);
            alerts.add(alert);
        }

        return alerts.toArray(new Alert[]{});


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
