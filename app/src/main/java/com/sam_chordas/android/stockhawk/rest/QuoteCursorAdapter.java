package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{

  private static Context mContext;
  private static Typeface robotoLight;    //specifies the typeface and intrinsic style of a font
  private boolean isPercent;
  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;
    
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){  // RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item
    robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");  //Provides access to an application's raw asset files;
    View itemView = LayoutInflater.from(parent.getContext())      //Obtains the LayoutInflater from the given context.
            .inflate(R.layout.list_item_quote, parent, false);    //inflate this view
    ViewHolder vh = new ViewHolder(itemView);     //put view in viewholder
    return vh;      //return the viewholder
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){  //The new ViewHolder will be used to display items of the adapter using onBindViewHolder(ViewHolder, int, List)
    viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));     //get the symbol
    viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex("bid_price"))); //get the bid price
    int sdk = Build.VERSION.SDK_INT;                            //get build version of android device
    if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1){  //is the stock up
      if (sdk < Build.VERSION_CODES.JELLY_BEAN){
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green)); //put a green background around the change #
      }else {
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }
    } else{
      if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red)); //if down put a red background around the change #
      } else{
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      }
    }
    if (Utils.showPercent){     //is the app set to show the percent changed
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change"))); //set data in the change textview
    } else{
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("change")));  //set data in the change textview
    }
  }

  @Override public void onItemDismiss(int position) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));             //get the symbol selected
    mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);    //deletes symbol from the database
    notifyItemRemoved(position); //Notify any registered observers that the item previously located at position has been removed from the data set
  }

  @Override public int getItemCount() {
    return super.getItemCount();
  }   //get item count
//ViewHolder class
  public static class ViewHolder extends RecyclerView.ViewHolder
      implements ItemTouchHelperViewHolder, View.OnClickListener{
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    public ViewHolder(View itemView){       //constructor
      super(itemView);

      //getting all textviews from listItemQuote
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(robotoLight);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected(){
      itemView.setBackgroundColor(Color.LTGRAY);
    }     //set backgound to gray if item is selected

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
    } //resets background color

    @Override
    public void onClick(View v) {

    }
  }
}
