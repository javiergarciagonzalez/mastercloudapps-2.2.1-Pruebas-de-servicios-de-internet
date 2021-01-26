# mastercloudapps-2.2.1-Pruebas-de-servicios-de-internet
Java and nodejs testing.

## Running Java app

``` bash
mvn clean install
mvn test
```

## Running Node app:
``` bash
npm install
npm test
```
 Alternatives:
 ```bash
npm run test:with-logs
// Debug purposes, it will run jest without --silent flag

npm run test:watch
// Development purposes, it uses Jest\'s dev server that implements HMR (Hot module reload).
 ```
