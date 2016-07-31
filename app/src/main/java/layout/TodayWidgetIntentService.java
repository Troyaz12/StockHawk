package layout;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by TroysMacBook on 7/30/16.
 */
public class TodayWidgetIntentService extends IntentService {

    private static final String[] STOCK_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };

    private static final int STOCK_ID=0;
    private static final int STOCK_SYMBOL=1;
    private static final int STOCK_BIDPRICE=2;
    private static final int STOCK_PERCENT_CHANGE=3;
    private static final int STOCK_CHANGE=4;
    private static final int STOCK_ISUP =5;

 //   private static final int CURSOR_LOADER_ID = 0;
  //  private QuoteCursorAdapter mCursorAdapter;




    public TodayWidgetIntentService(){
        super("TodayWidgetIntentService");
        System.out.println("today widget intent service cunstructor");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //get widget ids
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,stock_widget.class));

        //get data from Content Provider
        Cursor data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,STOCK_COLUMNS,null,null,null);

        if(data==null)
               return;

        if(!data.moveToFirst()){
            data.close();
            return;
        }

        for(int appWidgetId:appWidgetIds) {

       //     recyclerView.setLayoutManager(new LinearLayoutManager(this));
       //     getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);  //call ensures that a loader is initialized and active.

      //      int recyclerView = R.id.recycler_view;
    //        RemoteViews views = new RemoteViews(getPackageName(), recyclerView);
     //       mCursorAdapter = new QuoteCursorAdapter(this, null);
      //      views.setAdapter(mCursorAdapter);
       //     views.
            System.out.println("widget service exe.");
            int layoutId = R.layout.stock_widget;
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.stock_widget);
       //     do {
            //    RemoteViews views = new RemoteViews(getPackageName(),R.layout.widget_layout);
                //get data from cursor
                int stock_ID = data.getInt(STOCK_ID);
                String stockSymbol = "aaa";//data.getString(STOCK_SYMBOL);
                System.out.println("widget service exe" +stockSymbol);

                String stockBidPrice = "stockbid";//data.getString(STOCK_BIDPRICE);
                String stockPercentChange = "stockChange"; //data.getString(STOCK_PERCENT_CHANGE);
                String stokChange = data.getString(STOCK_CHANGE);
                String stockISUP = data.getString(STOCK_ISUP);

                //add data to views
        //    views.setTextViewText(R.id.stock_symbol_widget, stockSymbol);
            //     views.setTextViewText(R.id.bid_price_widget, stockBidPrice);
           //     views.setTextViewText(R.id.change_widget, stockPercentChange);

         //       views.addView(R.id.view_container,subView);

                //create an Intent to launch MainActivity
                Intent launchIntent = new Intent(this, MyStocksActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);

                //Tell the AppWidgetManager to perform an update on the current app widget
                appWidgetManager.updateAppWidget(appWidgetId, views);


     //       }while(data.moveToNext());

        }

                data.close();
    }


}
