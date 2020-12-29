#!/bin/bash

# initialise script
. ./test/init/functions.sh
. ./test/init/precheck.sh

msg "TEST START: Default Port"

# start kttpd.kexe
export SERVER_PATH=localhost:8080
PID=$( ./build/bin/native/releaseExecutable/kttpd.kexe >/dev/null & echo $! )
msg "started kttpd (PID: $PID)"

# execute test
. ./test/get_index_html.sh

# clean up
cleanUp

ok "TEST FINISHED: Default Port"
