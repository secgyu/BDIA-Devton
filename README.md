# BDIA-Devton 스마트 안전모: 저비용 스마트 산업재해 예방 솔루션 🌐

## 팀명: AICT
## 팀원: 추지우, 김규민, 이승준, 이재민, 한수혁, 한윤성

📜 프로젝트 개요
------------------------
- **목적**: 산업 현장에서의 안전사고 발생 시 작업자의 위치 파악과 구조를 신속히 지원하고, 안전 수칙을 준수하도록 돕는 **저비용 스마트 안전모**를 개발.
- **동기**: 기존의 안전모는 LTE/5G 통신망을 사용하는 고가 장비로, 비용과 유지 관리의 부담이 큽니다. 이를 해결하기 위해 **ESP32와 BLE 통신**을 사용하여 경제적이면서도 실용적인 솔루션을 제공하고자 합니다.

💡 주요 기능
------------------------
- **1. 착용 모니터링**: 
    - 압력 센서를 통해 작업자가 안전모를 착용하고 있는지 여부를 실시간으로 감지.
    - 착용 불량 시 관리자 모니터링 시스템에 경고 표시 🛡️

- **2. 위급 상황 알림**: 
    - 패닉 버튼을 누르면 안전모의 부저와 LED가 활성화되어 구조 요청을 알림.
    - 관리 시스템에 긴급 알림이 표시되어 신속한 구조가 가능 🚨

- **3. 위치 추적**:
    - BLE 통신을 사용하여 근거리에서 작업자의 위치를 파악 가능.
    - 작업자가 사고로 인해 매몰되거나 위치가 불명확할 때, BLE 스캐너를 통해 구조자가 근접 위치를 확인 📍

- **4. 근태 관리**:
    - 안전모 착용 시간과 위치 로그를 통해 작업자의 출퇴근 기록과 근무 상태를 모니터링 🕒

🛠️ 기술 스택
------------------------
- **하드웨어**:
    - **ESP32-WROOM DEVKIT**: BLE 및 Wi-Fi 기능을 활용한 통신 모듈
    - **FSR402 압력 센서**: 안전모 착용 여부 감지
    - **DTP903450 리튬이온 배터리**: 장시간 사용을 위한 전력 공급
    
- **소프트웨어**:
    - **Node-RED** 기반 모니터링 시스템: 관리자용 PC 및 모바일 대시보드를 통한 실시간 데이터 시각화 🌐
    - **MQTT 프로토콜**: BLE 통신을 통해 센서 데이터를 전송하고, 위치를 실시간으로 업데이트
    - **BLE 통신**: 모바일 기기와의 연동을 통해 사고 발생 시 위치 탐색 지원 📲

🔧 시스템 구성 및 사용 방법
------------------------
1. **관리자 시스템**:
    - Node-RED 기반 대시보드에서 작업자의 실시간 상태 및 위치 정보를 모니터링
    - **웹사이트**: https://smartadmin.aictlab.com
    - 로그인 정보: **ID**: admin, **PW**: aict2024!! **코드**: AICT
    
2. **안전모 사용법**:
    - 작업자가 패닉 버튼을 5초 이상 누르면 부저와 LED가 활성화되며, 관리자 페이지에 긴급 알림이 표시됨.
    - BLE 기능을 통해 구조자는 안전모의 BLE 신호를 탐지하여 근거리에 있는 작업자의 위치를 파악 가능.

3. **작업자 및 장비 관리**:
    - 신규 안전모 장치의 BLE ID를 시스템에 등록하여 관리.
    - 작업자 정보는 관리 시스템을 통해 간단히 조회 및 삭제 가능.

🔮 기대 효과
------------------------
- **안전성 향상**: 사고 발생 시 신속한 구조 및 응급 지원 가능 🆘
- **비용 효율성**: BLE 기반의 통신 방식과 오픈소스 프로그램을 활용하여 유지비용 절감 💰
- **근로자 관리**: 근무 시간 기록 및 안전 장비 착용 여부를 모니터링하여 효율적인 근태 관리와 규정 준수 강화 📈
- **확장 가능성**: 다양한 산업 현장(건설, 조선, 제조 등)에 적용 가능하여 산업 재해 예방과 생산성 향상에 기여 🌍

## Contributors
------------------------
- [**추지우** (Project Manager)](https://github.com/So-ko-nyun)
- [**김규민** (Developer)](https://github.com/secgyu)
- [**이승준** (Hardware Specialist)](https://github.com/username3)
- [**이재민** (Soldering)](https://github.com/username4)
- [**한수혁** (Cloud Specialist)](https://github.com/handylan)
- [**한윤성** (Document work)](https://github.com/username6)
