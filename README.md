# Android Digital-Voltage-Meter HW-App

Project which uses the headphone jack, VCO, and Zero-Crossing Frequency to connect Android Device to the outside world (from an EE's perspective ; )

### Motivation: 
#### Low-Cost Data-Logger (<$10), Utilizing Existing Smartphones for Analysis, Web-Connectivity, & Display

Existing data-loggers are extremely expensive and lack both web-connectivity and the ability for students to take them home.

This serves the dual purposes of creating an extremely low-cost solution for voltage levels as well as forging a path for students to continue their scientific education anywhere (as it is literally mobile device). 

Since most of my students have Smartphones, I designed, prototyped, and ironed out a way to use these smartphones as laboratory instruments (Android proof-of-concept only for now).



### Uses:

As a simple web-service-connectable solution for analog medical-diagnostic or engineering/scientific data analysis (patient temperature, spectroscopy measurements, soil-resistivity, etc.)




### Hardware:


#### VCO Chosen: The LM331N
For this project I used the LM331N as a low-cost, single-rail, Voltage-to-Frequency converter. (Link to datasheet)
It is wired as the datasheet suggests for single-rail operation,* and the inverse function of the V2F is used to retrieve the voltage.


*Important Modification: A voltage-divider at the end is necessary for reducing the peak-to-pead output voltage (somewhere on StackOverflow, I learned that the input for most phones caps at 1.7 volts).


#### Input Jack
The hardware can utilize either the microphone-input-jack (for highest-fidelity) or even the regular microphone (default).
Any smartphone that has a microphone input jack and a recorder can be used as a datalogger (well technically even without the mic-input-jack since we can perform frequency-space decomposition on any sound file).

I have successfully used this on Android tablets and Android phones which have mic input jacks (all credit card readers use mic input jacks, and most of the older ones I've tried also have them).



### Future Hardware Enhancements


1) Wire up second LM331N as a Frequency to Voltage Converter on the headphone output -- this would function as a DCA and a potential way to get data to a device (control of IC Potentiometers, although I'll have to see if I can generate fast enough signals to do serial communication -- which I could do already via USB but would save on the user-end complexity of setup).

### Future Software Enhancements


1) Create option to select sample-rate

2) Utilize wireless connectivity to periodically push data to the cloud.

3) Button to email data in CSV Format
