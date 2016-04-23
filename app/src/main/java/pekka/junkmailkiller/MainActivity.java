package pekka.junkmailkiller;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    JunkMailListenerTask junkMailListenerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        if (!checkMailServerSettings()) {
            // Some settings missing, go to settings page.
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                break;
            case R.id.action_keywords:
                intent = new Intent(this, KeywordsActivity.class);
                this.startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void startService(View view) {

        startService(new Intent(getBaseContext(), JunkMailListenerService.class));

        findViewById(R.id.startButton).setEnabled(false);
        findViewById(R.id.stopButton).setEnabled(true);

        /*
        if (junkMailListenerTask == null || junkMailListenerTask.getStatus() != AsyncTask.Status.RUNNING) {

            junkMailListenerTask = new JunkMailListenerTask();
            DBHelper dbHelper = new DBHelper(this);
            Settings settings = dbHelper.readSettings();

            junkMailListenerTask.execute(settings);

            findViewById(R.id.startButton).setEnabled(false);
            findViewById(R.id.stopButton).setEnabled(true);

            Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        }*/
    }

    public void stopService(View view) {

        stopService(new Intent(getBaseContext(), JunkMailListenerService.class));

        findViewById(R.id.startButton).setEnabled(true);
        findViewById(R.id.stopButton).setEnabled(false);

        /*
        if (junkMailListenerTask != null && junkMailListenerTask.getStatus() == AsyncTask.Status.RUNNING) {

            junkMailListenerTask.cancel(true);

            findViewById(R.id.startButton).setEnabled(true);
            findViewById(R.id.stopButton).setEnabled(false);

            Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        }*/
    }

    private void init() {


        if (!isMyServiceRunning(JunkMailListenerService.class)) {
        //if (junkMailListenerTask == null || junkMailListenerTask.getStatus() != AsyncTask.Status.RUNNING) {
            findViewById(R.id.startButton).setEnabled(true);
            findViewById(R.id.stopButton).setEnabled(false);
        } else {
            findViewById(R.id.startButton).setEnabled(false);
            findViewById(R.id.stopButton).setEnabled(true);
        }
    }

    private boolean isMyServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMailServerSettings() {

        DBHelper dbHelper = new DBHelper(this);
        Settings settings = dbHelper.readSettings();

        return !(settings.getHost() == null || settings.getUser() == null || settings.getPassword() == null);
    }

    /*
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (junkMailListenerTask != null && junkMailListenerTask.getStatus() == AsyncTask.Status.RUNNING) {

                junkMailListenerTask.cancel(true);

                Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
            }

        }
        return super.onKeyDown(keyCode, event);
    }*/

}
