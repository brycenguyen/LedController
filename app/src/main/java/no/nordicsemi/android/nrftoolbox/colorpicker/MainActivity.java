package no.nordicsemi.android.nrftoolbox.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import no.nordicsemi.android.nrftoolbox.colorpicker.MyColorPicker;
import no.nordicsemi.android.nrftoolbox.colorpicker.OnColorChangedListener;
import no.nordicsemi.android.nrftoolbox.uart.UARTActivity;//huy: import Uart to send data via BLE

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.uart.UARTInterface;
import no.nordicsemi.android.nrftoolbox.uart.domain.Command;
import no.nordicsemi.android.nrftoolbox.wearable.common.Constants;

public class MainActivity extends AppCompatActivity implements OnColorChangedListener,
        OnRGBSpeedListener,
        OnRGBModeListener {

    //TODO : Should move this into a seperate class
    private TableLayout table;
    private ArrayList<TableRowRGB> list = new ArrayList<TableRowRGB>();
    //private MyColorPicker picker;
    private RGBSpeedSetting setSpeedDialog;
    private RGBModeSetting setModeDialog;
    private RGBCommandBuilder cmdBuilder;
    private TextView txtSpeed;
    private TextView txtMode;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        table = (TableLayout) findViewById(R.id.table_layout);
        setSpeedDialog = new RGBSpeedSetting(this, this);
        setModeDialog = new RGBModeSetting(this, this);
        //picker = new ColorPickerRect(this, this, "COLOR_ADD", Color.BLACK, Color.WHITE);
        cmdBuilder = new RGBCommandBuilder();

        txtSpeed = (TextView) findViewById(R.id.txtSpeed);
        txtSpeed.setText(setSpeedDialog.getCurrentSpeedStr());
        txtMode = (TextView) findViewById(R.id.txtMode);
        txtMode.setText(setModeDialog.getCurrentMode());

    }

    public void openPickerColor(View view) {
        //picker.show();
        new MyColorPicker(this, this, "COLOR_ADD").show();
    }

    public void settingSpeed(View view) {
        setSpeedDialog.show();
    }

    public void settingMode(View view) {
        setModeDialog.show();
    }

    public void sendDataToDevice(View view) {
        ArrayList<String> strListValue = new ArrayList<String>();
        ArrayList<Integer> listColor = new ArrayList<Integer>();
        for(TableRowRGB row : list){
            listColor.add(row.getColor());
        }

        byte[] dataArr;
        dataArr = cmdBuilder.getCommand(listColor,setSpeedDialog.getChosenItemIndex(),setModeDialog.getChosenItemIndex());
        //Toast.makeText(getApplicationContext(),dataArr.toString(),Toast.LENGTH_LONG).show();
        //huy: send data via BLE
        Intent intent = new Intent();
        intent.putExtra("LED SETTING",dataArr);
        setResult(2,intent);
        finish();//finishing activity
    }


    @Override
    public void colorChanged(String key, int color) {
        if ("COLOR_ADD".equals(key)) {
            TableRowRGB row = new TableRowRGB(this, color);
            list.add(row);
            updateSetting();
        }
    }

    @Override
    public void speedChanged(String strSpeed) {
        txtSpeed.setText(strSpeed);
    }


    public void updateSetting() {
        table.removeAllViews();
        for (TableRowRGB r : list) {
            table.addView(r);
        }
    }

    @Override
    public void modeChanged(String str) {
        txtMode.setText(str);
    }

    @Override
    public void onStart() {
        super.onStart();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://no.nordicsemi.android.nrftoolbox.colorpicker/http/host/path")
        );

    }

    @Override
    public void onStop() {
        super.onStop();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://no.nordicsemi.android.nrftoolbox.colorpicker/http/host/path")
        );

    }


    private class TableRowRGB extends TableRow implements OnColorChangedListener, View.OnClickListener {

        Button mButton;
        TextView mText;
        Context mContext;
        OnColorChangedListener mListener;
        Dialog dialog;
        int mColor;

        public TableRowRGB(Context context, int color) {
            super(context);
            mButton = new Button(context);
            mText = new TextView(context);
            mText.setText("R:" + Color.red(color) + " G:" + Color.green(color) + " B:" + Color.blue(color));
            mContext = context;
            mListener = this;
            mColor = color;
            mButton.setBackgroundColor(mColor);

            addView(mButton);
            addView(mText);
            setOnClickListener(this);
        }
        public int getColor(){
            return mColor;
        }
        @Override
        public void colorChanged(String key, int color) {
            if ("COLOR_CHANGED".equals(key)) {
                mColor = color;
                mButton.setBackgroundColor(mColor);
                mText.setText("R:" + Color.red(color) + " G:" + Color.green(color) + " B:" + Color.blue(color));

                removeAllViews();
                addView(mButton);
                addView(mText);
            }
        }
        @Override
        public void onClick(View v) {
            dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.color_modification);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setTitle("Color Setting");
            dialog.show();

            Button btnRemove = (Button) dialog.findViewById(R.id.btnRemoveColor);
            btnRemove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.remove(TableRowRGB.this);
                    updateSetting();
                    dialog.dismiss();
                }
            });

            Button btnChange = (Button) dialog.findViewById(R.id.btnChangeColor);
            btnChange.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyColorPicker picker = new MyColorPicker(mContext, mListener, "COLOR_CHANGED");
                    picker.show();
                    list.set(list.indexOf(TableRowRGB.this), TableRowRGB.this);
                    updateSetting();
                    dialog.dismiss();
                }
            });
            //return false;
        }
    }

}
