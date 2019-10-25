ResCan
======

Resistor ring value scanner


Dependencies:

- OpenCV (Used Version:  4.1.1)
- FreeTTS (Used Version: 1.2.2)

You might have to setup JSAPI by copying the speech.properties file provided by FreeTTS to your home directory.


Binary
======

There are binaries for Windows 64bit and 32bit available in the releases section of GitHub.
https://github.com/armageddon421/ResCan/releases


Command Line Compile
======
Create a new folder called META-INF in the src folder.
Inside create file called "MANIFEST.MF"
Example MANIFEST.MF

```
Manifest-Version: 1.0
Class-Path: .	ReSCan_lib/cmu_us_kal.jar	ReSCan_lib/jsapi.jar	ReSCan_lib/cmu_time_awb.jar	ReSCan_lib/mbrola.jar	ReSCan_lib/freetts-jsapi10.jar	ReSCan_lib/cmudict04.jar	ReSCan_lib/en_us.jar	ReSCan_lib/cmutimelex.jar	ReSCan_lib/opencv-411.jar	ReSCan_lib/freetts.jar	ReSCan_lib/cmulex.jar
Main-Class: Webcam
```
CD to your src folder
Run:
>javac Webcam.java

Then Run
>jar cmvf META-INF/MANIFEST.MF ResCan.jar Webcam.class Webcam$1.class

Exec jar
>java -jar -ResCan.jar
