package com.example.droneservice.util;

public interface AppConstantsUtil {
     long BATTERY_LIMIT = 25;
     int DRONE_SERIAL_NUMBER_LIMIT =100;
     int WEIGHT_MAX =500;
     int BATTERY_MAX =100;
     int MEDICATION_CODE_MAX =100;
     int MEDICATION_NAME_MAX =100;
     String MEDICATION_NAME_PATTERN ="^[a-zA-Z0-9_\\-]*";
     String MEDICATION_CODE_PATTERN ="^[A-Z0-9_]*";
}
