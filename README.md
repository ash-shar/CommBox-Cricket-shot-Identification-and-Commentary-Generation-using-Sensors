# CommBox: Cricket Shot Identification and Comentary Generation using Sensors

Online cricket commentary has become very popular as the internet provides access to a large number of sports websites. A key challenge for them is to offer their readers an insightful and fast paced live commentary. In this project, we propose a framework to automate cricket shot identification and commentary generation using sensor data as features for machine learning models.

The system won the Best Academic Demo Award at COMSNETS 2017. Checkout the cool demo of CommBox here: https://www.youtube.com/watch?v=X4mZVrhCy1Y


You can find the description here:

- [Presentation](https://github.com/ash-shar/CommBox-Cricket-shot-Identification-and-Commentary-Generation-using-Sensors/blob/master/CommBox-Presentation.pdf)
- [Framework Description](https://github.com/ash-shar/CommBox-Cricket-shot-Identification-and-Commentary-Generation-using-Sensors/blob/master/CommBox-Abstract.pdf)

## Our Framework

![Framework Overview](https://github.com/ash-shar/CommBox-Cricket-shot-Identification-and-Commentary-Generation-using-Sensors/blob/master/framework.png?raw=true "Title")


## Prerequisites

CommBox requires the following dependencies: 
- IDE for Android App developmemnt (IntelliJ/Android Studio)
- Java
- Python (with sklearn and numpy packages)

Additionally, we make use of [MetaWear CPRO](https://store.mbientlab.com/product/metawear-cpro/), a coin sized sensor provided by [Mbient Lab](https://mbientlab.com/). 

## Development Setup

Our framework consists of 3 major parts:

- **CommBox-App:** An android based app which acts as a link between the sensor device and the computing server. Our sensor device, Metawear CPRO, is connected via bluetooth to an android device on which this app is installed. The acceleromemter and gyroscope readings as sampled by the sensor device are sent to this app. The app also keeps track of the surrounding sound frequency and amplitude which acts as a trigerring point of the server action (detecting cricket shot).

- **CommBox-Server:** The server receives accelerometer & gyroscope readings and the sound frequency and amplitude values from the CommBox-App. It then matches the logged sound attributes to that of ball hitting a bat and uses the accelerometer & gyroscope readings for identifying the cricket shot played.

- **CommBox-Model:** The machine learning model trained on the cricket shot detection task. Contains the Training/Testing codes and accelerometer/gyroscope readings of different shots as reported by shots played by one of our contributors.

## Our Team
- Ashish Sharma
- Jatin Arora
- Pritam Khan
- Sidhartha Satapathy
- Sumit Agarwal
- Satadal Sengupta
- Sankarshan Mridha
- Niloy Ganguly