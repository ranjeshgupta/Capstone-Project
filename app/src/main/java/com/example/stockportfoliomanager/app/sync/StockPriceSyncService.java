package com.example.stockportfoliomanager.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StockPriceSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static StockPriceSyncAdapter sStockPriceSyncAdapter = null;
    private final String LOG_TAG = StockPriceSyncService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate - StockPriceSyncService");
        synchronized (sSyncAdapterLock) {
            if (sStockPriceSyncAdapter == null) {
                sStockPriceSyncAdapter = new StockPriceSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sStockPriceSyncAdapter.getSyncAdapterBinder();
    }
}
