# Copyright 2018, Oath Inc.
# Licensed under the terms of the Apache Version 2.0 license.
# See LICENSE file in project root directory for terms.

import matplotlib.pyplot as plt
import sys

OUTPUT_DIR = "../forecast-client/"


def plot(id, historical, forecast):
    forecast_plot = [historical[-1]] + forecast
    plt.plot(range(1, len(historical) + 1), historical, color='green')
    plt.plot(range(len(historical), len(historical) + len(forecast_plot)), forecast_plot, color='blue', linestyle='--')
    fig = plt.gcf()
    fig.set_size_inches(10, 4)
    # plt.title(id, fontsize=12)
    dir = OUTPUT_DIR + id + "/"
    plt.savefig(dir + "plot-raw-and-actual.png")
    plt.gcf().clear()


plot(sys.argv[1], sys.argv[2].split(","), sys.argv[3].split(","))
