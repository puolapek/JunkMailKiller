package pekka.junkmailkiller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        showSettings();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Set title back key to act as normal back key.
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        DBHelper dbHelper = new DBHelper(this);
        Settings settings = dbHelper.readSettings();

        EditText host = (EditText)findViewById(R.id.keywordText);
        EditText user = (EditText)findViewById(R.id.userText);
        EditText password = (EditText)findViewById(R.id.passwdText);
        EditText freq = (EditText)findViewById(R.id.frequenceText);

        host.setText(settings.getHost());
        user.setText(settings.getUser());
        password.setText(settings.getPassword());
        freq.setText(settings.getFreq());
    }

    public void saveSettings(View view) {
        DBHelper dbHelper = new DBHelper(this);
        Settings settings = new Settings();

        settings.setHost(((EditText) findViewById(R.id.keywordText)).getText().toString());
        settings.setUser(((EditText) findViewById(R.id.userText)).getText().toString());
        settings.setPassword(((EditText) findViewById(R.id.passwdText)).getText().toString());
        settings.setFreq(((EditText) findViewById(R.id.frequenceText)).getText().toString());

        dbHelper.insertOrUpdateSettings(settings);

        checkConnection(settings);

        showSettingsMessage(dbHelper.readSettings());
    }

    private void showSettingsMessage(Settings settings) {
        if (settings.getSettingsOK().equals("true")) {
            Toast.makeText(this, "Settings OK. Connection to mail server can be established.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Invalid Settings. Connection to mail server can NOT be established!", Toast.LENGTH_LONG).show();
        }
    }

    private void checkConnection(Settings settings) {

        final DBHelper dbHelper = new DBHelper(this);
        final String host = settings.getHost();
        final String user = settings.getUser();
        final String password = settings.getPassword();

        Thread thread = new Thread(new Runnable(){

            @Override
            public void run() {

                try {
                    Properties properties = new Properties();
                    Session emailSession = Session.getDefaultInstance(properties);
                    Store store = emailSession.getStore("imap");

                    dbHelper.insertOrUpdateSettingsOK("false");
                    store.connect(host, user, password);
                    dbHelper.insertOrUpdateSettingsOK("true");
                    store.close();
                }  catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
