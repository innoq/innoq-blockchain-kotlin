curl http://localhost:8080/transactions -X POST -H 'Content-type: application/json' -d "{\"payload\": \"Payload: $(( ( RANDOM % 1000 )  + 1 ))\"}"
