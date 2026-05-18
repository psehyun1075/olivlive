# Alerts

## service down
- Query: ...
- Trigger test: scale deployment to 0
- Result: email notification received

## http 5xx detected
- Query: ...
- Trigger test: forced 5xx scenario
- Result: alert fired

## pod restart detected
- Query: ...
- Trigger test: pod restart
- Result: alert fired

## catalog live-control dependency failure
- Query: ...
- Trigger test: scale live-control-service to 0
- Result: 502 observed, alert fired