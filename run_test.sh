#!/bin/bash

# initialise script
. ./test/init/functions.sh
. ./test/init/precheck.sh

msg "TEST START"

# start kttpd.kexe
export SERVER_PORT=8888
export SERVER_PATH=localhost:$SERVER_PORT
PID=$( ./build/bin/native/releaseExecutable/kttpd.kexe --port $SERVER_PORT >/dev/null & echo $! )
msg "started kttpd (PID: $PID)"

# execute all the tests
for f in ./test/*.sh; do
    . "$f"
done

# clean up
cleanUp

ok "TEST FINISHED"
