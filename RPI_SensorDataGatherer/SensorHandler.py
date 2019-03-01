import grovepi


class SensorHandler:
    # sensor ports
    light_sensor = 0
    sound_sensor = 1
    temp_sensor = 2
    led = 3
    buzzer = 2
    rangefinder = 4

    def __init__(self):
        grovepi.pinMode(self.light_sensor, "INPUT")
        grovepi.pinMode(self.sound_sensor, "INPUT")
        grovepi.pinMode(self.temp_sensor, "INPUT")
        grovepi.pinMode(self.led, "OUTPUT")
        grovepi.pinMode(self.buzzer, "OUTPUT")

    def activate_led(self, status):
        grovepi.digitalWrite(self.led, status)

    def activate_buzzer(self, status):
        grovepi.digitalWrite(self.buzzer, status)

    def get_light_level(self):
        return grovepi.analogRead(self.light_sensor)

    def get_sound_level(self):
        return grovepi.analogRead(self.sound_sensor)

    def get_temp(self):
        return grovepi.analogRead(self.temp_sensor)

    def get_range(self):
        return grovepi.ultrasonicRead(self.rangefinder)
