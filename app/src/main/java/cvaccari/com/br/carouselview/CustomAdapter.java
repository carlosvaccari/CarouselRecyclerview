package cvaccari.com.br.carouselview;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cvaccari.com.br.carouselrecyclerview.CarouselView;


public class CustomAdapter extends CarouselView.Adapter<CustomAdapter.ViewHolder> {

    private final List<ItemVO> mList;
    private final Context mContext;

    public CustomAdapter(Context context, List<ItemVO> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(mContext).inflate(R.layout.carousel_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.getName().setText(mList.get(position).getText());
        viewHolder.getContainer().setBackgroundColor(Color.parseColor(mList.get(position).getColor()));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final TextView mName;
        final View mContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.text);
            mContainer = (View) itemView.findViewById(R.id.container);
        }

        public TextView getName() {
            return mName;
        }

        public View getContainer() {
            return mContainer;
        }
    }
}

