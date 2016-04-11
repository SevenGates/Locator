package mah.sys.locator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 09-Apr-16.
 */
public class ComplexAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<String> complexList = new ArrayList<String>();

    public ComplexAutoCompleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return complexList.size();
    }

    @Override
    public String getItem(int index) {
        return complexList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_dropdown_item,parent,false);
        }
        ((TextView)convertView.findViewById(R.id.text1)).setText(getItem(position));
        return  convertView;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    List<String> complexes = findComplexes(mContext,constraint.toString());

                    filterResults.values = complexes;
                    filterResults.count = complexes.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    complexList = (List<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private List<String> findComplexes(Context context, String searchString) {
        List<String> list = ServerCommunicator.getComplexes();
        List<String> filteredList = new ArrayList<String>();
        for (String S: list)
            if(S.substring(0,searchString.length()).toLowerCase().equals(searchString.toLowerCase()))
                filteredList.add(S);
        return filteredList;
    }
}
