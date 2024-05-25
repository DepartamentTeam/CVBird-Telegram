package ai.cvbird.cvbirdtelegram.controller;

import ai.cvbird.cvbirdtelegram.client.CVBirdSiteClient;
import ai.cvbird.cvbirdtelegram.dto.RequestGetUserStatistic;
import ai.cvbird.cvbirdtelegram.dto.StatisticConverter;
import ai.cvbird.cvbirdtelegram.dto.StatisticDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    CVBirdSiteClient cvBirdSiteFeignClient;

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
            User user = message.getFrom();
            User forwardUser = message.getForwardFrom();

            if(message.hasText() || message.hasDocument()){
                documentHandle(message);
                messageHandle(message);
            }
        }
    }

    private void documentHandle(Message message) {}


    private void messageHandle(Message message) {
        String text = message.getText();

        RequestGetUserStatistic requestGetUserStatistic = new RequestGetUserStatistic();
        requestGetUserStatistic.setTelegramId(message.getFrom().getId().toString());
        log(message.getFrom().getFirstName(),message.getFrom().getLastName(), message.getFrom().getId().toString(), message.getText());
        if (cvBirdSiteFeignClient.getUserStatistic(requestGetUserStatistic) == null) {
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
                                .text("Row 1 Column 1")
                                .callbackData("Row 1 Column 1")
                                .build(),
                        InlineKeyboardButton
                                .builder()
                                .text("Row 1 Column 2")
                                .callbackData("Row 1 Column 2")
                                .build())
                )
                .keyboardRow(Arrays.asList(InlineKeyboardButton
                                .builder()
                                .text("Row 2 Column 1")
                                .callbackData("Row 2 Column 1")
                                .build(),
                        InlineKeyboardButton
                                .builder()
                                .text("Row 2 Column 2")
                                .callbackData("Row 2 Column 2")
                                .build())
                )
                .build();
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(message.getChatId().toString());
            sendPhoto.setCaption("you said: " + message.getText());
            sendPhoto.setReplyMarkup(keyboard);
            sendPhoto.setPhoto(new InputFile(ResourceUtils.getFile("classpath:cvbird_logo.png")));
            return  sendPhoto;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage meCommand(Message message) {
        String me = cvBirdSiteFeignClient.userInfo();
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
        String req = cvBirdSiteFeignClient.saveStatistic(statisticDTO);
        System.out.println(req);
    }

    private void log(String first_name, String last_name, String user_id, String txt) {
        System.out.println("\n ----------------------------");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.print(dateFormat.format(date));
        System.out.print(" : Message from " + first_name + " " + last_name + ". (id = " + user_id + ") \n Text - " + txt);
    }

    //private static String getHelpMessage(String language) {
    //    String baseString = LocalisationService.getString("helpWeatherMessage", language);
    //    return String.format(baseString, Emoji.BLACK_RIGHT_POINTING_TRIANGLE.toString(),
    //            Emoji.BLACK_RIGHT_POINTING_DOUBLE_TRIANGLE.toString(), Emoji.ALARM_CLOCK.toString(),
    //            Emoji.EARTH_GLOBE_EUROPE_AFRICA.toString(), Emoji.STRAIGHT_RULER.toString());
    //}

    //private static ReplyKeyboardMarkup getMainMenuKeyboard(String language) {
    //    ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder<?, ?> replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
    //    replyKeyboardMarkupBuilder.selective(true);
    //    replyKeyboardMarkupBuilder.resizeKeyboard(true);
    //    replyKeyboardMarkupBuilder.oneTimeKeyboard(false);
//
    //    List<KeyboardRow> keyboard = new ArrayList<>();
    //    KeyboardRow keyboardFirstRow = new KeyboardRow();
    //    keyboardFirstRow.add(getCurrentCommand(language));
    //    keyboardFirstRow.add(getForecastCommand(language));
    //    KeyboardRow keyboardSecondRow = new KeyboardRow();
    //    keyboardSecondRow.add(getSettingsCommand(language));
    //    keyboardSecondRow.add(getRateCommand(language));
    //    keyboard.add(keyboardFirstRow);
    //    keyboard.add(keyboardSecondRow);
    //    replyKeyboardMarkupBuilder.keyboard(keyboard);
//
    //    return replyKeyboardMarkupBuilder.build();
    //}


    //private static ReplyKeyboardMarkup getRecentsKeyboard(Long userId, String language, boolean allowNew) {
    //    ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder<?, ?> replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
    //    replyKeyboardMarkupBuilder.selective(true);
    //    replyKeyboardMarkupBuilder.resizeKeyboard(true);
    //    replyKeyboardMarkupBuilder.oneTimeKeyboard(true);
//
    //    List<KeyboardRow> keyboard = new ArrayList<>();
    //    for (String recentWeather : DatabaseManager.getInstance().getRecentWeather(userId)) {
    //        KeyboardRow row = new KeyboardRow();
    //        row.add(recentWeather);
    //        keyboard.add(row);
    //    }
//
    //    KeyboardRow row = new KeyboardRow();
    //    if (allowNew) {
    //        row.add(getLocationCommand(language));
    //        keyboard.add(row);
//
    //        row = new KeyboardRow();
    //        row.add(getNewCommand(language));
    //        keyboard.add(row);
//
    //        row = new KeyboardRow();
    //    }
    //    row.add(getCancelCommand(language));
    //    keyboard.add(row);
//
    //    replyKeyboardMarkupBuilder.keyboard(keyboard);
//
    //    return replyKeyboardMarkupBuilder.build();
    //}
//

}
