package com.rssignaturecapture;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

import android.util.Base64;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import java.lang.Boolean;
import android.util.TypedValue;
import android.widget.TextView;

public class RSSignatureCaptureMainView extends RelativeLayout implements OnClickListener,RSSignatureCaptureView.SignatureCallback {

    RelativeLayout buttonsLayout;
    RSSignatureCaptureView signatureView;
    RelativeLayout infoLayout;

    Activity mActivity;
    int mOriginalOrientation;
    Boolean saveFileInExtStorage = false;
    String viewMode = "portrait";
    Boolean showNativeButtons = true;
    int maxSize = 500;
    final const int BUTTON_LAYOUT_ID = 10001;

    public RSSignatureCaptureMainView(Context context, Activity activity) {
        super(context);
        Log.d("React:", "RSSignatureCaptureMainView(Contructtor)");
        mOriginalOrientation = activity.getRequestedOrientation();
        mActivity = activity;

        this.signatureView = new RSSignatureCaptureView(context,this);
        // add the buttons and signature views
        this.buttonsLayout = this.buttonsLayout();
        this.buttonsLayout.setId(BUTTON_LAYOUT_ID);
        this.infoLayout = this.infoLayout(BUTTON_LAYOUT_ID);

        this.addView(this.signatureView);
        this.addView(this.buttonsLayout);
        this.addView(this.infoLayout);

        setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setSaveFileInExtStorage(Boolean saveFileInExtStorage) {
        this.saveFileInExtStorage = saveFileInExtStorage;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;

        if (viewMode.equalsIgnoreCase("portrait")) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (viewMode.equalsIgnoreCase("landscape")) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public void setShowNativeButtons(Boolean showNativeButtons) {
        this.showNativeButtons = showNativeButtons;
        if (showNativeButtons) {
            Log.d("Added Native Buttons", "Native Buttons:" + showNativeButtons);
            buttonsLayout.setVisibility(View.VISIBLE);
        } else {
            buttonsLayout.setVisibility(View.GONE);
        }
    }

    public void setMaxSize(int size) {
        this.maxSize = size;
    }

    private RelativeLayout infoLayout(int buttonViewID) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());

        RelativeLayout containerLayout = new RelativeLayout(this.getContext());
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        containerParams.height = height;
        containerParams.addRule(RelativeLayout.ABOVE, buttonViewID);
        containerLayout.setLayoutParams(containerParams);

        TextView infoTV = new TextView(this.getContext());
        infoTV.setText("เขียนชื่อที่นี่");
        RelativeLayout.LayoutParams infoParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        infoParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        infoTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
        containerLayout.addView(infoTV,infoParams);

        return containerLayout;
    }

    private RelativeLayout buttonsLayout() {

        // create the UI programatically

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics());
        int buttonHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
        int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        int vertivalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        System.out.println("height =>" + height);

        RelativeLayout containerLayout = new RelativeLayout(this.getContext());
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        containerParams.height = height;
        containerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        containerLayout.setLayoutParams(containerParams);


        Button saveBtn = new Button(this.getContext());
        Button clearBtn = new Button(this.getContext());

        containerLayout.setBackgroundColor(Color.parseColor("#f5f5f6"));

        // set texts, tags and OnClickListener
        clearBtn.setText("ลบ");
        clearBtn.setTag("Reset");
        clearBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
        clearBtn.setOnClickListener(this);
        clearBtn.setBackgroundColor(Color.parseColor("#fec221"));
        clearBtn.setHeight(buttonHeight);
        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        leftParams.setMargins(horizontalMargin, vertivalMargin, 0, 0);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        saveBtn.setText("บันทึก");
        saveBtn.setTag("Save");
        saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
        saveBtn.setBackgroundColor(Color.parseColor("#fec221"));
        saveBtn.setHeight(buttonHeight);
        saveBtn.setOnClickListener(this);
        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rightParams.setMargins(0, vertivalMargin, horizontalMargin, 0);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        containerLayout.addView(clearBtn,leftParams);
        containerLayout.addView(saveBtn,rightParams);

        // return the whoe layout
        return containerLayout;
    }

    // the on click listener of 'save' and 'clear' buttons
    @Override public void onClick(View v) {
        String tag = v.getTag().toString().trim();

        // save the signature
        if (tag.equalsIgnoreCase("save")) {
            this.saveImage();
        }

        // empty the canvas
        else if (tag.equalsIgnoreCase("Reset")) {
            this.reset();
        }
    }

    /**
     * save the signature to an sd card directory
     */
    final void saveImage() {

        String root = Environment.getExternalStorageDirectory().toString();

        // the directory where the signature will be saved
        File myDir = new File(root + "/saved_signature");

        // make the directory if it does not exist yet
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // set the file name of your choice
        String fname = "signature.png";

        // in our case, we delete the previous file, you can remove this
        File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }

        try {

            Log.d("React Signature", "Save file-======:" + saveFileInExtStorage);
            // save the signature
            if (saveFileInExtStorage) {
                FileOutputStream out = new FileOutputStream(file);
                this.signatureView.getSignature().compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            }


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap resizedBitmap = getResizedBitmap(this.signatureView.getSignature());
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);


            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            WritableMap event = Arguments.createMap();
            event.putString("pathName", file.getAbsolutePath());
            event.putString("encoded", encoded);
            ReactContext reactContext = (ReactContext) getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "topChange", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getResizedBitmap(Bitmap image) {
        Log.d("React Signature","maxSize:"+maxSize);
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public void reset() {
        if(this.infoLayout.getVisibility()!=View.VISIBLE) {
            this.infoLayout.setVisibility(View.VISIBLE);
        }

        System.out.print("reset =>");
        if (this.signatureView != null) {
            this.signatureView.clearSignature();
        }
    }

    @Override public void onBeginDrag() {
        if(this.infoLayout.getVisibility()!=View.INVISIBLE) {
            this.infoLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override public void onDragged() {

        WritableMap event = Arguments.createMap();
        event.putBoolean("dragged", true);
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "topChange", event);

    }
}
