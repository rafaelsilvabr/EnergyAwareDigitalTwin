import sys
import threading
import time
import json
from utils.command_line_utils import CommandLineUtils
import paho.mqtt.client as paho
from datetime import datetime
import os

class MQTTClientPaho:
    def __init__(self, cmdData):
        self.cmdData = cmdData
        self.received_count = 0
        self.received_all_event = threading.Event()
        self.mode = "medium"

    def classify_bpm(self, bpm):
        if bpm < 60:
            return "low"
        elif 60 <= bpm <= 90:
            return "medium"
        else:
            return "high"


    def on_message(self, client, userdata, message):
        print("[PAHO]Received message '" + str(message.payload) + "' on topic '"
              + message.topic + "' with QoS " + str(message.qos))
        self.received_count += 1
        if self.received_count == self.cmdData.input_count:
            self.received_all_event.set()

        message_str = message.payload.decode("utf-8")
        message_str = message_str.replace("\\r\\n", "\r\n").replace('\\"', '"')
        message_json = json.loads(message_str)

        if self.classify_bpm(message_json["bpm"]) != self.mode:
            self.mode = self.classify_bpm(message_json["bpm"])
            message_action = "{\"timeInterval\": 30000}"
            if self.mode == "low":
                message_action = "{\"timeInterval\": 1000}"
            if self.mode == "medium":
                message_action = "{\"timeInterval\": 30000}"
            if self.mode == "high":
                message_action = "{\"timeInterval\": 1000}"
            self.client.publish("thing/watch/interval", message_action, qos=1)
            print("[PAHO]Publishing message to topic 'thing/watch/interval': {}".format(message_action))

            message_action = json.loads(message_action)
            print(message_action)
            message_action['datetime'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            message_action = json.dumps(message_action)
            self.save_publish_to_file(message_action)


        try:
            message_json['datetime'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            message_with_time = json.dumps(message_json)
            print(f"[PAHO]Message Json:  '{message_with_time}' on topic '{message.topic}' with QoS {message.qos}")
            self.save_message_to_file(message_with_time)
        except json.JSONDecodeError:
            print(f"Unable to parse message payload as JSON: {message_str}")

    def connect(self):
        self.client = paho.Client(client_id=self.cmdData.input_clientId, clean_session=True, protocol=paho.MQTTv311)
        self.client.on_message = self.on_message
        self.client.connect(self.cmdData.input_endpoint, self.cmdData.input_port)
        self.client.loop_start()
        print("[PAHO] Connected!")


    def subscribe(self, topic="thing/watch/message"):
        print("[PAHO]Subscribing to topic '{}'".format(topic))
        self.client.subscribe(topic, qos=2)
        print("[PAHO]Subscribed to topic '{}'".format(topic))

    def publish(self, message_tosend, topic="thing/watch/interval"):
        message_count = self.cmdData.input_count
        message = "{}".format(message_tosend)
        print("[PAHO]Publishing message to topic '{}': {}".format(topic, message))
        self.client.publish(topic, message, qos=1)

    def wait_for_messages(self):
        if self.cmdData.input_count != 0 and not self.received_all_event.is_set():
            print("[PAHO]Waiting for all messages to be received...")
        self.received_all_event.wait()
        print("[PAHO]{} message(s) received.".format(self.received_count))

    def save_message_to_file(self, message):
        with open("received_messages.txt", "a") as file:
            file.write(message + os.linesep)

    def save_publish_to_file(self, message):
        with open("published_messages.txt", "a") as file:
            file.write(message + os.linesep)

    def disconnect(self):
        print("Disconnecting...")
        self.client.loop_stop()
        self.client.disconnect()
        print("Disconnected!")


if __name__ == '__main__':
    cmdData_paho = CommandLineUtils.parse_sample_input_pubsub()
    cmdData_paho.input_endpoint = "YOUR_ENDPOINT"
    cmdData_paho.input_port = "YOUR_PORT"
    cmdData_paho.input_clientId = "YOUR_CLIENT_ID"

    client_paho = MQTTClientPaho(cmdData_paho)
    client_paho.connect()
    client_paho.subscribe()

    client_paho.wait_for_messages()