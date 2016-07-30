package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the CursorRecyclerViewApater.java code and idea.
 */
public abstract class CursorRecyclerViewAdapter <VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
  private static final String LOG_TAG = CursorRecyclerViewAdapter.class.getSimpleName();
  private Cursor mCursor;
  private boolean dataIsValid;
  private int rowIdColumn;
  private DataSetObserver mDataSetObserver;  //recieves callback when data has been changed or invalid
  public CursorRecyclerViewAdapter(Context context, Cursor cursor){
    mCursor = cursor;
    dataIsValid = cursor != null;  //set to true if cursor is not null
    rowIdColumn = dataIsValid ? mCursor.getColumnIndex("_id") : -1;  //dataIsValid true return the column index, otherwise return -1
    mDataSetObserver = new NotifyingDataSetObserver();    //creates a new data set observer
    if (dataIsValid){
      mCursor.registerDataSetObserver(mDataSetObserver);  //Register an observer that is called when changes happen to the data used by this adapter.
    }
  }

  public Cursor getCursor(){
    return mCursor;
  }   //returns cursor

  @Override
  public int getItemCount(){            //returns number of items in the cursor
    if (dataIsValid && mCursor != null){
      return mCursor.getCount();
    }
    return 0;
  }

  @Override public long getItemId(int position) {       //get row id of item selected
    if (dataIsValid && mCursor != null && mCursor.moveToPosition(position)){
      return mCursor.getLong(rowIdColumn);
    }
    return 0;
  }

  @Override public void setHasStableIds(boolean hasStableIds) {
    super.setHasStableIds(true);
  }  //Returns true if this adapter publishes a unique long
     // value that can act as a key for the item at a given position in the data set.


  public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);  //Called by RecyclerView to display the data at the specified position.


  @Override
  public void onBindViewHolder(VH viewHolder, int position) {
    if (!dataIsValid){
      throw new IllegalStateException("This should only be called when Cursor is valid");
    }
    if (!mCursor.moveToPosition(position)){
      throw new IllegalStateException("Could not move Cursor to position: " + position);
    }

    onBindViewHolder(viewHolder, mCursor);
  }

  public Cursor swapCursor(Cursor newCursor){
    if (newCursor == mCursor){
      return null;
    }
    final Cursor oldCursor = mCursor;
    if (oldCursor != null && mDataSetObserver != null){
      oldCursor.unregisterDataSetObserver(mDataSetObserver);  //will no longer track changes for this cursor
    }
    mCursor = newCursor;
    if (mCursor != null){
      if (mDataSetObserver != null){
        mCursor.registerDataSetObserver(mDataSetObserver);  //track changes to the new cursor
      }
      rowIdColumn = newCursor.getColumnIndexOrThrow("_id");  //get row
      dataIsValid = true;
      notifyDataSetChanged();                 //Notify registered observers that the data has changed
    }else{
      rowIdColumn = -1;
      dataIsValid = false;
      notifyDataSetChanged();
    }
    return oldCursor;
  }

  private class NotifyingDataSetObserver extends DataSetObserver{
    @Override public void onChanged() {  //This method is called when the entire data set has changed, most likely through a call to requery() on a Cursor.
      super.onChanged();
      dataIsValid = true;
      notifyDataSetChanged();         //Notify any registered observers that the data set has changed.
    }

    @Override public void onInvalidated() {
      super.onInvalidated();
      dataIsValid = false;
      notifyDataSetChanged();
    }
  }
}
