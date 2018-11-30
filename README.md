
# PlanningAlerts Android Mobile

<table>
  <tr>
    <td>
      Build Status
    </td>
    <td>
      <a href="https://travis-ci.org/ldabreo/planningalerts-mobile">
        <img src="https://travis-ci.org/ldabreo/planningalerts-mobile.png?branch=master" alt="Build Status" />
      </a>
    </td>
  </tr>
   <tr>
      <td>
        Code Quality
      </td>
      <td>
        <a href="https://codeclimate.com/github/ldabreo/planningalerts-mobile">
          <img src="https://codeclimate.com/github/ldabreo/planningalerts-mobile/badges/gpa.svg" alt="Code Climate" />
        </a>
      </td>
    </tr>
  <tr>
    <td>
      Code Coverage
    </td>
    <td>
      <a href="https://codecov.io/github/ldabreo/planningalerts-mobile">
        <img src="https://img.shields.io/codecov/c/github/ldabreo/planningalerts-mobile/master.svg" alt="CodeCov" />
      </a>
    </td>
  </tr>

  <tr>
    <td>
      Other
    </td>
    <td>
      <a href="https://forthebadge.com/">
        <img src="https://forthebadge.com/images/badges/made-with-java.svg" alt="Made with Java" />
      </a>
    </td>
    <td>
          <a href="https://forthebadge.com/">
            <img src="https://forthebadge.com/images/badges/built-for-android.svg" alt="Made for Android" />
          </a>
    </td>
  </tr>
</table>

## Introduction

Find out and have your say about development applications in your area.

This is the source code for the PlanningAlerts Android Mobile Application written using Java. The app makes use of data and API's provided by [PlanningAlerts](https://www.planningalerts.org.au/).

PlanningAlerts - Mobile is brought to you by L.D'Abreo and made possible by PlanningAlerts, run by the charity [OpenAustralia Foundation](http://www.openaustraliafoundation.org.au)

## Where to get the app


## Development

The source code is Java and the project is built using Gradle in Android Studio.   You will need an Android device to run/test the app.

**Pre-requisites**
Android Play Services SDK level 28 or greater

**Install Dependencies**
 * Install Android Studio - Download from [https://developer.android.com/studio/](https://developer.android.com/studio/)

**Checkout The Project**
* Clone the project on GitHub. 
`git clone https://github.com/ldabreo/planningalerts-mobile.git`
 * Open the project in Studio

**Setup Server Keys**

Both the planning Alerts server and the Google APIs require keys that must be requested from
PlanningAlerts and Google respectively by you. These keys are not stored with the project source code 
but rather supplied by the build user's gradle.properties file at build time (or as an env var if in TravisCI). 
See the  .._api xml files for more details.

Note that the Google geo-coding service requires billing to be activated against your key's Google account.
* No google maps key means your map will be blank. 
* No PlanningAlerts key means you will not be able to fetch alerts from the PlanningAlerts server. 

To add your keys:

* Add your mobile google maps, places and geocoding key "google_google_maps_key" to your gradle.properties file under your home directory /.gradle
`google_google_maps_key="AIza..."`
* Add your planning alerts API key "oaf_planningalerts_key" to your gradle.properties file under your home directory /.gradle
`oaf_planningalerts_key="..."`

**Run tests**
* Run local unit tests  `gradlew test`
* append `jacocoTestReport` for coverage report

**Build and Run Project**
* Build a debug APK  `gradlew assembleDebug`
* Build and run on a connected device`gradlew installDebug`

## Setup/Contributing

* Fork/Clone the project on GitHub. 
`git clone https://github.com/ldabreo/planningalerts-mobile.git`
`cd planningalerts-mobile`
* Make a topic branch from the master branch.
`git branch <branch name>`
`git checkout <branch name>`
* Make your changes and write tests.
* Commit the changes without making changes to any files that aren't related to your enhancement or fix.
`git commit -a -m 'your comments'`
* Send a pull request against the master branch.
`git request-pull master ./`

## Attributions

Portions of this project are modifications based on work created and shared by the Android Open Source Project 
and used according to terms described in the Apache 2.0 license.

## License

Apache 2.0 see the LICENSE file for full details.