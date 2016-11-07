package com.cantrowitz.rxbroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;

/**
 * Created by adamcantrowitz on 9/1/15.
 */
public class RxBroadcast {

    private static final OrderedBroadcastAbortStrategy NO_OP_ORDERED_BROADCAST_STRATEGY = new
            OrderedBroadcastAbortStrategy() {
                @Override
                public void handleOrderedBroadcast(Context context,
                                                   Intent intent,
                                                   BroadcastReceiverAbortProxy abortProxy) {
                    //no-op
                }
            };

    private RxBroadcast() {
        throw new AssertionError("No instances");
    }

    /**
     * Create {@link Observable} that wraps {@link BroadcastReceiver} and emits received intents.
     *
     * @param context      the context the {@link BroadcastReceiver} will be created from
     * @param intentFilter the filter for the particular intent
     * @return {@link Observable} of {@link Intent} that matches the filter
     */
    public static Observable<Intent> fromBroadcast(Context context, IntentFilter intentFilter) {
        return fromBroadcast(context, intentFilter, NO_OP_ORDERED_BROADCAST_STRATEGY);
    }

    /**
     * Create {@link Observable} that wraps {@link BroadcastReceiver} and emits received intents.
     * <p>
     * <em>This is only useful in conjunction with Ordered Broadcasts, e.g.,
     * {@link Context#sendOrderedBroadcast(Intent, String)}</em>
     *
     * @param context                  the context the {@link BroadcastReceiver} will be
     *                                 created from
     * @param intentFilter             the filter for the particular intent
     * @param orderedBroadcastAbortStrategy the strategy to use for Ordered Broadcasts
     * @return {@link Observable} of {@link Intent} that matches the filter
     */
    public static Observable<Intent> fromBroadcast(
            Context context,
            IntentFilter intentFilter,
            OrderedBroadcastAbortStrategy orderedBroadcastAbortStrategy) {
        BroadcastRegistrar broadcastRegistrar = new BroadcastRegistrar(context, intentFilter);
        return createBroadcastObservable(broadcastRegistrar, orderedBroadcastAbortStrategy);
    }

    /**
     * Create {@link Observable} that wraps {@link BroadcastReceiver} and emits received intents.
     *
     * @param context             the context the {@link BroadcastReceiver} will be created from
     * @param intentFilter        the filter for the particular intent
     * @param broadcastPermission String naming a permissions that a broadcaster must hold in
     *                            order to send an Intent to you. If null, no permission is
     *                            required.
     * @param handler             Handler identifying the thread that will receive the Intent. If
     *                            null, the main thread of the process will be used.
     * @return {@link Observable} of {@link Intent} that matches the filter
     */
    public static Observable<Intent> fromBroadcast(
            Context context,
            IntentFilter intentFilter,
            String broadcastPermission,
            Handler handler) {
        return fromBroadcast(
                context,
                intentFilter,
                broadcastPermission,
                handler,
                NO_OP_ORDERED_BROADCAST_STRATEGY
        );
    }

    /**
     * Create {@link Observable} that wraps {@link BroadcastReceiver} and emits received intents.
     * <p>
     * <em>This is only useful in conjunction with Ordered Broadcasts, e.g.,
     * {@link Context#sendOrderedBroadcast(Intent, String)}</em>
     *
     * @param context                  the context the {@link BroadcastReceiver} will be created
     *                                 from
     * @param intentFilter             the filter for the particular intent
     * @param broadcastPermission      String naming a permissions that a broadcaster must hold
     *                                 in order to send an Intent to you. If null, no permission
     *                                 is required.
     * @param handler                  Handler identifying the thread that will receive the
     *                                 Intent. If null, the main thread of the process will be used.
     * @param orderedBroadcastAbortStrategy the strategy to use for Ordered Broadcasts
     * @return {@link Observable} of {@link Intent} that matches the filter
     */
    public static Observable<Intent> fromBroadcast(
            Context context,
            IntentFilter intentFilter,
            String broadcastPermission,
            Handler handler,
            OrderedBroadcastAbortStrategy orderedBroadcastAbortStrategy) {
        BroadcastWithPermissionsRegistrar broadcastWithPermissionsRegistrar =
                new BroadcastWithPermissionsRegistrar(
                        context,
                        intentFilter,
                        broadcastPermission,
                        handler);
        return createBroadcastObservable(
                broadcastWithPermissionsRegistrar,
                orderedBroadcastAbortStrategy);
    }

    /**
     * Create {@link Observable} that wraps {@link BroadcastReceiver} and emits received intents.
     * <p>
     * This uses a {@link LocalBroadcastManager}
     *
     * @param context      the context the {@link BroadcastReceiver} will be created from
     * @param intentFilter the filter for the particular intent
     * @return {@link Observable} of {@link Intent} that matches the filter
     */
    public static Observable<Intent> fromLocalBroadcast(
            Context context,
            IntentFilter intentFilter) {
        LocalBroadcastRegistrar localBroadcastRegistrar = new LocalBroadcastRegistrar(
                intentFilter,
                LocalBroadcastManager.getInstance(context));
        return createBroadcastObservable(localBroadcastRegistrar, NO_OP_ORDERED_BROADCAST_STRATEGY);
    }

    private static Observable<Intent> createBroadcastObservable(
            final BroadcastRegistrarStrategy broadcastRegistrarStrategy,
            final OrderedBroadcastAbortStrategy orderedBroadcastAbortStrategy) {
        return Observable.create(new ObservableOnSubscribe<Intent>() {

            @Override
            public void subscribe(final ObservableEmitter<Intent> intentEmitter) throws Exception {
                final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        intentEmitter.onNext(intent);

                        if (isOrderedBroadcast()) {
                            orderedBroadcastAbortStrategy.handleOrderedBroadcast(
                                    context,
                                    intent,
                                    BroadcastReceiverAbortProxy.create(this));
                        }
                    }
                };

                intentEmitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        broadcastRegistrarStrategy.unregisterBroadcastReceiver(broadcastReceiver);
                    }
                });

                broadcastRegistrarStrategy.registerBroadcastReceiver(broadcastReceiver);
            }
        });
    }
}
