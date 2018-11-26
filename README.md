
# PlanningAlerts Android Mobile

Find out and have your say about development applications in your area.

This is the source code for the [PlanningAlerts Android Mobile Application](https://www.planningalerts.org.au/) written using Java. The app makes use of data and API's provided by PlanningAlerts.

PlanningAlerts - Mobile is brought to you by L.D'Abreo and made possible by [PlanningAlerts run by the charity OpenAustralia Foundation](http://www.openaustraliafoundation.org.au)


## Development



**Build Passing**
[![Build Status](https://travis-ci.org/ldabreo/planningalerts-mobile.png?branch=master)](https://travis-ci.org/ldabreo/planningalerts-mobile)

**Code Coverage**
[![Code Coverage](https://img.shields.io/codecov/c/github/ldabreo/planningalerts-mobile/master.svg)](https://codecov.io/github/ldabreo/planningalerts-mobile?branch=master)

The source code is Java and the project is built using Gradle in Android Studio.   You will need an Android device to run/test the app.

**Pre-requisites**
Android Play Services SDK level 28 or greater

**Install Dependencies**
 * Install Android Studio - Download from [https://developer.android.com/studio/](https://developer.android.com/studio/)

**Checkout The Project**
 * Fork the project on Github
 * Checkout the project
 * Open the project in Studio

**Setup Server Keys**
* Add your mobile google maps, places and geocoding key "google_google_maps_key" to your gradle.properties file under your home directory /.gradle
* Add your planning alerts API key "oaf_planningalerts_key" to your gradle.properties file under your home directory /.gradle

**Run tests**
* Run local unit tests  `gradlew test`
* append `jacocoTestReport` for coverage report

**Build and Run Project**
* Build a debug APK  `gradlew assembleDebug`
* Build and run on a connected device`gradlew installDebug`

## Contributing
* Fork the project on GitHub.
* Make a topic branch from the master branch.
* Make your changes and write tests.
* Commit the changes without making changes to any files that aren't related to your enhancement or fix.
* Send a pull request against the master branch.

## Support
lara.dabreo@gmail.com

## Attributions

Portions of this project are modifications based on work created and shared by the Android Open Source Project 
and used according to terms described in the Apache 2.0 license.

## License

Apache 2.0 see the LICENSE file for full details.