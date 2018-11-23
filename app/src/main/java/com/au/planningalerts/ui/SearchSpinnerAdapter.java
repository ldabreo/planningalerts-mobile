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

package com.au.planningalerts.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.au.planningalerts.R;

/**
 * Custom dropdown search spinner for Search operations.
 */
public class SearchSpinnerAdapter extends ArrayAdapter<String> {

    protected String[] searchOptionTexts;
    protected int[] searchOptionIcons;


    public SearchSpinnerAdapter(Context context, int resource) {
        super(context, resource);


        searchOptionTexts = new String[]{
                context.getResources().getString(R.string.spinner_search),
                context.getResources().getString(R.string.spinner_current_location),
                context.getResources().getString(R.string.spinner_domain_qr)};

        searchOptionIcons = new int[]{
                R.drawable.search_icon_format, R.drawable.here_icon, R.drawable.qr_icon};

    }

    @Override
    public int getCount() {
        return searchOptionTexts.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        ViewHolder mViewHolder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.search_spinner_row, parent, false);
            mViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.searchOptionIcon);
            mViewHolder.mOption = (TextView) convertView.findViewById(R.id.searchOptionText);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.mIcon.setImageResource(searchOptionIcons[position]);
        mViewHolder.mOption.setText(searchOptionTexts[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private static class ViewHolder {
        ImageView mIcon;
        TextView mOption;
    }


}
