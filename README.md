🚪 RYZEN Smart Lock: The Ultimate BLE Anti-Intruder System 🚨
Welcome to the RYZEN Smart Lock repository!

Tired of unauthorized people wandering into your room? Want a door that magically opens for your friends but sounds a literal emergency siren if a banned user tries to sneak in? You've come to the right place.

This is an Arduino-based embedded security system complete with RFID scanning, a custom Android companion app via Bluetooth Low Energy (BLE), and a power-optimized servo mechanism.

🎓 Academic Context
This project was developed as a 4-person group mini-project for the Embedded Systems course at Universiti Putra Malaysia (UPM - FSKTM). It demonstrates data acquisition, hardware-software integration, and teamwork, engineered specifically to hit that "Level 5 - Excellent" rubric standard.

✨ Features
🛡️ Multi-Tier Access Control: Uses an MFRC522 RFID reader to manage access.

Master Admin (Aslam): Can unlock the door and reset active alarms.

Authorized Users (Wan Afiq, Alex, Safwan): Get standard entry.

Banned Users (Rofy): Triggers an immediate hardware lockdown and dual-siren alarm.

📱 Android Companion App: A custom-built Kotlin application that connects to the lock via BLE. It acts as a live serial terminal to monitor door activity and doubles as a secondary security alarm.

🔋 Power-Optimized "Sleepy Servo": Employs a custom software routine to put the power-hungry servo motor to sleep (detach()) when not in use, preventing catastrophic voltage drops and system brownouts.

📢 Dual-Siren Alert System: If an intruder is detected, the Arduino sounds a physical buzzer while the Android app simultaneously hijacks the phone's audio to play a CDMA emergency ringback tone.

🛠️ The Hardware Stack
We didn't just build a lock; we built a highly sensitive ecosystem of components. Here is what's under the hood:

Arduino Uno: The brain of the operation.

MFRC522 RFID Reader: For scanning tags.

BLE Module (JDY/HM-10): Disguised as an HC-05, this Bluetooth Low Energy chip broadcasts our serial data over the FFE1 pipeline.

SG90 Servo Motor: The physical locking mechanism (and our biggest power rival).

16x2 I2C LCD Screen: For real-time physical status updates.

Active Buzzer: Because what's a security system without some noise?

📱 The Software Stack
Embedded C++: The Arduino logic handling the RFID decryption, state machines, and servo timing.

Android / Kotlin: The mobile app bypasses outdated classic Bluetooth sockets and uses Android's modern BluetoothGatt libraries to intercept live data streams from the lock.

💡 Lessons Learned (The Hard Way)
If you are planning to fork this project or build your own, heed these warnings:

Beware the "Fake" HC-05: Many modules sold today are actually BLE chips. You cannot use standard BluetoothSocket RFCOMM connections with them. You must use GATT and hunt for the FFE1 characteristic!

Servos are Power Vampires: A servo holding its position at 90 degrees will actively fight to stay there, draining your Arduino's voltage regulator. Put your servos to sleep, or give them their own battery pack!

🤝 The Team
Developed by a dedicated squad of 4 UPM FSKTM students who survived the ultimate battle against hardware brownouts and Bluetooth -1 socket errors.

(Note to team: Add your names/student IDs here!)

Member 1: Aslam (Project Lead / Master Cardholder)

Member 2: Alex Nay Zin Min Lwin (Programmer / Designer)

Member 3: Wan Zuhaili Afiq (Presenter / Paperwork)

Member 4: Adam Safwan (Hardware Provider / Wiring Expert)

If this project helped you figure out your own BLE Arduino connection, leave a ⭐!
