# wake-on-lan

This is a small project to switch on my desktop through a website and enables me to access it from outside using internet. It helps me to turn off the computer when I do not need it and thus reduces the electricity bill. The setup uses a phone and a desktop connected to the same WiFi/LAN.  
The application on the phone pings an api at regular intervals to check if a "switch on" signal is enabled. If it finds the signal then then it broadcasts a "Wake On LAN" packet to the IP of the computer.

# wake-on-lan-web
Web interface which enables user to send a "switch on" signal and set the next ping interval.

# wake-on-lan-app
Android application which checks for change of signals and switches on the computer. After switching off the application again checks for the signal again in some time which is provided by the API.
