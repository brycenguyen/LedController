package no.nordicsemi.android.nrftoolbox.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import no.nordicsemi.android.nrftoolbox.colorpicker.ColorPickerRect;
import no.nordicsemi.android.nrftoolbox.colorpicker.OnColorChangedListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.nrftoolbox.R;

public class MainActivity extends AppCompatActivity implements  OnColorChangedListener,
                                                                OnRGBSpeedListener,
                                                                OnRGBModeListener{

    //TODO : Should move this into a seperate class
    private TableLayout table;
    private ArrayList<TableRowRGB> list = new ArrayList<TableRowRGB>();
    private ColorPickerRect picker;
    private RGBSpeedSetting setSpeedDialog;
    private RGBModeSetting setModeDialog;
    private RGBCommandBuilder cmdBuilder;

    private String strSettingValue = "";

    private TextView txtSpeed;
    private TextView txtMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        table = (TableLayout) findViewById(R.id.table_layout);
        setSpeedDialog = new RGBSpeedSetting(this,this);
        setModeDialog = new RGBModeSetting(this, this);
        picker = new ColorPickerRect(this, this, "COLOR_ADD", Color.BLACK, Color.WHITE);
        cmdBuilder = new RGBCommandBuilder();

        txtSpeed = (TextView)findViewById(R.id.txtSpeed);
        txtSpeed.setText(setSpeedDialog.getCurrentSpeedStr());
        txtMode = (TextView)findViewById(R.id.txtMode);
        txtMode.setText(setModeDialog.getCurrentMode());


    }

    public void openPickerColor(View view) {
        picker.show();
    }
    public void settingSpeed(View view) {
        setSpeedDialog.show();
    }
    public void settingMode(View view){
        setModeDialog.show();
    }
    public void sendDataToDevice(View view){
        ArrayList<String> strListValue = new ArrayList<String>();
        for(TableRowRGB row : list){
            String str = ((TextView)row.getChildAt(1)).getText().toString();
            strListValue.add(str);
        }
        strSettingValue = cmdBuilder.getCommand(strListValue,setSpeedDialog.getCurrentSpeedStr(),setModeDialog.getCurrentMode());
        Toast.makeText(getApplicationContext(),strSettingValue,Toast.LENGTH_LONG).show();
    }

    //Call this function
    public  String getColorSetting(){
        return strSettingValue;
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


    private class TableRowRGB extends TableRow implements OnColorChangedListener, View.OnClickListener {

        Button mButton;
        TextView mText;
        Context mContext;
        OnColorChangedListener mListener;
        int mColor;
        Dialog dialog;

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
                    ColorPickerRect picker = new ColorPickerRect(mContext, mListener, "COLOR_CHANGED", Color.BLACK, Color.WHITE);
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
