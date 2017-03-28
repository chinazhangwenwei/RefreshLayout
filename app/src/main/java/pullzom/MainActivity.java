package pullzom;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.admin.refreshlayoutdemo.PullToZoomListView;
import com.example.admin.refreshlayoutdemo.R;

public class MainActivity extends AppCompatActivity {
    MyPullToZoomListView listView;
    private String[] adapterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull);
        listView = (MyPullToZoomListView) findViewById(R.id.listview);
        adapterData = new String[]{"Activity", "Service", "Content Provider", "Activity", "Service", "Content Provider", "Activity", "Service", "Content Provider", "Activity", "Service", "Content Provider", "Intent", "BroadcastReceiver", "ADT", "Sqlite3", "HttpClient", "DDMS", "Android Studio", "Fragment", "Loader"};

        listView.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, adapterData));
        listView.addHeadContentView(View.inflate(MainActivity.this, R.layout.pull_zoom_head, null));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}