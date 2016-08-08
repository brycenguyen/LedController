package no.nordicsemi.android.nrftoolbox.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nordicsemi.android.nrftoolbox.R;

/**
 * Created by Administrator on 31/07/2016.
 */
public class RGBModeSetting extends Dialog{

    //TODO : List content is going to be determined later
    private ArrayList<String> mListContent = new ArrayList<String>(
            Arrays.asList(
                    "Flashing",
                    "Continuous",
                    "Single"
            )
    );

    private Context mContext;
    private OnRGBModeListener mListener;
    private ArrayAdapter adapter;

    private ListView listViewMode;
    private String mCurrentModeString = mListContent.get(0);
    private int mChosenItem = 0;

    public RGBModeSetting(Context context, OnRGBModeListener listener) {
        super(context);
        mContext = context;
        mListener = listener;
        setContentView(R.layout.setting_mode_dialog);
        setTitle("Mode Setting");

        adapter = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_list_item_1,mListContent);

        listViewMode = (ListView)this.findViewById(R.id.listMode);
        listViewMode.setAdapter(adapter);
        listViewMode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentModeString = ((TextView)view).getText().toString();
                mListener.modeChanged(mCurrentModeString);
                mChosenItem = position;
                RGBModeSetting.this.dismiss();

            }
        });
    }
    public int getChosenItemIndex(){
        return mChosenItem;
    }
    public void show(){
        super.show();
    }
    public String getCurrentMode(){
        return mCurrentModeString;
    }

}
