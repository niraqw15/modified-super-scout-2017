package com.example.sam.blutoothsocketreceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.example.sam.blutoothsocketreceiver.firebase_classes.Match;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {
    protected NQWSuperScoutApplication app;
    Activity context;
    EditText numberOfMatch;
    EditText teamNumberOne;
    EditText teamNumberTwo;
    EditText teamNumberThree;
    EditText searchBar;
    TextView alliance;
    ListView listView;
    Boolean isRed = false;
    Integer matchNumber = 0;
    DatabaseReference dataBase;
    String firstKey;
    String keys;
    String scoutAlliance;
    String previousScore, previousFoul;
    Boolean previous40kpa;
    Integer previousRotorNumAuto;
    Integer previousRotorNumTele;
    final static String dataBaseUrl = Constants.dataBaseUrl;
    int matchNum;
    int stringIndex;
    int intIndex;
    ArrayList<String> keysInKey;
    ArrayList<String> valueOfKeys;
    ArrayList<String> checkNumKeys;
    ArrayList<String> checkStringKeys;
    boolean isMute = false;
    boolean isOverriden;
    ToggleButton mute;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("test", "Logcat is up and running!");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        context = this;
        isOverriden = false;

        Constants.teamOneNoteHolder = "";
        Constants.teamTwoNoteHolder = "";
        Constants.teamThreeNoteHolder = "";

        numberOfMatch = (EditText) findViewById(R.id.matchNumber);
        teamNumberOne = (EditText) findViewById(R.id.teamOneNumber);
        teamNumberTwo = (EditText) findViewById(R.id.teamTwoNumber);
        teamNumberThree = (EditText) findViewById(R.id.teamThreeNumber);
        mute = (ToggleButton) findViewById(R.id.mute);
        alliance = (TextView) findViewById(R.id.allianceName);
        dataBase = FirebaseDatabase.getInstance().getReference();
        //If got intent from the last activity
        checkPreviousMatchNumAndAlliance();
        updateUI();
        numberOfMatch.setText(matchNumber.toString());
        matchNumber = Integer.parseInt(numberOfMatch.getText().toString());
        disenableEditTextEditing();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView = (ListView) findViewById(R.id.view_files_received);
        listView.setAdapter(adapter);
        updateListView();
        updateListView();


        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    updateUI();
                } catch (NullPointerException NPE) {
                    toasts("Teams not available", true);
                }
            }
        }, new IntentFilter("matches_updated"));

        //Change team numbers as the user changes the match number
        changeTeamsByMatchName();
        commitSharedPreferences();

        listenForResendClick();
        listLongClick();


    }

//resends all data on the currently viewed list of data
    public void resendAllClicked(View view) {
        new AlertDialog.Builder(this)
                .setTitle("RESEND ALL?")
                .setMessage("RESEND ALL DATA?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        List<JSONObject> dataPoints = new ArrayList<>();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            String content;
                            String name = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Super_scout_data/" + adapter.getItem(i);
                            content = readFile(name);
                            if (content != null) {
                                try {
                                    JSONObject data = new JSONObject(content);
                                    dataPoints.add(data);
                                } catch (JSONException jsone) {
                                    Log.i("JSON info", "Failed to parse JSON for resend all. unimportant");
                                }
                            }
                        }
                        resendSuperData(dataPoints);

                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void getSuperData(View view) {
        searchBar = (EditText) findViewById(R.id.searchEditText);
        searchBar.setFocusable(false);
        //listenForFileListClick();
        updateListView();
        searchBar.setFocusableInTouchMode(true);
    }

    public void catClicked(View view){
        if(mute.isChecked()){
           //Don't Do anything
            isMute = true;
        }else {
            isMute = false;
            int randNum = (int) (Math.random() * 3);
            playSound(randNum);
            Log.e("number", randNum + "");
            Log.e("cat", "sound");
        }
    }
    public void playSound(int playTrak){
        if (playTrak == 0){
            MediaPlayer mp = MediaPlayer.create(this, R.raw.catsound);
            mp.start();
        }else if(playTrak == 1){
            MediaPlayer mp = MediaPlayer.create(this, R.raw.catsound2);
            mp.start();
        }else if(playTrak == 2){
            MediaPlayer mp = MediaPlayer.create(this, R.raw.dog);
            mp.start();
        }else if(playTrak == 3){
            MediaPlayer mp = MediaPlayer.create(this, R.raw.kittenmeow);
            mp.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scout, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.changeAlliance) {
            isRed = !isRed;
            NQWSuperScoutApplication.isRed = true;
            commitSharedPreferences();
            updateUI();
        }
        if (id == R.id.scout) {
            if (!FirebaseLists.matchesList.getKeys().contains(matchNumber.toString()) && !isOverriden){
                Toast.makeText(context, "This Match Does Not Exist!", Toast.LENGTH_LONG).show();
                disenableEditTextEditing();
            }else{
                if (numberOfMatch.getText().toString().equals("")) {
                    Toast.makeText(context, "Input match name!", Toast.LENGTH_SHORT).show();
                } else if (teamNumberOne.getText().toString().equals("")) {
                    Toast.makeText(context, "Input team one number!", Toast.LENGTH_SHORT).show();
                } else if (teamNumberTwo.getText().toString().equals("")) {
                    Toast.makeText(context, "Input team two number!", Toast.LENGTH_SHORT).show();
                } else if (teamNumberThree.getText().toString().equals("")) {
                    Toast.makeText(context, "Input team three number!", Toast.LENGTH_SHORT).show();
                } //else if(teamNumberOne.getText().toString().equals("Not Available")){
//                    Toast.makeText(context, "This Match Does Not Exist!", Toast.LENGTH_SHORT).show();
//                }
                else {
                    commitSharedPreferences();
                    Intent intent = new Intent(context, ScoutingPage.class); //Was: "ScoutingPage.class" Needs to be: "FieldSetUp.class" TODO: Change this to FieldSetUp stuff.
                    //Intent intent = new Intent(context, FieldSetUp.class);

                    intent.putExtra("matchNumber", numberOfMatch.getText().toString());
                    intent.putExtra("teamNumberOne", teamNumberOne.getText().toString());
                    intent.putExtra("teamNumberTwo", teamNumberTwo.getText().toString());
                    intent.putExtra("teamNumberThree", teamNumberThree.getText().toString());
                    intent.putExtra("alliance", alliance.getText().toString());
                    intent.putExtra("dataBaseUrl", dataBaseUrl);
                    intent.putExtra("mute", isMute);
                    intent.putExtra("allianceColor", isRed);
                    Log.e("start alliance", alliance.getText().toString());
                    startActivity(intent);
                }
            }

        } else if (id == R.id.action_override) {
            if (item.getTitle().toString().equals("Override Match and Team Number")) {
                enableEditTextEditing();
                item.setTitle("Automate");
            } else if (item.getTitle().toString().equals("Automate")) {
                View view = context.getCurrentFocus();
                updateUI();
                commitSharedPreferences();
                disenableEditTextEditing();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                item.setTitle("Override Match and Team Number");
            }

        }
            return super.onOptionsItemSelected(item);
    }

    public void updateListView() {

        final EditText searchBar = (EditText)findViewById(R.id.searchEditText);
        final File dir;
        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Super_scout_data");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        final File[] files = dir.listFiles();
        adapter.clear();
        for (File tmpFile : files) {
            adapter.add(tmpFile.getName());
        }
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence Register, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchBar.getText().toString().equals("")){
                    adapter.clear();
                    searchBar.setFocusable(false);
                    for (File tmpFile : files) {
                        adapter.add(tmpFile.getName());
                    }
                    searchBar.setFocusableInTouchMode(true);
                    adapter.sort(new Comparator<String>() {
                        @Override
                        public int compare(String lhs, String rhs) {
                            File lhsFile = new File(dir, lhs);
                            File rhsFile = new File(dir, rhs);
                            Date lhsDate = new Date(lhsFile.lastModified());
                            Date rhsDate = new Date(rhsFile.lastModified());
                            return rhsDate.compareTo(lhsDate);
                        }
                    });
                }else{
                    for (int i = 0; i < adapter.getCount();){
                        if(adapter.getItem(i).startsWith((searchBar.getText().toString()).toUpperCase()) || adapter.getItem(i).contains((searchBar.getText().toString()).toUpperCase())){
                            i++;
                        }else{
                            adapter.remove(adapter.getItem(i));
                        }
                    }
                }
            }
        });
        adapter.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                File lhsFile = new File(dir, lhs);
                File rhsFile = new File(dir, rhs);
                Date lhsDate = new Date(lhsFile.lastModified());
                Date rhsDate = new Date(rhsFile.lastModified());
                return rhsDate.compareTo(lhsDate);
            }
        });
        adapter.notifyDataSetChanged();
    }
//updates the team numbers in the front screen according to the match number and the alliance;
    private void updateUI() {
        try {
            if (FirebaseLists.matchesList.getKeys().contains(matchNumber.toString())) {
                Match match = FirebaseLists.matchesList.getFirebaseObjectByKey(matchNumber.toString());

                List<Integer> teamsOnAlliance = new ArrayList<>();
                teamsOnAlliance.addAll((isRed) ? match.redAllianceTeamNumbers : match.blueAllianceTeamNumbers);
                alliance.setTextColor((isRed) ? Color.RED : Color.BLUE);
                alliance.setText((isRed) ? "Red Alliance" : "Blue Alliance");

                teamNumberOne.setText(teamsOnAlliance.get(0).toString());
                teamNumberTwo.setText(teamsOnAlliance.get(1).toString());
                teamNumberThree.setText(teamsOnAlliance.get(2).toString());
                teamNumberOne.setHint("");
                teamNumberTwo.setHint("");
                teamNumberThree.setHint("");

            } else {
                teamNumberOne.setHint("Not Available");
                teamNumberTwo.setHint("Not Available");
                teamNumberThree.setHint("Not Available");
                teamNumberOne.setText("");
                teamNumberTwo.setText("");
                teamNumberThree.setText("");

            }
        }catch(NullPointerException NPE){
            toasts("Teams not available", true);
        }
    }

    public void commitSharedPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences("prefs", MODE_PRIVATE).edit();
        editor.putInt("match_number", matchNumber);
        editor.putBoolean("allianceColor", isRed);
        editor.commit();
    }

    //changes the team numbers while the user changes the match number
    public void changeTeamsByMatchName() {
        EditText numberOfMatch = (EditText) findViewById(R.id.matchNumber);
        numberOfMatch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    matchNumber = Integer.parseInt(s.toString());
                } catch (NumberFormatException NFE) {
                    matchNumber = 0;
                }
                updateUI();
            }
        });
    }

    public void enableEditTextEditing() {

        numberOfMatch.setFocusableInTouchMode(true);
        teamNumberOne.setFocusableInTouchMode(true);
        teamNumberTwo.setFocusableInTouchMode(true);
        teamNumberThree.setFocusableInTouchMode(true);
        isOverriden = true;
    }

    public void disenableEditTextEditing() {

        numberOfMatch.setFocusable(false);
        teamNumberOne.setFocusable(false);
        teamNumberTwo.setFocusable(false);
        teamNumberThree.setFocusable(false);
        isOverriden = false;
    }
//reads the data of the clicked file
    public String readFile(String name) {
        BufferedReader file;
        try {
            file = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(name))));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Open File");
            Toast.makeText(context, "Failed To Open File", Toast.LENGTH_LONG).show();
            return null;
        }
        String dataOfFile = "";
        String buf;
        try {
            while ((buf = file.readLine()) != null) {
                dataOfFile = dataOfFile.concat(buf + "\n");
            }
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Read From File");
            Toast.makeText(context, "Failed To Read From File", Toast.LENGTH_LONG).show();
            return null;
        }
        Log.i("fileData", dataOfFile);
        return dataOfFile;
    }

//converts jsonArrays to arrays
//    public List<Object> jsonArrayToArray(JSONArray array) {
//        List<Object> os = new ArrayList<>();
//        for (int i = 0; i < array.length(); i++) {
//            try {
//                os.add(array.get(i));
//            } catch (Exception e) {
//                //do nothing
//            }
//        }
//        return os;
//    }

    public void resendSuperData(final List<JSONObject> dataPoints) {
        new Thread() {
            @Override
            public void run() {
                //read data from file
                for (int j = 0; j < dataPoints.size(); j++) {

                    Log.e("Beginning", "Test here");
                    Log.e("Test 1", "super file is not null!");
                    try {
                        Log.e("Test 2", "assign file data to Json");
                        JSONObject superData = dataPoints.get(j);

                        String teamOneFirstNotes = superData.getString("teamOneFirstNotes");
                        String teamTwoFirstNotes = superData.getString("teamTwoFirstNotes");
                        String teamThreeFirstNotes = superData.getString("teamThreeFirstNotes");
                        String teamOneFinalNotes = superData.getString("teamOneFinalNotes");
                        String teamTwoFinalNotes = superData.getString("teamTwoFinalNotes");
                        String teamThreeFinalNotes = superData.getString("teamThreeFinalNotes");

                        String matchNum = superData.get("matchNumber").toString();
                        String matchAndTeamOne = superData.get("teamOne") + "Q" + matchNum;
                        String matchAndTeamTwo = superData.get("teamTwo") + "Q" + matchNum;
                        String matchAndTeamThree = superData.get("teamThree") + "Q" + matchNum;
                        String teamOneNumber = superData.getString("teamOne");
                        String teamTwoNumber = superData.getString("teamTwo");
                        String teamThreeNumber = superData.getString("teamThree");
                        JSONObject teamOneData = superData.getJSONObject(teamOneNumber);
                        JSONObject teamTwoData = superData.getJSONObject(teamTwoNumber);
                        JSONObject teamThreeData = superData.getJSONObject(teamThreeNumber);

                        JSONObject teamOneKeyNames = new JSONObject(teamOneData.toString());
                        JSONObject teamTwoKeyNames = new JSONObject(teamTwoData.toString());
                        JSONObject teamThreeKeyNames = new JSONObject(teamThreeData.toString());

                        Iterator getTeamOneKeys = teamOneKeyNames.keys();
                        Iterator getTeamTwoKeys = teamTwoKeyNames.keys();
                        Iterator getTeamThreeKeys = teamThreeKeyNames.keys();
                        ArrayList<Integer> teamNumbers = new ArrayList<>(Arrays.asList(Integer.valueOf(teamOneNumber), Integer.valueOf(teamTwoNumber), Integer.valueOf(teamThreeNumber)));

                        for (int i = 0; i < teamNumbers.size(); i++){
                            dataBase.child("TeamInMatchDatas").child(teamNumbers.get(i) + "Q" + matchNum).child("teamNumber").setValue(teamNumbers.get(i));
                            dataBase.child("TeamInMatchDatas").child(teamNumbers.get(i) + "Q" + matchNum).child("matchNumber").setValue(Integer.parseInt(matchNum));
                        }

                        while (getTeamOneKeys.hasNext()) {
                            String teamOneKeys = (String) getTeamOneKeys.next();
                            dataBase.child("TeamInMatchDatas").child(matchAndTeamOne).child(teamOneKeys).setValue(Integer.parseInt(teamOneData.get(teamOneKeys).toString()));
                        }
                        while (getTeamTwoKeys.hasNext()) {
                            String teamTwoKeys = (String) getTeamTwoKeys.next();
                            dataBase.child("TeamInMatchDatas").child(matchAndTeamTwo).child(teamTwoKeys).setValue(Integer.parseInt(teamTwoData.get(teamTwoKeys).toString()));
                        }
                        while (getTeamThreeKeys.hasNext()) {
                            String teamThreeKeys = (String) getTeamThreeKeys.next();
                            dataBase.child("TeamInMatchDatas").child(matchAndTeamThree).child(teamThreeKeys).setValue(Integer.parseInt(teamThreeData.get(teamThreeKeys).toString()));
                        }
                        if (!isRed){
                            dataBase.child("Matches").child(matchNum).child("number").setValue(Integer.valueOf(matchNum));
                            dataBase.child("Matches").child(matchNum).child("blueAllianceTeamNumbers").setValue(teamNumbers);
                            dataBase.child("Matches").child(matchNum).child("blueScore").setValue(Integer.parseInt(superData.get("Blue Alliance Score").toString()));
                            dataBase.child("Matches").child(matchNum).child("didReach40KiloPascalsBlue").setValue(Boolean.valueOf((String) superData.get("boilerRPGained")));
                            dataBase.child("Matches").child(matchNum).child("didStartAllRotorsBlue").setValue(Boolean.valueOf((String) superData.get("rotorRPGained")));
                        }else{
                            dataBase.child("Matches").child(matchNum).child("number").setValue(Integer.valueOf(matchNum));
                            dataBase.child("Matches").child(matchNum).child("redAllianceTeamNumbers").setValue(teamNumbers);
                            dataBase.child("Matches").child(matchNum).child("redScore").setValue(Integer.parseInt(superData.get("Red Alliance Score").toString()));
                            dataBase.child("Matches").child(matchNum).child("didReach40KiloPascalsRed").setValue(Boolean.valueOf((String) superData.get("boilerRPGained")));
                            dataBase.child("Matches").child(matchNum).child("didStartAllRotorsRed").setValue(Boolean.valueOf((String) superData.get("rotorRPGained")));
                        }
                        dataBase.child("TeamInMatchDatas").child(matchAndTeamOne).child("superNotes").child("firstNotes").setValue(teamOneFirstNotes);
                        dataBase.child("TeamInMatchDatas").child(matchAndTeamTwo).child("superNotes").child("firstNotes").setValue(teamTwoFirstNotes);
                        dataBase.child("TeamInMatchDatas").child(matchAndTeamThree).child("superNotes").child("firstNotes").setValue(teamThreeFirstNotes);
                        dataBase.child("TeamInMatchDatas").child(matchAndTeamOne).child("superNotes").child("finalNotes").setValue(teamOneFinalNotes);
                        dataBase.child("TeamInMatchDatas").child(matchAndTeamTwo).child("superNotes").child("finalNotes").setValue(teamTwoFinalNotes);
                        dataBase.child("TeamInMatchDatas").child(matchAndTeamThree).child("superNotes").child("finalNotes").setValue(teamThreeFinalNotes);


                    } catch (JSONException JE) {
                        Log.e("json error", "failed to get super json");
                    }
                    // new ConnectThread(context, superName, uuid, name, text).start();
                }
                toasts("Resent Super data!", false);
            }
        }.start();
    }

    public void toasts(final String message, boolean isLongMessage) {
        if (!isLongMessage) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void checkPreviousMatchNumAndAlliance(){
        Intent backToHome = getIntent();
        if (backToHome.hasExtra("number")) {
            matchNumber = Integer.parseInt(backToHome.getExtras().getString("number")) + 1;
        } else {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            matchNumber = prefs.getInt("match_number", 1);
        }
        if (backToHome.hasExtra("shouldBeRed")) {
            isRed = getIntent().getBooleanExtra("shouldBeRed", false);
        } else {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            isRed = prefs.getBoolean("allianceColor", false);
        }
        if (!backToHome.hasExtra("mute")) {
            mute.setChecked(false);
        } else if (backToHome.hasExtra("mute")) {
            mute.setChecked(true);
        }
    }

    public void listenForResendClick(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                name = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Super_scout_data/" + name;

                final String fileName = name;
                final String[] nameOfResendMatch = name.split("Q");
                new AlertDialog.Builder(context)
                        .setTitle("RESEND DATA?")
                        .setMessage("RESEND " + "Q" + nameOfResendMatch[1] + "?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content = readFile(fileName);
                                JSONObject superData;
                                try {
                                    superData = new JSONObject(content);
                                } catch (JSONException jsone) {
                                    Log.e("File Error", "no valid JSON in the file");
                                    Toast.makeText(context, "Not a valid JSON", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                List<JSONObject> dataPoints = new ArrayList<>();
                                dataPoints.add(superData);
                                resendSuperData(dataPoints);
                            }
                        }).show();
            }
        });
    }

    public void listLongClick(){

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                final String name = parent.getItemAtPosition(position).toString();
                String splitName[] = name.split("_");
                final String editMatchNumber = splitName[0].replace("Q", "");
                Log.e("matchNameChange", editMatchNumber);
                String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Super_scout_data/" + name;
                final String content = readFile(filePath);
                final JSONObject superData;
                try {
                    superData = new JSONObject(content);
                    if (isRed) {
                        previousScore = superData.get("Red Alliance Score").toString();
                        previousFoul = superData.get("Red Alliance Foul").toString();
                        Log.e("previous Score", previousScore);
                    } else {
                        previousScore = superData.get("Blue Alliance Score").toString();
                        previousFoul = superData.get("Blue Alliance Foul").toString();
                        Log.e("previous Score", previousScore);
                    }
                    previous40kpa = Boolean.valueOf((String) superData.get("boilerRPGained"));
                    previousRotorNumAuto = (Integer) superData.get("numRotorsSpinningAuto");
                    previousRotorNumTele = (Integer) superData.get("numRotorsSpinningTele");
                } catch (JSONException JE) {
                    Log.e("read Super Data", "failed");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit Alliance Score for " + name + ": ");
                final View finalDataPtsView = LayoutInflater.from(context).inflate(R.layout.finaldatapoints, null);
                ((EditText) finalDataPtsView.findViewById(R.id.finalScoreEditText)).setText(previousScore);
                ((EditText) finalDataPtsView.findViewById(R.id.finalFoulEditText)).setText(previousFoul);
                ((ToggleButton) finalDataPtsView.findViewById(R.id.boilerToggleButton)).setChecked(previous40kpa);
                ((EditText) finalDataPtsView.findViewById(R.id.rotorAutoText)).setText(String.valueOf(previousRotorNumAuto));
                ((EditText) finalDataPtsView.findViewById(R.id.rotorTeleText)).setText(String.valueOf(previousRotorNumTele));
                builder.setView(finalDataPtsView);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;
                        File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Super_scout_data/" + name);
                        dir.mkdir();
                        previousScore = ((EditText) d.findViewById(R.id.finalScoreEditText)).getText().toString(); //Now it's the new score
                        previousFoul = ((EditText) d.findViewById(R.id.finalFoulEditText)).getText().toString(); //Foul refers to foul points gained by that team
                        previous40kpa = ((ToggleButton) d.findViewById(R.id.boilerToggleButton)).isChecked();
                        previousRotorNumAuto = Integer.parseInt(((EditText) d.findViewById(R.id.rotorAutoText)).getText().toString());
                        previousRotorNumTele = Integer.parseInt(((EditText) d.findViewById(R.id.rotorTeleText)).getText().toString());
                        if (!previousScore.equals("") && !previousFoul.equals("")) {
                            try {
                                JSONObject superScore = new JSONObject(content);
                                PrintWriter dirWriter = new PrintWriter(new FileOutputStream(dir, false));
                                if (isRed) {
                                    superScore.put("Red Alliance Score", Integer.valueOf(previousScore));
                                    superScore.put("Red Alliance Foul", Integer.valueOf(previousFoul));
                                    dataBase.child("Matches").child(editMatchNumber).child("redScore").setValue(Integer.parseInt(previousScore));
                                    dataBase.child("Matches").child(editMatchNumber).child("foulPointsGainedRed").setValue(Integer.parseInt(previousFoul));
                                    dataBase.child("Matches").child(editMatchNumber).child("redDidReach40KiloPascals").setValue(previous40kpa);
                                    dataBase.child("Matches").child(editMatchNumber).child("numRotorsSpinningRedAuto").setValue(previousRotorNumAuto);
                                    dataBase.child("Matches").child(editMatchNumber).child("numRotorsSpinningRedTele").setValue(previousRotorNumTele);
                                } else {
                                    superScore.put("Blue Alliance Score", Integer.valueOf(previousScore));
                                    superScore.put("Blue Alliance Foul", Integer.valueOf(previousFoul));
                                    dataBase.child("Matches").child(editMatchNumber).child("blueScore").setValue(Integer.parseInt(previousScore));
                                    dataBase.child("Matches").child(editMatchNumber).child("foulPointsGainedBlue").setValue(Integer.parseInt(previousFoul));
                                    dataBase.child("Matches").child(editMatchNumber).child("blueDidReach40KiloPascals").setValue(previous40kpa);
                                    dataBase.child("Matches").child(editMatchNumber).child("numRotorsSpinningBlueAuto").setValue(previousRotorNumAuto);
                                    dataBase.child("Matches").child(editMatchNumber).child("numRotorsSpinningBlueTele").setValue(previousRotorNumTele);
                                }
                                superScore.put("boilerRPGained", String.valueOf(previous40kpa));
                                superScore.put("numRotorsSpinningAuto", previousRotorNumAuto);
                                superScore.put("numRotorsSpinningTele", previousRotorNumTele);

                                dirWriter.println(superScore.toString());
                                dirWriter.close();
                                toasts("Score Updated.", false);
                            } catch (JSONException JSONex) {
                                JSONex.printStackTrace();
                                toasts("Failed to Save to Storage", false);
                            } catch (FileNotFoundException fnfe) {
                                fnfe.printStackTrace();
                                toasts("Storage Not Located.", false);
                            }
                        } else {
                            toasts("Please Enter Valid Inputs", false);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return true;
            }
        });

    }

}



