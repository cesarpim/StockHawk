package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String SYMBOL_KEY = "symbol";

    @BindView(R.id.chart_history)
    LineChart historyLineChart;
    private String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        symbol = getIntent().getStringExtra(SYMBOL_KEY);
        setTitle(symbol + " " + getString(R.string.detail_activity_title));
        setupHistoryChart();
    }

    private void setupHistoryChart() {
        // Getting the history string from the content provider
        String history = getHistoryString();
        if ((history != null) && !history.equals("")) {

            // Reading from the history string to create a list of dates and a list of stock values
            final List<String> formattedDates = new ArrayList<>();
            List<Float> stockValues = new ArrayList<>();
            try {
                CSVReader reader = new CSVReader(new StringReader(history));
                String[] line;
                while ((line = reader.readNext()) != null) {
                    if (line.length == 2) {
                        Date date = new Date(Long.valueOf(line[0]));
                        formattedDates.add(
                                new SimpleDateFormat("MMM-yy", Locale.ENGLISH).format(date));
                        stockValues.add(Float.valueOf(line[1]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Setting the chart entries in chronological order
            final int numEntries = formattedDates.size();
            List<Entry> entries = new ArrayList<>();
            for(int index = 0; index < numEntries; index++) {
                // The X values are indexes we create, in ascending order.
                // The Y values are the stock values in reverse order of how they were read, so that
                // they are now in ascending chronological order.
                entries.add(new Entry(index, stockValues.get((numEntries - 1) - index)));
            }
            historyLineChart.setData(new LineData(new LineDataSet(entries, null)));

            // Formatting the X Axis to show the dates instead of the indexes
            historyLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    // Since entries were added in reverse order of the lists of dates and stock
                    // values, then value can be used to index, in reverse order, the list of dates
                    return formattedDates.get((numEntries - 1) - (int) value);
                }
            });

            // Styling and refreshing the chart
            styleChart();
            historyLineChart.invalidate();
        }
    }

    private String getHistoryString() {
        Cursor data = getContentResolver().query(
                Contract.Quote.makeUriForStock(symbol),
                null,
                null,
                null,
                null);
        String history = "";
        if (data.moveToFirst()) {
            history = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        }
        data.close();
        return history;
    }

    private void styleChart() {
        historyLineChart.setDescription(null);
        historyLineChart.getLegend().setEnabled(false);
        styleChartAxis(historyLineChart.getXAxis());
        styleChartAxis(historyLineChart.getAxisLeft());
        styleChartAxis(historyLineChart.getAxisRight());
        historyLineChart.setExtraOffsets(12, 12, 12, 12);
        historyLineChart.setContentDescription(
                getString(R.string.chart_content_description) + symbol);
    }

    private void styleChartAxis(AxisBase axis) {
        axis.setTextColor(Color.WHITE);
        axis.setTextSize(getResources().getDimension(R.dimen.chart_axis_text_size));
    }

}
