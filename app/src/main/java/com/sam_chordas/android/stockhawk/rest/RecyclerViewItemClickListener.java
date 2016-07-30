package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by sam_chordas on 11/9/15.
 */
public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

  @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {   //Called when a child of RecyclerView does not want RecyclerView and its ancestors to intercept touch events with onInterceptTouchEvent(MotionEvent).

  }

  private GestureDetector gestureDetector;  //Detects various gestures and events using the supplied MotionEvents.
  private OnItemClickListener listener;     //Interface definition for a callback to be invoked when an item in this AdapterView has been clicked.

  public interface OnItemClickListener{
    public void onItemClick(View v, int position);
  }

  public RecyclerViewItemClickListener(Context context, OnItemClickListener listener) {
    this.listener = listener;
    gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
      @Override public boolean onSingleTapUp(MotionEvent e) {  //convenience class to extend when you only want to listen for a subset of all the gestures.
        return true;
      }
    });
  }

  @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {  //Silently observe and/or take over touch events sent to the RecyclerView before they are handled by either the RecyclerView itself or its child views.
    View childView = view.findChildViewUnder(e.getX(), e.getY());       //Find the topmost view under the given point.
    if (childView != null && listener != null && gestureDetector.onTouchEvent(e)) {  //gestureDetector.onTouchEvent(e)) If this method is used to detect click actions, it is recommended that the actions be performed by implementing and calling performClick(). This will ensure consistent system behavior, including:

      listener.onItemClick(childView, view.getChildPosition(childView));
      return true;
    }
    return false;
  }

  @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { } //overidden with nothing fill out this method
}
