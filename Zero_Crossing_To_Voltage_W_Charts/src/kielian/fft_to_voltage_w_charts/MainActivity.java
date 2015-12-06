package kielian.fft_to_voltage_w_charts;

import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;


//Update
//<uses-permission android:name="android.permission.RECORD_AUDIO" ></uses-permission>
//place above after application tag
//also go to properties-> JavaBuildPAth-> Projects to add the Jar library

// this version analyzes the frequency
// doesn't include freq below 100hz (due to 1/f noise)
// updates approx every 0.4 seconds (could be faster or slower)
//what I finished today:
// freq of max magnitude
// freq to voltage
// to make it:
// 1) install the jtransforms as a library (import it to libs, right click and add as library)
// 2) paste code into the main activity
// 3) mod the android manifest to include ability to record from mic
// 4) mod the main_activity.xml to include a textview named "textView1"
// 5) ...
// 6) profit!
//future work
// might consider zero-crossing method as an alternate to fft
// might want to guess where the peak is by interpolating nearest to highest value (not just return the highest sampled value)
// would definitely want to test to see if 1/f noise disappears for when the mic jack is plugged in -- if so we could simply have the for loop run from 0 to half of the buffer.length (or in our code below: 1/4 of 2xthebufferlength)
// a graph would be awesome
// tab view would be great
// numerical view + graphview would be amazing
// tweet or action when it hits a certain value or stays above a certain value for a certain amount of time.
// the possibility to perform moving average if there is noise
// lots of testing with spare android device.
// anything to make this faster would be great!
//import android.app.Activity;
//	import android.os.Bundle;
//import edu.emory.mathcs.jtransforms.fft.*;
//	import edu.emory.mathcs.utils.*; 


	//http://www.anddev.org/novice-tutorials-f8/acquire-samples-with-audiorecord-and-maybe-dump-them-t12957.html

	public class MainActivity extends Activity {

		//newCode
		   private GraphicalView mChart;
		   private int dasIndex = 1;
		    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

		    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

		    private XYSeries mCurrentSeries;

		    private XYSeriesRenderer mCurrentRenderer;
		    private void initChart() {
		        mCurrentSeries = new XYSeries("Sample Data");
		        mDataset.addSeries(mCurrentSeries);
		        mCurrentRenderer = new XYSeriesRenderer();
		        mCurrentRenderer.setColor(Color.rgb(255, 153, 0));
		        mRenderer.addSeriesRenderer(mCurrentRenderer);
		        mRenderer.setPanEnabled(true, true);
		        mRenderer.setZoomEnabled(true,true);
		        mRenderer.setYAxisMin(0);
		        mRenderer.setXTitle("Seconds");
		        mRenderer.setYTitle("Volts");
		        mRenderer.setXTitle("Seconds");
		        mRenderer.setXLabelsColor(Color.WHITE);
		    //    mRenderer.setYLabelsColor(Color.RED);
		        mRenderer.setChartTitle("Voltage Vs. Time");
		        mRenderer.setShowGrid(true);
		        mRenderer.setLabelsTextSize(15)
		        ;
		        mRenderer.setYLabels(10);
		        
		        
		        mRenderer.setYLabelsAlign(Align.RIGHT);
		        mRenderer.setXAxisMin(0);
		        mRenderer.setXAxisMax(400);
		        mRenderer.setYLabelsAngle(0);  
		        mRenderer.setYAxisMax(5);
		        mRenderer.setApplyBackgroundColor(true);
		        mRenderer.setBackgroundColor(Color.BLACK);
		    }

		    private void addSampleData(double Voltage) {
		        mCurrentSeries.add(dasIndex, Voltage);
		        dasIndex++;
	//	        mCurrentSeries.add(2, 3);
		//        mCurrentSeries.add(3, 2);
		  //      mCurrentSeries.add(4, 5);
		    //    mCurrentSeries.add(5, 4);
		    }

		//endnewCode
		
		
		public AudioRecord audioRecord;
		public int mSamplesRead; // how many samples read
		public int bufferSizeBytes;
		public int bufferLength;
		public double freq;
		public double freqMax;
		public double VoltageLevel;

		//public int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO; // CHANNEL_CONFIGURATION_MONO IS DEPRECATED, USE CHANNEL_IN_MONO instead
		public int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
		public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		public static short[] buffer; // +- 32767, which is the highest value (although I head 32768 is highest from some sources...)
		//answer: apparently min is -32768 and max is 32767, so dividing by 32768 is actually more appropriate for normalization (source: http://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html)
		public static double[] bufferDouble;
		public double re;
		public double im;
		public static double[] dasMagnitude;
		public double maxHolder;
		public static double[] bufferDoubleOut;
		public static final int SAMPLES_PER_SECOND = 8000; // can use any of 8000, 11025, 22050, 44100, or 48000, but 8000 max for emulator
		
		
		//FFT variables
		DoubleFFT_1D fft1d;
		
		/** Following is called when the activity is first created **/
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			 setContentView(R.layout.activity_main);// remove if using pure TV

			 
			 
			 
			 bufferSizeBytes = AudioRecord.getMinBufferSize(SAMPLES_PER_SECOND, channelConfiguration, audioEncoding);
			 //7684 on my phone
			buffer = new short[bufferSizeBytes]; //Great! we are learning the short method, this will save a step!
			bufferLength = bufferSizeBytes/2;
			bufferDouble = new double[bufferSizeBytes*2];
			dasMagnitude = new double[bufferSizeBytes*2];
			audioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC,SAMPLES_PER_SECOND, channelConfiguration, audioEncoding, bufferSizeBytes); // this is the constructor
			trigger();
			
		}//oncreate
		
		
		// ----------------- this is a neat way of separating code -------------------------
		
	//perform FFT
		public void PerformFFT() {
			DoubleFFT_1D fft1d = new DoubleFFT_1D(buffer.length);
//			DoubleFFT_1D fft1d = new DoubleFFT_1D(buffer.length*2);	
			for (int i=0 ; i< buffer.length; i++) {
				bufferDouble[i] = (double) buffer[i]; //buffer is a short, but DoubleFFT_1D requires a double array
			}
			fft1d.realForward(bufferDouble);
		}
		
		//Perform FFT
		
		
	public void trigger() 
	{
		//loop for now, like a polling method -- it will be best though to optimize this later by implementing threads.
	   	final Handler handler = new Handler(); 
	    Timer t = new Timer(); 
	    t.scheduleAtFixedRate(new TimerTask() { 
	            public void run() { 
	                    handler.post(new Runnable() { 
	                            public void run() { 
	                            	acquire();
	                            	dump(); 
	                            } 
	                    }); 
	            } 
	    },0, 1000); 
	  

		}
	

	    public static int calculate(int sampleRate, short [] audioData)
	    {
	        int numSamples = audioData.length;
	        int numCrossing = 0;
	        for (int p = 0; p < numSamples-1; p++)
	        {
	            if ((audioData[p] > 0 && audioData[p + 1] <= 0) || 
	                (audioData[p] < 0 && audioData[p + 1] >= 0))
	            {
	                numCrossing++;
	            }
	        }

	        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
	        float numCycles = numCrossing/2;
	        float frequency = numCycles/numSecondsRecorded;

	        return (int)frequency;
	    }
	


	public void acquire()
	{
		try
		{
			audioRecord.startRecording();
			mSamplesRead = audioRecord.read(buffer, 0, bufferSizeBytes);
			audioRecord.stop();
			
		} catch (Throwable t) {
			//Log.e("AudioRecord", "Recording Failed");
			
		}
	}
		
		
	public void dump() 
	{
		//TextView tv = new TextView(this);
		//setContentView(tv);
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setTextColor(Color.CYAN);
		tv.setText("bufferSizeBytes " + bufferSizeBytes + "\n");
		//	for (int i = 0; i < 256; i++)
		
		freqMax = calculate(SAMPLES_PER_SECOND,buffer);
		
/////		PerformFFT();
		

		//for (int i = 0; i < bufferDouble.length/4-1; i++)
	/*
	 * 
		for (int i = 0; i < bufferDouble.length/(4*2*10); i++)
		{
			re = bufferDouble[2*i];
			im = bufferDouble[2*i+1];
			dasMagnitude[i] = Math.sqrt(re*re + im*im); 
			freq = ((double) SAMPLES_PER_SECOND)/( (double) buffer.length)*i;
			tv.append("Freq: " + freq + " Hz; Magnitude: " + dasMagnitude[i] + " \n");
		}
		*/
		//makes it so that it starts counting at 100 hz *approx* since lower freq have 1/f noise, and are disproportionately loud
/////	/	int j= (int) (100.0/(SAMPLES_PER_SECOND)*(buffer.length));
	//////		re = bufferDouble[2*j];
	//////		im = bufferDouble[2*j+1];
	//////	maxHolder =  Math.sqrt(re*re + im*im);
	//////dasMagnitude[j] = maxHolder;

		
	//////	for (int i = (int) (100.0/(SAMPLES_PER_SECOND)*(buffer.length)); i < bufferDouble.length/(4); i++)
	//////	{
	//////		re = bufferDouble[2*i];
	//////		im = bufferDouble[2*i+1];
	//////			dasMagnitude[i] = Math.sqrt(re*re + im*im); 
	//////		if (dasMagnitude[i] > maxHolder )
	//////		{
	//////		maxHolder = dasMagnitude[i];
	//////	freqMax = ((double) SAMPLES_PER_SECOND)/( (double) buffer.length)*i;

	//////	}
	//////		}
		
		
		
		
		VoltageLevel = freqMax * 2.09 * 100000.0 / 12000.0 * 6800 *0.01/1000000;

		tv.append("Freq: " + freqMax + " Hz; Voltage Level: " + VoltageLevel + " \n");
	    addSampleData(VoltageLevel);
        mChart.repaint();



		//for (int i = 0; i < (bufferDouble.length)/2; i++)
		//{
		//	tv.append(" " + bufferDouble[i]);
		//}
		tv.invalidate();
	}




	//---------------------lifecycle callbacks ---------------------

	@Override
	public void onResume()
	{
		super.onResume();
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            //addSampleData();
            mChart = ChartFactory.getCubeLineChartView(this, mDataset, mRenderer, 0.3f);
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    
		trigger();
		
		
		
	}//onResume

	@Override
	public void onPause()
	{
		super.onPause();
		audioRecord.stop(); // makes sure the recording only happens when activity is in front
	}

	@Override
	public void onStop()
	{
		super.onStop();
		audioRecord.release();
		
	}//onStop  makes sure we relinquish the resources if our activity is killed

	@Override
	public boolean onTouchEvent(MotionEvent motionevent)
	{
	//	if (motionevent.getAction()==MotionEvent.ACTION_DOWN)
		//{
		//	trigger(); // acquire buffer full of samples
	//	}
		return true;
	}// onTouchEvent
	
	

	}//activity
	//---end of file ----
