/*
 *
 *  * Copyright (C) 2018 L.D'Abreo
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

package com.au.planningalerts.task;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.au.planningalerts.R;
import com.au.planningalerts.server.Alert;
import com.au.planningalerts.server.AlertSearchCriteria;
import com.au.planningalerts.server.GoogleMapsServer;
import com.au.planningalerts.server.IGoogleMapsServer;
import com.au.planningalerts.server.IPlanningAlertsServer;
import com.au.planningalerts.server.PlanningAlertsServer;
import com.au.planningalerts.server.propertyresolver.DomainServer;
import com.au.planningalerts.server.propertyresolver.PropertyResolverChain;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AsyncTaskTest {

    @Mock
    Context mMockContext;

    @Mock
    Resources mMockResources;
    ListenerAdapter<Void, String> l = new ListenerAdapter<Void, String>();

    @Test
    public void testAsyncTaskAdapter() throws Exception {


        ListenerAdapter<Void, String> l = new ListenerAdapter<Void, String>();

        final String param = "10 smith st, Mt View";
        final TaskSubclass task = spy(new TaskSubclass(mMockContext, l, true));


        Answer runTask = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                task.onPreExecute();
                String result = task.doInBackground(param);
                task.onPostExecute(result);
                return task;
            }
        };
        Mockito.doAnswer(runTask).when(task).execute(anyString());

        task.execute(param);

        assertThat(l.notifications).isEqualTo(2);
        assertThat((l.cancelled)).isFalse();
        assertThat(l.mresult).isEqualTo("hello");


    }

    @Test
    public void testGeocodingTask() throws Exception {


        when(mMockResources.getString(R.string.google_geocoding_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.google_geocoding_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.google_geocoding_url)).thenReturn("https://googlemaps");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        InputStream in = this.getClass().getClassLoader().getResourceAsStream("GoogleGeocodingPayload.json");

        // Mock URL
        final URL url = mock(URL.class);
        HttpsURLConnection con = mock(HttpsURLConnection.class);
        when(url.openConnection()).thenReturn(con);
        when(con.getResponseCode()).thenReturn(200);
        when(con.getInputStream()).thenReturn(in);

        final GoogleMapsServer googleMapsServer = new GoogleMapsServer(mMockContext.getResources()) {

            @Override
            public URL openURL(String urlString) throws MalformedURLException {
                return url;
            }


        };


        ListenerAdapter<Void, LatLng> l = new ListenerAdapter<Void, LatLng>();

        final String address = "10 smith st, Mt View";
        final GeocodingTask task = spy(new GeocodingTask(mMockContext, l) {
            @Override
            protected boolean checkConnectivity() {
                return true;
            }

            @Override
            public IGoogleMapsServer createServer(Resources resources) {
                return googleMapsServer;
            }


        });


        Answer runTask = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                task.onPreExecute();
                LatLng ll = task.doInBackground(address);
                task.onPostExecute(ll);
                return task;
            }
        };


        Mockito.doAnswer(runTask).when(task).execute(anyString());


        task.execute(address);

        assertThat(l.notifications).isEqualTo(2);
        assertThat((l.cancelled)).isFalse();
        assertThat(l.mresult.latitude).isEqualTo(-33.821413);
        assertThat(l.mresult.longitude).isEqualTo(151.1978822);


    }

    @Test
    public void testPlanningAlertsTask() throws Exception {


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

        final PlanningAlertsServer server = new PlanningAlertsServer(mMockContext.getResources()) {
            public URL openURL(String urlString) throws MalformedURLException {
                return url;
            }
        };

        ListenerAdapter<Void, Alert[]> l = new ListenerAdapter<Void, Alert[]>();

        final PlanningAlertsTask task = spy(new PlanningAlertsTask(mMockContext, l) {
            @Override
            protected boolean checkConnectivity() {
                return true;
            }

            @Override
            public IPlanningAlertsServer createServer(Resources resources) {
                return server;
            }


        });

        LatLng ll = new LatLng(-33.821413, 151.1978822);
        final AlertSearchCriteria criteria = new AlertSearchCriteria(ll);


        Answer runTask = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                task.onPreExecute();
                Alert[] alerts = task.doInBackground(criteria);
                task.onPostExecute(alerts);
                return task;
            }
        };


        Mockito.doAnswer(runTask).when(task).execute((AlertSearchCriteria) anyObject());


        task.execute(criteria);

        assertThat(l.notifications).isEqualTo(2);
        assertThat((l.cancelled)).isFalse();
        assertThat(l.mresult).isNotEmpty();


    }

    @Test
    public void testPropertyResolverTask() throws Exception {


        when(mMockResources.getString(R.string.server_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.server_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.domain_valid_qr_code)).thenReturn("^https://www.domain.com.au.*$");
        when(mMockResources.getString(R.string.mcgrath_valid_qr_code)).thenReturn("^HTTP://QRTRAK.NET/.*$");
        when(mMockResources.getString(R.string.mcgrath_valid_url)).thenReturn("^https://www.mcgrath.com.au.*$");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        // Mock URL
        DomainServer domainServer = spy(new DomainServer(mMockContext.getResources()));
        Mockito.doReturn("domain").when(domainServer).resolve(anyString());

        PropertyResolverChain chain = new PropertyResolverChain();


        final String url = "https://www.domain.com.au";
        chain.addResolver(domainServer);

        ListenerAdapter<Void, String> l = new ListenerAdapter<Void, String>();

        final PropertyURLResolverTask task = spy(new PropertyURLResolverTask(mMockContext, l, chain) {
            protected boolean checkConnectivity() {
                return true;
            }

        });


        Answer runTask = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                task.onPreExecute();
                String address = task.doInBackground(url);
                task.onPostExecute(address);
                return task;
            }
        };


        Mockito.doAnswer(runTask).when(task).execute(anyString());


        task.execute(url);

        assertThat(l.notifications).isEqualTo(2);
        assertThat((l.cancelled)).isFalse();
        assertThat(l.mresult).isEqualTo("domain");


    }

    class TaskSubclass extends AsyncNetworkTaskAdapter<String, Void, String> {

        boolean mConnectivity;

        public TaskSubclass(Context context, ITaskListener<Void, String> listener, boolean connectivity) {
            super(context, listener);
            this.mConnectivity = connectivity;
        }

        @Override
        protected String doInBackground(String... strings) {
            return "hello";
        }

        protected boolean checkConnectivity() {
            return mConnectivity;
        }
    }

    ;


    class ListenerAdapter<Progress, Result> implements ITaskListener<Progress, Result> {

        public int notifications;
        public boolean cancelled = false;
        public Result mresult = null;

        @Override
        public void onPreExecute() {
            ++notifications;
        }

        @Override
        public void onPostExecute(@Nullable Result r, @Nullable Exception error) {
            ++notifications;
            mresult = r;
        }

        @Override
        public void onProgressUpdate(Progress... values) {
            ++notifications;
        }

        @Override
        public void onCancelled(Result r, @Nullable Exception error) {
            cancelled = true;
        }
    }


}
