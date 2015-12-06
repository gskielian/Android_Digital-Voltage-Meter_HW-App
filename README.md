Android-VCO-FFT
===============


###Summary

This is Android Code for a Fourier Frequency Reading and Peak Analysis of a Texas Instruments LM331N Voltage Controlled Oscillator.


###Motivation: 
####Low-Cost Data-Logger (<$10), Utilizing Existing Smartphones for Analysis, Web-Connectivity, & Display

Existing data-loggers are extremely expensive and lack both web-connectivity and the ability for students to take them home.

This serves the dual purposes of creating an extremely low-cost solution for voltage levels as well as forging a path for students to continue their scientific education anywhere (as it is literally mobile device). 

Since most of my students have Smartphones, I designed, prototyped, and ironed out a way to use these smartphones* as laboratory instruments (Android proof-of-concept only for now).

*Android POC is available for now, I will create an IPhone version of this app as well (in addition to a donate version which looks a little bit more shiny).


###Uses:

As a simple web-service-connectable solution for analog medical-diagnostic or engineering/scientific data analysis (patient temperature, spectroscopy measurements, soil-resistivity, etc.)


###Licensing:

Against my better financial interests, but I am considering (just considering) an MIT License for this code -- maybe.


###Details:

This is for now a slow measurement technique (I believe I can get it to around 100 ms refresh rate eventually, from observation of currently published FFT apps on the Google Play Store).




###Hardware:


#### VCO Chosen: The LM331N
For this project I used the LM331N as a low-cost, single-rail, Voltage-to-Frequency converter. (Link to datasheet)
It is wired as the datasheet suggests for single-rail operation,* and the inverse function of the V2F is used to retrieve the voltage.


*Important Modification: A voltage-divider at the end is necessary for reducing the peak-to-pead output voltage (somewhere on StackOverflow, I learned that the input for most phones caps at 1.7 volts).


#### Input Jack  
The hardware can utilize either the microphone-input-jack (for highest-fidelity) or even the regular microphone (default).
Any smartphone that has a microphone input jack and a recorder can be used as a datalogger (well technically even without the mic-input-jack since we can perform frequency-space decomposition on any sound file).

I have successfully used this on Android tablets and Android phones which have mic input jacks (all credit card readers use mic input jacks, and most of the older ones I've tried also have them).



###Future Hardware Enhancements


1) Wire up second LM331N as a Frequency to Voltage Converter on the headphone output -- this would function as a DCA and a potential way to get data to a device (control of IC Potentiometers, although I'll have to see if I can generate fast enough signals to do serial communication -- which I could do already via USB but would save on the user-end complexity of setup).

###Future Software Enhancements

0) Utilizing data on hardware capability to prevent over-spawning of threads (I currently have it hard-coded to a relatively slow 1 sample per second, which works on all my devices)

1) Email data in CSV Format

2) Increasing resolution by Quadratic Interpolation: Logging of the nearest neighbors of max measured freq (FFT-Mode) and doing quadratic interpolation to find more precise estimate of max-freq.

3) Create toggle to allow for lower sample-rates

4) Utilize wireless connectivity to periodically push data to a web-server.

5) Optimize to push upper limit on sample-rate

6) Toggle switch between FFT and Zero-Crossing peak freq methods.

.
.
.

?) Implement wavelet analysis while taking larger chunks of data -- this will allow more rapid measurements with the trade off of doing post-processing (lower screen refresh rate -- which is okay if you simply want the device to take samples and come back later).
