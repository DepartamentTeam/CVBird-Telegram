package ai.cvbird.cvbirdtelegram;

import ai.cvbird.cvbirdtelegram.controller.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableFeignClients
public class CvbirdTelegramApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(CvbirdTelegramApplication.class, args);
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(ctx.getBean("telegramBot", TelegramLongPollingBot.class));
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

}
