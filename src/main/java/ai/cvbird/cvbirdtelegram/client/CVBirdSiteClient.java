package ai.cvbird.cvbirdtelegram.client;

import ai.cvbird.cvbirdtelegram.dto.RequestGetUserStatistic;
import ai.cvbird.cvbirdtelegram.dto.StatisticDTO;
import feign.Headers;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "CVBirdSite", url = "${feign.cvbird-site.url}")
public interface CVBirdSiteClient {

    @RequestLine("GET /telegram/registration")
    String telegramRegistration(String telegramId);

    @RequestLine("GET /telegram/get_user")
    String telegramGetUser();

    @RequestLine("POST /telegram/statistic/save")
    String saveStatistic(StatisticDTO statisticDTO);

    @RequestLine("POST /telegram/get_user_statistic")
    StatisticDTO getUserStatistic(@SpringQueryMap RequestGetUserStatistic requestGetUserStatistic);

    @RequestLine("GET /user/user_info")
    String userInfo();
}

