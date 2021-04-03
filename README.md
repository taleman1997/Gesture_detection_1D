# Gesture-detection-1D

## Aims
This project will develop algorithms to process the reflected acoustic signals acquired by the microphones on the device to derive hand movement information. This human-machine interaction method does not rely on any extra components, such as adding sensors on the hands, making it promising and challenging.

## Achievement
The code in this repostory is capabile to track 1D motion in the range of 15cm and the error is less than 1cm. The whole application can run in real-time mode. A demo is as below.


![demo](https://github.com/taleman1997/Gesture_detection_1D/blob/main/demo.gif)


## Android platform:
This algorithm is now based on Android 8 or above.

## Structure summary
The signal is emmitted from the speaker on the smartphone, then reflected by hand and recorded by the mirophone. The signal goes through modulation, down-sampling and then calculation the distance.
