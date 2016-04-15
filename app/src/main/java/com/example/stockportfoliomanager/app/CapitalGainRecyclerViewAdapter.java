package com.example.stockportfoliomanager.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.stockportfoliomanager.app.data.PortProvider;

public class CapitalGainRecyclerViewAdapter extends RecyclerView.Adapter<CapitalGainRecyclerViewAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mStockName;
        public final TextView mValue;
        public final TextView mRgl;
        public final TextView mUrgl;

        public ViewHolder(View view) {
            super(view);
            mStockName = (TextView) view.findViewById(R.id.capital_gain_stock_name);
            mValue = (TextView) view.findViewById(R.id.capital_gain_hold_value);
            mRgl = (TextView) view.findViewById(R.id.capital_gain_r_gl);
            mUrgl = (TextView) view.findViewById(R.id.capital_gain_ur_gl);
        }
    }

    public CapitalGainRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = R.layout.fragment_capital_gain_item;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);

        // Read cursor
        viewHolder.mStockName.setText(mCursor.getString(PortProvider.COL_HOLD_COMPANY_NAME));

        int holdId = mCursor.getInt(PortProvider.COL_HOLD_HOLDING_ID);
        Double unit = mCursor.getDouble(PortProvider.COL_HOLD_UNITS);
        Double price = mCursor.getDouble(PortProvider.COL_HOLD_PRICE);
        Double marketValue = unit * price;
        Double costValue = mCursor.getDouble(PortProvider.COL_HOLD_COST_VALUE);

        String value = mContext.getString(R.string.currency_symbol)
                + Utilities.formatNumber(mContext, marketValue);
        viewHolder.mValue.setText(value);

        Double[] gainLoss = Utilities.getRealisedUnrealisedGainLoss(mContext, holdId, price);
        Double rGainLoss = gainLoss[0];
        Double urGainLoss = gainLoss[1];

        String rglString = mContext.getString(R.string.r_gain_loss_prefix)
                + mContext.getString(R.string.currency_symbol)
                + Utilities.formatNumber(mContext, rGainLoss);
        viewHolder.mRgl.setText(rglString);

        String urglString = mContext.getString(R.string.ur_gain_loss_prefix)
                + mContext.getString(R.string.currency_symbol)
                + Utilities.formatNumber(mContext, urGainLoss);
        viewHolder.mUrgl.setText(urglString);
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
