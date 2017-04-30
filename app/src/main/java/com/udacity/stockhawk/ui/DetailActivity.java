package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.common.collect.Lists;
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
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    public static final String SYMBOL_KEY = "symbol";

    @BindView(R.id.text_symbol)
    TextView symbolTextView;
    @BindView(R.id.chart_history)
    LineChart historyLineChart;
    private String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        symbol = getIntent().getStringExtra(SYMBOL_KEY);

        symbolTextView.setText(symbol);
        setupHistoryGraph();
    }

    private void setupHistoryGraph() {
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
                                new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(date));
                        stockValues.add(Float.valueOf(line[1]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Setting the graph entries in chronological order
            final int numEntries = formattedDates.size();
            List<Entry> entries = new ArrayList<>();
            for(int index = 0; index < numEntries; index++) {
                // The X values are indexes we create, in ascending order.
                // The Y values are the stock values in reverse order of how they were read, so that
                // they are now in ascending chronological order.
                entries.add(new Entry(index, stockValues.get((numEntries - 1) - index)));
            }
            historyLineChart.setData(new LineData(new LineDataSet(entries, "History")));

            // Formatting the X Axis to show the dates instead of the indexes
            historyLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    // Since entries were added in reverse order of the lists of dates and stock
                    // values, then value can be used to index, in reverse order, the list of dates
                    return formattedDates.get((numEntries - 1) - (int) value);
                }
            });

            // Refreshing the graph
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

}
