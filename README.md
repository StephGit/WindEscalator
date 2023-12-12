# WindEscalator App

[![Build app](https://github.com/StephGit/WindEscalator/actions/workflows/push.yml/badge.svg)](https://github.com/StephGit/WindEscalator/actions/workflows/push.yml)

Simple wind alerting app for Android in Kotlin.

##### Table of Contents
[What is it?](#what-is-it)<br>
[Technologies](#technologies)<br>
[Getting Started](#getting-started)<br>


## What is it?

Android-App to alert driven windsurfers in Thun when the local Ober-Wind kicks in.


## Technologies

- Android SDK v33 (min v26)
- Kotlin 1.8.0
- Dagger 2.33
- Lifecycle 2.4.1
- Room 2.5.1 

## Getting Started

The app is not released yet, so you have to build a debug apk. 
Download the app by cloning this repository and use the `gradlew installDebug` command to build and install the project directly on your connected device or running emulator.

### Connect your device 

Follow these steps to connect your device:

1. Connect your device by USB
2. Enable 'Developer options > USB debugging' on your device (Developer options is hidden by default. To make it available, go to 'Settings > About phone' and tap 'Build number' seven times)
3. Now you should see your device with `adb device -l` (if not use `adb usb` to activate usb-connection)
> optional connection by wifi
4. Activate Wifi-Connection
5. `adb tcpip 5555`
6. `adb connect <XXX.XXX.X.XXX>:5555` > add the ip-adress of your device ('Settings > About phone > Status')
7. Enjoy


### BrainDump

Simplyfied AlertHandling

```mermaid
flowchart TD
    AlertFragment --renders alertEntries--> AlertRecyclerAdapter
    AlertRecyclerAdapter -- activate alert --> AlertRecyclerAdapter 
    AlertRecyclerAdapter --adds active alert--> AlarmHandler 
    AlarmHandler --creates --> AlarmBroadcastReceiver
    AlarmBroadcastReceiver -- enqueues work --> AlertJobIntentService
    AlertJobIntentService <-- checks if something is firing --> WindDataHandler
    AlertJobIntentService -- on fire, creates --> AlertBroadcastReceiver
    AlertBroadcastReceiver -- starts --> AlertNotificationActivity
    AlertNotificationActivity -- triggers --> NoiseHandler
    WindDataHandler <-- network calls --> WindResources 
    AlarmHandler -- calculates next alarm --> AlarmHandler
    BootBroadcastReciever -- inits alarms on boot --> AlarmHandler
    
```