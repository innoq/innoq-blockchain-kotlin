curl http://localhost:8080/nodes/register -X POST -d '{"host": "http://localhost:8081"}' -H 'Content-type: application/json'
curl http://localhost:8081/nodes/register -X POST -d '{"host": "http://localhost:8080"}' -H 'Content-type: application/json'
