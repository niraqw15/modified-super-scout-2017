package com.example.sam.blutoothsocketreceiver;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by sam on 5/11/16.
 */
public class Counter extends RelativeLayout {
    private String dataName;
    private int max;
    private int min;
    private int increment;
    private int value;
    private int startingValue;
    TextView counterTextView;
    TextView counterTitleTextView;
    Button addButton;
    Button subtractButton;
    SuperScoutingPanel superScoutingPanel;
    Boolean oneApplied;
    Boolean twoApplied;
    Boolean threeApplied;
    Boolean fourApplied;

    public Counter(Context context, AttributeSet attrs) {
        super(context, attrs);

        superScoutingPanel = new SuperScoutingPanel();

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.counter, this, true);

        counterTextView = (TextView)findViewById(R.id.scoreCounter);
        counterTitleTextView = (TextView)findViewById(R.id.dataName);
        addButton = (Button)findViewById(R.id.plusButton);
        subtractButton = (Button)findViewById(R.id.minusButton);


        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Counter,
                0, 0);

        try {
            this.dataName = a.getString(R.styleable.Counter_dataName);
            this.max = a.getInt(R.styleable.Counter_max, 4);
            this.min = a.getInt(R.styleable.Counter_min, 0);
            this.startingValue = a.getInt(R.styleable.Counter_startingValue, 2);
            this.increment = a.getInt(R.styleable.Counter_increment, 1);

            this.value = this.startingValue;

            Log.e("start value", counterTextView.getText().toString());

            listenForAddClicked();
            listenForMinusClicked();

            refreshCounter(startingValue);

            Log.e("start value1", counterTextView.getText().toString());


        } finally {
            a.recycle();
        }
    }

    private void listenForAddClicked(){
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("add", "clicked");
                Log.e("increment", increment + "");
                Log.e("new value", Integer.toString(value + increment));
                if (value + increment <= max) {
                    value += increment;
                    refreshCounter(value);
                    Log.e("add", "clicked");
                    Integer previousValue = value - 1;
                    makeArrayChanges(value, previousValue);
                }
            }
        });
    }

    public void makeArrayChanges(Integer currentValue, Integer previousValue){
        Log.i("HATRED DATANAME", dataName);
        switch(dataName){
            case "Defense" : {
                Log.i("HATRED", "DEFENSE");
                SuperScoutingPanel.Defense.set(currentValue, SuperScoutingPanel.Defense.get(currentValue) + 1);
                SuperScoutingPanel.Defense.set(previousValue, SuperScoutingPanel.Defense.get(previousValue) - 1);
                break;
            }
            case "Agility" : {
                Log.i("HATRED", "AGILITY");
                SuperScoutingPanel.Agility.set(currentValue, SuperScoutingPanel.Agility.get(currentValue) + 1);
                SuperScoutingPanel.Agility.set(previousValue, SuperScoutingPanel.Agility.get(previousValue) - 1);
                break;
            }
            case "Ball Control" : {
                Log.i("HATRED", "BALL CTRL");
                SuperScoutingPanel.BallControl.set(currentValue, SuperScoutingPanel.BallControl.get(currentValue) + 1);
                SuperScoutingPanel.BallControl.set(previousValue, SuperScoutingPanel.BallControl.get(previousValue) - 1);
                break;
            }
            //TODO: change gear control to torque, and find where SuperScoutingPanel.GearControl is so I can change/copy and change it
            /* case "Gear Control" : {
                Log.i("HATRED", "GEAR CTRL");
                SuperScoutingPanel.GearControl.set(currentValue, SuperScoutingPanel.GearControl.get(currentValue) + 1);
                SuperScoutingPanel.GearControl.set(previousValue, SuperScoutingPanel.GearControl.get(previousValue) - 1);
                break;
            } */
            case "Torque" : {
                Log.i("HATRED", "TORQUE");
                SuperScoutingPanel.Torque.set(currentValue, SuperScoutingPanel.Torque.get(currentValue) + 1);
                SuperScoutingPanel.Torque.set(previousValue, SuperScoutingPanel.Torque.get(previousValue) - 1);
                break;
            }
            case "Speed" : {
                Log.i("HATRED", "SPEED");
                SuperScoutingPanel.Speed.set(currentValue, SuperScoutingPanel.Speed.get(currentValue) + 1);
                SuperScoutingPanel.Speed.set(previousValue, SuperScoutingPanel.Speed.get(previousValue) - 1);
                break;
            }
        }
        for(int a = 0; a <= 4; a++){
            Log.i("ARRAYSINFO SPEED" + a, String.valueOf(SuperScoutingPanel.Speed.get(a)));
            Log.i("ARRAYSINFO DEFENSE" + a, String.valueOf(SuperScoutingPanel.Defense.get(a)));
            Log.i("ARRAYSINFO BALL CTRL" + a, String.valueOf(SuperScoutingPanel.BallControl.get(a)));
            //Log.i("ARRAYSINFO GEAR CTRL" + a, String.valueOf(SuperScoutingPanel.GearControl.get(a)));
            Log.i("ARRAYSINFO TORQUE" + a, String.valueOf(SuperScoutingPanel.Torque.get(a)));
            Log.i("ARRAYSINFO AGILITY" + a, String.valueOf(SuperScoutingPanel.Agility.get(a)));
        }
    }

    private void listenForMinusClicked(){
        subtractButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (value - increment >= min) {
                    value -= increment;
                    refreshCounter(value);
                    Log.e("subtract", "clicked");
                    Integer previousValue = value + 1;
                    makeArrayChanges(value, previousValue);
                }
            }
        });
    }


    private void refreshCounter(int someValue) {
        Log.e("test2", counterTitleTextView.getText().toString());
        counterTitleTextView.setText(dataName);
        counterTextView.setText(someValue + "");
    }

    public String getDataName() {
        return dataName;
    }

    public Integer getDataValue() {
        return value;
    }
}
