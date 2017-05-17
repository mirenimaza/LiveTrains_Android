package android.trains;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimetableActivity extends AppCompatActivity {

    String line;
    String stopID;
    String stopName;
    String urlAddress = "https://still-reef-32346.herokuapp.com";
    int IDRowInt[] =  {R.id.row0, R.id.row1, R.id.row2, R.id.row3, R.id.row4, R.id.row5, R.id.row6, R.id.row7, R.id.row8, R.id.row9, R.id.row10, R.id.row11, R.id.row12, R.id.row13, R.id.row14, R.id.row15, R.id.row16, R.id.row17, R.id.row18, R.id.row19, R.id.row20, R.id.row21, R.id.row22, R.id.row23};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        line = intent.getStringExtra("line");
        stopID = intent.getStringExtra("stopID");
        stopName = intent.getStringExtra("stopName");
        setTitle(stopName + " (linia: " + line + ")");

        JSONTask task = new JSONTask(urlAddress + "/stop/"+ stopID +"/line/" + line + "/timetable");
        task.execute();
        String jsonString = task.jsonResult;
        while (jsonString == null)
        {
            jsonString = task.jsonResult;
        }
        task.cancel(true);
        JSONArray array = null;
        try {
            array = (JSONArray) new JSONParser().parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int n = 0; n < array.size(); n++) {
            JSONObject object = (JSONObject) array.get(n);
            String dateString = (String)object.get("time");
            String[] dateStringparts = dateString.split(":");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            TableLayout table = (TableLayout) findViewById(R.id.table);
            try
            {
                Date date = simpleDateFormat.parse(dateString);
                for(int i = 0; i<24; i++)
                {
                    TableRow TR=(TableRow) table.getChildAt(i);
                    if (Integer.parseInt(dateStringparts[0])==i)
                    {
                        TextView TV= new TextView(this);
                        TV.setText(TV.getText() + "   " + dateStringparts[1]);
                        TR.addView(TV);
                    }
                }
            }
            catch (java.text.ParseException e) {
                e.printStackTrace();
            }

        }
    }

}
