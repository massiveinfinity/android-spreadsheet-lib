/**
 * Copyright
 */
package fr.nelaupe.spreadsheetlib;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import fr.nelaupe.spreadsheetlib.view.ArrowButton;

/**
 * Created with IntelliJ
 * Created by lucas
 * Date 26/03/15
 */
public class SimpleTextAdaptor extends SpreadSheetAdaptor<SpreadSheetData> {

    public SimpleTextAdaptor(Context context) {
        super(context);
    }

    @Override
    public View getCellView(SpreadSheetCell cell, Object object) {
        TextView recyclableTextView = new TextView(getContext());
        recyclableTextView.setText((object == null ? "" : object.toString()));
        recyclableTextView.setTextColor(getConfiguration().getTextColor());
        recyclableTextView.setGravity(getConfiguration().getTextGravity());
        recyclableTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getConfiguration().getTextSize());
        recyclableTextView.setWidth(getConfiguration().computeSize(cell.size()));
        recyclableTextView.setHeight(getConfiguration().getRowHeight());
        return recyclableTextView;
    }

    @Override
    public View getHeaderCellView(SpreadSheetCell cell) {
        ArrowButton button = new ArrowButton(getContext());
        button.setWidth(getConfiguration().computeSize(cell.size()));
        button.setHeight(getConfiguration().getHeaderRowHeight());
        button.setTextColor(getConfiguration().getHeaderTextColor());
        button.setBackgroundResource(0);
        button.setText(cell.name());
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getConfiguration().getHeaderTextSize());
        button.setTextGravity(getConfiguration().getTextGravity());
        if (((getConfiguration().getTextGravity() & Gravity.LEFT) == Gravity.LEFT) || ((getConfiguration().getTextGravity() & Gravity.START) == Gravity.START)) {
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icr_arrow_selector_sort, 0);
        } else if (((getConfiguration().getTextGravity() & Gravity.RIGHT) == Gravity.RIGHT) || ((getConfiguration().getTextGravity() & Gravity.END) == Gravity.END)) {
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icr_arrow_selector_sort, 0, 0, 0);
        } else {
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_empty, 0, R.drawable.icr_arrow_selector_sort, 0);
        }

        return button;
    }

}