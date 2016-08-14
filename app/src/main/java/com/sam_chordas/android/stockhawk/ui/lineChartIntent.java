package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.getStockHistoryTask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by TroysMacBook on 8/7/16.
 */
public class lineChartIntent extends AppCompatActivity{
    LineChart lineChart;
    String[][] stockData;
    ArrayList<Entry> entries = new ArrayList<>();;
    int count = 0;
    Float stockPrice;

    private static final String[] STOCK_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CREATED,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };

    private static final int STOCK_ID=0;
    private static final int STOCK_SYMBOL=1;
    private static final int STOCK_BIDPRICE=2;
    private static final int STOCK_PERCENT_CHANGE=3;
    private static final int STOCK_CREATED = 4;
    private static final int STOCK_CHANGE=5;
    private static final int STOCK_ISUP =6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        Bundle b = getIntent().getExtras();
        String ticker = (String) b.get("tickerSelected");
        Boolean connectionStatus= (Boolean) b.get("connected");

        if(connectionStatus==true){
            try {
                stockData = new getStockHistoryTask(this)
                        .execute(ticker)
                        .get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < stockData.length; i++) {

                if (count > 30) {
                    stockPrice = Float.parseFloat(stockData[i][1]);
                    entries.add(new Entry(i, stockPrice));
                    count = 0;
                }
                count++;
            }
        }else{
            Cursor stockSavedData = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, STOCK_COLUMNS,QuoteColumns.SYMBOL + " = ?",new String[]{ticker},STOCK_CREATED+" ASC");
            stockSavedData.moveToFirst();

            for (int i = 0; i < stockSavedData.getCount(); i++) {

                if (count > 30&&stockSavedData.getCount()>120) {

                  stockPrice =(float) stockSavedData.getColumnIndex("bid_price");
                    entries.add(new Entry(i, stockPrice));
                    count = 0;
                    stockSavedData.moveToNext();
                }else{
                    String stock =stockSavedData.getString(STOCK_BIDPRICE);

                    stockPrice = Float.parseFloat(stock);

                    entries.add(new Entry(i, stockPrice));
                    stockSavedData.moveToNext();
                }
                count++;
            }
        }

        lineChart = (LineChart) findViewById(R.id.lineChart);
        lineChart.setDescription(ticker +" 1 Year Stock Price Chart");
        lineChart.setDescriptionTextSize(16);
        lineChart.setDrawBorders(true);
        LineDataSet dataSet = new LineDataSet(entries, "stock chart");
        LineData lineData = new LineData(dataSet);
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(R.color.Gold);
        dataSet.setValueTextSize(10);

        lineChart.setData(lineData);
    }

}
