package layout;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.sam_chordas.android.stockhawk.service.StockTaskService;

/**
 * Implementation of App Widget functionality.
 */
public class stock_widget extends AppWidgetProvider {


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

    //    CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
    //    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
    //    views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
    //    appWidgetManager.updateAppWidget(appWidgetId, views);
/*
        String symbol = "IBM";
        String bidPrice = "165.00";
        String change = "-100";
        String description = "stocks";

        int layout_id = R.layout.stock_widget;
        RemoteViews views = new RemoteViews(context.getPackageName(), layout_id);

        //Add the data to the RemoteViews
        views.setTextViewText(R.id.stock_symbol_widget,symbol);
        views.setTextViewText(R.id.bid_price_widget,bidPrice);
        views.setTextViewText(R.id.change_widget,change);

        //create an intent to launch activity
        Intent intent = new Intent(context, MyStocksActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        views.setOnClickPendingIntent(R.id.widget,pendingIntent);

        //Tell the AppWidgetManger to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId,views);
      */
        context.startService(new Intent(context,TodayWidgetIntentService.class));


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
     //   for (int appWidgetId : appWidgetIds) {
     //       updateAppWidget(context, appWidgetManager, appWidgetId);
     //   }

        context.startService(new Intent(context,TodayWidgetIntentService.class));
    }

    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions){

        context.startService(new Intent(context,TodayWidgetIntentService.class));

    }
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent){
        super.onReceive(context,intent);
        if(StockTaskService.ACTION_DATA_UPDATED.equals(intent.getAction())){
            System.out.println("widgetProvider ACTIONDATAUPDATE RECIEVED");
            context.startService(new Intent(context,TodayWidgetIntentService.class));
        }

    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}

