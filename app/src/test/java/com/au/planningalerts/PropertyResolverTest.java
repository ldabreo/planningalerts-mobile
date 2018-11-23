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

import com.au.planningalerts.server.propertyresolver.DomainServer;
import com.au.planningalerts.server.propertyresolver.McGrathServer;
import com.au.planningalerts.server.propertyresolver.PropertyResolverChain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.net.HttpURLConnection;
import java.net.URL;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PropertyResolverTest {

    @Mock
    Context mMockContext;

    @Mock
    Resources mMockResources;


    @Test
    public void testDomainServer() {

        when(mMockResources.getString(R.string.server_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.server_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.domain_valid_qr_code)).thenReturn("^https://www.domain.com.au.*$");
        when(mMockResources.getString(R.string.domain_address_regexp)).thenReturn("\\\"property\\\":\\\\{\\\"address\\\":\\\"([\\\\s\\\\S]*?)\\\"");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        DomainServer server = new DomainServer(mMockContext.getResources());


        String url = "https://www.domain.com.au/1234?";
        assertThat(server.isResolvable(url)).isTrue();

        url = "https://www.domain.com.au/project/123445?";
        assertThat(server.isResolvable(url)).isTrue();


        url = "https://www.realestate.com.au/project/123445?";
        assertThat(server.isResolvable(url)).isFalse();

        url = "";
        assertThat(server.isResolvable(url)).isFalse();

        url = null;
        assertThat(server.isResolvable(url)).isFalse();

    }


    @Test
    public void testDomainServerURLResolution() throws Exception {

        when(mMockResources.getString(R.string.server_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.server_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.domain_valid_qr_code)).thenReturn("^https://www.domain.com.au.*$");
        when(mMockResources.getString(R.string.domain_address_regexp)).thenReturn("\"property\":\\{\"address\":\"([\\s\\S]*?)\"");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        // Mock URL


        DomainServer server = spy(new DomainServer(mMockContext.getResources()));

        boolean first = true;
        URL url = mock(URL.class);
        HttpURLConnection con = mock(HttpURLConnection.class);

        Answer<Integer> answerWithResponseCode = new Answer<Integer>() {

            boolean first = true;

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                if (first) {
                    first = false;
                    return HttpURLConnection.HTTP_MOVED_PERM;
                } else {
                    return HttpURLConnection.HTTP_ACCEPTED;
                }
            }
        };

        when(con.getResponseCode()).then(answerWithResponseCode);
        when(con.getHeaderField("Location")).thenReturn("905-184-forbes-street-darlinghurst-nsw-2010-2014767429");
        when(url.toExternalForm()).thenReturn("https://www.domain.com.au/905-184-forbes-street-darlinghurst-nsw-2010-2014767429");
        when(url.openConnection()).thenReturn(con);

        Mockito.doReturn(url).when(server).openURL(anyString());
        Mockito.doReturn(url).when(server).openUrlWithContext((URL) anyObject(), anyString());


        // test normal url
        String s = "https://www.domain.com.au/1234?";
        assertThat(server.resolve(s)).isEqualTo("905 184 forbes street darlinghurst nsw 2010");

        // test project url
        when(url.toExternalForm()).thenReturn("https://www.domain.com.au/project/2014767429");
        s = "https://www.domain.com.au/project/123445?";
        String contents = "XXXXXXX2 - new homes\",\"suburbId\":\"36632\",\"property\":{\"address\":\"1 Young Street, Randwick, NSW 2031\",\"agency\":\"Colliers YYYYYYYY";
        Mockito.doReturn(contents).when(server).readURL((HttpURLConnection) anyObject());
        assertThat(server.resolve(s)).isEqualTo("1 Young Street, Randwick, NSW 2031");


    }


    @Test
    public void testMcgrathServer() {

        when(mMockResources.getString(R.string.server_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.server_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.mcgrath_valid_qr_code)).thenReturn("^HTTP://QRTRAK.NET/.*$");
        when(mMockResources.getString(R.string.mcgrath_valid_url)).thenReturn("^https://www.mcgrath.com.au.*$");
        when(mMockResources.getString(R.string.mcgrath_address_regexp)).thenReturn("property=\\\"og:title\\\" content=\\\"([\\\\s\\\\S]*?)-");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        McGrathServer server = new McGrathServer(mMockContext.getResources());


        String url = "HTTP://QRTRAK.NET//1234";
        assertThat(server.isResolvable(url)).isTrue();

        url = "https://www.domain.com.au/project/123445?";
        assertThat(server.isResolvable(url)).isFalse();


        url = "https://www.realestate.com.au/project/123445?";
        assertThat(server.isResolvable(url)).isFalse();

        url = "";
        assertThat(server.isResolvable(url)).isFalse();

        url = null;
        assertThat(server.isResolvable(url)).isFalse();

    }


    @Test
    public void testMcGrathServerURLResolution() throws Exception {

        when(mMockResources.getString(R.string.server_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.server_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.mcgrath_valid_qr_code)).thenReturn("^HTTP://QRTRAK.NET/.*$");
        when(mMockResources.getString(R.string.mcgrath_valid_url)).thenReturn("^https://www.mcgrath.com.au.*$");
        when(mMockResources.getString(R.string.mcgrath_address_regexp)).thenReturn("property=\"og:title\" content=\"([\\s\\S]*?)-");
        when(mMockContext.getResources()).thenReturn(mMockResources);

        // Mock URL
        McGrathServer server = spy(new McGrathServer(mMockContext.getResources()));

        boolean first = true;
        URL url = mock(URL.class);
        HttpURLConnection con = mock(HttpURLConnection.class);

        Answer<Integer> answerWithResponseCode = new Answer<Integer>() {

            boolean first = true;

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                if (first) {
                    first = false;
                    return HttpURLConnection.HTTP_MOVED_PERM;
                } else {
                    return HttpURLConnection.HTTP_ACCEPTED;
                }
            }
        };

        when(con.getResponseCode()).then(answerWithResponseCode);
        when(con.getHeaderField("Location")).thenReturn("total rubbish");
        when(url.toExternalForm()).thenReturn("https://www.mcgrath.com.au/2014767429");
        when(url.openConnection()).thenReturn(con);


        String contents = "XXXXXXXproperty=\"og:title\" content=\"22 Gale Street,Hunters Hill, NSW House - For Sale - McGrath Estate Agents\">YYYYYYYY";
        Mockito.doReturn(contents).when(server).readURL((HttpURLConnection) anyObject());
        Mockito.doReturn(url).when(server).openURL(anyString());
        Mockito.doReturn(url).when(server).openUrlWithContext((URL) anyObject(), anyString());


        String s = "HTTP://QRTRAK.NET//1234";
        assertThat(server.isResolvable(s)).isTrue();


        String address = server.resolve(s);
        assertThat(address).isEqualTo("22 Gale Street,Hunters Hill, NSW House ");

    }

    @Test
    public void testResolverChain() throws Exception {

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

        // test an empty chain
        String url = "https://www.domain.com.au";
        assertThat(chain.isResolvable(url)).isFalse();
        assertThat(chain.isResolvable("")).isFalse();
        assertThat(chain.isResolvable(null)).isFalse();

        chain.addResolver(domainServer);

        assertThat(chain.isResolvable(url)).isTrue();
        assertThat(chain.resolve(url)).isEqualTo("domain");


        url = "https://www.rubbish.com.au";
        assertThat(chain.isResolvable(url)).isFalse();

        McGrathServer mcGrathServer = spy(new McGrathServer(mMockContext.getResources()));

        Mockito.doReturn("mcgrath").when(mcGrathServer).resolve(anyString());

        chain.addResolver(mcGrathServer);

        assertThat(chain.isResolvable(url)).isFalse();

        url = "HTTP://QRTRAK.NET/1234";
        assertThat(chain.isResolvable(url)).isTrue();
        assertThat(chain.resolve(url)).isEqualTo("mcgrath");

        url = "https://www.domain.com.au";
        assertThat(chain.isResolvable(url)).isTrue();
        assertThat(chain.resolve(url)).isEqualTo("domain");
        assertThat(chain.isResolvable("")).isFalse();
        assertThat(chain.isResolvable(null)).isFalse();


    }


}
