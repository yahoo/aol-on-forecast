/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.aol.one.reporting.forecastapi.client.ITTestUtil._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class ForecastPerformanceTest extends FreeSpec with MockitoSugar with Matchers with BeforeAndAfterEach {

  /**
    * - This test is to check if performance is overly slow. Verify 1second/call.
    * - Expected performance in production: 0.2s-0.5s/call.
    * - We test thread safety too, by running multiple clients in parallel with different expected forecast results.
    */
  "ForecastClient.forecast performance test" - {

    "average 1sec/call" in {

      val performanceTestClients = List(
        new PerformanceClient("client-1", 100, "daily-seasonal", 100),
        new PerformanceClient("client-2", 100, "weekly-seasonal", 100),
        new PerformanceClient("client-3", 100, "yearly-seasonal", 100)
      )

      performanceTestClients.foreach(_.start)
      performanceTestClients.foreach(_.join)
      performanceTestClients.foreach(client => assert(client.success))
    }
  }

  private class PerformanceClient(id: String, calls: Int, scenario: String, expectedCompletionTime: Int) extends Thread {

    var success = false

    override def run() {
      val start = System.nanoTime()
      for (i <- 1 to calls) {
        testForecast(scenario, false)
        println(s"$id : $i/$calls")
      }
      val diff = (System.nanoTime() - start) / (1000 * 1000 * 1000)
      assert(diff <= expectedCompletionTime)
      success = true
    }
  }

}


