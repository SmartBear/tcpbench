#!/usr/bin/env bash
set -euf -o pipefail
DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

CYAN='\033[0;36m'
NC='\033[0m'

if [ ! -f "${DIR}/classpath.txt" ] || [ "${DIR}/../pom.xml" -nt "${DIR}/classpath.txt" ]; then
  >&2 echo -e "${CYAN}Writing new classpath file...${NC}"
  mvn dependency:build-classpath -Dmdep.outputFile="${DIR}/classpath.txt" > /dev/null
  touch "${DIR}/classpath.txt"
fi

java \
  -classpath "$(cat "${DIR}/classpath.txt"):${DIR}/../target/classes" \
  com.smartbear.tcpbench.Main $*
