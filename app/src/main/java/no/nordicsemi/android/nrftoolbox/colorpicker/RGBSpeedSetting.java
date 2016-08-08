package no.nordicsemi.android.nrftoolbox.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.nordicsemi.android.nrftoolbox.R;

/**
 * Created by Administrator on 31/07/2016.
 */
public class RGBSpeedSetting extends Dialog{

    private ArrayList<String>  mListContent = new ArrayList<String>(
            Arrays.asList(
                    "Low",
                    "Medium",
                    "High"
            )

    );
    private String mCurrentSpeedStr = mListContent.get(0);
    private Context mContext;
    private OnRGBSpeedListener mListener;
    private ListView mListSpeed;
    private ArrayAdapter adapter = null;
    private int mChosenItem = 0;

    public RGBSpeedSetting(Context context, final OnRGBSpeedListener listener) {
        super(context);
        mContext = context;
        mListener = listener;
        setContentView(R.layout.setting_speed_dialog);
        setTitle("Speed setting");

        setCanceledOnTouchOutside(true);
        mListSpeed = (ListView)this.findViewById(R.id.lSpeed);
        adapter = new ArrayAdapter(mContext,android.R.layout.simple_list_item_1,mListContent);
        mListSpeed.setAdapter(adapter);
        mListSpeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RGBSpeedSetting.this.dismiss();
                mCurrentSpeedStr = ((TextView)view).getText().toString();
                mChosenItem = position;
                listener.speedChanged(mCurrentSpeedStr);
            }
        });
    }
    public int getChosenItemIndex(){
        return mChosenItem;
    }
    @Override
    public void show(){

        super.show();


    }

    public  String getCurrentSpeedStr(){
        return mCurrentSpeedStr;
    }

}
