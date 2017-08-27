package com.github.wreulicke.dropwizard.job;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.metrics.impl.NullMetricsFactory;
import com.amazonaws.services.kinesis.model.Record;

import io.dropwizard.lifecycle.Managed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JobProcessorFactory implements Managed, IRecordProcessorFactory {
  private final KinesisClientLibConfiguration configuration;

  private final ExecutorService workerService;

  @Override
  public void start() {
    log.info("job processor start...");
    workerService.submit(build());
  }

  @Override
  public void stop() throws Exception {}

  public Worker build() {
    return new Worker.Builder()
      .config(configuration)
      .recordProcessorFactory(this)
      .execService(workerService)
      .metricsFactory(new NullMetricsFactory())
      .build();
  }

  @Override
  public IRecordProcessor createProcessor() {
    log.info("created processor");

    return new IRecordProcessor() {

      @Override
      public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
        log.info("processor is shutdown reason: {}", reason.name());
        try {
          checkpointer.checkpoint();
        } catch (Exception e) {
          log.error("exception occured.", e);
        }

      }

      @Override
      public void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
        records.stream()
          .forEach(rec -> {
            log.info("partition key: {}", rec.getPartitionKey());
            log.info("encryption type: {}", rec.getEncryptionType());
            log.info("data : {}", new String(rec.getData()
              .array(), StandardCharsets.UTF_8));
            log.info("sequence number: {}", rec.getSequenceNumber());
          });
      }

      @Override
      public void initialize(String shardId) {
        log.info("shard {} is initialized", shardId);
      }
    };
  }

}
