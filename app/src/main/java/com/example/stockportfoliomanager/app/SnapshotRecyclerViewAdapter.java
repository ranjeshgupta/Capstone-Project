package com.example.stockportfoliomanager.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stockportfoliomanager.app.data.PortProvider;

public class SnapshotRecyclerViewAdapter extends RecyclerView.Adapter<SnapshotRecyclerViewAdapter.ViewHolder>{

    private Cursor mCursor;
    final private Context mContext;
    final private CoordinatorLayout mCoordinatorLayout;
    final String LOG_TAG = SnapshotRecyclerViewAdapter.class.getSimpleName();

    Fragment mFragment;
    Bundle mBundle;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mStockName;
        public final TextView mValue;
        public final TextView mValueChanges;
        public final TextView mUnit;
        public final TextView mPrice;
        public final TextView mPriceDate;
        public final ImageView mMenu;

        public ViewHolder(View view) {
            super(view);
            mStockName = (TextView) view.findViewById(R.id.stock_name);
            mValueChanges = (TextView) view.findViewById(R.id.value_changes);
            mValue = (TextView) view.findViewById(R.id.hold_value);
            mUnit = (TextView) view.findViewById(R.id.unit);
            mPrice = (TextView) view.findViewById(R.id.current_price);
            mPriceDate = (TextView) view.findViewById(R.id.price_date);
            mMenu = (ImageView) view.findViewById(R.id.menu_snapshot);
        }
    }

    public SnapshotRecyclerViewAdapter(Context context, CoordinatorLayout coordinatorLayout) {
        mContext = context;
        mCoordinatorLayout = coordinatorLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = R.layout.fragment_snapshot_item;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            final ViewHolder vh = new ViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(mContext, StockPageActivity.class);
                    StockPageActivity.STOCK_CODE = Integer.toString(getStockCode(vh.getAdapterPosition()));
                    StockPageActivity.STOCK_NAME= getStockName(vh.getAdapterPosition());
                    //myIntent.putExtra("STOCK_CODE", getStockCode(vh.getAdapterPosition()));
                    mContext.startActivity(myIntent);
                }
            });

            return vh;
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);

        // Read cursor
        viewHolder.mStockName.setText(mCursor.getString(PortProvider.COL_HOLD_COMPANY_NAME));

        double unit = mCursor.getDouble(PortProvider.COL_HOLD_UNITS);
        double price = mCursor.getDouble(PortProvider.COL_HOLD_PRICE);
        double marketValue = unit * price;
        double costValue = mCursor.getDouble(PortProvider.COL_HOLD_COST_VALUE);
        double changes = marketValue - costValue;

        if (changes > 0) {
            viewHolder.mValueChanges.setTextColor(mContext.getResources().getColor(R.color.colorGreen));
        }
        else if (changes < 0){
            viewHolder.mValueChanges.setTextColor(mContext.getResources().getColor(R.color.colorRed));
        }
        viewHolder.mValueChanges.setText(Utilities.formatNumber(mContext, changes, true));

        String mkValueString = mContext.getString(R.string.currency_symbol) + Utilities.formatNumber(mContext, marketValue);
        viewHolder.mValue.setText(mkValueString);

        String unitString = Utilities.formatNumber(mContext, unit) + mContext.getString(R.string.unit_symbol);
        viewHolder.mUnit.setText(unitString);

        String priceString = mContext.getString(R.string.price_prefix) + mContext.getString(R.string.currency_symbol)
                + Utilities.formatNumber(mContext, price);
        viewHolder.mPrice.setText(priceString);

        String priceDate = mContext.getString(R.string.date_prefix) + mCursor.getString(PortProvider.COL_HOLD_PRICE_DATE);
        viewHolder.mPriceDate.setText(priceDate);

        final int mHoldingID = mCursor.getInt(PortProvider.COL_HOLD_HOLDING_ID);

        viewHolder.mMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mContext, view);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.menu_snapshot, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                     public boolean onMenuItemClick(MenuItem item) {
                         Log.d(LOG_TAG, "HoldingID :" + mHoldingID);

                         int id = item.getItemId();
                         if(id==R.id.view_transactions) {
                             //Snackbar.make(mCoordinatorLayout, "View transaction for Holding ID: " + mHoldingID, Snackbar.LENGTH_LONG).show();
                             fragmentJump(mHoldingID);
                         }
                         else if (id == R.id.add_transactions) {
                             //Snackbar.make(mCoordinatorLayout, "Add transaction for Holding ID: " + mHoldingID, Snackbar.LENGTH_LONG).show();

                             Intent intentAddTransaction = new Intent(mContext, AddTransactionActivity.class);
                             AddTransactionActivity.mHoldingId = mHoldingID;
                             AddTransactionActivity.mTransactionId = 0;
                             mContext.startActivity(intentAddTransaction);
                         }

                         else if (id == R.id.delete_holding) {
                             //Snackbar.make(mCoordinatorLayout, "Remove holding for Holding ID: " + holdingId, Snackbar.LENGTH_LONG).show();
                             Utilities.removeHolding(mContext, mHoldingID);
                         }

                         return true;
                     }
                });

                popup.show();
            }
        });
    }


    private void fragmentJump(int holdingId) {
        mFragment = new TransactionFragment();
        mBundle = new Bundle();
        mBundle.putInt("HOLDING_ID", holdingId);
        mFragment.setArguments(mBundle);
        switchContent(R.id.container, mFragment);
    }

    public void switchContent(int id, Fragment fragment) {
        if (mContext == null)
            return;
        if (mContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) mContext;
            Fragment frag = fragment;
            int selectedNavigationMenuIndex = 3;
            mainActivity.switchContent(id, frag, selectedNavigationMenuIndex);
        }
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(PortProvider.COL_HOLD_HOLDING_ID);
        }
        return -1;
    }

    public int getStockCode(int position){
        if (mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getInt(PortProvider.COL_HOLD_COMPANY_CODE);
        }
        return -1;
    }

    public String getStockName(int position){
        if (mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getString(PortProvider.COL_HOLD_COMPANY_NAME);
        }
        return null;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
