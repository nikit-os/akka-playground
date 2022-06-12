## How to run:
```
sbt run
```

## Request example:
```
curl --request POST \
  --url http://localhost:8080/upload/7 \
  --header 'Content-Type: text/csv' \
  --data '11, -4, 3, 4, 3, 2
2, 5, 5, 3, 0, 1'
```