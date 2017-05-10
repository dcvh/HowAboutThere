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

public class FriendStatusAdapter extends ArrayAdapter<Pair<String, Integer>> {
    private Context mContext;

    public FriendStatusAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_status_list_item, null);
        }

        Pair<String, Integer> member = getItem(position);
        ((TextView) convertView.findViewById(R.id.tvAttendanceName)).setText(member.first);
        int drawableId = 0;
        switch (member.second) {
            case -1:
                drawableId = android.R.drawable.btn_star_big_off;
                break;
            case 0:
                drawableId = android.R.drawable.button_onoff_indicator_off;
                break;
            case 1:
                drawableId = android.R.drawable.btn_star_big_on;
                break;
        }

        ((ImageView) convertView.findViewById(R.id.ivStatus)).setImageDrawable(
                convertView.getResources().getDrawable(drawableId));


        return convertView;
    }
}
