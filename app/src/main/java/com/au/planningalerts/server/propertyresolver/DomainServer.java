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

package com.au.planningalerts.server.propertyresolver;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.au.planningalerts.R;


/**
 * Class for interpreting domain magazine URLs attached to QR codes.
 * <p>
 * Implements the {@Link Parcelable} interface to allow it to be serialized into
 * an Intent and passed to the barcode reader activity.
 * <p>
 * See {@link IPropertyURLResolver} for the URL resolver contract.
 */
public class DomainServer extends URLPropertyServerAdapter {


    public static final Parcelable.Creator<DomainServer> CREATOR
            = new Parcelable.Creator<DomainServer>() {
        public DomainServer createFromParcel(Parcel in) {
            return new DomainServer(in);
        }

        public DomainServer[] newArray(int size) {
            return new DomainServer[size];
        }
    };

    // RegExp for extracting/scrapping an address from a domain HTML page.
    protected String mAddressRegExp;


    public DomainServer(Parcel in) {
        super(in);
        mAddressRegExp = in.readString();
    }

    public DomainServer(Resources resources) {
        super(resources);
        mCodeRegexp = resources.getString(R.string.domain_valid_qr_code); // Used to determine if a URL is a domain one.
        mAddressRegExp = resources.getString(R.string.domain_address_regexp);
    }

    /**
     * Inspect the domain URL and either extract the underlying property address from the URL
     * itself or scrape it from the HTML page.
     *
     * @param qrDomainCode from a domain mag
     * @return an address reverse engineered from the domain URL
     * @throws Exception
     */
    @Override
    public String resolve(final String qrDomainCode) throws Exception {

        String address = null;

        // Read URL
        URLResult result = resolveURL(qrDomainCode, null, false);

        // Is it a new development, project URL?
        // If so need to read the entire contents of the page to access the property address.
        if (result.url.contains("project")) {
            // development url - read page...
            result = resolveURL(qrDomainCode, null, true);
            address = extractString(result.contents, mAddressRegExp);


        } else {
            // If not a new development URL then this is a standard domain URL and the address can be extracted
            // from the URL itself.
            String shortAddress = result.url.substring(result.url.lastIndexOf('/') + 1, result.url.lastIndexOf('-'));
            address = shortAddress.replace('-', ' ');
        }

        return address;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mAddressRegExp);
    }

}
