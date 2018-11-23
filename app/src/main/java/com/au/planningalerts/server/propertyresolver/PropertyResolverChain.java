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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing multiple Property resolvers in a chain-of-responsibility pattern.
 * Each resolver must implement the {@link IPropertyURLResolver} interface.
 * <p>
 * As the QR code structure is unknown upfront, each resolver in the chain is queried in turn with
 * the unknown QR code until a resolver is found that is able to interpret the code.
 * <p>
 * The use of a chain, provisions for extension as it allows multiple resolvers to be
 * 'plugged' in as need-be to resolve the different types of QR codes in use.
 * <p>
 * To use this class, instantiate it then add resolvers. Order is significant,  as the first
 * resolver found able to interpret a code will be the one used to resolve it.
 * However in general, resolvers are mutually exclusive in which QR codes
 * they can handle.
 * <p>
 * To create the chain:
 * <pre>
 *   PropertyResolverChain chain = new PropertyResolverChain();
 *   chain.addResolver(new DomainServer(getResources()));
 *   chain.addResolver(new McGrathServer(getResources()));
 *  </pre>
 * <p>
 * And to resolve an address:
 * <pre>
 *  String address = chain.resolve(..);
 *  </pre>
 * <p>
 * <p>
 * Implements the {@Link Parcelable} interface to allow it to be serialized into
 * an Intent and passed to the barcode reader activity.
 * Implements the {@link IPropertyURLResolver} interface.
 */
public class PropertyResolverChain implements IPropertyURLResolver {

    public static final Parcelable.Creator<PropertyResolverChain> CREATOR
            = new Parcelable.Creator<PropertyResolverChain>() {
        public PropertyResolverChain createFromParcel(Parcel in) {
            return new PropertyResolverChain(in);
        }

        public PropertyResolverChain[] newArray(int size) {
            return new PropertyResolverChain[size];
        }
    };
    protected List<IPropertyURLResolver> mResolverChain;

    public PropertyResolverChain() {
        mResolverChain = new ArrayList<IPropertyURLResolver>();
    }

    private PropertyResolverChain(Parcel in) {
        mResolverChain = new ArrayList<IPropertyURLResolver>();
        in.readList(mResolverChain, this.getClass().getClassLoader());
    }

    @Override
    public boolean isResolvable(String url) {

        for (IPropertyURLResolver resolver : mResolverChain) {
            if (resolver.isResolvable(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Traverse resolver chain to find a resolver that can handle this url.
     *
     * @param url the target URL.
     * @return a property address.
     * @throws Exception
     */
    @Override
    public String resolve(String url) throws Exception {
        for (IPropertyURLResolver resolver : mResolverChain) {
            if (resolver.isResolvable(url)) {
                return resolver.resolve(url);
            }
        }
        return null;
    }

    public List<IPropertyURLResolver> getResolvers() {
        return mResolverChain;
    }

    public PropertyResolverChain addResolver(IPropertyURLResolver resolver) {
        mResolverChain.add(resolver);
        return this;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(mResolverChain);
    }

}
