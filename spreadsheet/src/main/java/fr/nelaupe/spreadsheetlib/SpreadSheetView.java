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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.nelaupe.spreadsheetlib.view.ArrowButton;
import fr.nelaupe.spreadsheetlib.view.DispatcherHorizontalScrollView;

/**
 * Created with IntelliJ
 * Created by Lucas Nelaupe
 * Date 26/03/15
 */
@SuppressWarnings({"unused", "unchecked"})
public class SpreadSheetView extends LinearLayout implements View.OnClickListener {

    private int mColumnSortSelected;
    private boolean mIsDESC;

    private TableLayout mHeader;
    private TableLayout mTable;
    private TableLayout mFixed;
    private TableLayout mFixedHeader;

    private boolean mAutoSorting;

    private SpreadSheetAdaptor<SpreadSheetData> mAdaptor;

    public SpreadSheetView(Context context) {
        super(context);
        mAdaptor = new SimpleTextAdaptor(getContext());
        mAutoSorting = true;
        init();
    }

    public SpreadSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdaptor = new SimpleTextAdaptor(getContext());
        mAutoSorting = true;
        parseAttribute(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SpreadSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdaptor = new SimpleTextAdaptor(getContext());
        mAutoSorting = true;
        parseAttribute(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpreadSheetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mAdaptor = new SimpleTextAdaptor(getContext());
        mAutoSorting = true;
        parseAttribute(context, attrs);
        init();
    }

    private void parseAttribute(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.sheet);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.sheet_headerColor) {
                mAdaptor.getConfiguration().setHeaderBackgroundColor(a.getColor(attr, 0));
            } else if (attr == R.styleable.sheet_headerTextSize) {
                mAdaptor.getConfiguration().setHeaderTextSize(a.getDimensionPixelSize(attr, 0));
            } else if (attr == R.styleable.sheet_textSize) {
                mAdaptor.getConfiguration().setTextSize(a.getDimensionPixelSize(attr, 0));
            } else if (attr == R.styleable.sheet_textColor) {
                mAdaptor.getConfiguration().setTextColor(a.getColor(attr, 0));
            } else if (attr == R.styleable.sheet_headerTextColor) {
                mAdaptor.getConfiguration().setHeaderTextColor(a.getColor(attr, 0));
            } else if (attr == R.styleable.sheet_rowHeight) {
                mAdaptor.getConfiguration().setRowHeight(a.getDimensionPixelSize(attr, 0));
            } else if (attr == R.styleable.sheet_headerRowHeight) {
                mAdaptor.getConfiguration().setHeaderRowHeight(a.getDimensionPixelSize(attr, 0));
            } else if (attr == R.styleable.sheet_minFixedRowWidth) {
                mAdaptor.getConfiguration().setMinFixedRowWidth(a.getDimensionPixelSize(attr, 0));
            } else if (attr == R.styleable.sheet_textPaddingRight) {
                mAdaptor.getConfiguration().setTextPaddingRight(a.getDimensionPixelSize(attr, 0));
            } else if (attr == R.styleable.sheet_textPaddingLeft) {
                mAdaptor.getConfiguration().setTextPaddingLeft(a.getDimensionPixelSize(attr, 0));
            }
        }
        a.recycle();
    }

    private void init() {
        mIsDESC = false;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    }

    @Deprecated
    public List<? extends SpreadSheetData> getData() {
        return mAdaptor.getData();
    }

    @Deprecated
    public void add(SpreadSheetData data) {
        mAdaptor.add(data);
    }

    @Deprecated
    public void addAll(List<SpreadSheetData> data) {
        mAdaptor.addAll(data);
    }

    @Deprecated
    public void clearData() {
        mAdaptor.clearData();
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();

        if (i == R.id.filter) {

            int columnPosition = (Integer) v.getTag(R.id.filter_column_position);
            AnnotationFields annotationFields = mAdaptor.getFields().get(columnPosition);

            if (mAutoSorting) {
                try {
                    if (annotationFields.getField().get(mAdaptor.getData().get(0)) instanceof Comparable) {
                        doSorting(columnPosition, mAdaptor.sortBy(annotationFields.getField()), annotationFields);
                    }
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }

            } else {
                mIsDESC = !(mColumnSortSelected != columnPosition || mIsDESC);
                putArrow(columnPosition);
                mAdaptor.onSort(annotationFields, mIsDESC);
            }

        } else if (i == R.id.item) {
            Integer position = (Integer) v.getTag(R.id.item_number);
            if (mAdaptor.getItemClickListener() != null) {
                mAdaptor.getItemClickListener().onItemClick(mAdaptor.get(position));
            }
        }
    }

    /*
     *  View
     */
    private void addFixedHeader() {
        if (mAdaptor.getFixedViews().size() == 0) return;

        TableRow row = new TableRow(getContext());
        row.setLayoutParams(mAdaptor.getConfiguration().getTableLayoutParams());
        row.setGravity(mAdaptor.getConfiguration().getTextGravity());
        row.setBackgroundColor(mAdaptor.getConfiguration().getHeaderColor());
        for (String name : mAdaptor.getFixedViews()) {
            View view = mAdaptor.getFixedHeaderView(name);
            view.setMinimumWidth(mAdaptor.getConfiguration().getMinFixedRowWidth());
            view.setMinimumHeight(mAdaptor.getConfiguration().getHeaderRowHeight());
            view.setPadding(mAdaptor.getConfiguration().getTextPaddingLeft(), 0, mAdaptor.getConfiguration().getTextPaddingRight(), 0);
            row.addView(view);
        }

        mFixedHeader.addView(row);
    }

    private void addHeader() {
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(mAdaptor.getConfiguration().getTableLayoutParams());
        row.setGravity(mAdaptor.getConfiguration().getTextGravity());
        row.setBackgroundColor(mAdaptor.getConfiguration().getHeaderColor());

        int column = 0;

        for (AnnotationFields field : mAdaptor.getFields()) {
            CellInformation spreadSheetCell = field.getAnnotation();
            ArrowButton button = mAdaptor.getHeaderCellView(spreadSheetCell);
            button.setPadding(mAdaptor.getConfiguration().getTextPaddingLeft(), 0, mAdaptor.getConfiguration().getTextPaddingRight(), 0);
            button.setOnClickListener(this);
            button.setId(R.id.filter);
            button.setMinimumWidth(mAdaptor.getConfiguration().computeSize(spreadSheetCell.getSize()));
            button.setMinimumHeight(mAdaptor.getConfiguration().getHeaderRowHeight());
            button.setPadding(mAdaptor.getConfiguration().getTextPaddingLeft(), 0, mAdaptor.getConfiguration().getTextPaddingRight(), 0);
            button.setTag(R.id.filter_column_position, column);
            column++;

            row.addView(button);
        }

        mHeader.addView(row);
    }

    private void AddFixedRow(boolean colorBool, int position) {
        if (mAdaptor.getFixedViews().size() == 0) return;

        TableRow row = new TableRow(getContext());
        row.setLayoutParams(mAdaptor.getConfiguration().getTableLayoutParams());
        row.setGravity(mAdaptor.getConfiguration().getTextGravity());
        row.setBackgroundColor(getResources().getColor(colorBool ? R.color.white : R.color.grey_cell));

        for (String name : mAdaptor.getFixedViews()) {
            View view = mAdaptor.getFixedCellView(name, position);
            view.setMinimumWidth(mAdaptor.getConfiguration().getMinFixedRowWidth());
            view.setMinimumHeight(mAdaptor.getConfiguration().getRowHeight());
            view.setPadding(mAdaptor.getConfiguration().getTextPaddingLeft(), 0, mAdaptor.getConfiguration().getTextPaddingRight(), 0);
            row.addView(view);
        }

        mFixed.addView(row);
    }

    private void addRow() {
        Boolean colorBool = true;
        int position = 0;

        for (SpreadSheetData resource : mAdaptor.getData()) {

            AddFixedRow(colorBool, position);

            TableRow row = new TableRow(getContext());
            row.setLayoutParams(mAdaptor.getConfiguration().getTableLayoutParams());
            row.setGravity(mAdaptor.getConfiguration().getTextGravity());
            row.setBackgroundColor(getResources().getColor(colorBool ? R.color.white : R.color.grey_cell));
            row.setId(R.id.item);
            row.setTag(R.id.item_number, position);
            row.setOnClickListener(this);

            for (AnnotationFields field : mAdaptor.getFields()) {
                CellInformation spreadSheetCell = field.getAnnotation();
                try {
                    Object object = field.getField().get(resource);
                    View view = mAdaptor.getCellView(spreadSheetCell, object);
                    view.setMinimumWidth(mAdaptor.getConfiguration().computeSize(spreadSheetCell.getSize()));
                    view.setMinimumHeight(mAdaptor.getConfiguration().getRowHeight());
                    view.setPadding(mAdaptor.getConfiguration().getTextPaddingLeft(), 0, mAdaptor.getConfiguration().getTextPaddingRight(), 0);
                    row.addView(view);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }


            colorBool = !colorBool;
            mTable.addView(row);
            position++;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (mAdaptor.getFields().isEmpty()) return;

        mFixedHeader.removeAllViews();
        mHeader.removeAllViews();
        mFixed.removeAllViews();
        mTable.removeAllViews();

        addFixedHeader();

        addHeader();
        addRow();

        putArrow(mColumnSortSelected);
    }

    private void invalidateContent() {
        mTable.removeAllViews();
        mFixed.removeAllViews();

        addRow();
    }

    /*
     *  Sorting
     */
    private void invert(int columnId) {
        Collections.reverse(mAdaptor.getData());
    }

    private void sort(int columnId, Comparator comparator) {
        Collections.sort(mAdaptor.getData(), comparator);
    }

    private void doSorting(int columnId, Comparator<? extends SpreadSheetData> comparator, AnnotationFields annotationFields) {
        if (mColumnSortSelected == columnId) {
            invert(columnId);
            mIsDESC = !mIsDESC;
        } else {
            sort(columnId, comparator);
            mIsDESC = false;
        }
        putArrow(columnId);
        mAdaptor.onSort(annotationFields, mIsDESC);
        invalidateContent();
    }

    private void putArrow(int column) {
        TableRow row = (TableRow) (mHeader).getChildAt(0);
        for (int i = 0; i < row.getChildCount(); ++i) {
            ArrowButton childAt = (ArrowButton) row.getChildAt(i);
            if (column == (int) childAt.getTag(R.id.filter_column_position)) {
                mColumnSortSelected = column;
                if (mIsDESC) {
                    childAt.setState(ArrowButton.states.DOWN);
                } else {
                    childAt.setState(ArrowButton.states.UP);
                }
            } else {
                childAt.setState(ArrowButton.states.NONE);
            }
        }
    }

    public void setArrow(int column, boolean isDESC) {
        mColumnSortSelected = column;
        mIsDESC = isDESC;
    }

    public SpreadSheetAdaptor<SpreadSheetData> getAdaptor() {
        return mAdaptor;
    }

    public void setAdaptor(SpreadSheetAdaptor adaptor) {
        if (mAdaptor != null && adaptor.getData().size() == 0) {
            adaptor.addAll(mAdaptor.getData());
            adaptor.setConfiguration(mAdaptor.getConfiguration());
        }

        mAdaptor = adaptor;
    }

    public void setAutoSorting(boolean isAutoSort) {
        mAutoSorting = isAutoSort;
    }

}
