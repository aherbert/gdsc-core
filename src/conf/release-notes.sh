#!/usr/bin/env bash
#
# Print the release notes for a version from the change log.
#
# The notes are the body of the version section in CHANGELOG.md (excluding the
# version heading) and can be used as the description of a GitHub release:
#
#   src/conf/release-notes.sh 2.4 > notes.md
#   gh release create v2.4 --title "GDSC Core 2.4" --notes-file notes.md
#
# Exits with an error if the version is not in the change log.

set -euo pipefail

if [ $# -ne 1 ]; then
  echo "Usage: $0 <version>" >&2
  exit 2
fi

changelog="$(dirname "$0")/../../CHANGELOG.md"

awk -v version="$1" '
  # A version heading: start or end the section
  /^## / {
    if (found) exit
    found = ($2 == version)
    next
  }
  found {
    # Skip leading blank lines; buffer blank lines to drop any trailing blank lines
    if ($0 == "") { if (started) blank++; next }
    started = 1
    for (; blank > 0; blank--) print ""
    print
  }
  END {
    if (!found) {
      print "Version not found in change log: " version > "/dev/stderr"
      exit 1
    }
  }
' "$changelog"
