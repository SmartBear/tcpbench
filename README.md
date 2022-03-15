# Test Case Prioritization (TCP) Benchmark

A command-line utility that calculates the [APFD] score for different Test Case Prioritization algorithms and
presents them as a [box plot].

A box plot can be generated for each of the projects in the [RTPTorrent] dataset.
See [results/rtptorrent/metrics.md](results/rtptorrent/metrics.md) for details.

## Prerequisites

In order to generate the box plot metrics you need:

* GNU Make
* Gnuplot
* Maven
* A Comet API key (optional)

## Usage

Download and extract the [RTPTorrent dataset]. Define an environment variable pointing to the RTPTorrent directory:

    export RTP_TORRENT=...

Define Comet environment variables (optional)

    export COMET_URL=https://chiron.comet.smartesting.com
    export COMET_API_KEY=...

Generate the metrics:

    make

Generate a Markdown file

    ./scripts/boxplot-markdown.sh results/rtptorrent

## Adding new TCP algorithm

A new TCP algorithm can be added by implementing `com.smartbear.tcpbench.TcpEngine` and updating the `Makefile`
to include this algorithm.

[RTPTorrent]: https://toni.mattis.berlin/files/2020-preprint-mattis-rtptorrent-msr20.pdf
[box plot]: https://en.wikipedia.org/wiki/Box_plot
[APFD]: https://www.researchgate.net/publication/3187955_Test_Case_Prioritization_A_Family_of_Empirical_Studies
[RTPTorrent dataset]: https://zenodo.org/record/4046180
