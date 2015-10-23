#!/bin/bash

# Copyright 2015 Fluo authors (see AUTHORS)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if [ -z $WI_HOME ]; then
  echo "WI_HOME=$WI_HOME must be set!"
  exit 1
fi

export DATA_CONFIG=$WI_HOME/conf/data.yml
if [ ! -f $DATA_CONFIG ]; then
  echo "You must create $DATA_CONFIG"
  exit 1
fi

function get_prop {
  echo "`grep $1 $DATA_CONFIG | cut -d ' ' -f 2`"
}

function print_usage {
  echo -e "Usage: load.sh [--build]\n"
  echo "where:"
  echo "  --build   Optionally, force rebuild of jars"
  exit 1
}

hash spark-submit 2>/dev/null || { echo >&2 "Spark must be installed & spark-submit command must be on path.  Aborting."; exit 1; }
hash mvn 2>/dev/null || { echo >&2 "Maven must be installed & mvn command must be on path.  Aborting."; exit 1; }

# Stop if any command after this fails
set -e

BUILD=false
if [ ! -z $1 ]; then
  if [ "$1" == "--build" ]; then
    BUILD=true
  else
    echo "Unknown argument $1"
    exit 1
  fi
fi

export WI_DATA_JAR=$WI_HOME/modules/data/target/webindex-data-0.0.1-SNAPSHOT.jar
if [ "$BUILD" = true -o ! -f $WI_DATA_JAR ]; then
  echo "Building $WI_DATA_JAR"
  cd $WI_HOME
  mvn clean install -DskipTests
fi
export WI_DATA_DEP_JAR=$WI_HOME/modules/data/target/webindex-data-0.0.1-SNAPSHOT-jar-with-dependencies.jar
if [ "$BUILD" = true -o ! -f $WI_DATA_DEP_JAR ]; then
  echo "Building $WI_DATA_DEP_JAR"
  cd $WI_HOME/modules/data
  mvn package assembly:single -DskipTests
fi
