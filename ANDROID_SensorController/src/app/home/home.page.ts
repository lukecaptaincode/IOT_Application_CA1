import {Component} from '@angular/core';
import * as firebase from 'firebase';

@Component({
    selector: 'app-home',
    templateUrl: 'home.page.html',
    styleUrls: ['home.page.scss'],
})
export class HomePage {
    sensors: string[] = ['led', 'buzzer', 'lightsensor', 'soundsensor','tempsensor','rangesensor']; // sensors
    sensorDelays: number [] = [1, 1,1,1]; // sensor delays

    constructor() {
        this.getSensorsStatus(); // Get the status of all sensors on load
    }

    /**
     * reads the current state of the sensors and flips in based on its own value
     * this is independent of state of the state of the toggle in the ui intentionally
     * @param sensor string that describes the sensor url to read then update
     */
    toggleSensor(sensor: string) {
        const sensorData = firebase.database().ref(sensor); // The sensor ref to toggle
        /**
         * Get the value of from the ref once, async promise then pass the data snapshot
         */
        sensorData.once('value').then((snapshot: any) => {
            let isOn = snapshot.val()['isOn']; // get if the sensor is on
            isOn = !isOn; // Flip the sensor value
            /**
             * Update the sensors using a promise
             */
            sensorData.update({isOn}).then(() => {
                console.log('success');
            });
        });
    }

    /**
     * Changes the delay value of the passed sensor using the passed delay data
     * @param sensor the sensor to change
     * @param delay the delay for the sensor
     */
    changeSensorDelay(sensor: string, delay: number) {
        const sensorData = firebase.database().ref(sensor + 'pushRate'); // set the ref to passed sensor
        /**
         * update the pushRate passing the delay as object
         */
        sensorData.update({delay}).then(() => {
            console.log('delay success');
        });
    }

    /**
     * Pulls the value for all sensor status and checks each for the status of the sensor
     * if the sensor is on turn the toggle on
     */
    getSensorsStatus() {
        const sensorStatus = firebase.database().ref('/'); // ref entire db
        /**
         * Read once as and async promise
         */
        sensorStatus.once('value').then((snapshot: any) => {
            for (let i = 0; i < this.sensors.length; i++) { // Every sensor
                const status = snapshot.val()[this.sensors[i]]['isOn']; // get is on value
                const sensor = snapshot.val()[this.sensors[i]]['datatype']; // get what the datatype of the sensor is
                // For every sensor toggle that's on, flip them on
                if (status) {
                    document.getElementById(this.sensors[i] + '-toggle').setAttribute('checked', '');
                }
                // For sensors with scales set the value of the scale
                if (sensor === 'light') {
                    console.log(snapshot.val()[this.sensors[i]]['pushRate']['delay']);
                    this.sensorDelays[0] = snapshot.val()[this.sensors[i]]['pushRate']['delay'];
                }
                if (sensor === 'sound') {
                    this.sensorDelays[1] = snapshot.val()[this.sensors[i]]['pushRate']['delay'];
                }
                if (sensor === 'temp') {
                    this.sensorDelays[2] = snapshot.val()[this.sensors[i]]['pushRate']['delay'];
                }
                if (sensor === 'range') {
                    this.sensorDelays[3] = snapshot.val()[this.sensors[i]]['pushRate']['delay'];
                }

            }
        });
    }
}
