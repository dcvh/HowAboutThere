package tcd.android.com.howaboutthere;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.TextInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ADMIN on 07/05/2017.
 */

public class FriendListAdapter extends ArrayAdapter<String> {
    private Context mContext;

    public FriendListAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_options_list_item, null);
        }

        String friendName = getItem(position);
        ((TextView)convertView.findViewById(R.id.tvFriendName)).setText(friendName);

        return convertView;
    }
}