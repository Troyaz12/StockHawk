package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService { //handle asynchronous requests (expressed as Intents) on demand.

  public StockIntentService(){
    super(StockIntentService.class.getName());  //return the name of the class
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {    //This method is invoked on the worker thread with a request to process.
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.

    int results = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));    //get result code
    sendBroadcast(results);

    System.out.println("gsm result: "+results);
  }

  private void sendBroadcast(int results){
    Intent intent = new Intent ("tickerStatus");
    intent.putExtra("result", results);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
  }

}
