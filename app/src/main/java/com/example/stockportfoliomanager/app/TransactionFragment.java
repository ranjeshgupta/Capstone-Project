package com.example.stockportfoliomanager.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.stockportfoliomanager.app.data.PortContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = TransactionFragment.class.getSimpleName();
    private CoordinatorLayout mCoordinatorLayout;
    private Context mContext;
    RecyclerView mRecyclerView;
    TextView mEmptyView;
    private TransactionRecyclerViewAdapter mTransactionAdapter;
    private static final int TRANSACTION_LOADER = 0;
    AdView mAdView;

    static final String HOLDING_ID = "HOLDING_ID";
    private int mHoldingId = 0;

    public TransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        mContext = view.getContext();
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mHoldingId = arguments.getInt(TransactionFragment.HOLDING_ID, 0);
        }
        Log.d(LOG_TAG, "Holding ID: " + mHoldingId);

        // Set the adapter
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_transaction);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources()
                .getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mTransactionAdapter = new TransactionRecyclerViewAdapter(mContext, mCoordinatorLayout);
        mRecyclerView.setAdapter(mTransactionAdapter);

        mEmptyView = (TextView) view.findViewById(R.id.listview_empty);

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().setTitle(R.string.title_transaction_view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRANSACTION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().setTitle(R.string.title_transaction_view);
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(mHoldingId != 0) {
            Uri transactionUri = PortContract.TransactionEntry.buildHoldingAllTransactions(mHoldingId);

            return new CursorLoader(mContext,
                    transactionUri,
                    null,
                    null,
                    null,
                    null);
        }
        else {
            int portId = Integer.parseInt(Utilities.getPreferencePortId(mContext));
            Uri transactionUri = PortContract.TransactionEntry.buildPortAllTransactions(portId);

            return new CursorLoader(mContext,
                    transactionUri,
                    null,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTransactionAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mTransactionAdapter.swapCursor(data);
        updateEmptyView();
    }

    private void updateEmptyView() {
        if ( mRecyclerView.getAdapter().getItemCount() == 0 ) {
            int message = R.string.empty_holding_list;
            mEmptyView.setText(message);
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
        else {
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
