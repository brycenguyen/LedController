package no.nordicsemi.android.nrftoolbox.colorpicker;

import android.graphics.Color;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Administrator on 31/07/2016.
 */
/*
* Command format : 'S' ll ss mm rgb rgb rgb
* 'S' : Start package , 1 byte
* ll  : package len   , 1 byte
* ss  : speed setting , 1 byte
* mm  : mode setting  , 1 byte
* rgb : color, red green blue, 3 byte
* */
public class RGBCommandBuilder {
    private final int L_START_PACKAGE = 1;
    private final int L_PACKAGE_LEN = 1;
    private final int L_SPEED_SETTING = 1;
    private final int L_MODE_SETTING = 1;
    private final int L_PREFIX  =   L_START_PACKAGE + L_PACKAGE_LEN + L_SPEED_SETTING + L_MODE_SETTING;
    public byte[] getCommand(ArrayList<Integer> listColor, int speed, int mode){
        byte[] commandArr = new byte[listColor.size()*3 + L_PREFIX];

        commandArr[0] = 'S';
        commandArr[1] = (byte)commandArr.length;
        commandArr[2] = (byte)speed;
        commandArr[3] = (byte)mode;
        int indx = L_PREFIX;
        for(Integer i : listColor){
            commandArr[indx++] = (byte)Color.red(i);
            commandArr[indx++] = (byte)Color.green(i);
            commandArr[indx++] = (byte)Color.blue(i);
        }
        return commandArr;
    }
}
