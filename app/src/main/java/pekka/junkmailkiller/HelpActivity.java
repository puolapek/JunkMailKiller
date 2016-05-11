package pekka.junkmailkiller;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ArrowKeyMovementMethod;
import android.widget.ScrollView;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView help = (TextView)findViewById(R.id.helpTextView);
        help.setText(Html.fromHtml(getString(R.string.help_text)));
        help.setMovementMethod(ArrowKeyMovementMethod.getInstance());
    }

}
