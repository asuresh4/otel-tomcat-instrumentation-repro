# OpenTelemetry Java Tomcat instrumentation
Reproduces potential issue with OpenTelemetry Java instrumentation where it does not always capture exception stack trace.

## Steps to run

### Download splunk-otel-javaagent

Download the instrumentation JAR from https://github.com/signalfx/splunk-otel-java/tags and place it in the project directory.

### Build the Maven project

```
mvn clean install
```

### Run the application on Docker

Use the following command.

```
docker run --rm -p 8080:8080 \
    -v $PWD/target/helloworld-servlet.war:/usr/local/tomcat/webapps/helloworld-servlet.war \
    -v $PWD/splunk-otel-javaagent.jar:/splunk-otel-javaagent.jar \
    -e CATALINA_OPTS="-javaagent:/splunk-otel-javaagent.jar -Dotel.traces.exporter=console,otlp -Dotel.logs.exporter=none -Dotel.metrics.exporter=none" \
    -e OTEL_EXPORTER_OTLP_TRACES_PROTOCOL="http/protobuf" \
    -e OTEL_EXPORTER_OTLP_TRACES_ENDPOINT="https://ingest.<REALM>.signalfx.com/v2/trace/otlp" \
    -e SPLUNK_ACCESS_TOKEN="<TOKEN>" \
    tomcat:9.0
```

## Observations

The applications spins up 3 endpoints.

1. http://localhost:8080/helloworld-servlet/hello?error=runtime
2. http://localhost:8080/helloworld-servlet/hello?error=custom
3. http://localhost:8080/helloworld-servlet/hello?error=custom-updated-span


http://localhost:8080/helloworld-servlet/hello?error=runtime - triggers a runtime exception
http://localhost:8080/helloworld-servlet/hello?error=custom - triggers a custom exception that’s handled in the application but response.setStatus(500); is set.
http://localhost:8080/helloworld-servlet/hello?error=custom-updated-span - triggers a custom exception just like in (2) but manually record exception by getting the current span.

The instrumentation records the stack trace from the exception for case (1). However, it did not record the exception automatically for case (2).

It appears, it's required to manually record the stack trace to capture any exceptions handled internally by the application.

For example traces from each of the above scenarios, see [examples](./examples).
