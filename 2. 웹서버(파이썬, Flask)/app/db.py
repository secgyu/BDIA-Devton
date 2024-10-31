import sqlite3
from datetime import datetime, timedelta


# 데이터베이스 연결 함수
def get_db_connection():
    conn = sqlite3.connect("app_data.db")
    conn.row_factory = sqlite3.Row
    return conn


# 테이블 수정 또는 생성 함수
def create_db():
    conn = get_db_connection()
    c = conn.cursor()

    # 테이블 생성 또는 수정
    c.execute(
        """CREATE TABLE IF NOT EXISTS device_status
                 (id INTEGER PRIMARY KEY AUTOINCREMENT,
                  디바이스명 TEXT DEFAULT NULL,
                  전원상태 INT DEFAULT FALSE,
                  착용상태 INT DEFAULT FALSE,
                  동작시간 TEXT DEFAULT NULL,
                  마지막동작시간 TEXT DEFAULT NULL,
                  비상 INT DEFAULT FALSE
                  )"""
    )

    conn.commit()
    conn.close()


# 기기 상태 정보 가져오기 함수
def get_all_device_status():
    conn = get_db_connection()
    c = conn.cursor()
    c.execute("SELECT * FROM device_status")
    devices = c.fetchall()
    conn.close()
    return devices


# 받은 MQTT값 DB에 저장하는 함수
def save_device_status(
    device_id, power_status, wear, active_time, last_time, emergency_icon
):
    query = "INSERT INTO device_status (디바이스명, 전원상태, 착용상태, 동작시간, 마지막동작시간, 비상) VALUES (?, ?, ?, ?, ?, ?)"
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(
        query,
        (
            device_id,
            int(power_status),
            int(wear),
            str(active_time),
            str(last_time),
            int(emergency_icon),
        ),
    )
    conn.commit()
    conn.close()


def update_device_status(device_id, power_status, emergency_icon, current_time):
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM device_status WHERE 디바이스명 = ?", (device_id,))
        existing_device = cursor.fetchone()

        if existing_device:
            # 비상 상태가 변경되면 업데이트
            if int(power_status) == 1:
                cursor.execute(
                    """
                    UPDATE device_status
                    SET 전원상태 = ?, 동작시간 = ?, 비상 = ?
                    WHERE 디바이스명 = ?;
                    """,
                    (
                        int(power_status),
                        str(current_time),
                        str(emergency_icon),
                        device_id,
                    ),
                )
            elif int(power_status) == 0:
                cursor.execute(
                    """
                    UPDATE device_status
                    SET 전원상태 = ?, 착용상태 = ?, 마지막동작시간 = ?, 비상 = ?
                    WHERE 디바이스명 = ?;
                    """,
                    (
                        int(power_status),
                        int(power_status),
                        str(current_time),
                        str(emergency_icon),
                        device_id,
                    ),
                )
        # print
        conn.commit()
    except sqlite3.Error as e:
        print(f"Database error: {e}")
        conn.rollback()
    finally:
        conn.close()


def update_device_sensor(device_id, wear):
    conn = get_db_connection()
    cursor = conn.cursor()
    print("Fuck111")
    try:
        cursor.execute("SELECT * FROM device_status WHERE 디바이스명 = ?", (device_id,))
        existing_device = cursor.fetchone()
        if existing_device:
            # 비상 상태가 변경되면 업데이트
            cursor.execute(
                """
                UPDATE device_status
                SET 착용상태 = ?
                WHERE 디바이스명 = ?;
                """,
                (
                    int(wear),
                    device_id,
                ),
            )
            print(f"Updated wear status to {wear} for device {device_id}")

        else:
            print("No Device here")

        conn.commit()
    except sqlite3.Error as e:
        print(f"Database error: {e}")
        conn.rollback()
    finally:
        conn.close()


def check_exist(device_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM device_status WHERE 디바이스명 = ?", (device_id,))
        existing_device = cursor.fetchone()
        if existing_device:
            return True
        else:
            return False

    except sqlite3.Error as e:
        print(f"Database error: {e}")
        conn.rollback()
    finally:
        conn.close()


# 기기 삭제 처리 함수
def delete_device(device_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("DELETE FROM device_status WHERE id = ?", (device_id,))
        conn.commit()
    except sqlite3.Error as e:
        print(f"Database error: {e}")
        conn.rollback()
    finally:
        conn.close()


# 실행
create_db()
