import firebase_admin
from firebase_admin import credentials
from firebase import firebase
from datetime import datetime
import time
import threading
import random

# Set to true to use test data or if not using a raspberry pi
isTest = True
if not isTest:
    # import and init sensor handler
    from SensorHandler import SensorHandler

    handler = SensorHandler()
# Use the application default credentials
cred = credentials.Certificate('iot-applications-ca1-firebase-adminsdk-buwd0-423ae91d3b.json')
firebase_admin.initialize_app(cred)  # initialize the app
baseUrl = 'https://iot-applications-ca1.firebaseio.com'  # set the base url for the db
firebase = firebase.FirebaseApplication('https://iot-applications-ca1.firebaseio.com', None)  # init firebase app

# Init and default values
ledStatus = False
buzzerStatus = False
SensorStatus = {'light': {'isOn': False, 'pushRate': {'delay': 4}},
                'sound': {'isOn': False, 'pushRate': {'delay': 4}},
                'temp': {'isOn': False, 'pushRate': {'delay': 4}},
                'range': {'isOn': False, 'pushRate': {'delay': 4}},
                'led': {'isOn': False},
                'buzzer': {'isOn': False}}


# Gets the status of each sensor every five seconds to avoid being blocked by firebase
# @param self
def get_sensor_status():
    # LED Control, gets if led is on or not then activates or deactivates it based on status
    while True:
        try:
            SensorStatus['led'] = firebase.get('/led', None)
            SensorStatus['buzzer'] = firebase.get('/buzzer', None)
            SensorStatus['light'] = firebase.get('/lightsensor', None)
            SensorStatus['sound'] = firebase.get('/soundsensor', None)
            SensorStatus['temp'] = firebase.get('/tempsensor', None)
            SensorStatus['range'] = firebase.get('/rangesensor', None)
            time.sleep(2)  # to save firebase from being bombarded
        except Exception as sensorStatusError:
            print(sensorStatusError)


def digital_Sensor():
    while True:
        # Led
        if SensorStatus['led']['isOn']:
            if not isTest:
                print('led on')
                handler.activate_led(1)
        else:
            if not isTest:
                print('led off')
                handler.activate_led(0)
        # Buzzer
        if SensorStatus['buzzer']['isOn']:
            if not isTest:
                print('buzzer on')
                handler.activate_buzzer(1)
        else:
            if not isTest:
                print('buzzer off')
                handler.activate_buzzer(0)


# Handles the data push to firebase of all analog sensors.
# The rate is determined by pushRate + 1 seconds with a minimum of
# 2 seconds as I dont want to get firebase errors. If we are in test
# mode it generates random data.
# @param self
# @param sensor the sensor to get and push data for
# @param pushRate the rate at which the data should be pushed
def push_Data(sensor, runForever):
    while runForever:  # only run when runForever
        pushRate = SensorStatus[sensor]['pushRate']['delay']  # set the delay to the pulled pushRate delay
        if pushRate == 0 or not pushRate:  # if it is 0 or null
            pushRate = 1  # set it to one
        print(sensor + ' push sleeping for: ' + str(pushRate))  # print
        time.sleep(1 + pushRate)  # sleep for the pushRate +1 this allows us to control the rate at which data is pushed
        if SensorStatus[sensor]['isOn']:  # if the sensor is on
            if isTest:
                SensorData = random.randint(0, 10)  # create random data for testing if test is on
            else:
                if sensor == 'light':
                    SensorData = handler.get_light_level()  # else get the data from the sensor
                if sensor == 'sound':
                    SensorData = handler.get_sound_level()
                if sensor == 'temp':
                    SensorData = handler.get_temp()
                if sensor == 'range':
                    SensorData = handler.get_range()

            #  Post the data to firebase using the passed data and sensor name
            datapost = firebase.post('/data/' + sensor,
                                     data={"data": SensorData, "time": datetime.now()},
                                     params={'print': 'pretty'})

            print(sensor + ' posted: ' + str(datapost))


if __name__ == "__main__":
    try:
        t1 = threading.Thread(target=get_sensor_status)
        t1.start()
        time.sleep(2)  # sleep for 2 seconds to allow data to to be pulled
        t2 = threading.Thread(target=push_Data, args=('light', True))  # Init light sensor data push
        t3 = threading.Thread(target=push_Data, args=('sound', True))  # Init sound sensor data push
        t4 = threading.Thread(target=push_Data, args=('temp', True))  # Init temp sensor data push
        t5 = threading.Thread(target=push_Data, args=('range', True))  # Init range sensor data push
        t6 = threading.Thread(target=digital_Sensor)  # Init range sensor data push
        t2.start()  # Start light sensor thread
        t3.start()  # Start sound sensor thread
        t4.start()  # Start temp sensor thread
        t5.start()  # Start range sensor thread
        t6.start()  # Start range sensor thread
    except Exception as e:
        print('Error: unable to start thread' + str(e))  # Crap pant
