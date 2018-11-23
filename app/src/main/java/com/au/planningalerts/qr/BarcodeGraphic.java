/*
 *
 *  * Copyright (C) The Android Open Source Project
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
 */
package com.au.planningalerts.qr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.au.planningalerts.qr.camera.GraphicOverlay;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Graphic instance for rendering barcode position, size, and ID within an associated graphic
 * overlay view.
 * <p>
 * Paints a yellow box around  a QR code.
 */
public class BarcodeGraphic extends GraphicOverlay.Graphic {

    private static int mCurrentColorIndex = 0;
    private int mId;
    private Paint mRectPaint;
    private Paint mRectOutlinePaint;
    private Paint mTextPaint;
    private volatile Barcode mBarcode;

    private Context context;

    BarcodeGraphic(GraphicOverlay overlay) {
        super(overlay);


        mRectPaint = new Paint();
        mRectPaint.setColor(Color.LTGRAY);
        mRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRectPaint.setAlpha(150);

        mRectOutlinePaint = new Paint();
        mRectOutlinePaint.setColor(Color.YELLOW);
        mRectOutlinePaint.setStyle(Paint.Style.STROKE);
        mRectOutlinePaint.setStrokeWidth(10.0f);


        mTextPaint = new Paint();
        mTextPaint.setColor(Color.LTGRAY);
        mTextPaint.setTextSize(36.0f);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public Barcode getBarcode() {
        return mBarcode;
    }

    /**
     * Updates the barcode instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateItem(Barcode barcode) {
        mBarcode = barcode;
        postInvalidate();
    }

    /**
     * Draws the barcode annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Barcode barcode = mBarcode;
        if (barcode == null) {
            return;
        }

        // Draws the bounding box around the barcode.
        RectF rect = new RectF(barcode.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRoundRect(rect, (float) 10.0, (float) 10.0, mRectPaint);
        canvas.drawRoundRect(rect, (float) 10.0, (float) 10.0, mRectOutlinePaint);

    }
}
