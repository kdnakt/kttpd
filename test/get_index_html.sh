#!/bin/bash

msg "retrieve index.html"
http --check-status --timeout=4.5 get $SERVER_PATH/index.html &>/dev/null
if [ "$?" != "0" ]
then
  fail "get index.html failed"
else
  ok "get index.html success"
fi