package pekka.junkmailkiller;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class KeywordsActivity extends AppCompatActivity {

    ArrayList<String> keywords = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    private final int INPUT_MODE = 1;
    private final int LIST_MODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywords);
        if (getKeywords() != null) {
            keywords = getKeywords();
        }
        setupKeywordListListener();

        manageObjectsVisibility(LIST_MODE);
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

    private void manageObjectsVisibility(int inputMode) {

        final ListView listview = (ListView) findViewById(R.id.listView);
        final Button saveButton = (Button) findViewById(R.id.saveButton);
        final Button addKeywordButton = (Button) findViewById(R.id.addKeywordButton);
        final TextView keywordText = (TextView) findViewById(R.id.keywordText);
        InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMode == LIST_MODE) {
                saveButton.setVisibility(View.INVISIBLE);
                keywordText.setVisibility(View.INVISIBLE);
                listview.setVisibility(View.VISIBLE);
                // Close soft keyboard.
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                addKeywordButton.setVisibility(View.VISIBLE);
        } else
            if (inputMode == INPUT_MODE){
                listview.setVisibility(View.INVISIBLE);
                addKeywordButton.setVisibility(View.INVISIBLE);
                keywordText.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                // Open soft keyboard.
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void setupKeywordListListener() {
        final DBHelper dbHelper = new DBHelper(this);

        final ListView listview = (ListView) findViewById(R.id.listView);
        adapter=new ArrayAdapter<String>(this,
                R.layout.activity_keywords_text_view,
                keywords);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                keywords.remove(item);
                                dbHelper.deleteKeyword(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }

        });
    }

    public void addNewKeyword(View view) {
        manageObjectsVisibility(INPUT_MODE);
    }

    public void saveKeyword(View view) {
        final TextView keyword = (TextView) findViewById(R.id.keywordText);
        DBHelper dbHelper = new DBHelper(this);

        if (keyword == null || keyword.getText() == null || keyword.getText().toString().length() == 0) {
            return;
        }
        dbHelper.insertKeyword(keyword.getText().toString());
        Toast.makeText(this, this.getString(R.string.msg_keyword_added), Toast.LENGTH_LONG).show();

        if (keywords == null) {
            keywords = new ArrayList<String>();
        }
        keywords.add(keyword.getText().toString());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        keyword.setText("");

        manageObjectsVisibility(LIST_MODE);
    }

    private ArrayList getKeywords() {
        DBHelper dbHelper = new DBHelper(this);
        Settings settings = dbHelper.readSettings();
        return settings.getKeyWords();
    }
}
