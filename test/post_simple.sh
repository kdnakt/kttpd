#!/bin/bash

msg "post simple form"

RES=$(http --check-status --ignore-stdin --timeout=4.5 --form post $SERVER_PATH/api/simple-post name=E2Etest)
diff -w <(echo "$RES") <(echo "Hello, E2Etest san!")
if [ "$?" != "0" ] ; then
  fail "wrong response"
else
  ok "right response"
fi