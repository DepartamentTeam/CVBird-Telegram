package ai.cvbird.cvbirdtelegram.service;

import ai.cvbird.cvbirdtelegram.client.AIServiceClient;
import ai.cvbird.cvbirdtelegram.client.CVBirdSiteClient;
import ai.cvbird.cvbirdtelegram.dto.*;
import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    CVBirdSiteClient cvBirdSiteClient;

    @Autowired
    AIServiceClient aiServiceClient;

    public final String START_C = "/start";
    public final String FIRST_MESSAGE = "Hello! Welcome to CVBird — your personal job search assistant!\n" +
            "We use cutting-edge artificial intelligence technology to analyze your resume and find the most suitable jobs around the world.\n" +
            "\n" +
            "To get started, go to our mini-app and upload your resume. Just click the button below and we will help you find your dream job!\n" +
            "\n" +
            "Good luck in your job search!\n\n";

    //public TelegramBot(@Value("${tg.bot.token}") String botToken) {
    //    super(botToken);
    //}

    public TelegramBot(@Value("${tg.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return "CVBirdBot";
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if(update.hasMessage()){
            Message message = update.getMessage();
            if (message.hasText() || message.hasDocument()){
                messageHandle(message);
            }
        }
    }

    public void sendEmployerResponse(EmployerResponse employerResponse) {
        log("EMPLOYER RESPONCE:", employerResponse.getApplicantTelegramId(), employerResponse.getJobId(), "SEND");
        String applicantTelegramId = employerResponse.getApplicantTelegramId();
        String jobId = employerResponse.getJobId();

        if (!applicantTelegramId.isBlank()) {
            StatisticDTO statisticDTO = cvBirdSiteClient.getTelegramUser(applicantTelegramId);
            String userAccountName = "@"+statisticDTO.getTelegramUserName();

            CVDataDTO cvDataDTO = cvBirdSiteClient.getCVByTelegramId(applicantTelegramId);

            if (cvDataDTO != null) {
                String stringCVBase64 = cvDataDTO.getResponse();
                InputStream inputStream = base64DecodeStream(stringCVBase64);
                InputFile telegramFile = new InputFile(inputStream, statisticDTO.getTelegramUserName() + "-cv.pdf");

                JobRequest jobRequest = new JobRequest(jobId);
                String job = aiServiceClient.getJobById(jobRequest);
                if (job != null) {
                    JSONObject jobJson = new JSONObject(job);
                    String jobTitle = jobJson.getJSONObject("job_info").getString("title");
                    String chatId = jobJson.getJSONObject("job_info").optString("tg_chat_id");
                    String caption = userAccountName + " has responded to your vacancy " + jobTitle;
                    if (!jobTitle.isBlank() && chatId != null) {
                        SendDocument  sendDocument = new SendDocument();
                        sendDocument.setChatId(chatId);
                        sendDocument.setDocument(telegramFile);
                        sendDocument.setCaption(caption);
                        try {
                            execute(sendDocument);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }
    }

    private void messageHandle(Message message) {
        String text = message.getText();
        log(message.getFrom().getFirstName(),message.getFrom().getLastName(), message.getFrom().getId().toString(), message.getText());

        if (START_C.equals(text)) {
            User user = message.getFrom();
            TelegramStatisticDTO telegramStatisticDTO = new TelegramStatisticDTO();
            telegramStatisticDTO.setTelegramId(user.getId());
            telegramStatisticDTO.setTelegramUserName(user.getUserName());
            telegramStatisticDTO.setTelegramFirstName(user.getFirstName());
            telegramStatisticDTO.setTelegramLastName(user.getLastName());
            telegramStatisticDTO.setTelegramIsBot(user.getIsBot());
            telegramStatisticDTO.setTelegramLanguageCode(user.getLanguageCode());
            try{
                System.out.println(cvBirdSiteClient.saveTelegramUser(telegramStatisticDTO));
            }catch (FeignException e) {
                System.out.println(e);
            }
            SendMediaGroup sendMediaGroup = defaultCommand(message);
            try {
                execute(sendMediaGroup);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SendMediaGroup defaultCommand(Message message) {
        try {
            InputMedia inputMedia = new InputMediaPhoto();
            inputMedia.setParseMode(ParseMode.HTML);
            inputMedia.setCaption(FIRST_MESSAGE + "<b>Your ChatID : " + message.getChatId().toString() +"</b>");
            inputMedia.setMedia(ResourceUtils.getFile("start_logo_1.png"), "one");

            InputMedia inputMedia2 = new InputMediaPhoto();
            inputMedia2.setMedia(ResourceUtils.getFile("start_logo_2.png"), "two");

            InputMedia inputMedia3 = new InputMediaPhoto();
            inputMedia3.setMedia(ResourceUtils.getFile("start_logo_3.png"), "3");

            InputMedia inputMedia4 = new InputMediaPhoto();
            inputMedia4.setMedia(ResourceUtils.getFile("start_logo_4.png"), "4");

            SendMediaGroup mediaGroup = new SendMediaGroup();
            mediaGroup.setChatId(message.getChatId());
            mediaGroup.setMedias(List.of(inputMedia, inputMedia2, inputMedia3, inputMedia4));
            return  mediaGroup;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void log(String first_name, String last_name, String user_id, String txt) {
        System.out.println("\n ----------------------------");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.print(dateFormat.format(date));
        System.out.print(" : Message from " + first_name + " " + last_name + ". (id = " + user_id + ") \n Text - " + txt);
    }

    private InputStream base64DecodeStream(String base64String) {
        try {
            // To be short I use a corrupted PDF string, so make sure to use a valid one if you want to preview the PDF file
            byte[] decoder = Base64.getDecoder().decode(base64String);
            InputStream inputStream = new ByteArrayInputStream(decoder);
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
