# X6 Handheld Scanner

Install the plugin using:

```
cordova plugin add cordova-plugin-ia-x6-scanner
```

Use the plugin in your JS file:

```typescript
window["Alps2Scanner"].getIsInitialized(
  (isInitialized: boolean) => {
    //
  },
  (error: any) => {
    //
  }
);
```

## Functions

`getIsInitialized(successCallback, errorCallback)`

Check if the plugin service is initialized. If false, initialize with _initializeService_.

`uninitializeService(successCallback, errorCallback)`

De-initialize service running on the device. Not used normally.

`initializeService(successCallback, errorCallback)`

Initialize the service running on the device.

`triggerUHFContinuous(successCallback, errorCallback)`

Starts continuous RFID scanning. successCallback called every time an RFID is scanned. Stop with _triggerUHFEnd_.

`triggerUHFSingle(successCallback, errorCallback)`

Starts RFID scanner and stops it after first scan. Cancel with _triggerUHFEnd_.

`triggerUHFEnd(successCallback, errorCallback)`

Ends RFID scanning, whether single or continuous.

`scanBarcode(successCallback, errorCallback)`

Triggers a single barcode scan.
