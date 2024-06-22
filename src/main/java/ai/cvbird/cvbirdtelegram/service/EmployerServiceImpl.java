package ai.cvbird.cvbirdtelegram.service;

import ai.cvbird.cvbirdtelegram.dto.EmployerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployerServiceImpl implements EmployerService{

    @Autowired
    TelegramBot telegramBot;

    @Override
    public void sendEmployerResponse(EmployerResponse employerResponse) {
        telegramBot.sendEmployerResponse(employerResponse);
    }
}
