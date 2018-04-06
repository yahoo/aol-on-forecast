### Forecast API

Forecast API is a REST service for making time-series forecasting. 
It is suitable for making forecasts that exhibit daily, weekly and yearly
seasonalities. Trends through time can also be detected. For example you 
can use it to forecast number of webpage views for the coming week given 
data for the past month.

### Server

Run using Docker

    docker run --name forecast-api -p=9072:8080 vidible/forecast-api:1.0.0

Or build from source

    mvn install
    mvn build:docker

### Client

A scala client is provided. See the `client` directory.

Alternatively, you can write your own REST client. For a service deployed on 
localhost you can find Swagger docs at `http://localhost:9072/ifs-api`. Here 
is an example call that provides a time-series of 15 historical numbers and
requests the next 7 numbers.

    curl -X POST -H "Content-Type: application/json" -d '{ "timeSeries": [ 0, 1, 2, 3, 2, 1, 0, 0, 1, 2, 3, 2, 1, 0, 0 ], "cannedSets": ["REG-NONE-ADD-AUTO"], "numberForecasts": 7 }' "http://localhost:9072/forecast-api/forecast"

Response:

    {
      "forecast" : [ 1.0000000000000002, 2.0000000000000004, 3.0000000000000004, 2.0000000000000004, 1.0000000000000004, 2.664535259100377E-16, -4.736951571734001E-16 ],
      "selectedCannedSet" : "REG-NONE-ADD-AUTO",
      "time" : 108
    }

### Examples

Here are some forecast scenarios. Solid line shows historical data and dotted lines are forecasts. 

![scenario](https://github.com/vidible/aol-on-forecast/blob/master/client/src/test/resources/forecast-client/daily-seasonal/plot-raw-and-actual.png)

![scenario](https://github.com/vidible/aol-on-forecast/blob/master/client/src/test/resources/forecast-client/daily-seasonal-with-trend/plot-raw-and-actual.png)

![scenario](https://github.com/vidible/aol-on-forecast/blob/master/client/src/test/resources/forecast-client/real-data-video-view-supply-with-trend/plot-raw-and-actual.png)

### License
Forecast API is released under the Apache License, Version 2.0
