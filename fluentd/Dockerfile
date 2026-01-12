# ARCHITEKTURA: Obraz Docker dla Fluentd z pluginem do obsługi Elasticsearch.
FROM fluent/fluentd:v1.16-1
USER root
RUN gem install fluent-plugin-elasticsearch --no-doc-format
USER fluent