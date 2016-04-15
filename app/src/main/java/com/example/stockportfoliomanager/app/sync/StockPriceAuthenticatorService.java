package com.example.stockportfoliomanager.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StockPriceAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private StockPriceAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new StockPriceAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
