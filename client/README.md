### Forecast client

Client library for forecast service.

### Usage

```scala
// forecast second week given first week data
// client chooses service URL based on AOL_ENVIRONMENT 
val client = new ForecastClientImpl()
val forecast = client.forecast(Array(1, 2, 3, 4, 3, 2, 1), 7)
```
