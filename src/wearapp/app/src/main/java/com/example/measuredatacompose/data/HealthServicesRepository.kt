/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.measuredatacompose.data

//import java.net.InetAddress

//import android.net.*
//import android.webkit.*

//import java.io.IOException

//import android.R
//import org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory

import android.content.Context
import android.os.BatteryManager
import android.util.Base64
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.SampleDataPoint
import com.example.measuredatacompose.R
import com.example.measuredatacompose.TAG
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import kotlin.random.Random

import javax.net.ssl.SSLSocket
import java.net.Socket
import java.net.InetAddress

import java.util.Properties


/**
 * Entry point for HealthServicesClient APIs. This also provides suspend functions around
 * those APIs to enable use in coroutines.
 */


class HealthServicesRepository(private val context: Context) {
    private val healthServicesClient = HealthServices.getClient(context)
    private val measureClient = healthServicesClient.measureClient
    private val passiveClient = healthServicesClient.passiveMonitoringClient
    private var lastNetworkCallTime = System.currentTimeMillis()
    private var timeInterval: Long = 30000

    private val broker = "YOUR-ENDPOINT"
    private val clientId = MqttClient.generateClientId()
    private val persistence = MemoryPersistence()

    private val mqttClient: MqttClient = MqttClient(broker, clientId, persistence)

    init {
        Log.i("MqttClient", "Initializing")
        connectToBroker()
    }

    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = measureClient.getCapabilitiesAsync().await()
        Log.i("capabilities","$capabilities")
        return (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure)
    }

    suspend fun hasPassiveCapabilities(): Boolean{
        val capabilities = passiveClient.getCapabilitiesAsync().await()
        Log.i("passiveCapabilities", "$capabilities")
        return true
    }


    /**
     * Returns a cold flow. When activated, the flow will register a callback for heart rate data
     * and start to emit messages. When the consuming coroutine is cancelled, the measure callback
     * is unregistered.
     *
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    fun heartRateMeasureFlow() = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                // Only send back DataTypeAvailability (not LocationAvailability)
                if (availability is DataTypeAvailability) {
                    trySendBlocking(MeasureMessage.MeasureAvailability(availability))
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                val currentTime = System.currentTimeMillis()
                val heartRateBpm = data.getData(DataType.HEART_RATE_BPM)
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val batteryPercentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

                if(currentTime - lastNetworkCallTime >= timeInterval && heartRateBpm.last().value > 0.0){
                    CoroutineScope(Dispatchers.IO).launch {
                        sendDataToMqttBroker(batteryPercentage, heartRateBpm.last().value)
                        lastNetworkCallTime = System.currentTimeMillis()
                        withContext(Dispatchers.Main) {
                        }
                    }
                }

                trySendBlocking(MeasureMessage.MeasureData(heartRateBpm))
            }
        }

        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            Log.d(TAG, "Unregistering for data")
            runBlocking {
                measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
                    .await()
                if (mqttClient.isConnected) {
                    mqttClient.disconnect()
                    Log.i("MqttClient", "Disconnected")
                }
            }
        }

    }

    private fun connectToBroker() {
//        val username = "YOUR_USERNAME"
//        val password = "YOUR_PASSWORD"

        val options = MqttConnectOptions().apply {
             //this.userName = username
            //this.password = password.toCharArray()
            //isCleanSession = true
            //connectionTimeout = 60
            //socketFactory = getSocketFactory()
        }

        mqttClient.setCallback(object : org.eclipse.paho.client.mqttv3.MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.i("MqttClient", "Connection lost", cause)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.i("MqttClient", "Message arrived")
                Log.i("MqttClient", "Topic: $topic")
                Log.i("MqttClient", "Message: ${message.toString()}")
                val jsonMessage = JSONObject(message?.toString())
                if(jsonMessage.has("timeInterval")){
                    timeInterval = jsonMessage.getLong("timeInterval")
                }
            }

            override fun deliveryComplete(token: org.eclipse.paho.client.mqttv3.IMqttDeliveryToken?) {
                Log.i("MqttClient", "Delivery complete")
            }
        })

        if (!mqttClient.isConnected) {
            try{
                Log.i("MqttClient", "Connecting to broker")
                mqttClient.connect(options)
                Log.i("MqttClient", "Connected")
                mqttClient.subscribe("YOUR_TOPIC", 0)
            }
            catch (e: Exception){
                Log.e("MqttClient", "Error connecting to broker")
                Log.e("MqttClient", e.toString())
            }
        }
    }

    suspend fun sendDataToMqttBroker(batteryPercentage: Int, heartRateBpmValue: Double) {
        withContext(Dispatchers.IO) {
            if (!mqttClient.isConnected) {
                Log.i("MqttClient", "Connecting to broker")
                connectToBroker()
            }

            val topic = "YOUR_TOPIC_2"
            val content = "{\\r\\n\\\"bpm\\\": $heartRateBpmValue,\\r\\n\\\"watchBattery\\\": $batteryPercentage\\r\\n}"
            val message = MqttMessage(content.toByteArray(Charsets.UTF_8))
            message.qos = 0

            if(mqttClient.isConnected){
                println("Publishing message: $content")
                mqttClient.publish(topic, message)
                println("Message published")
            }
        }
    }

    private fun getSocketFactory(): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, null, null)
        return object : SSLSocketFactory() {
            override fun getDefaultCipherSuites(): Array<String> = sslContext.socketFactory.defaultCipherSuites
            override fun getSupportedCipherSuites(): Array<String> = sslContext.socketFactory.supportedCipherSuites

            override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
                val socket = sslContext.socketFactory.createSocket(s, host, port, autoClose) as SSLSocket
                socket.enabledProtocols = arrayOf("TLSv1.2")
                val sslParameters = socket.sslParameters
                sslParameters.applicationProtocols = arrayOf("x-amzn-mqtt-ca")
                socket.sslParameters = sslParameters
                return socket
            }

            override fun createSocket(host: String?, port: Int): Socket = createSocket(null, host, port, true)
            override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket = createSocket(null, host, port, true)
            override fun createSocket(host: InetAddress?, port: Int): Socket = createSocket(null, host?.hostName, port, true)
            override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket = createSocket(null, address?.hostName, port, true)
        }
    }
}

sealed class MeasureMessage {
    class MeasureAvailability(val availability: DataTypeAvailability) : MeasureMessage()
    class MeasureData(val data: List<SampleDataPoint<Double>>) : MeasureMessage()
}
