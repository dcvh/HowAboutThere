package tcd.android.com.howaboutthere;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public class PlaceDetailsActivity extends AppCompatActivity {

    private TextView tvPlaceName;
    private TextView tvPlaceAddress;
    private TextView tvDateTime;
    private TextView tvPersons;
    private TextView tvGoing;
    private TextView tvBusy;

    private ImageView ivMapButton;

    private ListView lvGroupList;
    private FriendStatusAdapter adapter;

    private Button btnYes;
    private Button btnNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        tvPlaceName = (TextView) findViewById(R.id.tvPlaceName);
        tvPlaceAddress = (TextView) findViewById(R.id.tvPlaceAddress);
        tvDateTime = (TextView) findViewById(R.id.tvDateTime);
        tvBusy = (TextView) findViewById(R.id.tvBusy);
        tvPersons = (TextView) findViewById(R.id.tvPersons);
        tvGoing = (TextView) findViewById(R.id.tvGoing);

        ivMapButton = (ImageView) findViewById(R.id.ivMapButton);

        lvGroupList = (ListView) findViewById(R.id.lvGroupList);
        adapter = new FriendStatusAdapter(this);
        lvGroupList.setAdapter(adapter);

        // yes/no buttons
        btnYes = (Button) findViewById(R.id.btnYes);
        btnNo = (Button) findViewById(R.id.btnNo);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("User decision", 1);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("User decision", 0);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        // get group's members from previous activity's intent
        final Intent prevIntent = getIntent();
        tvPlaceName.setText(prevIntent.getStringExtra("Place name"));
        tvPlaceAddress.setText(prevIntent.getStringExtra("Place address"));
        tvDateTime.setText(prevIntent.getStringExtra("Date and time"));
        HashMap<String, Integer> mapGroupStatuses
                = (HashMap<String, Integer>) prevIntent.getSerializableExtra("Member statuses");
        tvPersons.setText(String.valueOf(mapGroupStatuses.size()) + " " + getResources().getString(R.string.persons));

        // calculate number of goings, and busy ones
        int going = 0, busy = 0;
        for (Map.Entry<String, Integer> entry : mapGroupStatuses.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (value == 1)
                going++;
            else if (value == 0)
                busy++;
            adapter.add(new Pair<String, Integer>(key, value));
        }
        tvGoing.setText(String.valueOf(going) + " " + getResources().getString(R.string.going));
        tvBusy.setText(String.valueOf(busy) + " " + getResources().getString(R.string.busy));

        // open map application
        ivMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + prevIntent.getStringExtra("LatLng")));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // not choosing neither options
        Intent resultIntent = new Intent();
        resultIntent.putExtra("User decision", -1);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        super.onBackPressed();
    }
}
