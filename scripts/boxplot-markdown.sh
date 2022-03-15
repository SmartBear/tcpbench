#!/usr/bin/env bash

dir=$1
md=$dir/metrics.md
echo "# Metrics" > $md
for svg in $dir/*.svg
do
  url=$(basename $svg)
  name=$(basename $svg .svg | tr '@' '/')
  echo "## [$name](https://github.com/$name)" >> $md
  echo "![$name]($url)" >> $md
  echo >> $md
done
