package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by CesarPim on 28-04-2017.
 */

public class StocksWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            private DecimalFormat dollarFormat;
            private DecimalFormat dollarFormatWithPlus;
            private DecimalFormat percentageFormat;



            @Override
            public void onCreate() {

//                final long identityToken = Binder.clearCallingIdentity();

                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix(getString(R.string.dollar_positive_prefix));
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix(getString(R.string.percentage_positive_resource));

//                onDataSetChanged();
//                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // Because this method is called from outside our app (typically by the launcher)
                // and our content provider isn't exported, we must clear the calling identity
                // before using the provider and restore it after using
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION
                        || data == null
                        || !data.moveToPosition(position)) {
                    return null;
                }
                // data cursor is in the intended position
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                // Processing the symbol
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                setupRemoteTextView(
                        views,
                        R.id.text_widget_symbol,
                        symbol,
                        getString(R.string.symbol_content_description) + symbol);

                // Processing the price
                String price =  dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE));
                setupRemoteTextView(
                        views,
                        R.id.text_widget_price,
                        price,
                        getString(R.string.price_content_description) + price);

                // Processing the change
                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
                views.setInt(
                        R.id.text_widget_change,
                        "setBackgroundResource",
                        rawAbsoluteChange > 0 ?
                                R.drawable.percent_change_pill_green :
                                R.drawable.percent_change_pill_red);
                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                if (PrefUtils.getDisplayMode(getApplicationContext())
                        .equals(getString(R.string.pref_display_mode_percentage_key))) {
                    change = percentageFormat.format(percentageChange / 100);
                }
                setupRemoteTextView(
                        views,
                        R.id.text_widget_change,
                        change,
                        getString(R.string.change_content_description) + change);

                // TODO: set the on click fill intent
//                final Intent fillInIntent = new Intent();
//                String locationSetting =
//                        Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
//                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                        locationSetting,
//                        dateInMillis);
//                fillInIntent.setData(weatherUri);
//                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            private void setupRemoteTextView(
                    RemoteViews views,
                    int viewId,
                    String text,
                    String contentDescription) {
                views.setTextViewText(viewId, text);
                views.setContentDescription(viewId, contentDescription);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(Contract.Quote.POSITION_ID);
                } else {
                    // TODO: wtf?
                    return position;
                }
            }

            @Override
            public boolean hasStableIds() {
                // TODO: sera?
                return true;
            }
        };
    }
}
