#include <WiFi.h>//ESP32 라이브러리
#include <PubSubClient.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <BLEBeacon.h>

#define DEVICE_NAME         "SmartHardHat-001"
#define SERVICE_UUID        "7A0247E7-8E88-409B-A959-AB5092DDB03E"
#define BEACON_UUID         "2D7A9F0C-E0E8-4CC9-A71B-A21DB2D034A1"
#define BEACON_UUID_REV     "A134D0B2-1DA2-1BA7-C94C-E8E00C9F7A2D"
#define CHARACTERISTIC_UUID "82258BAA-DF72-47E8-99BC-B73D7ECD08A5"

// 연결 상태
bool power = false;

// 와이파이 공유기 ID PW
const char* ssid = "AICT"; // 값 입력 필요
const char* password = "aict2024!!"; //값 입력 필요
// 브로커 주소(이게 서버 주손가)
const char* mqtt_server = "smartadmin.aictlab.com"; //값 입력 필요
// MQTT_Explorer에서 위의 주소로 접속해서 업로드, 전송 결과 확인


// const char* ssid = "AICT"; // 와이파이 이름
// const char* password = "aict2024!!"; // 와이파이 pw
// const char* mqtt_server = "192.168.0.164"; // Node-RED 서버 IP

// 버튼 부저
const int btn_pinNo = 10; // mini는 10번 핀에 연결
const int buzz_pin = 20; // mini는 20번 핀에 연결
const int timeLeft = 5000; // 5초 준다
unsigned long pushedTime = 0; // 누른 시간 
int buttonState = 0; // 버튼 상태 초기화
int pushedCount = 0; // 버튼 누른 횟수 초기화
bool count = false; // 횟수 셀까?
bool emergency = false; //긴급할까?
String statusMsg = "";
String sensorMsg = "";

//led
const int ledPin = 21;

//계이름
const int dd = 523;
const int dds = 554;
const int re = 587;
const int res = 622;
const int mi = 659;
const int pa = 698;
const int pas = 740; 
const int sol = 784;
const int sols = 831;
const int ra = 880;
const int ras = 932;
const int si = 988;
const int ddo = 1046;
const int ddos = 1109;
const int rre = 1175;
const int rres = 1245;
const int mmi = 1319;
const int ppa = 1397;
const int ppas = 1480;
const int ssol = 1567;
const int ssols = 1661;
const int rra = 1760;
const int rras = 1865;
const int ssi = 1976;

// FSR 센서
const int fsrPin0 = 1;   // FSR 센서가 연결된 아날로그 핀 1
const int fsrPin1 = 2;   // FSR 센서가 연결된 아날로그 핀 2
unsigned long lastCheck = 0; // 마지막 확인 시간
unsigned long timeSet = 0; // 시간 temp
bool wear = false;      // 장착했나?
int wearTimes = 0; // 장착시간

// BLE 비콘
BLEServer *pServer;
BLECharacteristic *pCharacteristic;
bool deviceConnected = false;
uint8_t BLEvalue = 0;

WiFiClient espClient;
PubSubClient client(espClient);
String lastMsg = "";
#define MSG_BUFFER_SIZE	(50)
char msg[MSG_BUFFER_SIZE];
int value = 0;

// 인터넷 공유기 접속
void setup_wifi() {

  delay(10);
  //와이파이 네트워크 연결
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

// 데이터 수신
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

// ESP32가 MQTT서버와 접속하는 부분
void reconnect() {
  // 서버와 연결될때까지 반복
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // 무작위 클라이언트 ID 생성
    String clientId = "ESP32Client-";
    clientId += String(random(0xffff), HEX);
    // 연결 시도
    lastMsg = String(DEVICE_NAME) +" "+ String(power) +" "+ String(wear) +" "+ String(emergency);
    if (client.connect(clientId.c_str(), "", "", "aict/status", 1, true, lastMsg.c_str())) {
      // 연결 성공
      Serial.println("connected");
      // 발행, 여기 TOPIC을 우리 웹서버가 구독하게 하면 값을 받아올 수 있음
      power = true;
      statusMsg = String(DEVICE_NAME) +" "+ String(power) +" "+ String(wear) +" "+ String(emergency);
      client.publish("aict/status", statusMsg.c_str());
    } else {
      // 연결 실패
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // 재접속 시도하기전에 5초 대기
      delay(5000);
    }
  }
}

// BLE 서버 콜백 서비스
class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *pServer) {
    deviceConnected = true;
    Serial.println("deviceConnected = true"); // 연결됬을때 콘솔 출력
  };

  void onDisconnect(BLEServer *pServer) {
    deviceConnected = false;
    Serial.println("deviceConnected = false"); // 연결 끊겼을때 콘솔 출력

    // 광고 재시작해서 다시 연결 되도록 설정
    BLEAdvertising *pAdvertising;
    pAdvertising = pServer->getAdvertising();
    pAdvertising->start();
    Serial.println("iBeacon advertising restarted"); // 광고 재시작 메시지 출력
  }
};

// BLE 특성 콜백 클래스
class MyCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) {
    String rxValue = pCharacteristic->getValue(); // 수신된 값 가져오기

    if (rxValue.length() > 0) {
      Serial.println("*********");
      Serial.print("Received Value: ");
      for (int i = 0; i < rxValue.length(); i++) { //수신된 값 하나씩 출력
        Serial.print(rxValue[i]);
      }
      Serial.println();
      Serial.println("*********");
    }
  }
};

// BLE서비스 초기화
void init_service() {
  BLEAdvertising *pAdvertising;
  pAdvertising = pServer->getAdvertising();
  pAdvertising->stop(); // 광고 중지

  // Create the BLE Service
  BLEService *pService = pServer->createService(BLEUUID(SERVICE_UUID));

  // Create a BLE Characteristic
  pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_NOTIFY
  );
  pCharacteristic->setCallbacks(new MyCallbacks()); // 콜백
  pCharacteristic->addDescriptor(new BLE2902()); // BLE2902 디스크립터 추가

  pAdvertising->addServiceUUID(BLEUUID(SERVICE_UUID)); // 광고에 서비스 UUID 추가

  // Start the service
  pService->start();

  pAdvertising->start(); // 광고 시작
}

// 비콘 초기화
void init_beacon() {
  BLEAdvertising *pAdvertising;
  pAdvertising = pServer->getAdvertising();
  pAdvertising->stop();
  // iBeacon
  BLEBeacon myBeacon;
  myBeacon.setManufacturerId(0x4c00);
  myBeacon.setMajor(5);
  myBeacon.setMinor(88);
  myBeacon.setSignalPower(0xc5);
  myBeacon.setProximityUUID(BLEUUID(BEACON_UUID_REV));

  BLEAdvertisementData advertisementData;
  advertisementData.setFlags(0x1A);
  advertisementData.setManufacturerData(myBeacon.getData());
  pAdvertising->setAdvertisementData(advertisementData);

  pAdvertising->start();
}

// ESP32 최초 시작 후 세팅
void setup() {
  Serial.begin(115200);
  setup_wifi(); //공유기 접속
  client.setServer(mqtt_server, 1883); //MQTT서버의 설정을 가져옴
  client.setCallback(callback); //데이터 수신 함수 등록(수신 함수 포인터 등록)
  // setcallback 안에 메세지 수신부 이름이 뭔지 알아오는 부분이고 괄호안에 callback은 line 43의 callback과 같은거, 노칸다가 이거밖에 설명 안해줌

  // 긴급 버튼 셋업
  pinMode(btn_pinNo, INPUT_PULLUP); // 핀을 입력 모드로 설정
  pinMode(buzz_pin, OUTPUT); // 부저 핀을 출력으로 설

  // led 셋업
  pinMode(ledPin, OUTPUT);   // LED 핀을 출력으로 설정
  
  // 압력 센서 셋업
  pinMode(fsrPin0, INPUT);
  pinMode(fsrPin1, INPUT);

  xTaskCreate(led, "EmergencyLED", 2048, NULL, 1, NULL);
  xTaskCreate(fsrWear, "Wear", 4096, NULL, 2, NULL);

  //BLE setup
  BLEDevice::init(DEVICE_NAME);
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  init_service();
  init_beacon();

  Serial.println("iBeacon + service defined and advertising!"); // 완료 메시지
}

// 긴급 버튼 함수
void btnEmer() {
  buttonState = digitalRead(btn_pinNo); // 버튼 상태 확인
  if (buttonState == LOW) { // 버튼이 눌렸을 때
    if (!count) {
      count = !count; // 횟수 카운트 시작
      pushedTime = millis(); // 시간 측정 시작
      Serial.println("지금부터 5초 준다."); // 측정 시작 확인
    }
    pushedCount++; // 누른 횟수 증가
    delay(500);
  }
  if (millis() - pushedTime > timeLeft ) { // 5초 센다
    if (count && pushedCount < 3){
      Serial.println("실수로 눌렀구나."); // 5초 안에 세 번 못 누름
      count = !count; // 횟수 카운트 종료
      pushedCount = 0; // 누른 횟수 초기화
    }
  }
  if (pushedCount >= 3) { // 세 번 누름
    Serial.println("Emergency!"); // 이멀젼시!
    emergency = true;
    statusMsg = String(DEVICE_NAME) +" "+ String(power) +" "+ String(wear) +" "+ String(emergency);
    client.publish("aict/status", statusMsg.c_str());
    xTaskCreate(buzEmr, "EmergencyBuzz", 2048, NULL, 1, NULL);
    //client.publish("status", "UUID: "+ String(DEVICE_NAME) +" Power: " + String(power) +" Emergency: " + String(emergency));
    count = !count; // 횟수 카운트 종료
    pushedCount = 0; // 누른 횟수 초기화
    delay(2000); // 오작동 방지 2초

  }
}

// 긴급시 LED
void led(void *parameter) {
  while (true){
    if (emergency){
      digitalWrite(ledPin, HIGH);
    }
    vTaskDelay(100 / portTICK_PERIOD_MS);
  }
}

// vTaskDelay(100 / portTICK_PERIOD_MS): cpu가 해당 작업을 100ms동안 대기, 그 동안 다른 테스크를 우선 수행할 수 있게 함
// portTICK_PERIOD_MS: 단위가 1ms임을 표기

void buzEmr(void *parameter) {
    if(emergency && pushedCount >= 3){
    // tone(부저 핀번호, 주파수(int형), 시간(ms))
    // tone(부저 핀번호, 주파수(int형),) delay((int형))(ms)
      tone(buzz_pin, ddo);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, rre);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, ppa);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, rra);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, ddo);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      noTone(buzz_pin);
      vTaskDelay(400 / portTICK_PERIOD_MS);

      tone(buzz_pin, rre);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, ppa);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, ssol);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, ssi);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, rre);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      noTone(buzz_pin);
      vTaskDelay(400 / portTICK_PERIOD_MS);

      tone(buzz_pin, rras);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, rra);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, rras);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, rra);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, ppa);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, rra);
      vTaskDelay(200 / portTICK_PERIOD_MS);
      tone(buzz_pin, ssol);
      vTaskDelay(300 / portTICK_PERIOD_MS);
      noTone(buzz_pin);
  }
}

void fsrWear(void *parameter) {
  while(true) {
    Serial.println("fsrWear Loaded.");
    unsigned long startTime = millis();
    int readings = 0;

    // 10초 동안 FSR 값 측정
    while (millis() - startTime < 10000) {  
      int fsrValue0 = analogRead(fsrPin0);
      int fsrValue1 = analogRead(fsrPin1);
      if (fsrValue0 >= 2000 && fsrValue1 >= 2000) {
        readings++;
        Serial.print((String)"FSR0: "+fsrValue0);
        Serial.println((String)" | FSR1: "+fsrValue1);
      }
      vTaskDelay(100 / portTICK_PERIOD_MS); // 100ms 대기
    }
    wear = (readings > 20);
    Serial.println(String(wear));
    statusMsg = String(DEVICE_NAME) +" "+ String(power) +" "+ String(wear) +" "+ String(emergency);
    sensorMsg = String(DEVICE_NAME) +" "+ String(wear);
    client.publish("aict/status", statusMsg.c_str());
    client.publish("aict/sensor", sensorMsg.c_str());
    vTaskDelay(290000 / portTICK_PERIOD_MS); // 290초 대기
  }
}
void loop() {

// ESP32와 MQTT서버간 연결 유지부(삭제 금지)
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // BLE ibeacon
  if (deviceConnected) {
    //Serial.printf("*** NOTIFY: %d ***\n", BLEvalue);
    pCharacteristic->setValue(&BLEvalue, 1);
    pCharacteristic->notify();
    BLEvalue++;
  }
  
  // 긴급 버튼
  btnEmer();
}

