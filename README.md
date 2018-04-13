[![Build Status](https://travis-ci.org/vidible/aol-on-forecast.svg?branch=master)](https://travis-ci.org/vidible/aol-on-forecast)

## Forecast API

Forecast API is a REST service for making time-series forecasting. 
It is suitable for making forecasts that exhibit daily, weekly and yearly
seasonalities. Trends through time can also be detected. For example you 
can use it to forecast number of webpage views for the coming week given 
data for the past month.

## Quickstart

Run the forecast-api docker image and make a rest call to get forecasts

    docker run --name forecast-api -p=9072:8080 vidible/forecast-api:2.0.0
    curl -X POST -H "Content-Type: application/json" -d '{ "timeSeries": [ 0, 1, 2, 3, 2, 1, 0, 0, 1, 2, 3, 2, 1, 0, 0 ], "cannedSets": ["REG-NONE-ADD-AUTO"], "numberForecasts": 7 }' "http://localhost:9072/forecast-api/forecast"

Expected response

    {
      "forecast" : [ 1.0000000000000002, 2.0000000000000004, 3.0000000000000004, 2.0000000000000004, 1.0000000000000004, 2.664535259100377E-16, -4.736951571734001E-16 ],
      "selectedCannedSet" : "REG-NONE-ADD-AUTO",
      "time" : 108
    }

## Java client

Forecast-API comes with a java based client. It should be straightforward
to write a simple REST client for other languages.
 
##### build.sbt

```scala
    "com.aol.one.reporting" % "forecast-api-client" % INSERT_LATEST_VERSION
```

##### pom.xml
```xml
<dependency>
  <groupId>com.aol.one.reporting</groupId>
  <artifactId>forecast-api-client_2.11</artifactId>
  <version>INSERT_LATEST_VERSION</version>
</dependency> 
```

##### Usage

Example below provides a timeseries with 14 data points and requests forecast for the next 7 data points:

```scala
val client = new ForecastClientImpl("http://localhost:9072/forecast-api/forecast")
val forecast = client.forecast(Array(1, 2, 3, 4, 3, 2, 1, 1, 2, 3, 4, 3, 2, 1), 7)
```

## Docs
Swagger docs can be found at http://localhost:9072/forecast-api. 

## Example scenarios

Here are some forecast scenarios. Solid line shows historical data and dotted lines are forecasts. 

<img src="https://raw.githubusercontent.com/vidible/aol-on-forecast/master/client/src/test/resources/forecast-client/daily-seasonal/plot-raw-and-actual.png" width="400">

<img src="https://raw.githubusercontent.com/vidible/aol-on-forecast/master/client/src/test/resources/forecast-client/daily-seasonal-with-trend/plot-raw-and-actual.png" width="400">

<img src="https://raw.githubusercontent.com/vidible/aol-on-forecast/master/client/src/test/resources/forecast-client/real-data-video-view-supply-with-trend/plot-raw-and-actual.png" width="400">

## Build from source

Server:

    cd server
    mvn install

Client:

    cd client
    sbt compile


## License
Forecast API is released under the Apache License, Version 2.0
