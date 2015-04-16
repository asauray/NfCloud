package view.custom.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infotel.greenwav.infotel.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import model.Document;
import model.db.external.GetDocument;

/**
 * Custom adapter for Line
 * @see model.Document
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */
public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private List<Document> mDataset;
    private Activity activity;
    private Calendar calendar;

    private static final String TAG="LINE_ADATER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public DocumentAdapter(Activity activity) {
        this.activity = activity;
        mDataset = new ArrayList<Document>();
        calendar = Calendar.getInstance();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DocumentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
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
        Document d = mDataset.get(position);
        holder.name.setText(d.getName());
        holder.specification.setText(d.getSpecification());
        holder.description.setText(d.getDescription());

        if(holder.documentItem.getLocation() != null){
            holder.icon.setImageResource(R.drawable.ic_cloud_done);
        }
        else{
            holder.icon.setImageResource(R.drawable.ic_cloud_ready);
        }


        if(calendar.get(Calendar.YEAR) != holder.documentItem.getCalendar().get(Calendar.YEAR)){
            holder.date.setText(d.getCalendar().get(Calendar.YEAR)+"");
        }
        else if(calendar.get(Calendar.MONTH) != holder.documentItem.getCalendar().get(Calendar.MONTH)){
            holder.date.setText(d.getCalendar().get(Calendar.MONTH)+"");
        }
        else if(calendar.get(Calendar.DAY_OF_MONTH) != holder.documentItem.getCalendar().get(Calendar.DAY_OF_MONTH)){
            holder.date.setText(d.getCalendar().get(Calendar.DAY_OF_MONTH)+"");
        }
        else{
            holder.date.setText(d.getCalendar().get(Calendar.HOUR_OF_DAY)+":"+d.getCalendar().get(Calendar.MINUTE));
        }

    }

    public void add(Document item) {
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
        Collections.sort(mDataset);
        notifyItemRangeChanged(0, mDataset.size());
    }

    public void removeAll(){
        int size = mDataset.size();
        mDataset.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void remove(int position){
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        TextView name, specification, description, date;
        ImageView icon;
        Document documentItem;
        Activity activity;

        public ViewHolder(Activity activity, RelativeLayout lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            this.activity = activity;
            name = (TextView) lyt_main.findViewById(R.id.name);
            specification = (TextView) lyt_main.findViewById(R.id.specification);
            description = (TextView) lyt_main.findViewById(R.id.description);
            date = (TextView) lyt_main.findViewById(R.id.date);
            icon = (ImageView) lyt_main.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View v) {
            new GetDocument(activity).execute(documentItem);
        }

        public void setItem(Document item) {
            documentItem = item;
        }
    }
}
