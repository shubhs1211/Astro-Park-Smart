#include <LCD.h>
#include <LiquidCrystal.h>
#include <LiquidCrystal_I2C.h>
#include<Servo.h>
#include<Wire.h>
#include <ESP8266WiFi.h>
#include "Timer.h"

///////////////Parameters/////////////////
// WIFI params
const char* ssid = "onem2m";
const char* password = "hackathon";

// CSE params
const char* host = "10.24.47.29";
const int httpPort = 8080;

// AE params
const int aePort   = 80;
const int id = 4;
const String origin   = "Cae_device";
const String deviceUri = "/server/mydevice";
Servo servo;
LiquidCrystal_I2C lcd(0x27,2,1,0,4,5,6,7);
///////////////////////////////////////////

Timer t;

WiFiServer server(aePort);
int sensorPin = A0; // select the input pin for LDR 
int sensorValue = 0; // variable to store the value coming from the sensor
int state = 0; 

void setup() {

  Serial.begin(115200);
  delay(10);

  // Configure pin 5 for LED control
  pinMode(0, OUTPUT);
  digitalWrite(0, 0);

  setAll();
  Serial.println();
  Serial.println();

  // Connect to WIFI network
  Serial.print("Connecting to ");
  Serial.println(ssid);
 
  WiFi.persistent(false);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  lcdPrint(String() + "Available PS" + (id - 1));
  // Start HTTP server
  server.begin();
  Serial.println("Server started");

  // Create AE resource
  String s = "{\"m2m:ae\":{\"rn\":\"mydevice";
  
  String resulat = send("/server",2, s + id + "\",\"api\":\"mydevice" + id + ".company.com\",\"rr\":\"true\",\"poa\":[\"http://"+WiFi.localIP().toString()+":"+aePort+"\"]}}");
  
  if(resulat=="HTTP/1.1 201 Created"){
    // Create Container resource
    send(deviceUri + id,3,"{\"m2m:cnt\":{\"rn\":\"luminosity\"}}");

    // Create ContentInstance resource
    send(deviceUri + id + "/luminosity",4,"{\"m2m:cin\":{\"con\":\"0\"}}");
    
    // Create Container resource
    send(deviceUri + id ,3,"{\"m2m:cnt\":{\"rn\":\"led\"}}");

    // Create ContentInstance resource
    send(deviceUri + id + "/led",4,"{\"m2m:cin\":{\"con\":\"OFF\"}}");

    // Create Subscription resource
    send(deviceUri + id + "/led",23,String() + "{\"m2m:sub\":{\"rn\":\"led_sub\",\"nu\":[\""+ origin + id +"\"],\"nct\":1}}");
  }

  t.every(1000*5, push);
}

// Method in charge of receiving event from the CSE
void loop(){
  t.update();
  // Check if a client is connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }
  
  // Wait until the client sends some data
  Serial.println("new client");
  while(!client.available()){
    delay(1);
  }
  
  // Read the request
  String req = client.readString();
  client.flush();

  if(req.indexOf("1,0,0") != - 1){
    lcdPrint(String() + "Available PS" + (id - 1));
    state = 0;
//    digitalWrite(5, 1);
//    digitalWrite(4, 0);
    digitalWrite(0, 0);
  }
  if(req.indexOf("1,1,0") != - 1){
    lcdPrint("Reserved");
    state = 1;
    servoDir(true);
//    digitalWrite(5, 1);
//    digitalWrite(4, 1);
    digitalWrite(0, 0);
  }
  if(req.indexOf("1,1,1") != - 1){
    lcdPrint("Unlocked");
    state = 2;
    servoDir(false);
//    digitalWrite(5, 1);
//    digitalWrite(4, 1);
    digitalWrite(0, 1);
  }
  if(req.indexOf("0,1,1") != - 1){
    lcdPrint("Reserve inuse");
    state = 3; 
//    digitalWrite(5, 0);
//    digitalWrite(4, 1);
    digitalWrite(0, 1);
  }
  if(req.indexOf("0,0,0") != - 1){
    lcdPrint("inuse");
    state = 4; 
//    digitalWrite(5, 0);
//    digitalWrite(4, 0);
    digitalWrite(0, 0);
  }
  client.flush();

  // Send HTTP response to the client
  String s = "HTTP/1.1 200 OK\r\n";
  client.print(s);
  delay(1);
  Serial.println("Client disonnected");
 
}


// Method in charge of sending request to the CSE
String send(String url,int ty, String rep) {

  // Connect to the CSE address
  Serial.print("connecting to ");
  Serial.println(host);
 
  WiFiClient client;
 
  if (!client.connect(host, httpPort)) {
    Serial.println("connection failed");
    return "error";
  }

  
  // prepare the HTTP request
  String req = String()+"POST " + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" +
               "X-M2M-Origin: " + origin + id + "\r\n" +
               "Content-Type: application/json;ty="+ty+"\r\n" +
               "Content-Length: "+ rep.length()+"\r\n"
               "Connection: close\r\n\n" + 
               rep;


  // Send the HTTP request
  client.print(req);
               
  unsigned long timeout = millis();
  while (client.available() == 0) {
    if (millis() - timeout > 5000) {
      Serial.println(">>> Client Timeout !");
      client.stop();
      return "error";
    }
  }

  // Read the HTTP response
  String res="";
  if(client.available()){
    res = client.readStringUntil('\r');
//    Serial.print(res);
  }
  while(client.available()){
    String line = client.readStringUntil('\r');
//    Serial.print(line);
  }
  
  Serial.println();
  Serial.println("closing connection");
  Serial.println();
  return res;
}

void push(){
  sensorValue = analogRead(sensorPin);
  Serial.println(sensorValue);
  String data = String()+"{\"m2m:cin\":{\"con\":\"" + id + "," +sensorValue+"\"}}";
  send(deviceUri + id + "/luminosity",4,data);

}

void setAll(){
  lcd.begin(16,2);
  lcd.setBacklightPin(3,POSITIVE);
  lcd.setBacklight(HIGH);
  Serial.begin(115200);
  servo.attach(10);
  servo.write(0);
  delay(2000);
}

void servoDir(boolean dir){
   
   if(dir){
     servo.write(90);
     delay(1000);
   }
   else {
     servo.write(0);
     delay(1000);
   }
}

void lcdPrint(String str){
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print(str);
}


