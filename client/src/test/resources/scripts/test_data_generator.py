# Copyright 2018, Oath Inc.
# Licensed under the terms of the Apache Version 2.0 license.
# See LICENSE file in project root directory for terms.

import matplotlib.pyplot as plt
import numpy as np
import os

OUTPUT_DIR = "../forecast-client/"


def insufficient_data():
    return [3400, 2100]


def sudden_drop():
    return [400, 500, 500, 400, 400, 300, 350, 400, 300, 300, 300, 300, 300, 300, 0, 0, 0, 0]


def weekly_random():
    return [63, 92, 57, 79, 98, 10, 55, 28, 34, 77, 11, 87, 62, 48, 38, 52, 88, 34, 68, 33]


def daily_seasonal():
    half_day = [100.0, 101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0, 109.5, 110.0]
    seasonal_day = half_day + half_day[::-1]
    seasonal_6_days = np.tile(seasonal_day, 6)
    return seasonal_6_days


def real_video_view_supply():
    # Data sampled from Supply Forecast for "AOL Brands" from 2006-09-01 to 2017-03-06
    return [19668440.0, 24873847.0, 27818773.0, 32631934.0, 36677927.0, 39563081.0, 33565713.0,
            31794775.0, 39732729.0, 37053602.0, 36896278.0, 40470464.0, 41759821.0, 28445750.0,
            24682040.0, 32925512.0, 35472280.0, 40497144.0, 40211159.0, 41568115.0, 32957929.0,
            28379756.0, 41831700.0, 42451364.0, 38425549.0, 42139185.0, 47492015.0, 33875888.0,
            29305097.0, 41075337.0, 48524891.0, 55195938.0, 47232801.0, 43792809.0, 28903448.0,
            28593603.0, 36122348.0, 23902979.0, 24465116.0, 25090285.0, 23377300.0, 16457929.0,
            15579258.0, 27597149.0, 27821299.0, 22574927.0, 24487199.0, 26536998.0, 18994434.0,
            13240386.0, 22421672.0, 24241006.0, 22022113.0, 19038456.0, 21904537.0, 15564092.0,
            15519931.0, 18374821.0, 21804044.0, 23052971.0, 23330505.0, 22565482.0, 16745373.0,
            15152948.0, 21069259.0]


def daily_seasonal_trend():
    seasonal_6_days = daily_seasonal()
    trend_addition_6_days = np.arange(0, len(seasonal_6_days)) * 0.1
    seasonal_trend_6_days = np.add(seasonal_6_days, trend_addition_6_days)
    return seasonal_trend_6_days


def weekly_seasonal():
    seasonal_week = [1, 2, 3, 4, 3, 2, 1]
    seasonal_3_weeks = np.tile(seasonal_week, 4)
    return seasonal_3_weeks


def two_weeks_seasonal():
    seasonal_week = [1, 2, 3, 4, 5, 6, 7, 7, 6, 5, 4, 3, 2, 1]
    seasonal_2_weeks = np.tile(seasonal_week, 2)
    return seasonal_2_weeks


def weekly_trend():
    return np.arange(1001, 1018, 1)


def weekly_seasonal_with_trend():
    # rule - ascend for 5 entries, descending for next 2 entries, and so on
    return [101, 102, 103, 104, 105, 104, 103, 104, 105, 106, 107, 108, 107, 106, 107, 108, 109, 110, 111, 110, 109,
            110, 111, 112, 113, 114, 113, 112]


def yearly_seasonal():
    constant_year = np.tile(100, 365)
    seasonal_addition = np.sin(np.arange(0.0, 365.0) * 2 * np.pi / 365) * 10.0
    seasonal_year = np.add(constant_year, seasonal_addition)
    seasonal_3_years = np.tile(seasonal_year, 3)
    return seasonal_3_years


def yearly_seasonal_with_trend():
    seasonal = yearly_seasonal()
    trend_addition = np.arange(0, len(seasonal)) * 0.05
    seasonal_trend = np.add(seasonal, trend_addition)
    return seasonal_trend


def weekly_yearly_seasonal():
    constant_year = np.tile(100, 364 * 3)
    weekly_seasonal_addition = np.tile([10, 11, 12, 13, 12, 11, 10], 52 * 3)
    yearly_seasonal_addition = np.tile(np.sin(np.arange(0.0, 364.0) * 2 * np.pi / 364) * 2, 3)
    seasonal_week_year = np.add(np.add(constant_year, weekly_seasonal_addition), yearly_seasonal_addition)
    return seasonal_week_year


def weekly_yearly_seasonal_trend():
    seasonal_week_year = weekly_yearly_seasonal()
    trend_addition = np.arange(0, len(seasonal_week_year)) * 0.05
    seasonal_week_year_trend = np.add(seasonal_week_year, trend_addition)
    return seasonal_week_year_trend


def plot(data, title, id):
    print("writing: " + id)
    dir = OUTPUT_DIR + id + "/"
    if not os.path.exists(dir):
        os.makedirs(dir)
    with open(dir + "raw-data", 'w') as f:
        for d in data:
            formatted = "{0:.1f}".format(d)
            f.write(formatted + ",\n")
    plt.plot(data)
    fig = plt.gcf()
    fig.set_size_inches(10, 4)
    plt.title(title, fontsize=12)
    plt.savefig(dir + "plot-raw.png")
    plt.gcf().clear()


plot(insufficient_data(), "insufficient data", "insufficient-data")
plot(sudden_drop(), "sudden drop", "sudden-drop")

plot(daily_seasonal(), "daily seasonality", "daily-seasonal")
plot(daily_seasonal_trend(), "daily seasonality with trend", "daily-seasonal-with-trend")

plot(weekly_random(), "random", "weekly-random")
plot(weekly_seasonal(), "weekly seasonality", "weekly-seasonal")
plot(weekly_trend(), "weekly trend", "weekly-trend")
plot(weekly_seasonal_with_trend(), "weekly seasonality with trend", "weekly-seasonal-with-trend")

plot(two_weeks_seasonal(), "two weeks seasonality", "two-weeks-seasonal")

plot(yearly_seasonal(), "yearly seasonality", "yearly-seasonal")
plot(yearly_seasonal_with_trend(), "yearly seasonality with trend", "yearly-seasonal-with-trend")
plot(weekly_yearly_seasonal(), "weekly and yearly seasonality", "weekly-yearly-seasonal")
plot(weekly_yearly_seasonal_trend(), "weekly and yearly seasonality with trend", "weekly-yearly-seasonal-with-trend")

plot(real_video_view_supply(), "real video view supply data", "real-data-video-view-supply-with-trend")
