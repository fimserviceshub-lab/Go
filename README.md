# Go

This repository has been updated as a Java-based Android app with modernized platform APIs, while keeping the original XML/View approach familiar.

## What Was Updated

- Java 17 toolchain
- Android Gradle Plugin 9.2.0
- `targetSdk` 36 and `compileSdk` 36
- modern location permission flow with Activity Result APIs
- `FusedLocationProviderClient.getCurrentLocation(...)`
- safer `SQLiteOpenHelper` database code
- updated RecyclerView and Google Maps dependencies

## What The App Still Does

- track your current location on demand
- reverse geocode the address
- save favorite places locally
- edit title and note
- share a saved place
- open directions from a saved address

## API Key Setup

The old hard-coded Maps API key has been removed from source. Add this to `local.properties`:

```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```
