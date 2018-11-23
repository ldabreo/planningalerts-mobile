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

package com.au.planningalerts.ui;

import android.content.Intent;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;

/**
 * Class that subclasses the Google Auto complete fragment.
 * <p>
 * Overrides the base class implementation to show the full search address (not just the first line)
 * in the search box.
 * <p>
 * Note this is somewhat fraught as the underlying method has been annotated with @Hide. No
 * known workaround at this point rather than developing a completely alternative
 * autocomplete UI implementation.
 */
public class AddressAutoCompleteFragment extends PlaceAutocompleteFragment {


    @SuppressWarnings({"RestrictedApi"})
    public void onActivityResult(int var1, int var2, Intent var3) {
        super.onActivityResult(var1, var2, var3);
        Place place = PlaceAutocomplete.getPlace(this.getActivity(), var3);

        if (place == null) {
            return;
        }

        // No address selected
        if (place.getAddress() == null) {
            return;
        }
        // Show FULL address text not just the first address line...
        setText(place.getAddress().toString());
    }
};



