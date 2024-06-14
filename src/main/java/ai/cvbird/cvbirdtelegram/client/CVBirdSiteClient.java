package ai.cvbird.cvbirdtelegram.client;

import ai.cvbird.cvbirdtelegram.dto.CVDataDTO;
import ai.cvbird.cvbirdtelegram.dto.StatisticDTO;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cvbird-site", url = "${feign.cvbird-site.url}")
public interface CVBirdSiteClient {

    @RequestLine("GET telegram/get_cvbird_user/{telegramId}")
    StatisticDTO getTelegramUser(@Param("telegramId") String telegramId);

    @RequestLine("GET cv/get_by_telegram_id/{telegramId}")
    CVDataDTO getCVByTelegramId(@Param("telegramId") String telegramId);

}

