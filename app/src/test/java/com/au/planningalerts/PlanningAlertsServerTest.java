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

package com.au.planningalerts;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.au.planningalerts.server.Alert;
import com.au.planningalerts.server.AlertSearchCriteria;
import com.au.planningalerts.server.PlanningAlertsServer;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PlanningAlertsServerTest {

    @Mock
    Context mMockContext;

    @Mock
    Resources mMockResources;


    protected String readStream(InputStream stream)
            throws IOException, UnsupportedEncodingException {
        InputStreamReader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader br = new BufferedReader(reader);

        try {
            StringBuilder sb = new StringBuilder();

            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Problem reading data", e);
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return "";
    }

    @Test
    public void testPlanningAlertAlert() throws Exception {

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("Alert.json");
        String json = readStream(in);


        JSONObject o = new JSONObject(json);
        Alert a = Alert.fromJSON(o);

        assertThat(a.getId()).isEqualTo("948845");
        assertThat(a.getBearing()).isEqualTo("351");
        assertThat(a.getCouncil_reference()).isEqualTo("X/52/2018");
        assertThat(a.getAuthority()).isEqualTo("Blue Mountains City Council");
        assertThat(a.getAddress()).isEqualTo("17 David Street, Glenbrook, NSW");
        assertThat(a.getDescription()).isEqualTo("Alterations and additions to dwelling, a double garage, verandah and rainwater tank");
        assertThat(a.getInfo_url()).isEqualTo("https://www2.bmcc.nsw.gov.au/DATracking/Pages/XC.Track/SearchApplication.aspx?id=673922");


    }


    @Test
    public void testPlanningAlertSearchCritera() throws Exception {

        LatLng ll = new LatLng(-33.821413, 151.1978822);
        LatLng lll = new LatLng(-1.0, 2.0);

        AlertSearchCriteria criteria = new AlertSearchCriteria(ll);


        String params = criteria.toQueryParameters(1);
        assertThat(params).isEqualTo("page=1&count=100&lat=-33.821413&lng=151.1978822&radius=300");

        params = criteria.toQueryParameters(2);
        assertThat(params).isEqualTo("page=2&count=100&lat=-33.821413&lng=151.1978822&radius=300");


        criteria = new AlertSearchCriteria(ll, lll);
        params = criteria.toQueryParameters(1);
        assertThat(params).isEqualTo("page=1&count=100&bottom_left_lat=-33.821413&bottom_left_lng=151.1978822&top_right_lat=-1.0&top_right_lng=2.0");


        criteria = new AlertSearchCriteria(ll).setRadius(AlertSearchCriteria.Radius.AREA);
        params = criteria.toQueryParameters(1);
        assertThat(params).isEqualTo("page=1&count=100&lat=-33.821413&lng=151.1978822&radius=2000");

        criteria = new AlertSearchCriteria("100 way");
        params = criteria.toQueryParameters(1);
        assertThat(params).isEqualTo("page=1&count=100&address=100 way&radius=300");

    }


    @Test
    public void testPlanningAlertsAPI() throws Exception {

        when(mMockResources.getString(R.string.planningalerts_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.planningalerts_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.planningalerts_url)).thenReturn("https://api.planningalerts.org.au/applications.js");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        InputStream in = this.getClass().getClassLoader().getResourceAsStream("PlanningAlertsPayload.json");

        // Mock URL
        final URL url = mock(URL.class);
        HttpsURLConnection con = mock(HttpsURLConnection.class);
        when(url.openConnection()).thenReturn(con);
        when(con.getResponseCode()).thenReturn(200);
        when(con.getInputStream()).thenReturn(in);

        PlanningAlertsServer server = new PlanningAlertsServer(mMockContext.getResources()) {
            public URL openURL(String urlString) throws MalformedURLException {
                return url;
            }
        };


        LatLng ll = new LatLng(-33.821413, 151.1978822);

        AlertSearchCriteria criteria = new AlertSearchCriteria(ll);

        Alert[] alerts = server.findAlerts(criteria);

        assertThat(alerts).isNotEmpty();

    }


}
