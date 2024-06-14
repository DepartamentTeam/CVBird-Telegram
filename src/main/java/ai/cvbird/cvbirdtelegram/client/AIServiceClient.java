package ai.cvbird.cvbirdtelegram.client;

import ai.cvbird.cvbirdtelegram.dto.JobRequest;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service-client", url = "${feign.ai-service.url}")
public interface AIServiceClient {

    @RequestLine("GET /get_job_by_id/")
    String getJobById(@RequestBody JobRequest jobRequest);
}
