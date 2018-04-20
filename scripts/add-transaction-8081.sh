curl http://localhost:8081/transactions -X POST -H 'Content-type: application/json' -d "{\"payload\": \"Payload: $(( ( RANDOM % 1000 )  + 1 ))\"}"
