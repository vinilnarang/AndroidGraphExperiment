package com.vinil.the_game.tworoadsassignment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        buyStockButton = (Button) findViewById(R.id.buyStockButton);
        buyStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Buy Something!",Toast.LENGTH_SHORT).show();
            }
        });
        sellStockButton = (Button) findViewById(R.id.sellStockButton);
        sellStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Sell Something!",Toast.LENGTH_SHORT).show();
            }
        });
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //The toggle is enabled
                    //Snackbar.make(buttonView, " Started data fetching!", Snackbar.LENGTH_SHORT)
                    //        .setAction("Action", null).show();
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
                String[] elements = data.split(",");
                String buyBestPrice = elements[2].trim();
                String sellBestPrice = elements[4].trim();
                String buyItems = elements[3].trim();
                String sellItems = elements[5].split("\n")[0].trim();
                DataPoint sellItemDataPoint = new DataPoint(count,Integer.parseInt(sellItems));
                DataPoint buyItemDataPoint = new DataPoint(count,Integer.parseInt(buyItems));
                DataPoint sellBestPriceDataPoint = new DataPoint(count,Double.parseDouble(sellBestPrice));
                DataPoint buyBestPriceDataPoint = new DataPoint(count,Double.parseDouble(buyBestPrice));
                buyItemsGraph.setTitle("#Buy Items : "+buyItems);
                sellItemsGraph.setTitle("#Sell Items : "+sellItems);
                buyBestPriceGraph.setTitle("#Buy Best Price : "+buyBestPrice);
                sellBestPriceGraph.setTitle("#Sell Best Price : "+sellBestPrice);
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

    /*public void function2() throws IOException {

        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {

                try{
                    String url = "http://192.168.0.105:48129/";
                    //String url = "http://api.androidhive.info/contacts/";
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse response = httpclient.execute(new HttpGet(url));

                    if(response.getEntity().isStreaming()){
                        HttpEntity _entity = response.getEntity();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(_entity.getContent(),"utf-8"),8);
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            System.out.println( line.toString());
                        }
//                        _entity.getContent().close();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                tv.setText("Fetched!");
            }
        };
        asyncTask.execute();
    }

    public void function(){

        String url = "http://192.168.0.105:48129/";

        // Request a string response
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        // Result handling
                        System.out.println(response.substring(0,100));
                        tv.setText(response.substring(0,100));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Error handling
                System.out.println("Something went wrong!");
                error.printStackTrace();

            }
        });

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);
    }*/



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(asyncTask!=null){
            asyncTask.cancel(true);
        }
        super.onDestroy();
    }
}
