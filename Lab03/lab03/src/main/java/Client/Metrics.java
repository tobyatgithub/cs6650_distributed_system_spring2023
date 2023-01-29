package Client;

import java.util.ArrayList;
import java.util.List;

public class Metrics {
    private List<Long> latencyList = new ArrayList<>();
    private int requestCount = 0;
    private int failCount = 0;
    private int successCount = 0;

    public Metrics(List<Long> latencyList, int requestCount, int failCount, int successCount) {
        this.latencyList = latencyList;
        this.requestCount = requestCount;
        this.failCount = failCount;
        this.successCount = successCount;
    }

    public void add(long latency) {
        latencyList.add(latency);
    }

    public void requestAdd() {
        requestCount++;
    }

    public void successAdd() {
        successCount++;
    }

    public void failAdd() {
        failCount++;
    }

    public List<Long> getLatencyList() {
        return latencyList;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public int getLatencySum() {
        int latencySum = 0;
        for (int i = 0; i < latencyList.size(); i++) {
            latencySum += latencyList.get(i);
        }
        return latencySum;
    }
}
