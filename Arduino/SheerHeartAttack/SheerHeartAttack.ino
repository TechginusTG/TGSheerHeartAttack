#include <BluetoothSerial.h>

// --- 나중에 실제 핀 번호로 수정할 부분 ---
// 모터 드라이버 (L298N 또는 유사 드라이버 기준)
#define MOTOR_A_IN1 0 // 왼쪽 바퀴 정방향
#define MOTOR_A_IN2 0 // 왼쪽 바퀴 역방향
#define MOTOR_B_IN1 0 // 오른쪽 바퀴 정방향
#define MOTOR_B_IN2 0 // 오른쪽 바퀴 역방향

// 열 감지 센서 (예: MLX90614 또는 AMG8833)
#define THERMAL_SENSOR_PIN 0 // 혹은 I2C 핀 (SDA, SCL)

// 사운드 재생 모듈 (예: DFPlayer Mini)
#define SOUND_TRIGGER_PIN 0

// --- 전역 변수 ---
BluetoothSerial SerialBT;

// 작동 모드 관리 (true: 자동 모드, false: 수동 제어 모드)
bool isAutoMode = true; 

// --- 기본 제어 함수 ---

void move_forward() {
  Serial.println("Action: Forward");
  digitalWrite(MOTOR_A_IN1, HIGH);
  digitalWrite(MOTOR_A_IN2, LOW);
  digitalWrite(MOTOR_B_IN1, HIGH);
  digitalWrite(MOTOR_B_IN2, LOW);
}

void move_backward() {
  Serial.println("Action: Backward");
  digitalWrite(MOTOR_A_IN1, LOW);
  digitalWrite(MOTOR_A_IN2, HIGH);
  digitalWrite(MOTOR_B_IN1, LOW);
  digitalWrite(MOTOR_B_IN2, HIGH);
}

void rotate_clockwise() {
  Serial.println("Action: Rotate Clockwise");
  digitalWrite(MOTOR_A_IN1, HIGH);
  digitalWrite(MOTOR_A_IN2, LOW);
  digitalWrite(MOTOR_B_IN1, LOW);
  digitalWrite(MOTOR_B_IN2, HIGH);
}

void rotate_counter_clockwise() {
  Serial.println("Action: Rotate Counter-Clockwise");
  digitalWrite(MOTOR_A_IN1, LOW);
  digitalWrite(MOTOR_A_IN2, HIGH);
  digitalWrite(MOTOR_B_IN1, HIGH);
  digitalWrite(MOTOR_B_IN2, LOW);
}

void stop_all_movement() {
  Serial.println("Action: Stop");
  digitalWrite(MOTOR_A_IN1, LOW);
  digitalWrite(MOTOR_A_IN2, LOW);
  digitalWrite(MOTOR_B_IN1, LOW);
  digitalWrite(MOTOR_B_IN2, LOW);
}

void play_sound() {
  Serial.println("Sound: Kocchi wo miro!");
  // 사운드 모듈을 제어하는 코드를 여기에 추가합니다.
  // 예: digitalWrite(SOUND_TRIGGER_PIN, HIGH); delay(100); digitalWrite(SOUND_TRIGGER_PIN, LOW);
}


// --- 모드별 작동 함수 ---

void handle_bluetooth_command() {
  if (SerialBT.available()) {
    char cmd = SerialBT.read();
    
    // 수동 제어 명령이 들어오면 자동 모드를 해제
    isAutoMode = false; 

    switch (cmd) {
      case 'F': // Forward
        move_forward();
        break;
      case 'B': // Backward
        move_backward();
        break;
      case 'R': // Clockwise Rotation
        rotate_clockwise();
        break;
      case 'L': // Counter-Clockwise Rotation
        rotate_counter_clockwise();
        break;
      case 'S': // Stop
        stop_all_movement();
        break;
      case 'K': // "Kocchi wo miro"
        play_sound();
        break;
      case 'A': // Auto-mode on
        isAutoMode = true;
        Serial.println("Mode: Automatic");
        break;
    }
  }
}

void automatic_mode() {
  // 여기에 열 감지 로직을 구현합니다.
  // bool heatDetected = readThermalSensor();
  bool heatDetected = false; // 임시 값

  if (heatDetected) {
    // 1. 모든 움직임을 멈춘다.
    stop_all_movement();
    
    // 2. "이쪽을 봐라" 소리를 재생한다.
    play_sound();
    delay(2000); // 소리가 끝날 때까지 잠시 대기
    
    // 3. 감지된 방향으로 전진한다.
    move_forward();
    
    // 목표에 도달할 때까지 잠시 자동 모드를 비활성화할 수 있습니다.
    // isAutoMode = false;
    
  } else {
    // 열이 감지되지 않으면 제자리에서 회전하며 스캔합니다.
    // 여기서는 바퀴를 이용한 느린 회전으로 대체합니다.
    rotate_clockwise();
    delay(100); // 회전 속도 조절
    stop_all_movement();
    delay(500);
  }
}

// --- 메인 함수 ---

void setup() {
  Serial.begin(115200);
  Serial.println("Sheer Heart Attack initializing...");

  // 모터 핀 모드 설정
  pinMode(MOTOR_A_IN1, OUTPUT);
  pinMode(MOTOR_A_IN2, OUTPUT);
  pinMode(MOTOR_B_IN1, OUTPUT);
  pinMode(MOTOR_B_IN2, OUTPUT);
  
  // 사운드 모듈 핀 설정
  pinMode(SOUND_TRIGGER_PIN, OUTPUT);
  digitalWrite(SOUND_TRIGGER_PIN, LOW);
  
  // 블루투스 시작
  SerialBT.begin("SheerHeartAttack"); 
  Serial.println("Bluetooth device is ready to pair.");

  stop_all_movement(); // 시작 시 정지 상태 유지
}

void loop() {
  // 블루투스 명령이 있는지 확인
  if (SerialBT.available()) {
    handle_bluetooth_command();
  }
  
  // 자동 모드가 활성화되어 있으면 자동 모드 실행
  if (isAutoMode) {
    automatic_mode();
  }
  
  // 수동 제어 후 일정 시간이 지나면 다시 자동 모드로 복귀하는 로직 (선택 사항)
  /*
  static unsigned long lastCommandTime = 0;
  if (!isAutoMode && SerialBT.available() == 0) {
      if(millis() - lastCommandTime > 5000) { // 5초간 명령 없으면
          isAutoMode = true;
          Serial.println("Mode: Automatic (Timeout)");
      }
  } else {
      lastCommandTime = millis();
  }
  */
}