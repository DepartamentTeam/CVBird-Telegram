package ai.cvbird.cvbirdtelegram.dto;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class StatisticConverter {
    public StatisticDTO fromUser (User user) {
        StatisticDTO statisticDTO = new StatisticDTO();
        statisticDTO.setTelegramId(user.getId());
        statisticDTO.setTelegramIsBot(user.getIsBot());
        statisticDTO.setTelegramFirstName(user.getFirstName());
        statisticDTO.setTelegramLastName(user.getFirstName());
        statisticDTO.setTelegramUserName(user.getUserName());
        statisticDTO.setTelegramLanguageCode(user.getLanguageCode());
        return statisticDTO;
    }
}

