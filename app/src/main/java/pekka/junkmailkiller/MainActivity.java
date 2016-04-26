package pekka.junkmailkiller;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableButtons();

        if (!checkMailServerSettings()) {
            // Something wrong in settings, go to settings page.
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

        if (checkMailServerSettings()) {
            startService(new Intent(getBaseContext(), JunkMailListenerService.class));
        } else {
            Toast.makeText(this, "Check Settings. Connection to mail server can NOT be established!", Toast.LENGTH_LONG).show();
        }

        enableButtons();
    }

    public void stopService(View view) {

        stopService(new Intent(getBaseContext(), JunkMailListenerService.class));

        enableButtons();
    }

    private void enableButtons() {

        if (!isMyServiceRunning(JunkMailListenerService.class)) {
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
        if (settings.getHost() == null || settings.getUser() == null || settings.getPassword() == null || settings.getSettingsOK().equals("false")) {
            return false;
        } else {
            return true;
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isMyServiceRunning(JunkMailListenerService.class)) {
                stopService(new Intent(getBaseContext(), JunkMailListenerService.class));
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
