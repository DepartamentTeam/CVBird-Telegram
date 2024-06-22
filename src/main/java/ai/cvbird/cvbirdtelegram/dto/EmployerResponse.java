package ai.cvbird.cvbirdtelegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import feign.form.FormProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployerResponse {

    @NonNull
    @FormProperty("applicant_telegram_id")
    @JsonProperty("applicant_telegram_id")
    String applicantTelegramId;

    @NotNull
    @FormProperty("job_id")
    @JsonProperty("job_id")
    String jobId;
}
