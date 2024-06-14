package ai.cvbird.cvbirdtelegram.controller;

import ai.cvbird.cvbirdtelegram.client.AIServiceClient;
import ai.cvbird.cvbirdtelegram.client.CVBirdSiteClient;
import ai.cvbird.cvbirdtelegram.dto.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppData;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public final String JOB_RESPONSE = "job_response";
    public final String FIRST_MESSAGE = "tg://user?id=895038310 Hello! Welcome to CVBird — your personal job search assistant!\n We use cutting-edge artificial intelligence technology to analyze your resume and find the most suitable jobs around the world.\n" +
            "\n" +
            "To get started, go to our mini-app and upload your resume. Just click the button below and we will help you find your dream job!\n" +
            "\n" +
            "If you have any questions or need help, don't hesitate to contact me. Good luck in your job search!\n\n";

    //public TelegramBot(@Value("${tg.bot.token}") String botToken) {
    //    super(botToken);
    //}

    public TelegramBot() {
        super("6986236750:AAEv0VeXfx3_e3QQuFIKNnbuMgyr7m3_dFA");
    }

    @Override
    public String getBotUsername() {
        return "CVBirdBot";
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if(update.hasMessage()){
            Message message = update.getMessage();
            WebAppData webAppData = message.getWebAppData();

            if (webAppData != null) {
                webAppDataHandle(webAppData);
            }
            if (message.hasText() || message.hasDocument()){
                messageHandle(message);
            }
        }
    }

    private void webAppDataHandle(WebAppData webAppData) {
        String data = webAppData.getData();
        String buttonText = webAppData.getButtonText();
        if (JOB_RESPONSE.equals(buttonText)) {
            JSONObject obj = new JSONObject(data);
            String applicantTelegramId = obj.getString("applicant_telegram_id");
            String jobId = obj.getString("job_id");

            if (!applicantTelegramId.isBlank()) {
                StatisticDTO statisticDTO = cvBirdSiteClient.getTelegramUser(applicantTelegramId);
                String userAccountName = "@"+statisticDTO.getTelegramUserName();

                CVDataDTO cvDataDTO = cvBirdSiteClient.getCVByTelegramId(applicantTelegramId);

                if (cvDataDTO != null) {
                    String stringCVBase64 = cvDataDTO.getResponse();
                    String path = base64Decode(stringCVBase64);
                    if (path != null) {
                        File file = new File(path);
                        InputFile telegramFile = new InputFile(file);

                        JobRequest jobRequest = new JobRequest(jobId);
                        String job = aiServiceClient.getJobById(jobRequest);
                        if (job != null) {
                            JSONObject jobJson = new JSONObject(job);
                            String jobTitle = jobJson.getJSONObject("job_info").getString("job_title");
                            String chatId = jobJson.getJSONObject("job_info").getString("chat_id");

                            String caption = userAccountName + " has responded to your vacancy " + jobTitle;

                            if (!jobTitle.isBlank() && !chatId.isBlank()) {
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
                        deleteFile(path);
                    }
                }
            }
        }
    }

    private void messageHandle(Message message) {
        String text = message.getText();
        log(message.getFrom().getFirstName(),message.getFrom().getLastName(), message.getFrom().getId().toString(), message.getText());

        if (START_C.equals(text)) {
            SendPhoto sendPhoto = defaultCommand(message);
            try {
                execute(sendPhoto);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SendPhoto defaultCommand(Message message) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup
                .builder()
                .keyboardRow(Arrays.asList(InlineKeyboardButton
                                .builder()
                                .text("Open in App")
                                .callbackData("Open in App")
                                .url("https://tg.cvbird.ai")
                                .build()))
                .build();
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(message.getChatId().toString());
            sendPhoto.setParseMode(ParseMode.HTML);
            sendPhoto.setCaption(FIRST_MESSAGE + "<b>Your ChatID : " + message.getChatId().toString() +"</b>");
            sendPhoto.setReplyMarkup(keyboard);
            File file = new File("tmp_logo.png");
            InputStream inputStream = new ClassPathResource("cvbird_logo.png").getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, file);
            sendPhoto.setPhoto(new InputFile(file));
            //sendPhoto.setPhoto(new InputFile(ResourceUtils.getFile("classpath:cvbird_logo.png")));
            return  sendPhoto;
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

    private String base64Decode(String base64String) {
        File file = new File(String.valueOf(System.currentTimeMillis()) + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(file); ) {
            // To be short I use a corrupted PDF string, so make sure to use a valid one if you want to preview the PDF file
            byte[] decoder = Base64.getDecoder().decode(base64String);
            fos.write(decoder);
            return file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteFile(String path) {
        File file = new File(path);
        file.delete();
    }
}
