package com.husky.uw.myapplication;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link android.app.Service} that publishes a {@link com.google.android.glass.timeline.LiveCard} in the timeline.
 */
public class LiveCardService extends Service {

    private static final String LIVE_CARD_TAG = "LiveCardService";

    private LiveCard mLiveCard;
    private TCPClient tcp;
    private CopyOnWriteArrayList<String> responses;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            this.responses = new CopyOnWriteArrayList<String>();
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000);
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            LiveCardRenderer renderer = new LiveCardRenderer(getResources(),this, this.responses);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(renderer);

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            GetCredentials credentials = new GetCredentials(getResources(),this,this.tcp, this.responses);
            credentials.execute();
            mLiveCard.attach(this);
            mLiveCard.publish(PublishMode.REVEAL);
        } else {
            mLiveCard.navigate();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }

        super.onDestroy();
    }
}