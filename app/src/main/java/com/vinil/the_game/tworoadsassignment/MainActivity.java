package com.vinil.the_game.tworoadsassignment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {

    public TextView tv;
    public AsyncTask asyncTask;
    public HttpURLConnection httpURLConnection;
    public InputStream inputStream;
    public BufferedReader bufferedReader;
    public String urlString = "http://192.168.0.105:48129/";
    public ToggleButton toggleButton;
    public Button buyStockButton;
    public Button sellStockButton;
    public GraphView buyItemsGraph,sellItemsGraph,buyBestPriceGraph,sellBestPriceGraph;
    public LineGraphSeries<DataPoint> sellItemsSeries;
    public LineGraphSeries<DataPoint> buyItemsSeries;
    public LineGraphSeries<DataPoint> sellBestPriceSeries;
    public LineGraphSeries<DataPoint> buyBestPriceSeries;
    public int count = -1 ;
    public static final int SCROLL_LIMIT = 11;
    public SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        db = openOrCreateDatabase("StockDB", Context.MODE_PRIVATE, null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        urlString = intent.getStringExtra("url");
        tv = (TextView)findViewById(R.id.helloTextView);
        buyItemsGraph = (GraphView) findViewById(R.id.buyItemsGraph);
        buyBestPriceGraph = (GraphView) findViewById(R.id.buyBestPriceGraph);
        sellItemsGraph = (GraphView) findViewById(R.id.sellItemsGraph);
        sellBestPriceGraph = (GraphView) findViewById(R.id.sellBestPriceGraph);
        sellItemsSeries = new LineGraphSeries<DataPoint>();
        buyItemsSeries = new LineGraphSeries<DataPoint>();
        sellBestPriceSeries = new LineGraphSeries<DataPoint>();
        buyBestPriceSeries = new LineGraphSeries<DataPoint>();
        setPropertiesForGraph(buyItemsGraph, 0, 60, 0, SCROLL_LIMIT - 1, "#Buy Items", 6);
        setPropertiesForGraph(sellItemsGraph,0,60,0,SCROLL_LIMIT-1,"#Sell Items",6);
        setPropertiesForGraph(buyBestPriceGraph,44,46,0,SCROLL_LIMIT-1,"#Buy Best Price",6);
        setPropertiesForGraph(sellBestPriceGraph,44,46,0,SCROLL_LIMIT-1,"#Sell Best Price",6);


        sellItemsGraph.addSeries(sellItemsSeries);
        buyItemsGraph.addSeries(buyItemsSeries);
        buyBestPriceGraph.addSeries(buyBestPriceSeries);
        sellBestPriceGraph.addSeries(sellBestPriceSeries);
        sellItemsSeries.appendData(new DataPoint(-1, 0), false, SCROLL_LIMIT);
        buyItemsSeries.appendData(new DataPoint(-1, 0), false, SCROLL_LIMIT);
        buyBestPriceSeries.appendData(new DataPoint(-1,0),false,SCROLL_LIMIT);
        sellBestPriceSeries.appendData(new DataPoint(-1,0),false,SCROLL_LIMIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buyStockButton = (Button) findViewById(R.id.buyStockButton);
        buyStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForOrderType("buy");
            }
        });
        sellStockButton = (Button) findViewById(R.id.sellStockButton);
        sellStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForOrderType("sell");
            }
        });
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //The toggle is enabled
                    startFetchingData();
                } else {
                    //The toggle is disabled
                    if(asyncTask!=null){
                        asyncTask.cancel(true);
                    }
                }
            }
        });
    }

    public void setPropertiesForGraph(GraphView graph, double minY, double maxY, double minX, double maxX, String title, int numGrids){
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(minY);
        graph.getViewport().setMaxY(maxY);
        graph.getViewport().setMinX(minX);
        graph.getViewport().setMaxX(maxX);
        graph.setTitle(title);
        graph.getGridLabelRenderer().setNumHorizontalLabels(numGrids);
    }

    public void startFetchingData(){
        asyncTask = new AsyncTask() {

            @Override
            protected void onProgressUpdate(Object[] values) {
                if(values[0]=="ConnectionClosed"){
                    Toast.makeText(MainActivity.this,"Connection closed!",Toast.LENGTH_SHORT).show();
                    return;
                }
                super.onProgressUpdate(values);
                count++;
                String data = (String)values[0];
                plotGraphAndStoreData(data);
                return;
            }

            @Override
            protected Object doInBackground(Object[] params) {
                URL url1;
                try {
                    url1 = new URL(urlString);
                    httpURLConnection = (HttpURLConnection)url1.openConnection();
                    inputStream = httpURLConnection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while((line = bufferedReader.readLine()) != null){
                        if(this.isCancelled()) break;
                        publishProgress(line);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    closeBufferedReader(bufferedReader);
                    closeInputStream(inputStream);
                    closeHttpURLConnection(httpURLConnection);
                    publishProgress("ConnectionClosed");
                }

                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                closeBufferedReader(bufferedReader);
                closeInputStream(inputStream);
                closeHttpURLConnection(httpURLConnection);
                toggleButton.setChecked(false);
                return;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                toggleButton.setChecked(false);
            }
        };
        asyncTask.execute();
    }

    public void plotGraphAndStoreData(String data){

        db.execSQL("CREATE TABLE IF NOT EXISTS stock(_ID INTEGER PRIMARY KEY AUTOINCREMENT,time VARCHAR, buyBestPrice VARCHAR," +
                " buyItems VARCHAR, sellBestPrice VARCHAR, sellItems VARCHAR);");
        String[] elements = data.split(",");
        String time = elements[0].trim();
        String buyBestPrice = elements[2].trim();
        String sellBestPrice = elements[4].trim();
        String buyItems = elements[3].trim();
        String sellItems = elements[5].split("\n")[0].trim();
        db.execSQL("INSERT INTO stock VALUES('"+time+"','"+buyBestPrice+"','"+buyItems+"','"+sellBestPrice+"','"+sellItems+"');");
        DataPoint sellItemDataPoint = new DataPoint(count,Integer.parseInt(sellItems));
        DataPoint buyItemDataPoint = new DataPoint(count,Integer.parseInt(buyItems));
        DataPoint sellBestPriceDataPoint = new DataPoint(count,Double.parseDouble(sellBestPrice));
        DataPoint buyBestPriceDataPoint = new DataPoint(count,Double.parseDouble(buyBestPrice));
        buyItemsGraph.setTitle("#Buy Items : "+buyItems);
        sellItemsGraph.setTitle("#Sell Items : "+sellItems);
        buyBestPriceGraph.setTitle("#Buy Best Price : " + buyBestPrice);
        sellBestPriceGraph.setTitle("#Sell Best Price : " + sellBestPrice);
        if(count>=SCROLL_LIMIT) {
            sellItemsSeries.appendData(sellItemDataPoint, true, SCROLL_LIMIT);
            buyItemsSeries.appendData(buyItemDataPoint, true, SCROLL_LIMIT);
            sellBestPriceSeries.appendData(sellBestPriceDataPoint, true, SCROLL_LIMIT);
            buyBestPriceSeries.appendData(buyBestPriceDataPoint, true, SCROLL_LIMIT);
        }
        else {
            sellItemsSeries.appendData(sellItemDataPoint, false, SCROLL_LIMIT);
            buyItemsSeries.appendData(buyItemDataPoint, false, SCROLL_LIMIT);
            sellBestPriceSeries.appendData(sellBestPriceDataPoint, false, SCROLL_LIMIT);
            buyBestPriceSeries.appendData(buyBestPriceDataPoint, false, SCROLL_LIMIT);
        }
        Cursor c=db.rawQuery("SELECT * FROM stock", null);
        Toast.makeText(MainActivity.this,""+c.getCount(),Toast.LENGTH_SHORT).show();
        return;

    }

    public void showAlertForOrderType(final String category){
        CharSequence[] orderTypes = {"Market Order","Limit Order"};
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Choose order type : ");
        alertDialogBuilder.setSingleChoiceItems(orderTypes, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showAlertForOrderInput(0, category);
                        break;
                    case 1:
                        showAlertForOrderInput(1, category);
                        break;
                }
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void showAlertForOrderInput(final int orderType, final String category){
        if(orderType == 0) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Enter number of orders : ");
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_view_market, null);
            final EditText editTextOrderMarket = (EditText) view.findViewById(R.id.editTextOrderMarket);
            alertDialogBuilder.setView(view);
            alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmMarketOrder(Integer.parseInt(editTextOrderMarket.getText().toString().trim()), category);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else{
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Enter number of orders : ");
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_view_limit, null);
            final EditText editTextOrderLimit = (EditText) view.findViewById(R.id.editTextOrderLimit);
            final EditText editTextPriceLimit = (EditText) view.findViewById(R.id.editTextPriceLimit);
            alertDialogBuilder.setView(view);
            alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String price = editTextPriceLimit.getText().toString();
                    String numOrders = editTextOrderLimit.getText().toString();
                    confirmLimitOrder(price,numOrders,category);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void confirmMarketOrder(int numOrders, String category){
        if(category=="buy") {
            Cursor c = db.rawQuery("SELECT * FROM stock ORDER BY buyBestPrice ASC", null);
            c.moveToFirst();
            ContentValues contentValues = new ContentValues();
            Toast.makeText(MainActivity.this, "Avl. Items : "+c.getString(2)+" Price : "+c.getString(1), Toast.LENGTH_SHORT).show();
            if(Integer.parseInt(c.getString(2)) <= numOrders){
                contentValues.put("buyItems","0");
            }else{
                contentValues.put("buyItems",""+(Integer.parseInt(c.getString(2))-numOrders));
            }
            db.update("stock",contentValues,"time='"+c.getString(0)+"'",null);
            Toast.makeText(MainActivity.this, "Avl. Items : "+c.getString(2)+" Price : "+c.getString(1), Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void confirmLimitOrder(String price, String numOrders, String category){
        Toast.makeText(MainActivity.this, category+" Limit Order : "+" Price : "+price+" Number : "+numOrders, Toast.LENGTH_SHORT).show();
    }

    public void closeBufferedReader(BufferedReader bufferedReader){
        if(bufferedReader!=null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Buffered Reader Closed!");
        return;
    }

    public void closeInputStream(InputStream inputStream){
        if(inputStream!=null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Input Stream Closed!");
        return;
    }

    public void closeHttpURLConnection(HttpURLConnection httpURLConnection){
        if(httpURLConnection!=null) {
            httpURLConnection.disconnect();
        }
        System.out.println("HttpURLConnection Closed!");
        return;
    }

    public void closeDatabaseConnection(){
        if(db.isOpen()) {
            db.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_orders) {
            Intent intent = new Intent(MainActivity.this,OrdersActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        closeDatabaseConnection();
        if(asyncTask!=null){
            asyncTask.cancel(true);
        }
        super.onDestroy();
    }
}
