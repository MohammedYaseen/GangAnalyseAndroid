package com.davengo.ga;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import com.davengo.ga.common.CommonContext;

public class SettingsActivity extends AppCompatActivity {

    private  EditText rightSoleEditText;
    private  EditText leftSoleEditText;



    private EditText leftFullMaxEditText;

    private EditText leftFullMinEditText;

    private EditText leftHeelMaxEditText;

    private EditText leftHeelMinEditText;

    private EditText rightFullMaxEditText;

    private EditText rightFullMinEditText;

    private EditText rightHeelMaxEditText;

    private EditText rightHeelMinEditText;

    private EditText leftSmoothingEditText;

    private EditText rightSmoothingEditText;

    private EditText leftZeroLineValueEditText;

    private EditText rightZeroLineValueEditText;

    private CheckBox lowPassFilterCheckBox;

    private CheckBox zeroLineFilterCheckBox;

    private CommonContext commonContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        commonContext = CommonContext.getInstance();

        rightSoleEditText = (EditText) findViewById(R.id.rightSoleEditText);
        leftSoleEditText = (EditText) findViewById(R.id.leftSoleEditText);

        leftFullMaxEditText = (EditText) findViewById(R.id.leftFullMaxEditText);

        leftFullMinEditText = (EditText) findViewById(R.id.leftFullMinEditText);

        leftHeelMaxEditText = (EditText) findViewById(R.id.leftHeelMaxEditText);

        leftHeelMinEditText = (EditText) findViewById(R.id.leftHeelMinEditText);

        rightFullMaxEditText = (EditText) findViewById(R.id.rightFullMaxEditText);

        rightFullMinEditText = (EditText) findViewById(R.id.rightFullMinEditText);

        rightHeelMaxEditText = (EditText) findViewById(R.id.rightHeelMaxEditText);

        rightHeelMinEditText = (EditText) findViewById(R.id.rightHeelMinEditText);

        leftSmoothingEditText = (EditText) findViewById(R.id.leftSmoothingEditText);

        rightSmoothingEditText = (EditText) findViewById(R.id.rightSmoothingEditText);

        leftZeroLineValueEditText = (EditText) findViewById(R.id.leftZeroLineValueEditText);

        rightZeroLineValueEditText = (EditText) findViewById(R.id.rightZeroLineValueEditText);

        lowPassFilterCheckBox = (CheckBox) findViewById(R.id.lowPassFilterCheckBox);

        zeroLineFilterCheckBox = (CheckBox) findViewById(R.id.zeroLineFilterCheckBox);

        rightSoleEditText.setText(commonContext.getConfiguration().getInsoles().getRightInsole().getBluetoothID());
        leftSoleEditText.setText(commonContext.getConfiguration().getInsoles().getLeftInsole().getBluetoothID());
        leftFullMaxEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getLeftInsole().getFullMax()));
        leftFullMinEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getLeftInsole().getFullMin()));
        leftHeelMaxEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getLeftInsole().getHeelMax()));
        leftHeelMinEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getLeftInsole().getFullMin()));
        rightFullMaxEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getRightInsole().getFullMax()));
        rightFullMinEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getRightInsole().getFullMin()));
        rightHeelMaxEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getRightInsole().getHeelMax()));
        rightHeelMinEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getRightInsole().getHeelMin()));
        leftSmoothingEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getLeftInsole().getSmoothing()));
        rightSmoothingEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getRightInsole().getSmoothing()));
        leftZeroLineValueEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getLeftInsole().getZeroLineValue()));
        rightZeroLineValueEditText.setText(String.valueOf(commonContext.getConfiguration().getInsoles().getRightInsole().getZeroLineValue()));

        lowPassFilterCheckBox.setChecked(commonContext.getConfiguration().getFilterOptions().isSimpleLowPass());
        zeroLineFilterCheckBox.setChecked(commonContext.getConfiguration().getFilterOptions().isZeroLine());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
