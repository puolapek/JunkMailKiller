package pekka.junkmailkiller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

        EditText host = (EditText)findViewById(R.id.keywordText);
        EditText user = (EditText)findViewById(R.id.userText);
        EditText password = (EditText)findViewById(R.id.passwdText);
        EditText freq = (EditText)findViewById(R.id.frequenceText);

        dbHelper.insertOrUpdateSettings(host.getText().toString(), user.getText().toString(), password.getText().toString(), freq.getText().toString());

        Toast.makeText(this, "Settings saved.", Toast.LENGTH_LONG).show();
    }
}
