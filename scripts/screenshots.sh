#!/usr/bin/env bash
# Captures README screenshots of the four tiles on a running Wear OS emulator (large round).
# The system tile carousel can't be driven reliably through adb on a fresh emulator, so the sample's
# tile gallery (MainActivity) inflates each tile layout with the Wear OS tiles renderer, on fixed data
# and a fixed clock, from the `scene` extra. Each capture must show the tile's title, be the resumed
# activity and pass the blank image check.
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

install_sample

fresh_launch --es scene progress
capture tile-progress "Steps"

fresh_launch --es scene stats
capture tile-stats "Weather"

fresh_launch --es scene shortcuts
capture tile-shortcuts "Quick start"

fresh_launch --es scene countdown
capture tile-countdown "Rain in"
