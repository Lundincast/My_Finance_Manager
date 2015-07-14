package com.lundincast.my_finance_manager.activities.data;

/**
 * Created by lundincast on 5/07/15.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class TransactionExpandableDataProvider {
    private List<Pair<GroupData, List<ChildData>>> mData;

    // for undo group item
    private Pair<GroupData, List<ChildData>> mLastRemovedGroup;
    private int mLastRemovedGroupPosition = -1;

    // for undo child item
    private ChildData mLastRemovedChild;
    private long mLastRemovedChildParentGroupId = -1;
    private int mLastRemovedChildPosition = -1;



    public TransactionExpandableDataProvider(Context context, Cursor cursor) {

        mData = new LinkedList<>();

        long groupId = -1;
        long childId = 0;
        double groupTotalPrice = 0;
        String currentMonth = "";
        ConcreteGroupData group = null;
        List<ChildData> children = new ArrayList<>();

        // String arrays for proper date display
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] monthsComplete = {"January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December"};

        // get preferences for currency display
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String currPref = sharedPref.getString("pref_key_currency", "1");

        cursor.moveToFirst();
        do {
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_MONTH));
            if (!month.equals(currentMonth)) {
                if (groupId != -1) {
                    // when new group, add total price to group, commit pair for last group and reset children
                    String formattedGroupTotalPrice;
                    if (currPref.equals("2")) {
                        formattedGroupTotalPrice = String.format("%.2f", groupTotalPrice) + " $";
                    } else {
                        formattedGroupTotalPrice = String.format("%.2f", groupTotalPrice) + " €";
                    }
                    group.setTotalPrice(formattedGroupTotalPrice);
                    groupTotalPrice = 0;
                    mData.add(new Pair<GroupData, List<ChildData>>(group, children));
                    children = new ArrayList<>();
                }
                // set group
                groupId++;
                currentMonth = month;
                String groupText = monthsComplete[Integer.valueOf(month)]
                        + " " + cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_YEAR));
                int groupSwipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
                group = new ConcreteGroupData(groupId, groupText, groupSwipeReaction);
            }
                // set children
                childId = group.generateNewChildId();
                final long childTransactionId = cursor.getLong(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_ID));
                final String childName = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_COMMENT));
                // retrieve date from cursor and convert it to displayable string
                final long date = cursor.getLong(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_DATE));
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(date);
                final String childDate = days[cal.get(Calendar.DAY_OF_WEEK) - 1].toUpperCase() + ", "
                        + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + " "
                        + months[cal.get(Calendar.MONTH)] + " "
                        + Integer.toString(cal.get(Calendar.YEAR));
                final double childPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_PRICE));
                String formattedChildPrice;
                if (currPref.equals("2")) {
                    formattedChildPrice = String.format("%.2f", childPrice) + " $";
                } else {
                    formattedChildPrice = String.format("%.2f", childPrice) + " €";
                }
                // update group total price
                groupTotalPrice += childPrice;
                final String childCategory = cursor.getString(cursor.getColumnIndexOrThrow(DbSQLiteHelper.TRANSACTION_CATEGORY));
                final int childSwipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;

                children.add(new ConcreteChildData(childId, childTransactionId, childName, formattedChildPrice, childDate, childCategory, childSwipeReaction));

        } while (cursor.moveToNext());

        // when last group, add total price to group, commit pair for last group and reset children
        String formattedGroupTotalPrice;
        if (currPref.equals("2")) {
            formattedGroupTotalPrice = String.format("%.2f", groupTotalPrice) + " $";
        } else {
            formattedGroupTotalPrice = String.format("%.2f", groupTotalPrice) + " €";
        }
        group.setTotalPrice(formattedGroupTotalPrice);
        mData.add(new Pair<GroupData, List<ChildData>>(group, children));

    }


    public int getGroupCount() {
        return mData.size();
    }


    public int getChildCount(int groupPosition) {
        return mData.get(groupPosition).second.size();
    }


    public GroupData getGroupItem(int groupPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        return mData.get(groupPosition).first;
    }


    public ChildData getChildItem(int groupPosition, int childPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        final List<ChildData> children = mData.get(groupPosition).second;

        if (childPosition < 0 || childPosition >= children.size()) {
            throw new IndexOutOfBoundsException("childPosition = " + childPosition);
        }

        return children.get(childPosition);
    }


    public void moveGroupItem(int fromGroupPosition, int toGroupPosition) {
        if (fromGroupPosition == toGroupPosition) {
            return;
        }

        final Pair<GroupData, List<ChildData>> item = mData.remove(fromGroupPosition);
        mData.add(toGroupPosition, item);
    }


    public void moveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
        if ((fromGroupPosition == toGroupPosition) && (fromChildPosition == toChildPosition)) {
            return;
        }

        final Pair<GroupData, List<ChildData>> fromGroup = mData.get(fromGroupPosition);
        final Pair<GroupData, List<ChildData>> toGroup = mData.get(toGroupPosition);

        final ConcreteChildData item = (ConcreteChildData) fromGroup.second.remove(fromChildPosition);

        if (toGroupPosition != fromGroupPosition) {
            // assign a new ID
            final long newId = ((ConcreteGroupData) toGroup.first).generateNewChildId();
            item.setChildId(newId);
        }

        toGroup.second.add(toChildPosition, item);
    }


    public void removeGroupItem(int groupPosition) {
        mLastRemovedGroup = mData.remove(groupPosition);
        mLastRemovedGroupPosition = groupPosition;

        mLastRemovedChild = null;
        mLastRemovedChildParentGroupId = -1;
        mLastRemovedChildPosition = -1;
    }


    public void removeChildItem(int groupPosition, int childPosition) {
        mLastRemovedChild = mData.get(groupPosition).second.remove(childPosition);
        mLastRemovedChildParentGroupId = mData.get(groupPosition).first.getGroupId();
        mLastRemovedChildPosition = childPosition;

        mLastRemovedGroup = null;
        mLastRemovedGroupPosition = -1;
    }



    public long undoLastRemoval() {
        if (mLastRemovedGroup != null) {
            return undoGroupRemoval();
        } else if (mLastRemovedChild != null) {
            return undoChildRemoval();
        } else {
            return RecyclerViewExpandableItemManager.NO_EXPANDABLE_POSITION;
        }
    }

    private long undoGroupRemoval() {
        int insertedPosition;
        if (mLastRemovedGroupPosition >= 0 && mLastRemovedGroupPosition < mData.size()) {
            insertedPosition = mLastRemovedGroupPosition;
        } else {
            insertedPosition = mData.size();
        }

        mData.add(insertedPosition, mLastRemovedGroup);

        mLastRemovedGroup = null;
        mLastRemovedGroupPosition = -1;

        return RecyclerViewExpandableItemManager.getPackedPositionForGroup(insertedPosition);
    }

    private long undoChildRemoval() {
        Pair<GroupData, List<ChildData>> group = null;
        int groupPosition = -1;

        // find the group
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).first.getGroupId() == mLastRemovedChildParentGroupId) {
                group = mData.get(i);
                groupPosition = i;
                break;
            }
        }

        if (group == null) {
            return RecyclerViewExpandableItemManager.NO_EXPANDABLE_POSITION;
        }

        int insertedPosition;
        if (mLastRemovedChildPosition >= 0 && mLastRemovedChildPosition < group.second.size()) {
            insertedPosition = mLastRemovedChildPosition;
        } else {
            insertedPosition = group.second.size();
        }

        group.second.add(insertedPosition, mLastRemovedChild);

        mLastRemovedChildParentGroupId = -1;
        mLastRemovedChildPosition = -1;
        mLastRemovedChild = null;

        return RecyclerViewExpandableItemManager.getPackedPositionForChild(groupPosition, insertedPosition);
    }

    public static final class ConcreteGroupData extends GroupData {

        private final long mId;
        private final String mText;
        private String mtotalPrice;
        private final int mSwipeReaction;
        private boolean mPinnedToSwipeLeft;
        private long mNextChildId;

        ConcreteGroupData(long id, String text, int swipeReaction) {
            mId = id;
            mText = text;
            mSwipeReaction = swipeReaction;
            mNextChildId = 0;
        }

        @Override
        public long getGroupId() {
            return mId;
        }

        public String getTotalPrice() {
            return mtotalPrice;
        }


        public void setTotalPrice(String totalPrice) {
            mtotalPrice = totalPrice;
        }

        @Override
        public boolean isSectionHeader() {
            return false;
        }

        @Override
        public int getSwipeReactionType() {
            return mSwipeReaction;
        }

        @Override
        public String getText() {
            return mText;
        }

        @Override
        public void setPinnedToSwipeLeft(boolean pinnedToSwipeLeft) {
            mPinnedToSwipeLeft = pinnedToSwipeLeft;
        }

        @Override
        public boolean isPinnedToSwipeLeft() {
            return mPinnedToSwipeLeft;
        }

        public long generateNewChildId() {
            final long id = mNextChildId;
            mNextChildId += 1;
            return id;
        }
    }

    public static final class ConcreteChildData extends ChildData {

        private long mId;
        private long mTransactionId;
        private final String mName;
        private final String mPrice;
        private final String mDate;
        private final String mCategory;
        private final int mSwipeReaction;
        private boolean mPinnedToSwipeLeft;

        ConcreteChildData(long id, long transactionId, String name, String price, String date, String category, int swipeReaction) {
            mId = id;
            mTransactionId = transactionId;
            mName = name;
            mPrice = price;
            mDate = date;
            mCategory = category;
            mSwipeReaction = swipeReaction;
        }

        @Override
        public long getChildId() {
            return mId;
        }

        @Override
        public long getTransactionId() {
            return mTransactionId;
        }

        @Override
        public int getSwipeReactionType() {
            return mSwipeReaction;
        }

        @Override
        public String getChildName() {
            return mName;
        }

        @Override
        public String getChildPrice() {
            return mPrice;
        }

        @Override
        public String getChildDate() {
            return mDate;
        }

        @Override
        public String getChildCategory() {
            return mCategory;
        }

        @Override
        public void setPinnedToSwipeLeft(boolean pinnedToSwipeLeft) {
            mPinnedToSwipeLeft = pinnedToSwipeLeft;
        }

        @Override
        public boolean isPinnedToSwipeLeft() {
            return mPinnedToSwipeLeft;
        }

        public void setChildId(long id) {
            this.mId = id;
        }
    }

    public static abstract class BaseData {

        public abstract int getSwipeReactionType();

        public abstract void setPinnedToSwipeLeft(boolean pinned);

        public abstract boolean isPinnedToSwipeLeft();
    }

    public static abstract class GroupData extends BaseData {
        public abstract boolean isSectionHeader();
        public abstract String getText();
        public abstract String getTotalPrice();
        public abstract void setTotalPrice(String totalPrice);
        public abstract long getGroupId();
    }

    public static abstract class ChildData extends BaseData {
        public abstract long getChildId();
        public abstract long getTransactionId();
        public abstract String getChildName();
        public abstract String getChildPrice();
        public abstract String getChildDate();
        public abstract String getChildCategory();
    }
}
