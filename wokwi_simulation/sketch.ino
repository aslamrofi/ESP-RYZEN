#include <Servo.h>
#include <LiquidCrystal_I2C.h>
#include <SPI.h>
#include <MFRC522.h>
#include <SoftwareSerial.h>

#define SS_PIN 10
#define RST_PIN 9
#define BUZZER_PIN 5 // Buzzer Pin

// Bluetooth Pins: RX=2, TX=4
SoftwareSerial bluetooth(2, 4); 

Servo doorServo;
LiquidCrystal_I2C lcd(0x27, 16, 2);
MFRC522 rfid(SS_PIN, RST_PIN);

bool alarmActive = false; // Track the alarm state

void setup() {
  Serial.begin(9600);    // For USB Monitor
  bluetooth.begin(9600); // For Android BLE App
  
  SPI.begin();
  rfid.PCD_Init();
  
  lcd.init();
  lcd.backlight();
  
  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW); // Ensure buzzer is off
  
  // --- SERVO POWER HACK (SETUP) ---
  doorServo.attach(3);   // Wake up servo
  doorServo.write(90);   // Move to locked position
  delay(500);            // Give it 0.5 seconds to physically move
  doorServo.detach();    // PUT SERVO TO SLEEP (Stops power drain!)
  
  bluetooth.println("System Online: Ready");
  showIdleMessage();
}

void loop() {
  // If alarm is active, keep the buzzer screaming
  if (alarmActive) {
    digitalWrite(BUZZER_PIN, HIGH); 
  }

  // Look for new cards
  if (!rfid.PICC_IsNewCardPresent() || !rfid.PICC_ReadCardSerial()) {
    return;
  }

  // Read the UID
  String ID = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    ID.concat(String(rfid.uid.uidByte[i] < 0x10 ? " 0" : " "));
    ID.concat(String(rfid.uid.uidByte[i], HEX));
  }
  ID.toUpperCase();
  ID.trim();

  lcd.clear();
  lcd.setCursor(0, 0);

  // 1. MASTER CARD LOGIC (ASLAM)
  if (ID == "01 02 03 04") {
    if (alarmActive) {
      // Master resets the alarm
      alarmActive = false;
      digitalWrite(BUZZER_PIN, LOW);
      bluetooth.println("ALARM DEACTIVATED BY MASTER");
      lcd.print("ALARM RESET");
      lcd.setCursor(0, 1);
      lcd.print("MASTER AUTH");
      delay(2000);
    } else {
      handleAccess("MASTER (ASLAM)");
    }
  } 
  // 2. BANNED CARD LOGIC (ROFY)
  else if (ID == "AA BB CC DD") {
    alarmActive = true;
    lcd.print("BANNED TAG!");
    lcd.setCursor(0, 1);
    lcd.print("ALARM TRIGGERED");
    bluetooth.println("SECURITY ALERT: BANNED TAG (ROFY) DETECTED!");
    // The buzzer will stay on because alarmActive is now true
  } 
  // 3. OTHER AUTHORIZED CARDS (Only work if alarm is OFF)
  else if (!alarmActive) {
    if (ID == "55 66 77 88") {
      handleAccess("WAN AFIQ");
    } 
    else if (ID == "11 22 33 44") {
      handleAccess("ALEX");
    } 
    else if (ID == "04 11 22 33 44 55 66") {
      handleAccess("SAFWAN");
    } 
    else {
      denyAccess("UNKNOWN CARD");
    }
  } 
  else {
    // If alarm is on, don't allow anyone else in
    lcd.print("ALARM ACTIVE!");
    lcd.setCursor(0,1);
    lcd.print("SCAN MASTER");
    delay(1000);
  }

  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();

  showIdleMessage();
}

void handleAccess(String name) {
  lcd.print(name);
  lcd.setCursor(0, 1);
  lcd.print("ACCESS GRANTED");
  
  // Send data to Phone
  bluetooth.print("SUCCESS: ");
  bluetooth.println(name);
  
  // --- SERVO POWER HACK (ACCESS GRANTED) ---
  doorServo.attach(3);   // WAKE UP
  doorServo.write(180);  // Open door
  delay(1000);           // Keep open for 1 second 
  doorServo.write(90);   // Close door 
  delay(1000);           // Wait 1 second for it to finish moving back
  doorServo.detach();    // GO BACK TO SLEEP
}

void denyAccess(String reason) {
  lcd.print(reason);
  lcd.setCursor(0, 1);
  lcd.print("ACCESS DENIED");
  
  // Send alert to Phone
  bluetooth.print("ALERT: Access Denied - ");
  bluetooth.println(reason);
  
  delay(2000);
}

void showIdleMessage() {
  if (alarmActive) return; // Don't show idle message if alarm is screaming
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("  READY TO SCAN ");
  lcd.setCursor(0, 1);
  lcd.print("  PLEASE TAP ID ");
}