package tcd.android.com.howaboutthere;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.TextInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by ADMIN on 07/05/2017.
 */

public class PlanListAdapter extends ArrayAdapter<Plan> {
    private Context mContext;

    public PlanListAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.plan_options_list_item, null);
        }

        Plan placeInfo = getItem(position);
        ((TextView)convertView.findViewById(R.id.tvPlaceName)).setText(placeInfo.getName());
        ((TextView)convertView.findViewById(R.id.tvPlaceAddress)).setText(placeInfo.getAddress());
        ((TextView)convertView.findViewById(R.id.tvDateTime)).setText(placeInfo.getDatetime());
        ((TextView)convertView.findViewById(R.id.tvPersons)).setText(String.valueOf(placeInfo.getNumPerson())
                + " " + convertView.getResources().getString(R.string.persons));
        ((TextView)convertView.findViewById(R.id.tvGoing)).setText(String.valueOf(placeInfo.getGoing())
                + " " + convertView.getResources().getString(R.string.going));
        ((TextView)convertView.findViewById(R.id.tvBusy)).setText(String.valueOf(placeInfo.getBusy())
                + " " + convertView.getResources().getString(R.string.busy));

        return convertView;
    }
}
