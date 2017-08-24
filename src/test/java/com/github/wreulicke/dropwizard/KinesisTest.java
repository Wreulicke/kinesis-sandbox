package com.github.wreulicke.dropwizard;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.metrics.impl.NullMetricsFactory;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KinesisTest {
	ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	
	@Test
	public void test() throws Exception {
		System.setProperty("com.amazonaws.sdk.disableCbor", "1");
		CyclicBarrier barrier = new CyclicBarrier(6);
		CyclicBarrier processBarrier = new CyclicBarrier(2);
		IRecordProcessorFactory factory = () -> {
			return new IRecordProcessor() {
				
				@Override
				public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
					log.info("test: shutdown");
				}
				
				@Override
				public void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
					log.info("test: processRecords");
					records.stream().forEach(r -> log.info(r.toString()));
					try {
						processBarrier.await();
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					} catch (BrokenBarrierException e) {
						throw new AssertionError(e);
					}
				}
				
				@Override
				public void initialize(String shardId) {
					try {
						barrier.await();
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					} catch (BrokenBarrierException e) {
						throw new AssertionError(e);
					}
					log.info("test: initialize {}", shardId);
				}
			};
		};
		
		AWSStaticCredentialsProvider dummyProvider =
				new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "dummy"));
		
		KinesisClientLibConfiguration config =
				new KinesisClientLibConfiguration("testApp", "testStream", dummyProvider, "testWorker")
					.withInitialPositionInStream(InitialPositionInStream.LATEST)
					.withKinesisEndpoint("http://localhost:4568/")
					.withDynamoDBEndpoint("http://localhost:4569/");
		AmazonKinesis kinesis = AmazonKinesisClientBuilder.standard().withCredentials(dummyProvider)
			.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4568", null))
			.build();
		
		kinesis.createStream("testSteram", 5).getSdkResponseMetadata();
		String key = RandomStringUtils.randomAlphanumeric(10);
		String data = "{\"key_vvv\":\"test\"}";
		
		Worker worker = new Worker.Builder()
			.config(config)
			.recordProcessorFactory(factory)
			.metricsFactory(new NullMetricsFactory()).build();
		executorService.submit(worker);
		barrier.await();
		
		PutRecordRequest recordRequest =
				new PutRecordRequest();
		recordRequest.setStreamName("testStream");
		recordRequest.setData(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)));
		recordRequest.setPartitionKey(key);
		kinesis.putRecord(recordRequest);
		
		executorService.submit(worker);
		processBarrier.await();
		
		kinesis.deleteStream("testSteram").getSdkResponseMetadata();
	}
	
}
