[![test-java](https://github.com/SmartBear/tcpbench/actions/workflows/test.yaml/badge.svg)](https://github.com/SmartBear/tcpbench/actions/workflows/test.yaml)

# Test Case Prioritization (TCP) Benchmark

A command-line utility that calculates the APTF score for different Test Case Prioritization algorithms and
presents them as a box plot. See [results/rtptorrent/metrics.md](results/rtptorrent/metrics.md) for details.

A [box plot] can be generated for each of the projects in the [RTPTorrent] dataset.

## APTF

The aim of a TCP algorithm is to order the test cases such that the ones that are bound to fail are run earlier in the testing progress.
To quantify this goal we use a new metric to estimate the rate of test failures of a test suite.
This is an adaptation of the [APFD] metric that uses *test failure* rather than *fault detection*.
Following the naming conventions of the [APFD] metric, we call this new metric **Average Percentage of Test Failures** (APTF).

Let `T` be a test suite containing `n` test cases, out of which `m` are failing. 
Let `TFi` be the index of the `i`th failing test on ordering `T′` of `T`.
The APTF of test suite `T′` is given by the equation:

```math
APTF = 1 - {{\sum_{i=1}^m TF_i} \over nm} + {1 \over 2n}
```

High values indicate that the test failures occur early in the testing process while low values indicate the test failures occur later. 
Notice that the APTF metric is equivalent to the APFD metric in the case where each test reveals one different fault.

## Prerequisites

In order to generate the box plot metrics you need:

* GNU Make
* Gnuplot
* Maven
* A Comet API key (optional)
* A build of SmartBear's TCP engine (optional)

## Usage

Download and extract the [RTPTorrent dataset]. Define an environment variable pointing to the RTPTorrent directory:

    export RTP_TORRENT=...

Define Comet environment variables (optional)

    export COMET_URL=https://chiron.comet.smartesting.com
    export COMET_API_KEY=...

Define SmartBear TCP environment variables (optional)

    export SMARTBEAR_TCP_DIR=...

Generate the metrics:

    make

Generate a Markdown file

    ./scripts/boxplot-markdown.sh results/rtptorrent

## How the benchmark works

The `Makefile` runs the `com.smartbear.tcpbench.Main` program once for each combination of `RTPTorrent project` and `TCP algoritm`.
It outputs the [APFD] for each test cycle (git commit) in a single-column CSV file. Example:

```csv
0.20588235294117652
0.20588235294117652
0.9117647058823529
0.4411764705882353
0.5588235294117647
0.8333333333333334
0.5833333333333334
0.9722222222222222
0.6944444444444445
0.9166666666666666
0.5833333333333334
0.6944444444444445
0.13888888888888895
0.7903225806451613
0.7258064516129032
0.6612903225806451
0.9624999999999999
0.35365853658536583
0.7374999999999999
0.3625
0.5375
0.3875
```

Command line options:

* `--training` - the number of test cycles used to do the initial training of the algorithm
* `--prediction` - the number of test cycles we want to generate predictions for
* `--rtptorrent` - the path to the RTPTorrent project directory
* `--engine` - the name of the class interacting with the TCP engine (`implements com.smartbear.tcpbench.TcpEngine`)

Once all the APFD CSVs have been generated, a box plot is generated for each project, comparing the APFD of each algorithm.

## Adding new TCP algorithm

A new TCP algorithm can be added by implementing `com.smartbear.tcpbench.TcpEngine` and updating the `Makefile`
to include this algorithm. See the JavaDocs of `com.smartbear.tcpbench.TcpEngine` for more details.

[RTPTorrent]: https://toni.mattis.berlin/files/2020-preprint-mattis-rtptorrent-msr20.pdf
[box plot]: https://en.wikipedia.org/wiki/Box_plot
[APFD]: https://www.researchgate.net/publication/3187955_Test_Case_Prioritization_A_Family_of_Empirical_Studies
[RTPTorrent dataset]: https://zenodo.org/record/4046180
