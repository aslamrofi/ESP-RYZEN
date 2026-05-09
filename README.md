# 🚪 RYZEN Smart Lock
### BLE-Based Embedded Door Security System

An Arduino-powered smart lock system featuring RFID authentication, Bluetooth Low Energy (BLE) monitoring, real-time alerts, and power-optimized servo control.

Built as a mini-project for the Embedded Systems course at Universiti Putra Malaysia (UPM - FSKTM).

---

# 📌 Project Overview

RYZEN Smart Lock provides:

- RFID-based access control
- BLE monitoring through a custom Android app
- Intruder detection with alarm triggers
- Power-efficient servo lock control

---

# ✨ Features

## 🛡️ Multi-Level Access Control

Using the MFRC522 RFID reader, the system classifies users into multiple permission levels:

| User Type | Access |
|------------|---------|
| Master Admin (Aslam) | Unlock door + reset alarms |
| Authorized Users | Standard entry access |
| Banned Users | Triggers lockdown + siren |

---

## 📱 Android BLE Companion App

Custom Android application built with Kotlin:

- Connects via Bluetooth Low Energy (BLE)
- Displays real-time lock activity
- Functions as a secondary alarm system
- Uses BluetoothGatt instead of classic RFCOMM sockets

---

## 🔋 Power-Optimized Servo System

Custom "Sleepy Servo" implementation:

- Automatically detaches servo after movement
- Reduces power drain
- Prevents voltage drops and Arduino brownouts

---

## 🚨 Dual-Siren Intruder Alert

If unauthorized access is detected:

- Arduino activates physical buzzer alarm
- Android app plays emergency alert ringtone simultaneously

---

# 🛠 Hardware Stack

| Component | Purpose |
|------------|---------|
| Arduino Uno | Main controller |
| MFRC522 RFID Reader | RFID authentication |
| JDY / HM-10 BLE Module | BLE communication |
| SG90 Servo Motor | Physical lock mechanism |
| 16x2 I2C LCD | Status display |
| Active Buzzer | Alarm system |

---

# 💻 Software Stack

## Embedded System

- Arduino C++
- RFID handling
- Servo timing control
- State-machine logic

## Mobile Application

- Android Studio
- Kotlin
- BluetoothGatt BLE communication
- Real-time serial monitoring

---

# ⚠ Lessons Learned

## Fake HC-05 Modules Exist

Many modules sold as HC-05 are actually BLE devices.

This means:

❌ BluetoothSocket will fail  
✅ BluetoothGatt must be used instead

You may also need to locate the FFE1 characteristic manually.

---

## Servos Consume Serious Power

Servos continuously holding position can:

- Drain voltage regulators
- Cause instability
- Trigger Arduino resets

Solutions:

- Detach servo when idle
- Use dedicated external power supply

---

# 👥 Team Members

| Member | Role |
|---------|------|
| Aslam | Project Lead / Master Cardholder |
| Alex Nay Zin Min Lwin | Programmer / Designer |
| Wan Zuhaili Afiq | Presenter / Documentation |
| Adam Safwan | Hardware / Wiring |

---

# ⭐ Acknowledgements

Developed by a team of FSKTM students who survived:

- BLE debugging
- Servo brownouts
- Mysterious -1 socket errors
- Endless jumper wire problems

If this project helped you understand BLE + Arduino communication, consider leaving a ⭐ on the repository.
