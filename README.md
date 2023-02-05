# Departure Widget for PATH

This repo contains my Android App providing a widget for departures of the [PATH train](https://www.panynj.gov/path/en/index.html) which connects New York City to nearby areas of New Jersey. I built this partly to demonstrate the capabilities of the new [Jetpack Glance library](https://android-developers.googleblog.com/2021/12/announcing-jetpack-glance-alpha-for-app.html), to which I've contributed, and partly because I just wanted to know when the next damn train was coming.

## Libraries and technologies used

The data source is the same as the RidePath app: https://www.panynj.gov/bin/portauthority/ridepath.json. 
See also [Matthew Razza's API](https://github.com/mrazza/path-data), which may be useful depending on
your use case.

I also used these fabulous open source libraries:

- [Retrofit](https://square.github.io/retrofit/) to turn the API into a Kotlin interface
- [Jake Wharton's Converter](https://github.com/JakeWharton/retrofit2-kotlinx-serialization-converter) for using Retrofit with Kotlin serialization
- [Kotlin serialization](https://github.com/Kotlin/kotlinx.serialization/) to create data classes that can be serialized from the API and for serializing data classes to storage
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the app UI

and of course, the star of the show,

- [Jetpack Glance](https://android-developers.googleblog.com/2021/12/announcing-jetpack-glance-alpha-for-app.html) to create the widget UI

<a href='https://play.google.com/store/apps/details?id=com.sixbynine.transit.path&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>
