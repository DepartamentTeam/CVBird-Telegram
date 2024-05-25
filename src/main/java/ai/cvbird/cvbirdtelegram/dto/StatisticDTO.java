package ai.cvbird.cvbirdtelegram.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StatisticDTO {

    Long telegramId;

    String telegramFirstName;

    Boolean telegramIsBot;

    String telegramUserName;

    String telegramLastName;

    String telegramLanguageCode;
}
