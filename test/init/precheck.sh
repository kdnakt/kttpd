#!/bin/bash

if ! command -v http &>/dev/null ; then
  echo "httpie not installed, 'apt-get install httpie', 'yum install -y httpie' or  'brew install httpie'"
  exit 1
fi

if ! command -v jq &>/dev/null ; then
  echo "jq not installed, 'apt-get install jq', 'yum install -y jq' or  'brew install jq'"
  exit 1
fi
