package ai.cvbird.cvbirdtelegram.client;

import ai.cvbird.cvbirdtelegram.dto.CVDataDTO;
import ai.cvbird.cvbirdtelegram.dto.RequestGetUserStatistic;
import ai.cvbird.cvbirdtelegram.dto.StatisticDTO;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CVBirdSite", url = "${feign.cvbird-site.url}")
public interface CVBirdSiteClient {

    @RequestLine("GET /telegram/registration")
    String telegramRegistration(String telegramId);

    @RequestLine("GET telegram/get_cvbird_user/{telegramId}")
    StatisticDTO getTelegramUser(@PathVariable String telegramId);
    @RequestLine("GET cv/get_by_telegram_id/{telegramId}")
    CVDataDTO getCVByTelegramId(@PathVariable String telegramId);

    @RequestLine("POST /telegram/statistic/save")
    String saveStatistic(StatisticDTO statisticDTO);

    @RequestLine("POST /telegram/get_user_statistic")
    StatisticDTO getUserStatistic(@SpringQueryMap RequestGetUserStatistic requestGetUserStatistic);

    @RequestLine("GET /user/user_info")
    String userInfo();
}

