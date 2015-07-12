package com.lundincast.my_finance_manager.activities.data;

/**
 * Created by lundincast on 5/07/15.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.EditTransactionActivity;

// import com.h6ah4i.android.example.advrecyclerview.R;
//import com.h6ah4i.android.example.advrecyclerview.common.compat.MorphButtonCompat;
//import com.h6ah4i.android.example.advrecyclerview.common.utils.ViewUtils;
// import com.wnafee.vector.MorphButton;

public class MyExpandableItemAdapter
        extends AbstractExpandableItemAdapter<MyExpandableItemAdapter.MyGroupViewHolder, MyExpandableItemAdapter.MyChildViewHolder> {
    private static final String TAG = "MyExpandableItemAdapter";

    private Context mContext;
    private TransactionExpandableDataProvider mProvider;

    public static abstract class MyBaseViewHolder extends AbstractExpandableItemViewHolder {
        public FrameLayout mContainer;
        public View mDragHandle;
        public TextView mTextView;


        public MyBaseViewHolder(View v) {
            super(v);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
//            mDragHandle = v.findViewById(R.id.drag_handle);
            mTextView = (TextView) v.findViewById(R.id.transaction_list_header);

            // hide the drag handle
//            mDragHandle.setVisibility(View.GONE);
        }
    }

    public static class MyGroupViewHolder extends MyBaseViewHolder {
//        public MorphButtonCompat mMorphButton;

        public MyGroupViewHolder(View v) {
            super(v);
//            mMorphButton = new MorphButtonCompat(v.findViewById(R.id.indicator));
        }
    }

    public static class MyChildViewHolder extends MyBaseViewHolder implements View.OnClickListener {

        public TextView mNameTvChild;
        public TextView mDateTvChild;
        public TextView mPriceTvChild;
        public TextView mHiddenIdChild;
        public ViewHolderClicks mListener;

        public MyChildViewHolder(View v, ViewHolderClicks listener) {
            super(v);
            mListener = listener;
            mNameTvChild = (TextView) v.findViewById(R.id.comment_entry);
            mDateTvChild = (TextView) v.findViewById(R.id.name_entry);
            mPriceTvChild = (TextView) v.findViewById(R.id.transaction_price);
            mHiddenIdChild = (TextView) v.findViewById(R.id.hidden_id);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onViewItemClick();
        }

        public static interface ViewHolderClicks {
            public void onViewItemClick();
        }
    }

    public MyExpandableItemAdapter(Context context, TransactionExpandableDataProvider dataProvider) {
        mContext = context;
        mProvider = dataProvider;

        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return mProvider.getGroupCount();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return mProvider.getChildCount(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mProvider.getGroupItem(groupPosition).getGroupId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mProvider.getChildItem(groupPosition, childPosition).getChildId();
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.activity_list_transaction_group, parent, false);
        return new MyGroupViewHolder(v);
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.activity_list_transactions_entry, parent, false);
        MyChildViewHolder vh = new MyChildViewHolder(v, new MyChildViewHolder.ViewHolderClicks() {
            @Override
            public void onViewItemClick() {
                Intent editIntent = new Intent(mContext, EditTransactionActivity.class);
                TextView hiddenIdTv = (TextView) v.findViewById(R.id.hidden_id);
                editIntent.putExtra("transactionId", Long.valueOf(hiddenIdTv.getText().toString()));
                mContext.startActivity(editIntent);
            }
        });
        return vh;
    }

    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder holder, int groupPosition, int viewType) {
        // child item
        final TransactionExpandableDataProvider.GroupData item = mProvider.getGroupItem(groupPosition);

        // set text
        holder.mTextView.setText(item.getText());

        // mark as clickable
        holder.itemView.setClickable(true);

        // set background resource (target view ID: container)
        final int expandState = holder.getExpandStateFlags();

        if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;
//            MorphButton.MorphState indicatorState;

            if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_EXPANDED) != 0) {
//                bgResId = R.drawable.bg_group_item_expanded_state;
//                indicatorState = MorphButton.MorphState.END;
            } else {
//                bgResId = R.drawable.bg_group_item_normal_state;
//                indicatorState = MorphButton.MorphState.START;
            }

//            holder.mContainer.setBackgroundResource(bgResId);

//            if (holder.mMorphButton.getState() != indicatorState) {
//                holder.mMorphButton.setState(indicatorState, true);
//            }
        }
    }

    @Override
    public void onBindChildViewHolder(MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
        // group item
        final TransactionExpandableDataProvider.ChildData item = mProvider.getChildItem(groupPosition, childPosition);

        // set text
        holder.mNameTvChild.setText(item.getChildName());
        holder.mDateTvChild.setText(String.valueOf(item.getChildDate()));
        holder.mPriceTvChild.setText(String.valueOf(item.getChildPrice()));
        holder.mHiddenIdChild.setText(String.valueOf(item.getTransactionId()));

        // mark as clickable
        holder.itemView.setClickable(true);

        // set background resource (target view ID: container)
//        int bgResId;
//        bgResId = R.drawable.bg_item_normal_state;
//        holder.mContainer.setBackgroundResource(bgResId);
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        // check the item is *not* pinned
        if (mProvider.getGroupItem(groupPosition).isPinnedToSwipeLeft()) {
            // return false to raise View.OnClickListener#onClick() event
            return false;
        }

        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())) {
            return false;
        }

        final View containerView = holder.mContainer;
        final View dragHandleView = holder.mDragHandle;

//        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
//        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

//        return !ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
        return true;
    }
}