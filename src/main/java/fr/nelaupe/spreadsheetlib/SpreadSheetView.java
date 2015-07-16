/*
 * Copyright 2015-present Lucas Nelaupe
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package fr.nelaupe.spreadsheetlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.nelaupe.spreadsheetlib.view.ArrowButton;
import fr.nelaupe.spreadsheetlib.view.DispatcherHorizontalScrollView;

/**
 * Created with IntelliJ
 * Created by Lucas Nelaupe
 * Date 26/03/15
 */
@SuppressWarnings({"unused", "unchecked"})
public class SpreadSheetView extends LinearLayout implements View.OnClickListener {

    private final float screenDensity;
    private final Context mContext;
    private final TableRow.LayoutParams wrapWrapTableRowParams;
    private List<SpreadSheetData> mData;
    private Map<String, Integer> mFixedViewData;
    private String mPreviousID;
    private boolean mInvert;
    private TableLayout mHeader;
    private TableLayout mTable;
    private TableLayout mFixed;
    private TableLayout mFixedHeader;
    private OnItemClickListener mItemClickListener;
    private OnSortingListener mSortingListener;

    private int mHeaderBackgroundColor;
    private int mHeaderTextColor;
    private int mTextColor;

    private float mRowHeight;
    private float mHeaderTextSize;
    private float mTextSize;
    private float mMinFixedRowWidth;

    public SpreadSheetView(Context context) {
        super(context);
        wrapWrapTableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mContext = context;
        screenDensity = getContext().getResources().getDisplayMetrics().density;
        init();
    }

    public SpreadSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        wrapWrapTableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mContext = context;
        screenDensity = getContext().getResources().getDisplayMetrics().density;
        parseAttribute(context, attrs);
        init();
    }

    public SpreadSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wrapWrapTableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mContext = context;
        screenDensity = getContext().getResources().getDisplayMetrics().density;
        parseAttribute(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpreadSheetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        wrapWrapTableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mContext = context;
        screenDensity = getContext().getResources().getDisplayMetrics().density;
        parseAttribute(context, attrs);
        init();
    }

    private void parseAttribute(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.sheet);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.sheet_headerColor) {
                mHeaderBackgroundColor = a.getColor(attr, 0);
            } else if (attr == R.styleable.sheet_headerTextSize) {
                mHeaderTextSize = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.sheet_textSize) {
                mTextSize = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.sheet_textColor) {
                mTextColor = a.getColor(attr, 0);
            } else if (attr == R.styleable.sheet_headerTextColor) {
                mHeaderTextColor = a.getColor(attr, 0);
            } else if (attr == R.styleable.sheet_rowHeight) {
                mRowHeight = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.sheet_minFixedRowWidth) {
                mMinFixedRowWidth = a.getDimensionPixelSize(attr, 0);
            }
        }
        a.recycle();
    }

    private void init() {
        mData = new ArrayList<>();
        mFixedViewData = new HashMap<>();
        mPreviousID = "";
        mInvert = false;
        wrapWrapTableRowParams.gravity = Gravity.CENTER;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inflatedView = inflater.inflate(R.layout.spread_sheet_layout, this, true);

        mHeader = (TableLayout) inflatedView.findViewById(R.id.table_header);
        mTable = (TableLayout) inflatedView.findViewById(R.id.scrollable_part);
        mFixed = (TableLayout) inflatedView.findViewById(R.id.fixed_column);
        mFixedHeader = (TableLayout) inflatedView.findViewById(R.id.fixed_column_header);

        DispatcherHorizontalScrollView scrollViewTab = (DispatcherHorizontalScrollView) inflatedView.findViewById(R.id.scrollViewHorizontal);
        DispatcherHorizontalScrollView scrollViewHeader = (DispatcherHorizontalScrollView) inflatedView.findViewById(R.id.scrollViewHorizontalHeader);
        scrollViewHeader.setHorizontalScrollBarEnabled(false);

        scrollViewTab.setTarget(scrollViewHeader);
        scrollViewHeader.setTarget(scrollViewTab);
        scrollViewTab.setHorizontalScrollBarEnabled(true);

        invalidate();
    }

    /*
     *  Data
     */
    public List<? extends SpreadSheetData> getData() {
        return mData;
    }

    public void add(SpreadSheetData data) {
        mData.add(data);
    }

    public void addAll(List<? extends SpreadSheetData> data) {
        mData.addAll(data);
    }

    public void addFixedView(String name, int layout) {
        mFixedViewData.put(name, layout);
    }

    /*
     *  Colors parameters
     */
    public int getHeaderColor() {
        return (mHeaderBackgroundColor == 0) ? getResources().getColor(R.color.header_color) : mHeaderBackgroundColor;
    }

    public void setHeaderColor(int color) {
        mHeaderBackgroundColor = color;
    }

    public float getTextSize() {
        return (mTextSize == 0) ? getResources().getDimension(R.dimen.text) : mTextSize;
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
    }

    public float getHeaderTextSize() {
        return (mHeaderTextSize == 0) ? getResources().getDimension(R.dimen.text) : mHeaderTextSize;
    }

    public void setHeaderTextSize(int headerTextSize) {
        this.mHeaderTextSize = headerTextSize;
    }

    public int getTextColor() {
        return (mTextColor == 0) ? getResources().getColor(R.color.text) : mTextColor;
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    public int getHeaderTextColor() {
        return (mHeaderTextColor == 0) ? getResources().getColor(R.color.white) : mHeaderTextColor;
    }

    public void setHeaderTextColor(int headerTextColor) {
        this.mHeaderTextColor = headerTextColor;
    }

    public int getRowHeight() {
        return (int) ((mRowHeight == 0) ? getResources().getDimension(R.dimen.rowHeight) : mRowHeight);
    }

    public void setRowHeight(float rowHeight) {
        mRowHeight = computeSize(rowHeight);
    }

    public int getMinFixedRowWidth() {
        return (int) ((mMinFixedRowWidth == 0) ? getResources().getDimension(R.dimen.minFixedRowWidth) : mMinFixedRowWidth);
    }

    public void setMinFixedRowWidth(float minFixedRowWidth) {
        mMinFixedRowWidth = computeSize(minFixedRowWidth);
    }

    private int computeSize(float size) {
        return (int) (size * screenDensity + 0.5f);
    }

    /*
     *  Click
     */
    @Override
    public void onClick(View v) {

        int i = v.getId();

        if (i == R.id.filter) {
            SpreadSheetData cls = mData.get(0);
            try {
                String filterName = String.valueOf(v.getTag(R.id.filter_name));
                if(!TextUtils.isEmpty(filterName)) {
                    Comparator<SpreadSheetData> comparator = (Comparator<SpreadSheetData>) cls.getClass().getDeclaredField(filterName).get(mData.get(0));
                    doSorting(v, comparator);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (i == R.id.item) {
            SpreadSheetData item = (SpreadSheetData) v.getTag(R.id.item_data);
            onItemClick(item);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void onItemClick(SpreadSheetData item) {
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(item);
        }
    }

    public void setOnSortingListener(OnSortingListener mSortingListener) {
        this.mSortingListener = mSortingListener;
    }

    public void onSort() {
        if (mSortingListener != null) {
            mSortingListener.onSort();
        }
    }

    /*
     *  View
     */
    private void addFixedHeader() {
        TableRow row = new TableRow(mContext);
        row.setLayoutParams(wrapWrapTableRowParams);
        row.setGravity(Gravity.CENTER);
        row.setBackgroundColor(getHeaderColor());
        for (String name : mFixedViewData.keySet()) {
            Button textView = new Button(mContext);
            textView.setText(name);
            textView.setTextColor(getHeaderTextColor());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getHeaderTextSize());
            textView.setGravity(Gravity.CENTER);
            textView.setWidth(getMinFixedRowWidth());
            textView.setHeight(getRowHeight());
            textView.setBackground(null);
            row.addView(textView);
        }

        mFixedHeader.addView(row);
    }

    private void addHeader() {
        SpreadSheetData cls = mData.get(0);
        TableRow row = new TableRow(mContext);
        row.setLayoutParams(wrapWrapTableRowParams);
        row.setGravity(Gravity.CENTER);
        row.setBackgroundColor(getHeaderColor());

        for (Field field : cls.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation instanceof SpreadSheetCell) {
                    SpreadSheetCell trueField = (SpreadSheetCell) annotation;
                    ArrowButton button = new ArrowButton(mContext);
                    button.setWidth(computeSize(trueField.size()));
                    button.setHeight(getRowHeight());
                    button.setTextColor(getHeaderTextColor());
                    button.setBackground(null);
                    button.setText(trueField.name());
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getHeaderTextSize());
                    button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icr_arrow_selector_sort, 0, R.drawable.arrow_empty, 0);
                    button.setOnClickListener(this);
                    button.setId(R.id.filter);
                    button.setTag(R.id.filter_name, trueField.filterName());
                    row.addView(button);
                }
            }
        }
        mHeader.addView(row);
    }

    private void AddFixedRow(boolean colorBool) {
        TableRow row = new TableRow(mContext);
        row.setLayoutParams(wrapWrapTableRowParams);
        row.setGravity(Gravity.CENTER);
        row.setBackgroundColor(mContext.getResources().getColor(colorBool ? R.color.white : R.color.grey_cell));
        for (Map.Entry<String, Integer> entry : mFixedViewData.entrySet()) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(entry.getValue(), null);
            view.setMinimumHeight(getRowHeight());
            view.setMinimumWidth(getMinFixedRowWidth());
            row.addView(view);
        }

        mFixed.addView(row);
    }

    private void addRow() {
        Boolean colorBool = true;
        int nb = 0;
        for (SpreadSheetData resource : mData) {

            AddFixedRow(colorBool);

            TableRow row = new TableRow(mContext);
            row.setLayoutParams(wrapWrapTableRowParams);
            row.setGravity(Gravity.CENTER);
            row.setBackgroundColor(mContext.getResources().getColor(colorBool ? R.color.white : R.color.grey_cell));
            row.setId(R.id.item);
            row.setTag(R.id.item_data, resource);
            row.setOnClickListener(this);

            for (Field field : resource.getClass().getDeclaredFields()) {
                try {
                    for (Annotation annotation : field.getDeclaredAnnotations()) {
                        if (annotation instanceof SpreadSheetCell) {
                            SpreadSheetCell spreadSheetCell = (SpreadSheetCell) annotation;
                            TextView recyclableTextView = new TextView(mContext);
                            Object object = field.get(resource);
                            recyclableTextView.setText((object == null ? "" : object.toString()));
                            recyclableTextView.setTextColor(getTextColor());
                            recyclableTextView.setGravity(Gravity.CENTER);
                            recyclableTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize());
                            recyclableTextView.setWidth(computeSize(spreadSheetCell.size()));
                            recyclableTextView.setHeight(getRowHeight());
                            row.addView(recyclableTextView);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            colorBool = !colorBool;
            mTable.addView(row);
            nb++;
        }
    }

    public void clearData() {
        mData = new ArrayList<>();
    }

    public void invalidate() {
        if (mData.isEmpty()) return;

        mFixedHeader.removeAllViews();
        mHeader.removeAllViews();
        mFixed.removeAllViews();
        mTable.removeAllViews();

        addFixedHeader();
        addHeader();
        addRow();
    }

    private void invalidateContent() {
        mTable.removeAllViews();
        mFixed.removeAllViews();

        addRow();
    }

    /*
     *  Sorting
     */
    private void invert(View v) {
        Collections.reverse(mData);
        if (!mInvert) {
            setArrowDown((ArrowButton) v);
        } else {
            setArrowUP((ArrowButton) v);
        }
        mInvert = !mInvert;
    }

    private void sort(View v, Comparator<SpreadSheetData> comparator) {
        setArrowUP((ArrowButton) v);
        mPreviousID = String.valueOf(v.getTag(R.id.filter_name));
        mInvert = false;
        Collections.sort(mData, comparator);
    }

    private void doSorting(View v, Comparator<SpreadSheetData> comparator) {
        if (mPreviousID.equals(v.getTag(R.id.filter_name))) {
            invert(v);
        } else {
            sort(v, comparator);
        }
        onSort();
        invalidateContent();
    }

    private void resetArrow() {
        TableRow row = (TableRow) (mHeader).getChildAt(0);
        for (int i = 0; i < row.getChildCount(); ++i) {
            ArrowButton childAt = (ArrowButton) row.getChildAt(i);
            childAt.setState(ArrowButton.states.NONE);
        }
    }

    private void setArrowDown(ArrowButton view) {
        resetArrow();
        view.setState(ArrowButton.states.DONW);
    }

    private void setArrowUP(ArrowButton view) {
        resetArrow();
        view.setState(ArrowButton.states.UP);
    }

}
