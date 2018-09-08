package ml.guru.barcodescanner.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import ml.guru.barcodescanner.R;
import ml.guru.barcodescanner.model.Items;

public class DBAdapter extends BaseAdapter {

    private Context context;
    private List<Items> itemsList;

    public DBAdapter(Context context, List<Items> itemsList){
        this.context = context;
        this.itemsList = itemsList;
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }

    @Override
    public Object getItem(int i) {
        return itemsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.list_item, viewGroup, false);

        TextView idText = rowView.findViewById(R.id.listview_item_id);
        final EditText countText = rowView.findViewById(R.id.listview_item_count);

        idText.setText(itemsList.get(i).getItemId());
        countText.setText(String.valueOf(itemsList.get(i).getCount()));

        if(itemsList.get(i).getPresent()){
            rowView.setBackgroundColor(Color.GREEN);
        }
        else{
            rowView.setBackgroundColor(Color.WHITE);
        }

        return rowView;
    }
}
