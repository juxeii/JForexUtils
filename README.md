# JForexUtils

[![Build Status](https://travis-ci.org/juxeii/JForexUtils.svg?branch=master)](https://travis-ci.org/juxeii/JForexUtils)
[![Coverage Status](https://coveralls.io/repos/github/juxeii/JForexUtils/badge.svg?branch=master)](https://coveralls.io/github/juxeii/JForexUtils?branch=master)

JForexUtils is a collection of user-friendly, robust and useful tools for working with the <a href="https://www.dukascopy.com/wiki/#">JForex API</a> from <a href="https://www.dukascopy.com">Dukascopy</a>.

The purpose of this library is to avoid cumbersome boilerplate coding and an easier way of working the API.
Here's a short list of what can be done:

- Order creation, changing and closing in a simple and declarative way
- Creating/combining currencies and instruments from different sources
- Converting amounts from one instrument to the other
- Executing actions and callables on the strategy thread in one line
- Automatic reconnect support for standalone API

For all features and HowTo's see the <a href="https://github.com/juxeii/JForexUtils/wiki">Wiki Home</a>.

## Java compatibility

JForexUtils relies purely on Java 8, so **it is not compatible with Java versions < 8!** 

## Binaries

Currently, JForexUtils is not hosted on MavenCentral or any other online repository(will be done later).
To use the library in your project you need to grab it from <a href="https://github.com/juxeii/JForexUtils/releases">Releases page</a>.
Here you find two versions: 
- the "JForexUtilsUberJar-*.jar file includes all dependencies
- the "JForexUtils-*.jar file has no included dependencies; it exports its packages so that you can use the file in an OSGI environment

## Build

To build:

```
$ git clone git@github.com:juxeii/JForexUtils.git
$ cd JForexUtils/
$ ./gradlew build
```

## Bugs and Discussion

For bugs and discussions refer to [Github Issues](https://github.com/juxeii/JForexUtils/issues).
