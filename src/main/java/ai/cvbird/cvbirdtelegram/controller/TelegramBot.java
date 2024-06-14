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
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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

    @Autowired
    StatisticConverter statisticConverter;

    public final String START_C = "/start";
    public final String HELP_C = "/help";
    public final String HELLO_C = "/hello";
    public final String REGISTRATION_C = "/registration";
    public final String ME_C = "/me";
    public final String COMMANDS_M = "Commands";
    public final String VACANCIES_M = "Vacancies";
    public final Integer ONE = 1;
    public final String JOB_RESPONSE = "job_response";

    //public TelegramBot(@Value("${tg.bot.token}") String botToken) {
    //    super(botToken);
    //}

    public TelegramBot() {
        super(System.getenv("TG_BOT_TOKEN"));
    }

    @Override
    public String getBotUsername() {
        return "CVBirdBot";
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if(update.hasMessage()){
            Message message = update.getMessage();
            User user = message.getFrom();
            User forwardUser = message.getForwardFrom();
            WebAppData webAppData = message.getWebAppData();

            if (webAppData != null) {
                webAppDataHandle(webAppData);
            }


            if(message.hasText() || message.hasDocument()){
                documentHandle(message);
                messageHandle(message);
            }
        }
    }

    private void documentHandle(Message message) {}

    private void webAppDataHandle(WebAppData webAppData) {
        String data = webAppData.getData();
        String buttonText = webAppData.getButtonText();
        if (JOB_RESPONSE.equals(buttonText)) {
            JSONObject obj = new JSONObject(data);
            String employerId  = obj.getString("employer_id");
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

        RequestGetUserStatistic requestGetUserStatistic = new RequestGetUserStatistic();
        requestGetUserStatistic.setTelegramId(message.getFrom().getId().toString());
        log(message.getFrom().getFirstName(),message.getFrom().getLastName(), message.getFrom().getId().toString(), message.getText());
        if (cvBirdSiteClient.getUserStatistic(requestGetUserStatistic) == null) {
            saveStatistic(message);
        }
        if (text != null) {
            SendPhoto sendPhoto = null;
            SendMessage sendMessage = null;
            switch (text) {
                case HELLO_C:
                case REGISTRATION_C:
                case ME_C:
                    sendMessage = meCommand(message);
                    break;
                case VACANCIES_M:
                case START_C:
                    sendMessage = startCommand(message);
                    break;
                case COMMANDS_M:
                    sendMessage = getCommands(message);
                    break;
                default:
                    sendPhoto = defaultCommand(message);
                    break;
            }
            try {
                if (sendMessage != null) {
                    execute(sendMessage);
                } else if ( sendPhoto != null) {
                    execute(sendPhoto);
                }

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SendMessage startCommand(Message message) {

       // ReplyKeyboardMarkup replyKeyboardMarkup = getMainMenuKeyboard(language);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(getKeyboardMarkup());
      //  sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setText("<b>bold</b>, <strong>bold</strong>\n" +
                "<i>italic</i>, <em>italic</em>\n" +
                "<u>underline</u>, <ins>underline</ins>\n" +
                "<s>strikethrough</s>, <strike>strikethrough</strike>, <del>strikethrough</del>\n" +
                "<span class=\"tg-spoiler\">spoiler</span>, <tg-spoiler>spoiler</tg-spoiler>\n" +
                "<b>bold <i>italic bold <s>italic bold strikethrough <span class=\"tg-spoiler\">italic bold strikethrough spoiler</span></s> <u>underline italic bold</u></i> bold</b>\n" +
                "<a href=\"http://www.example.com/\">inline URL</a>\n" +
                "<a href=\"tg://user?id=123456789\">inline mention of a user</a>\n" +
                "<tg-emoji emoji-id=\"5368324170671202286\">\uD83D\uDC4D</tg-emoji>\n" +
                "<code>inline fixed-width code</code>\n" +
                "<pre>pre-formatted fixed-width code block</pre>\n" +
                "<pre><code class=\"language-python\">pre-formatted fixed-width code block written in the Python programming language</code></pre>\n" +
                "<blockquote>Block quotation started\\nBlock quotation continued\\nThe last line of the block quotation</blockquote>" + message.getText());
        return  sendMessage;
    }

    private SendMessage getCommands(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(getKeyboardMarkup());
        //  sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setText("/start\n" +
                "/help\n" +
                "/me\n" +
                "/registration\n");
        return  sendMessage;
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
            sendPhoto.setCaption("Your ChatID :  " + message.getChatId().toString());
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

    private SendMessage meCommand(Message message) {
        String me = cvBirdSiteClient.userInfo();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.enableHtml(true);
        //  sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setText(me);
        return  sendMessage;
    }

    private ReplyKeyboardMarkup getKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        // Create the keyboard (list of keyboard rows)
        List<KeyboardRow> keyboard = new ArrayList<>();
        // Create a keyboard row
        KeyboardRow row = new KeyboardRow();
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add("Vacancies");
        row.add("Commands");
        keyboard.add(row);

        // Set the keyboard to the markup
        keyboardMarkup.setKeyboard(keyboard);

        keyboardMarkup.setResizeKeyboard(true);
        //keyboardMarkup.setIsPersistent(true);

        return keyboardMarkup;
    }

    private void saveStatistic(Message message) {
        StatisticDTO statisticDTO =  statisticConverter.fromUser(message.getFrom());
        String req = cvBirdSiteClient.saveStatistic(statisticDTO);
        System.out.println(req);
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
