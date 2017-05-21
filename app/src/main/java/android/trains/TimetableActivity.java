package android.trains;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The class that is used to display the timetable for a given stop and a given line
 */
public class TimetableActivity extends AppCompatActivity {
    String line;                //name of given line
    String stopID;              //ID of given stop
    String stopName;            //name of given stop
    String urlAddress = "https://still-reef-32346.herokuapp.com";

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
                Date date = simpleDateFormat.parse(dateString);             //date will be use for changing color of trains which already gone
                for(int i = 0; i<24; i++)
                {
                    TableRow TR=(TableRow) table.getChildAt(i);
                    if (Integer.parseInt(dateStringparts[0])==i)
                    {
                        TextView TV= new TextView(this);
                        TV.setText(TV.getText() + "   " + dateStringparts[1]);      //display only minutes because hour is display at the beginning of the row
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
