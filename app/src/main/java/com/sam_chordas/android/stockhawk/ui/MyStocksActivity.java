package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{ //Callback interface for a client to interact with the manager.


  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */

  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private CharSequence mTitle;        //A CharSequence is a readable sequence of char values.
  private Intent mServiceIntent;
  private ItemTouchHelper mItemTouchHelper;   //This is a utility class to add swipe to dismiss and drag & drop support to RecyclerView.
  private static final int CURSOR_LOADER_ID = 0;
  private QuoteCursorAdapter mCursorAdapter;
  private Context mContext;
  private Cursor mCursor;
  boolean isConnected;
  private int activityResult = 0;     //stores if the ticker was valid or not from the service
  RecyclerView recyclerView;
  TextView emptyView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;

    ConnectivityManager cm =    //Class that answers queries about the state of network connectivity. It also notifies applications when network connectivity changes.
        (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE); //get an instance of connectivity manager

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo(); // get an instance that represents the current network connection.
    isConnected = activeNetwork != null &&
        activeNetwork.isConnectedOrConnecting();  //Indicates whether network connectivity exists or is in the process of being established.
    setContentView(R.layout.activity_my_stocks);  //main screen that shows stocks
    // The intent service is for executing immediate pulls from the Yahoo API
    // GCMTaskService can only schedule tasks, they cannot execute immediately
    mServiceIntent = new Intent(this, StockIntentService.class);      //create service
    if (savedInstanceState == null){
      // Run the initialize task service so that some stocks appear upon an empty database
      mServiceIntent.putExtra("tag", "init");
      if (isConnected){
        startService(mServiceIntent);   //run service
      } else{
        networkToast();
      }
    }
    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);  //call ensures that a loader is initialized and active.

    mCursorAdapter = new QuoteCursorAdapter(this, null);
    recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
            new RecyclerViewItemClickListener.OnItemClickListener() {
              @Override public void onItemClick(View v, int position) {

                //get cursor
                Cursor c = mCursorAdapter.getCursor();
                //move to stock selected
                c.moveToPosition(position);
                //get symbol from cursor and turn into a string
                String stockSymbol = c.getString(c.getColumnIndex("symbol"));

                //pass to method
                viewChart(stockSymbol);

              }
            }));

    recyclerView.setAdapter(mCursorAdapter);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setContentDescription(getString(R.string.add_stocks));
    fab.attachToRecyclerView(recyclerView);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (isConnected){
          new MaterialDialog.Builder(mContext).title(R.string.symbol_search)    //creates a dialog
              .content(R.string.content_test)   //text label
              .inputType(InputType.TYPE_CLASS_TEXT)       //type of data to enter
              .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                  // On FAB click, receive user input. Make sure the stock doesn't already exist
                  // in the DB and proceed accordingly
                  Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                      new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
                      new String[] { input.toString().toUpperCase() }, null);
                  if (c.getCount() != 0) {
                    Toast toast =
                        Toast.makeText(MyStocksActivity.this, R.string.stock_is_saved,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                    toast.show();
                    return;
                  } else {
                    // Add the stock to DB
                    mServiceIntent.putExtra("tag", "add");
                    mServiceIntent.putExtra("symbol", input.toString().toUpperCase());
                    startService(mServiceIntent);
                  }
                }
              })
              .show();
        } else {
          networkToast();
        }

      }
    });

    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(recyclerView); //Attaches the ItemTouchHelper to the provided RecyclerView.

    mTitle = getTitle(); //get activity title
    if (isConnected){
      long period = 3600L;
      long flex = 10L;
      String periodicTag = "periodic";

      // create a periodic task to pull stocks once every hour after the app has been opened. This
      // is so Widget data stays up to date.
      PeriodicTask periodicTask = new PeriodicTask.Builder() //periodic task is one that will recur at the specified interval, without needing to be rescheduled.
          .setService(StockTaskService.class)
          .setPeriod(period)  //in seconds
          .setFlex(flex)
          .setTag(periodicTag)
          .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
          .setRequiresCharging(false)
          .build();
      // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
      // are updated.
      GcmNetworkManager.getInstance(this).schedule(periodicTask);
    }
  }


  @Override
  public void onResume() {
    super.onResume();
    LocalBroadcastManager.getInstance(this).registerReceiver(BReciever, new IntentFilter("tickerStatus"));
    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

  }
  protected void onPause(){
    super.onPause();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(BReciever);
  }
//broadcast to recieve data from the service
  private BroadcastReceiver BReciever = new BroadcastReceiver(){

    @Override
    public void onReceive(Context context, Intent intent) {
      activityResult = intent.getIntExtra("result",0);

      if(activityResult==2)
       tickerNotFoundToast();
    }
  };

  //runs line chart activity
  public void viewChart(String symbol){
    Intent lineChartIntent1 = new Intent(mContext, lineChartIntent.class);
    //pass symbol to intent
    lineChartIntent1.putExtra("tickerSelected", symbol);
    lineChartIntent1.putExtra("connected",isConnected);


    mContext.startActivity(lineChartIntent1);


  }
//ticker is not found, create a toast message to the user
  public void tickerNotFoundToast(){
    Toast.makeText(mContext, R.string.stock_not_found, Toast.LENGTH_SHORT).show();
  }
//no internet connection, create a toast message to the user
  public void networkToast(){
    Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
  }

  private void updateEmptyView(){
    emptyView = (TextView) findViewById(R.id.listview_stocktable_empty);

    if(null !=emptyView){
      int message = R.string.empty_list;
      if(!isConnected){
        message = R.string.no_network_available;
      }
      emptyView.setText(message);
    }
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.my_stocks, menu);
      restoreActionBar();
      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {

      return true;
    }

    if (id == R.id.action_change_units){
      // this is for changing stock changes from percent value to dollar value
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args){
    // This narrows the return to only the stocks that are most current.
    return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
        QuoteColumns.ISCURRENT + " = ?",
        new String[]{"1"},
        null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data){
    mCursorAdapter.swapCursor(data);
    mCursor = data;

    //if there is no data then show empty data mesasge
    if(mCursor.getCount()==0){
      updateEmptyView();
      recyclerView.setVisibility(View.INVISIBLE);
      emptyView.setVisibility(View.VISIBLE);
    }else{
      recyclerView.setVisibility(View.VISIBLE);

      if(emptyView!=null)
        emptyView.setVisibility(View.INVISIBLE);
    }

  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader){
    mCursorAdapter.swapCursor(null);
  }

}
