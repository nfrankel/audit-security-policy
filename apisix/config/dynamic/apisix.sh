#!/bin/sh
curl http://apisix:9180/apisix/admin/routes/1 -H 'X-API-KEY: edd1c9f034335f136f87ad84b625c8f1' -X PUT -d '
{
  "uri": "/finance/salary*",
  "upstream": {
    "nodes": {
      "boot:8080": 1
    }
  },
  "plugins": {
    "key-auth": {
      "header": "Authorization"
    },
    "proxy-rewrite": {
      "headers": {
        "set": {
          "X-Account": "$consumer_name"
        }
      }
    }
  }
}'
