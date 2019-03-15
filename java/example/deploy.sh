#!/bin/sh
# Deploys to App Engine

set -e
cd $(dirname "$0")/..

gradle build
gcloud beta app deploy --quiet --project=echo-example build/war
