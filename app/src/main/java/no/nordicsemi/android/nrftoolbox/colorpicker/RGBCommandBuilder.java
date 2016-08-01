package no.nordicsemi.android.nrftoolbox.colorpicker;

import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Administrator on 31/07/2016.
 */
public class RGBCommandBuilder {
    public String getCommand(ArrayList<String> listRow, String strSpeed, String strMode){
        String strValue = "";
        strValue += "Speed: "+strSpeed + "--";
        strValue += "Mode: "+strMode + "--";
        strValue += "Value: ";

        for(String r : listRow){
            strValue += " " + r;
        }
        return strValue;
    }
}
