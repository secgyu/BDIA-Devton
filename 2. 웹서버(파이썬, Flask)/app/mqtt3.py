import paho.mqtt.client as mqtt
from db import *
import time

# MQTT 브로커 설정
MQTT_BROKER = "smartadmin.aictlab.com"  # 브로커 주소 (localhost 또는 IP 주소)
MQTT_PORT = 1883  # MQTT 포트
MQTT_SENSOR = "aict/sensor"  # 구독할 센서 토픽: 디바이스명, 착용상태
MQTT_STATUS = "aict/status"  # 구독할 상태 토픽: 디바이스명, 전원상태, 착용상태, 비상


# MQTT 메시지 수신 콜백 함수
def on_message_status(client, userdata, msg):
    if msg.topic == MQTT_STATUS:
        try:
            print(msg.topic)
            device_id, power_status, wear, emergency_icon = msg.payload.decode().split()
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M")
            if check_exist(device_id):
                update_device_status(
                    device_id, power_status, emergency_icon, current_time
                )
                print("running update_status")

            else:
                save_device_status(
                    device_id,
                    power_status,
                    wear,
                    current_time,
                    "0",
                    emergency_icon,
                )
                print("running save_status")
        except ValueError as e:
            print(
                f"ValueError: {e} - Received unexpected message format: {msg.payload.decode()}"
            )
        except Exception as e:
            print(f"Unexpected error: {e}")
    elif msg.topic == MQTT_SENSOR:
        try:
            print(msg.topic)
            device_id, wear = msg.payload.decode().split()
            update_device_sensor(device_id, wear)
        except ValueError as e:
            print(
                f"ValueError: {e} - Received unexpected message format: {msg.payload.decode()}"
            )
        except Exception as e:
            print(f"Unexpected error: {e}")


mqtt_client = mqtt.Client()


# MQTT 연결 시 호출되는 콜백 함수
def on_connect(client, userdata, flags, rc):
    print(f"Connected with result code {rc}")
    # 연결 후 구독 시작
    # client.subscribe(MQTT_TOPIC)
    client.subscribe([(MQTT_STATUS, 1), (MQTT_SENSOR, 1)])
    # client.subscribe([("sensor", 0), ("battery", 0), ("power", 0), ("all", 0)])


mqtt_client.on_connect = on_connect
mqtt_client.on_message = on_message_status

# MQTT 브로커에 연결
mqtt_client.connect(MQTT_BROKER, MQTT_PORT)

# MQTT 클라이언트 루프 시작
mqtt_client.loop_start()

# 프로그램이 종료되지 않도록 대기
try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    mqtt_client.loop_stop()
    mqtt_client.disconnect()
