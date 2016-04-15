package com.example.stockportfoliomanager.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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

public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private CoordinatorLayout mCoordinatorLayout;
    final String LOG_TAG = TransactionRecyclerViewAdapter.class.getSimpleName();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        public final TextView mStockName;
        public final TextView mUnit;
        public final TextView mValue;
        public final TextView mType;
        public final TextView mPrice;
        public final TextView mDate;
        public final ImageView mMenu;

        public ViewHolder(View view) {
            super(view);
            mStockName = (TextView) view.findViewById(R.id.transaction_stock_name);
            mUnit = (TextView) view.findViewById(R.id.transaction_unit);
            mValue = (TextView) view.findViewById(R.id.transaction_value);
            mType = (TextView) view.findViewById(R.id.transaction_type);
            mPrice = (TextView) view.findViewById(R.id.transaction_price);
            mDate = (TextView) view.findViewById(R.id.transaction_date);

            mMenu = (ImageView) view.findViewById(R.id.menu_transaction);

            //view.setOnLongClickListener(this);
            mMenu.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == mMenu) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.menu_transaction);
                popup.setOnMenuItemClickListener(this);
                popup.show();
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final int position = getAdapterPosition();
            Log.d(LOG_TAG, "Adapter position " + position);
            final int transactionId;
            final int holdingId;
            if (position >= 0) {
                if (mCursor != null && mCursor.moveToPosition(position)) {
                    transactionId = mCursor.getInt(PortProvider.COL_TRANS_TRANSACTION_ID);
                    holdingId = mCursor.getInt(PortProvider.COL_TRANS_HOLDING_ID);
                    Log.d(LOG_TAG, "Transaction ID :" + transactionId + "; Holding ID :" + holdingId);

                    int id = item.getItemId();
                    if(id==R.id.edit_transaction) {
                        Snackbar.make(mCoordinatorLayout, "Edit transaction for Transaction ID: " + transactionId, Snackbar.LENGTH_LONG).show();

                        Intent intentAddTransaction = new Intent(mContext, AddTransactionActivity.class);
                        AddTransactionActivity.mHoldingId = holdingId;
                        AddTransactionActivity.mTransactionId = transactionId;
                        mContext.startActivity(intentAddTransaction);
                    }
                    else if (id == R.id.delete_transaction) {
                        //Snackbar.make(mCoordinatorLayout, "Delete transaction for Transaction ID: " + transactionId, Snackbar.LENGTH_LONG).show();
                        Utilities.removeTransaction(mContext, transactionId, holdingId, mCoordinatorLayout);
                    }

                    return true;
                }
            }

            Snackbar.make(mCoordinatorLayout, R.string.holding_id_error,
                    Snackbar.LENGTH_LONG).show();
            return true;
        }

    }

    public TransactionRecyclerViewAdapter(Context context, CoordinatorLayout coordinatorLayout) {
        mContext = context;
        mCoordinatorLayout = coordinatorLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = R.layout.fragment_transaction_item;
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
        viewHolder.mStockName.setText(mCursor.getString(PortProvider.COL_TRANS_COMPANY_NAME));

        int transType = mCursor.getInt(PortProvider.COL_TRANS_TRANSACTION_TYPE);
        if(transType==3){
            viewHolder.mUnit.setText(Utilities.formatNumber(mContext, null));
        }
        else {
            String unit = Utilities.formatNumber(mContext, mCursor.getDouble(PortProvider.COL_TRANS_UNITS))
                    + mContext.getString(R.string.unit_symbol);
            viewHolder.mUnit.setText(unit);
        }

        if(transType==4 || transType==5){
            viewHolder.mValue.setText(Utilities.formatNumber(mContext, null));
        }
        else {
            String value = mContext.getString(R.string.currency_symbol) +
                    Utilities.formatNumber(mContext, mCursor.getDouble(PortProvider.COL_TRANS_VALUE));
            viewHolder.mValue.setText(value);
        }

        viewHolder.mType.setText(mCursor.getString(PortProvider.COL_TRANS_TRANSACTION_DESC));

        String price = "";
        if (transType==1 || transType==2) {
            price = mContext.getString(R.string.price_prefix) + mContext.getString(R.string.currency_symbol)
                    + Utilities.formatNumber(mContext, mCursor.getDouble(PortProvider.COL_TRANS_PRICE));
        }
        else if (transType==3){
            price = mContext.getString(R.string.price_prefix) +
                    Utilities.formatNumber(mContext, mCursor.getDouble(PortProvider.COL_TRANS_DIVIDEND_PERC))
                    + mContext.getString(R.string.percentage_symbol);
        }
        else if(transType==4 || transType==5){
            price = mContext.getString(R.string.offered_prefix)
                    + Utilities.formatNumber(mContext, mCursor.getDouble(PortProvider.COL_TRANS_OFFERED))
                    + mContext.getString(R.string.held_prefix)
                    + Utilities.formatNumber(mContext, mCursor.getDouble(PortProvider.COL_TRANS_HELD));
        }
        viewHolder.mPrice.setText(price);

        String date = mContext.getString(R.string.trans_date_prefix) + mCursor.getString(PortProvider.COL_TRANS_TRANSACTION_DATE);
        viewHolder.mDate.setText(date);
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
