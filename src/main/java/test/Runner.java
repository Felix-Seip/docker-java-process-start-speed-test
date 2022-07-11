package test;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.apigee.trireme.core.NodeException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

public class Runner {

    public static void main(String[] args) throws InterruptedException {
        ThreadPoolTaskExecutor taskExecutor = createBlockingAndProcessorLimitedTaskExecutor();
        int counter = 0;
        Stopwatch stopwatch = Stopwatch.createStarted();
        int iterationCount = Integer.parseInt(System.getenv("ITERATION_COUNT"));
        CountDownLatch countDownLatch = new CountDownLatch(iterationCount);
        while (counter < iterationCount) {
            taskExecutor.execute(() -> {
                try {
                    startNodeProcess();
                    countDownLatch.countDown();
                } catch (InterruptedException | IOException | NodeException | ExecutionException | ScriptException |
                         TimeoutException e) {
                    throw new RuntimeException(e);
                }
            });
            counter++;
        }
        countDownLatch.await();

        System.out.printf("Process start speed per %d iterations : %d milliseconds", iterationCount, stopwatch.elapsed().toMillis());
        System.exit(0);
    }

    private static void startNodeProcess() throws InterruptedException, IOException, NodeException, ExecutionException, ScriptException, TimeoutException {
        File file = new File("./node/index.js");

        Process process = new ProcessBuilder("node", file.getName())
                .directory(file.getParentFile())
                .redirectErrorStream(true)
                .start();
        process.waitFor();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            // NOOP
        }
    }

    private static ThreadPoolTaskExecutor createBlockingAndProcessorLimitedTaskExecutor() {
        ThreadPoolTaskExecutor executorService = new ThreadPoolTaskExecutor();
        executorService.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("transform-task-executor-%d").build());
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executorService.setQueueCapacity(0);
        executorService.setMaxPoolSize(10);
        executorService.setCorePoolSize(1);
        executorService.setAwaitTerminationSeconds(30);
        executorService.setKeepAliveSeconds(60);
        executorService.setWaitForTasksToCompleteOnShutdown(true);
        executorService.initialize();
        return executorService;
    }
}
