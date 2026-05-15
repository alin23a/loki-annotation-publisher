package com.loki.annotationpublisher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name                     = "publish-annotation",
        mixinStandardHelpOptions = true,
        version                  = "1.0",
        description              = "Publish a Grafana annotation directly to Loki without the web frontend.",
        sortOptions              = false
)
public final class AnnotationPublisherCli implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(AnnotationPublisherCli.class);

    @Option(
            names       = {"--date", "-d"},
            required    = true,
            description = "Annotation date, e.g. 2024-01-15"
    )
    private String date;

    @Option(
            names       = {"--version", "-v"},
            required    = true,
            description = "Release version, e.g. 1.2.3"
    )
    private String version;

    @Option(
            names       = {"--comment", "-c"},
            required    = true,
            description = "Free-text annotation comment"
    )
    private String comment;

    @Option(
            names        = {"--loki-url"},
            description  = "Loki push base URL. Env fallback: $LOKI_URL. Default: ${DEFAULT-VALUE}",
            defaultValue = "${env:LOKI_URL:-http://localhost:3100}"
    )
    private String lokiUrl;

    @Option(
            names        = {"--app-name"},
            description  = "Value for the 'app' stream label. Env fallback: $LOKI_APP_NAME. Default: ${DEFAULT-VALUE}",
            defaultValue = "${env:LOKI_APP_NAME:-grafana-annotation-publisher}"
    )
    private String appName;

    @Option(
            names        = {"--env"},
            description  = "Value for the 'env' stream label. Env fallback: $LOKI_ENV. Default: ${DEFAULT-VALUE}",
            defaultValue = "${env:LOKI_ENV:-local}"
    )
    private String environment;

    @Option(
            names        = {"--instances", "-i"},
            required     = true,
            split        = ",",
            description  = "One or more 3-digit instance numbers, comma-separated. e.g. --instances 422,423,425"
    )
    private List<Integer> instances;

    public static void main(final String[] args) {
        final int exitCode = new CommandLine(new AnnotationPublisherCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        for (final int instance : instances) {
            if (instance < 100 || instance > 999) {
                logger.error("Invalid instance {} — must be a 3-digit number (100–999)", instance);
                return 1;
            }
        }

        if (comment.length() > 500) {
            logger.warn("Comment is unusually long. length={}", comment.length());
        }

        final Map<String, String> streamLabels  = buildStreamLabels();
        final String              timestampNanos = String.valueOf(Instant.now().toEpochMilli() * 1_000_000L);
        final String              message        = "Grafana annotation published | date=" + date
                                                 + " | version=" + version
                                                 + " | comment=" + comment;
        final String json = buildLokiPayload(streamLabels, timestampNanos, message);

        logger.info("Publishing annotation to Loki at {}. version={}, date={}, instances={}", lokiUrl, version, date, instances);
        logger.debug("Loki payload={}", json);

        final HttpResponse<String> response = sendToLoki(json);
        final int                  status   = response.statusCode();

        if (status >= 200 && status < 300) {
            logger.info("Successfully published annotation. HTTP {}", status);
            return 0;
        } else {
            logger.error("Failed to publish annotation. HTTP {} — {}", status, response.body());
            return 1;
        }
    }

    private Map<String, String> buildStreamLabels() {
        final Map<String, String> labels = new LinkedHashMap<>();
        labels.put("app", appName);
        labels.put("env", environment);
        labels.put("instances", instances.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
        labels.put("type", "grafana-annotation");
        labels.put("version", version);

        return labels;
    }

    private HttpResponse<String> sendToLoki(final String json) throws Exception {
        final HttpClient  client  = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(lokiUrl + "/loki/api/v1/push"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String buildLokiPayload(
            final Map<String, String> labels,
            final String tsNanos,
            final String message) {

        final JsonObject labelsNode = new JsonObject();
        labels.forEach(labelsNode::addProperty);

        final JsonArray valueEntry = new JsonArray();
        valueEntry.add(tsNanos);
        valueEntry.add(message);

        final JsonArray values = new JsonArray();
        values.add(valueEntry);

        final JsonObject streamNode = new JsonObject();
        streamNode.add("stream", labelsNode);
        streamNode.add("values", values);

        final JsonArray streams = new JsonArray();
        streams.add(streamNode);

        final JsonObject root = new JsonObject();
        root.add("streams", streams);

        return root.toString();
    }
}
