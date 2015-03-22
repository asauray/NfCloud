package view.custom.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infotel.greenwav.infotel.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Document;
import model.Group;
import model.db.external.GetIcon;
import view.activity.DocumentActivity;

/**
 * Custom adapter for Group
 * @see model.Group
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private List<Group> mDataset;
    private Activity activity;

    private static final String TAG="LINE_ADATER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public GroupAdapter(Activity activity) {
        this.activity = activity;
        mDataset = new ArrayList<Group>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(activity, (RelativeLayout) v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setItem(mDataset.get(position));
        Group g = mDataset.get(position);
        holder.name.setText(g.getName());
        holder.admin.setText(g.getAdmin());
        holder.description.setText(g.getDescription());
        new GetIcon(activity, holder.icon).execute();
    }

    public void add(Group item) {
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
        Collections.sort(mDataset);
        notifyItemRangeChanged(0, mDataset.size());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        TextView name, admin, description;
        ImageView icon;
        Group groupItem;
        Activity activity;

        public ViewHolder(Activity activity, RelativeLayout lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            this.activity = activity;
            name = (TextView) lyt_main.findViewById(R.id.name);
            admin = (TextView) lyt_main.findViewById(R.id.admin);
            description = (TextView) lyt_main.findViewById(R.id.description);
            icon = (ImageView) lyt_main.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(activity, DocumentActivity.class);
            intent.putExtra("GROUP", groupItem);
            activity.startActivity(intent);
        }

        public void setItem(Group item) {
            groupItem = item;
        }
    }
}
