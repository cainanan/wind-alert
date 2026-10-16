#!/usr/bin/env bash
set -euo pipefail
cd /home/cainanan/Senra/wind-alert
mkdir -p logs
/usr/bin/java -jar target/wind-alert-0.0.1-SNAPSHOT.jar "$@" >> logs/wind-alert.log 2>&1
