package ai.cvbird.cvbirdtelegram;

import ai.cvbird.cvbirdtelegram.service.TelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@SpringBootTest
public class TelegramBotTest {

    @Autowired
    TelegramBot telegramBot;

    @Test
    void testFindAllEmployees() {
        WebAppData webAppData = new WebAppData("{\"applicant_telegram_id\": \"10078\", \"job_id\": \"1\"}","job_response");
        Method method = null;

        try {
            method = TelegramBot.class.getDeclaredMethod("webAppDataHandle", WebAppData.class);
            method.setAccessible(true);
            method.invoke(telegramBot, webAppData);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
