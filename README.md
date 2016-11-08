# RxBroadcast
Reactive `Broadcast` and `LocalBroadcast` for Android.

![badge](https://travis-ci.org/cantrowitz/RxBroadcast.svg?branch=master)

# Usage
#### System Broadcast
`Observable<Intent> = RxBroadcast.fromBroadcast(context, intentFilter);`

or 

`Observable<Intent> = RxBroadcast.fromBroadcast(context, intentFilter, broadcastPermission, handler);`

#### LocalBroadcast
`Observable<Intent> = RxBroadcast.fromLocalBroadcast(context, intentFilter);`

# Unregistering
Be sure to unregistering your receiver, this is typically done in the `Activity.onStop()`, by simply unsubscribing from your earlier subscription.

`Disposable disposable = RxBroadcast.fromLocalBroadcast(context, intentFilter).subscribe(...);`

...
```
  @Override
  protected void onStop() {
    super.onStop();

    if (disposable != null) {
      disposable.dispose();
    }
  }
```

# Ordered Broadcasts
```java
   IntentFilter intentFilter = new IntentFilter(ACTION_PRIORITY);
   intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
   final OrderedBroadcastAbortStrategy allowMultiplesOfFive = new
           OrderedBroadcastAbortStrategy() {
               @Override
               public void handleOrderedBroadcast(
                             Context context, 
                             Intent intent,
                             BroadcastReceiverAbortProxy broadcastReceiverAbortProxy) {
                   int value = intent.getIntExtra(EXTRA_DATA, 0);
                   if (value % 5 == 0) {
                       broadcastReceiverAbortProxy.clearAbortBroadcast();
                   } else {
                       broadcastReceiverAbortProxy.abortBroadcast();
                   }
               }
           };
   return RxBroadcast.fromBroadcast(
           this,
           intentFilter,
           allowMultiplesOfFive)
```

# Installation

```groovy
compile 'com.cantrowitz:rxbroadcast:2.0.0'
```

License
-------

    Copyright 2015 Adam Cantrowitz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
