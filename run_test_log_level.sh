#!/bin/bash

# initialise script
. ./test/init/functions.sh
. ./test/init/precheck.sh

function testLogLevel () {
  msg "TEST START: Log Level ($1)"

  # start kttpd.kexe
  export SERVER_PATH=localhost:8080
  PID=$( ./build/bin/native/releaseExecutable/kttpd.kexe --logLevel $1>/dev/null & echo $! )
  msg "started kttpd (PID: $PID)"

  # execute test
  . ./test/get_index_html.sh

  # clean up
  cleanUp

  ok "TEST FINISHED: Log Level ($1)"
}

testLogLevel "debug"
testLogLevel "info"
testLogLevel "error"
