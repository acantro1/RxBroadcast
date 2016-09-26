package com.cantrowitz.rxbroadcast;

import android.content.Context;
import android.content.Intent;

/**
 * Created by adamcantrowitz on 9/26/16.
 */

public interface OrderedBroadcastAborter {
    /**
     * Based on the input parameters this method decides if the
     * {@linkplain android.content.BroadcastReceiver} should abort in the case of an Ordered
     * Broadcast
     *
     * @param context - Context from the BroadcastReceiver
     * @param intent  - Intent to perform logic on
     * @return true if the Broadcast should be aborted, false otherwise
     */
    boolean shouldAbortBroadcast(Context context, Intent intent);

    /**
     * Based on the input parameters this method decides if the
     * {@linkplain android.content.BroadcastReceiver} should clear the abort in the case of an
     * Ordered Broadcast
     *
     * @param context - Context from the BroadcastReceiver
     * @param intent  - Intent to perform logic on
     * @return true if the Broadcast should have the abort canceled, false otherwise
     */
    boolean shouldClearAbortBroadcast(Context context, Intent intent);

    class Factory {

        static OrderedBroadcastAborter createAlwaysFalseAborter() {
            return new OrderedBroadcastAborter() {

                @Override
                public boolean shouldAbortBroadcast(Context context, Intent intent) {
                    return false;
                }

                @Override
                public boolean shouldClearAbortBroadcast(Context context, Intent intent) {
                    return false;
                }
            };

        }
    }
}
