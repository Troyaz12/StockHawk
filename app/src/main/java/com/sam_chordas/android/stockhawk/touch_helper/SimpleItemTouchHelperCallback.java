package com.sam_chordas.android.stockhawk.touch_helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by sam_chordas on 10/6/15.
 * credit to Paul Burke (ipaulpro)
 * this class enables swipe to delete in RecyclerView
 */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{  //This class is the contract between ItemTouchHelper
  private final ItemTouchHelperAdapter mAdapter;                              //and your application. It lets you control which touch behaviors are enabled per each ViewHolder and also receive callbacks when user performs these actions.
  public static final float ALPHA_FULL = 1.0f;                                //behaviors are enabled per each ViewHolder and also receive callbacks when user performs these actions.

  public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter){
    mAdapter = adapter;
  }

  @Override
  public boolean isItemViewSwipeEnabled(){
    return true;
  }

  @Override
  public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder){ //To control which actions user can take on each view,
    final int dragFlags = 0;
    final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
    return makeMovementFlags(dragFlags, swipeFlags);
  }

  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder sourceViewHolder, RecyclerView.ViewHolder targetViewHolder){
    return true;        //Called when ItemTouchHelper wants to move the dragged item from its old position to the new position.
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int i){ //Called when a ViewHolder is swiped by the user.
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
  }


  @Override
  public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState){ //Called when the ViewHolder swiped or dragged by the ItemTouchHelper is changed.

    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE){
      ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
      itemViewHolder.onItemSelected();
    }

    super.onSelectedChanged(viewHolder, actionState);
  }

  @Override
  public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) { //Called by the ItemTouchHelper when the user interaction with an element is over and it also completed its animation.
    super.clearView(recyclerView, viewHolder);

    ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
    itemViewHolder.onItemClear();
  }
}
