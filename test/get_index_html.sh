#!/bin/bash

msg "retrieve index.html"
assert_status_code "get" "index.html" 200

RES=$(http --check-status --ignore-stdin --timeout=4.5 get $SERVER_PATH/index.html)
diff -w <(echo $RES) <(cat ./public/index.html | tr -s '\n' ' ')
if [ "$?" != "0" ] ; then
  fail "wrong index.html"
else
  ok "right index.html"
fi