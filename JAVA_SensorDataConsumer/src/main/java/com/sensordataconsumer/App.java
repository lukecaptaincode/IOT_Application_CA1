package com.sensordataconsumer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static jdk.nashorn.internal.objects.NativeNumber.valueOf;

public class App {
    // Init all class variables
    private FirebaseDatabase database;
    private ArrayList<SensorData> lightData = new ArrayList<SensorData>();
    private ArrayList<SensorData> soundData = new ArrayList<SensorData>();
    private ArrayList<SensorData> tempData = new ArrayList<SensorData>();
    private ArrayList<SensorData> rangeData = new ArrayList<SensorData>();
    private XYChart lightDataChart;
    private XYChart soundDataChart;
    private XYChart tempDataChart;
    private XYChart rangeDataChart;
    private JFrame frame;
    public App(){
        initFireBase(); // Initialize firebase
        getSensorData("light");// get light sensor data
        getSensorData("sound");// get sound sensor data
        getSensorData("temp");// get temp sensor data
        getSensorData("range");// get range sensor data

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create and set up the window.
                frame = new JFrame("Sensor Data Consumer");
                frame.setLayout(new GridLayout(2, 2));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                // chart
                lightDataChart = createChart("light");
                soundDataChart = createChart("sound");
               tempDataChart = createChart("temp");
                rangeDataChart = createChart("range");
                // Create the chart panels
                JPanel chartPanel = new XChartPanel<XYChart>(lightDataChart);
                JPanel chartPanel2 = new XChartPanel<XYChart>(soundDataChart);
                JPanel chartPanel3 = new XChartPanel<XYChart>(tempDataChart);
               JPanel chartPanel4 = new XChartPanel<XYChart>(rangeDataChart);
                // Add the panels to the frame
                frame.add(chartPanel);
                frame.add(chartPanel2);
                frame.add(chartPanel3);
               frame.add(chartPanel4);
                // Display the window.
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {
            new App();
    }

    /**
     * Updates the all the charts by calling the get chart data method, passing the sensor name
     * of the chart to be updated which returns double arrays
     * Then we call xCharts updateXYSeries method to update the charts
     */
    private void updateCharts() {
        double[][] dataLight = getChartData("light"); // get data
        lightDataChart.updateXYSeries("a", dataLight[0], dataLight[1], null); // Update chart
        double[][] dataSound = getChartData("sound");
        soundDataChart.updateXYSeries("a", dataSound[0], dataSound[1], null);
       double[][] dataTemp = getChartData("temp");
       tempDataChart.updateXYSeries("a", dataTemp[0], dataTemp[1], null);
        double[][] dataRange = getChartData("range");
        rangeDataChart.updateXYSeries("a", dataRange[0], dataRange[1], null);
        try {
            TimeUnit.SECONDS.sleep(2);
            frame.repaint();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Handles most of the heavy lifting for the charts
     * takes the name of the sensor and uses that to assign the
     * temporary SensorData array list to the passed sensors SensorData Arraylist
     * parse the actual data out and spits it back as the object
     *
     * @param sensor the sensor which should have the data parsed
     * @return double object array
     */
    private double[][] getChartData(String sensor) {

        int i = 0; // Used for x plotting
        ArrayList<SensorData> data; // temp arraylist
        data = lightData; // Default assign to light data

        //Conditionally assign temp to other sensor data based on passed sensro
        if (sensor.equals("sound")) {
            data = soundData;
        }
        else if (sensor.equals("temp")) {
            data = tempData;
        }
        else if (sensor.equals("range")) {
            data = rangeData;
        }
        // Double arrays for the data that is to be plotted, sized using the size of the data array
        double[] xData = new double[data.size()];
        double[] yData = new double[data.size()];
        /**
         * For each SensorData object in the Arraylist feed the actual sensor data
         * to the ydata data double array and also add a simple double to the xdata
         * to plot the sensor data on
         **/
        for (SensorData d1 : data) {
            yData[i] = d1.getData();
            xData[i] = i * 1.0; // Multiply the int by 1.0 to turn it into a double
            i++;
        }
        return new double[][]{xData, yData};
    }

    /**
     * Creates a chart with dummy data
     *
     * @param chartName the name of the chart which is used to label the series and the chart
     * @return XYChart the chart object to added to the ui
     */
    private static XYChart createChart(String chartName) {
        final XYChart chart = new XYChartBuilder().width(600).height(400).title(chartName + " sensor").xAxisTitle("Time").yAxisTitle("Data").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);// Set to line chart

        // Series dummy
        chart.addSeries("a", new double[]{0, 3, 5, 7, 9}, new double[]{-3, 5, 9, 6, 5});

        return chart;
    }

    /**
     * Gets the instance of the firebase database and assigns it to the database local variable
     *
     * @return void
     */
    private void initFireBase() {
        // Class loader to help getting the firebase account file
        ClassLoader classLoader = getClass().getClassLoader();
        // Firebase account file
        File file = new File(classLoader.getResource("iot-applications-ca1-firebase-adminsdk-buwd0-423ae91d3b.json").getFile());
        // Init the FileInputStream
        FileInputStream serviceAccount = null;
        try {
            //Load the file into the stream
            serviceAccount = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            errorHandler("Cannot find account file - do you wish to exit?");
            e.printStackTrace();
        }
        // Init the FirebaseOptions object
        FirebaseOptions options = null;
        try {
            // Create are options object proper, with credentials and the database url
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://iot-applications-ca1.firebaseio.com")
                    .build();
        } catch (IOException e) {
            errorHandler("Cannot connect to firebase - do you wish to exit");
            e.printStackTrace();
        }
        // Initialize the app with options
        FirebaseApp.initializeApp(options);
        // Get an instance of our database
        database = FirebaseDatabase.getInstance();
    }

    /**
     * Gets the sensor data for the passes sensor string and
     * creates an addValueEventListener event which does and initial pull of
     * all data at the url,
     * but also does a pull each time the data gets changed on firebase.
     * Additionally when the data is updated this class calls the updateCharts
     * method, to feed the data to the charts.
     * On five consecutive failures to connect or malformed data pull the
     * application will exit while displaying an error alert.
     *
     * @param sensor the sensor to pull the data for
     */
    private void getSensorData(final String sensor) {
        // The database reference from where to source the data built using passed sensor string
        DatabaseReference ref = database.getReference("/data/" + sensor);
        ref.addValueEventListener(new ValueEventListener() { // Start the listener
            int errorCount = 0; // Init the error counter

            /**
             * OnData change handle what should happen when data on firebase at
             * ref changes - On first run this just vomits all the data in ref
             * @param dataSnapshot the data that was changed
             */
            public void onDataChange(DataSnapshot dataSnapshot) {
                errorCount = 0; // reset because success
                Double tempDataDouble; // Temporary double to hold sensor data
                SensorData sensorDataObject; // Init a sensor data object fro adding to Map
                System.out.println(dataSnapshot); // Puke the data snapshot to console

                /**
                 * For each child snapshot in the main data snapshot
                 * spit the values into a temporary object map and use that map
                 * to feed the data into the SensorData Arraylist for the sensor
                 */
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tmp = (Map<String, Object>) child.getValue(); // Temp Map
                    try {
                        tempDataDouble = valueOf(tmp.get("data")); // Get data from map to temp double
                    } catch (Exception e) {
                        tempDataDouble = 0.0; // if data comes back malformed
                    }
                    // Spit the values into a new SensorData object
                    sensorDataObject = new SensorData(tempDataDouble, tmp.get("time").toString());

                    // Conditionals for which Arraylist to add the object to
                    if (sensor.equals("light")) {
                        lightData.add(sensorDataObject);
                    }
                    else if (sensor.equals("sound")) {
                        soundData.add(sensorDataObject);
                    }
                    else if (sensor.equals("temp")) {
                        tempData.add(sensorDataObject);
                    }
                    else if (sensor.equals("range")) {
                        rangeData.add(sensorDataObject);
                    }
                }
                updateCharts(); // Call update charts when everything is finished
            }

            /**
             * Handles what happens when the handler is cancelled, in our case
             * this will only happen on error. Will retry 5 times before closing the program
             * this is to prevent us from overloading out limits on firebase
             * @param databaseError the error sent from firebase
             */
            public void onCancelled(DatabaseError databaseError) {
                if (errorCount >= 5) { // if this is the fifth  try, display the erro
                    errorHandler("Too many database errors: \n" + databaseError.getMessage()
                            + "\n Do you wish to exit?");
                } else { // else spit the error to console, increment the count and try again
                    System.out.println("The read failed: " + databaseError.getCode());
                    getSensorData(sensor);
                    errorCount++;
                }

            }
        });
    }

    /**
     * Handles the error message an exiting for the program, gives the user the option
     * of whether or not to quit because there may be data they want to view
     *
     * @param errorMessage the actual error message to display
     */
    private void errorHandler(String errorMessage) {
        System.out.println(errorMessage); // Spit the error message
        // Display the option pane to user and cast their response to an int
        int input = JOptionPane.showOptionDialog(null,
                errorMessage
                , "Firebase error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                null,
                null);
        // If yes exit the application
        if (input == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
