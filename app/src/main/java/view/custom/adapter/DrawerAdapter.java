package view.custom.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.infotel.greenwav.infotel.R;

import model.Group;
import model.Mode;

/**
 * Created by asauray on 2/18/15.
 */
public class DrawerAdapter extends ArrayAdapter<Group>{

    private Context context;
    private Group[] items;

    public DrawerAdapter(Context context, int resource, Group[] items) {
        super(context, resource);
        this.context = context;
        this.items = items;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.item_drawer, null);
        }
        Group g = (Group) (items[position]);

        TextView tv = (TextView) view.findViewById(R.id.mode);
        tv.setText(g.getName());
        ImageView icon = (ImageView) view.findViewById(R.id.icon);

        if(g.isChecked()){
            //icon.setImageDrawable(context.getResources().getDrawable(g.getLightIcon()));
            icon.setAlpha(1f);
            view.setBackgroundColor(context.getResources().getColor(R.color.accent));
            tv.setTextColor(Color.WHITE);
        }
        else{
            //icon.setImageDrawable(context.getResources().getDrawable(g.getDarkIcon()));
            icon.setAlpha(0.54f);
            view.setBackgroundColor(Color.WHITE);
            tv.setTextColor(context.getResources().getColor(android.R.color.primary_text_light));
        }
        return view;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return items.length;
    }

    @Override
    public Group getItem(int position) {
        // TODO Auto-generated method stub
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
}
