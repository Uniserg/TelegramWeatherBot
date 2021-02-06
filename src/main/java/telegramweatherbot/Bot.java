package telegramweatherbot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import telegramweatherbot.parsers.Case;
import telegramweatherbot.parsers.CurrentWeather;
import telegramweatherbot.parsers.HourlyForecast3Days;
import telegramweatherbot.parsers.HourlyForecastWeek;

public class Bot extends TelegramLongPollingBot {

    public HashMap<String, String> buttonIcons;

    private static final String API_KEY = "1946b0c3abfe50a3352de413456b55fd";
    HashMap<String, String> subscribes;
    HashSet<String> broadcast;
    Calendar c1;
    Calendar c2;
    Timer timer;
    boolean isChangeSettings = false;
    Function<String, String> getForecast;

    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            Bot bot = new Bot();
            bot.initBot();
            telegramBotsApi.registerBot(bot);
            bot.sendOn20();
            bot.sendOn9();

        } catch (TelegramApiRequestException ignored) {
        }

    }

    public void sendOn9() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                broadcast();
            }
        }, c1.getTime(), 86400000);
    }

    public void sendOn20() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                broadcast();
            }
        }, c2.getTime(), 86400000);
    }

    public void initBot() {
        buttonIcons = new HashMap<String, String>() {
            {
                put("settings", "⚙");
                put("subscribe", "📥");
                put("unsubsribe", "📤");
                put("cancel", "❌");
                put("night", "");
                put("city", "🏙");
                put("broadcast", "📨");
                put("current", "📋");
                put("3days", "🌅");
                put("week", "📅");
            }
        };
        subscribes = new HashMap<>();
        broadcast = new HashSet<>();

        //ВЫНЕСТИ В ОТДЕЛЬНЫЙ ПОТОК
        c1 = Calendar.getInstance();
        c2 = Calendar.getInstance();
        timer = new Timer();

        c1.set(Calendar.HOUR_OF_DAY, 9);
        c1.set(Calendar.MINUTE, 26);
        c1.set(Calendar.SECOND, 0);

        c2.set(Calendar.HOUR_OF_DAY, 20);
        c2.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
    }

    public String getWeatherWeek(String city) {
        return new HourlyForecastWeek(Case.toTitle(city), API_KEY).get();
    }

    public String getWeather3Days(String city) {
        return new HourlyForecast3Days(Case.toTitle(city), API_KEY).get();
    }

    public String getWeatherCurrent(String city) {
        return new CurrentWeather(Case.toTitle(city), API_KEY).get();
    }

    @Override
    public String getBotToken() {
        return "1473191847:AAHgGFs1F9IPKPEJxJAfd0VQwZ0Ne9Rn9nk";
    }

    public void sendMsg(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }

    public void sendSettings(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);

        try {
            setSettingsKeyboard(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);

        try {
            setKeyboard(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }

    private void setForecast(Message message, Function<String, String> forecast) {
        String chatId = message.getChatId().toString();
        if (!subscribes.containsKey(chatId)) {
            sendMsg(message, "Укажите город.");
            getForecast = forecast;
        } else {
            sendMsg(message, forecast.apply(subscribes.get(chatId)));
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            String text = message.getText();
            String chatId = message.getChatId().toString();
            text = text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").toLowerCase().trim();
            switch (text) {
                case ("/help"):
                    sendMsg(message, "Чем могу помочь?");
                    break;
                case ("/settings"):
                case ("настройки"):
                    if (subscribes.containsKey(chatId)) {
                        sendSettings(message, "Что будем настраивать?");
                        isChangeSettings = true;
                    } else {
                        sendMsg(message, "Вы не подписаны.");
                    }
                    break;
                case ("сменить город"):
                    if (subscribes.containsKey(chatId)) {
                        sendMsg(message, "Укажите город.");
                        isChangeSettings = true;
                    } else {
                        sendMsg(message, "Вы не подписаны!");
                    }
                    break;
                case ("/start"):
                    sendMsg(message, "Здравствуйте! 🤩 👋\nВвведите в чат город 🏙️, чтобы получить информацию о погоде на сегодня!\nВы можете подписаться 📥, указав город, по которому будет приходить прогноз. Также с подпиской можно получать автоматическую рассылку 📨.");
                    break;
                case ("/subscribe"):
                case ("подписаться"):
                    if (!subscribes.containsKey(chatId)) {
                        subscribes.put(chatId, "Москва");
                        getForecast = this::getWeatherCurrent;
                        sendMsg(message, "Отлично! Вы подписались на рассылку погоды. По умолчанию, информация о погоде по городу Москва. Вы можете сменить город, нажав на кнопку \"Настройки\" или написав команду /settings.");
                    } else {
                        sendMsg(message, "Вы уже подписались на рассылку!");
                    }
                    break;
                case ("/unsubscribe"):
                case ("отписаться"):
                    if (subscribes.containsKey(chatId)) {
                        subscribes.remove(chatId);
                        broadcast.remove(chatId);
                        sendMsg(message, "Вы отписались от рассылки погоды.");
                    } else {
                        sendMsg(message, "Вы не подписывались на рассылку!");
                    }
                    break;
                case ("текущая погода"):
                    setForecast(message, this::getWeatherCurrent);
                case ("получать рассылку"):
                    if (subscribes.containsKey(chatId)) {
                        broadcast.add(chatId);
                        sendMsg(message, "Отлично! Теперь Вам будет приходить уведомление о текущей погоде в 9:00 и в 20:00 по МСК каждый день.");
                    } else {
                        sendMsg(message, "Чтобы получать рассылку, нужно сначала подписаться.");
                    }
                    break;
                case ("отменить рассылку"):
                    if (broadcast.contains(chatId)) {
                        broadcast.remove(chatId);
                        sendMsg(message, "Вы отказались от рассылки. ");
                    } else {
                        sendMsg(message, "Вы не получаете рассылку.");
                    }
                    break;
                case ("погода на ближайшие 3 дня"):
                    setForecast(message, this::getWeather3Days);
                case ("погода на неделю"):
                    setForecast(message, this::getWeatherWeek);
                
                default:
                    String weather;
                    if (getForecast == null) {
                        getForecast = this::getWeatherCurrent;
                    }
                    weather = getForecast.apply(text);
                    if (!weather.equals("404")) {
                        if (isChangeSettings) {
                            subscribes.put(chatId, text);
                            sendMsg(message, "Город был успешно изменен.");
                            isChangeSettings = false;
                        } else {
                            sendMsg(message, getForecast.apply(text));
                        }
                    } else {
                        sendMsg(message, "К сожалению, мы не нашли такой город. Попробуйте еще раз!");
                    }
            }
        }
    }

    private ReplyKeyboardMarkup createKeyboard(SendMessage sendMessage, boolean selective, boolean resize, boolean oneTime) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(selective);
        replyKeyboardMarkup.setResizeKeyboard(resize);
        replyKeyboardMarkup.setOneTimeKeyboard(oneTime);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyKeyboardMarkup;
    }

    private void addRow(ReplyKeyboardMarkup keyboardMarkup, String[] buttons) {
        KeyboardRow keyboardRow = new KeyboardRow();
        for (String button : buttons) {
            KeyboardButton kb = new KeyboardButton();
            kb.setText(button);
            keyboardRow.add(kb);
        }
        keyboardMarkup.getKeyboard().add(keyboardRow);
    }

    private void addOneRowButtons(ReplyKeyboardMarkup keyboardMarkup, String[] buttons) {
        for (String button : buttons) {
            KeyboardRow keyboardRow = new KeyboardRow();
            KeyboardButton kb = new KeyboardButton();
            kb.setText(button);
            keyboardRow.add(kb);
            keyboardMarkup.getKeyboard().add(keyboardRow);
        }
    }

    private KeyboardRow addRow(ReplyKeyboardMarkup keyboardMarkup) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardMarkup.getKeyboard().add(keyboardRow);
        return keyboardRow;
    }

    private void setKeyboard(SendMessage sendMessage) {
        String chatId = sendMessage.getChatId();

        ReplyKeyboardMarkup replyKeyboardMarkup = createKeyboard(sendMessage, true, true, false);

        String[] buttons = {"Текущая погода " + buttonIcons.get("current"),
            "Погода на ближайшие 3 дня " + buttonIcons.get("3days"),
            "Погода на неделю " + buttonIcons.get("week")};

        addOneRowButtons(replyKeyboardMarkup, buttons);
        KeyboardRow subscribe = addRow(replyKeyboardMarkup);
        KeyboardButton subButton = new KeyboardButton();

        if (!subscribes.containsKey(chatId)) {
            subButton.setText("Подписаться " + buttonIcons.get("subscribe"));
        } else {
            KeyboardButton broadButton = new KeyboardButton();

            if (!broadcast.contains(chatId)) {
                subscribe.add(broadButton.setText("Получать рассылку " + buttonIcons.get("broadcast")));
            } else {
                subscribe.add(broadButton.setText("Отменить рассылку " + buttonIcons.get("cancel")));
            }
            subscribe.add(new KeyboardButton().setText("Настройки️ " + buttonIcons.get("settings")));

            subButton.setText("Отписаться " + buttonIcons.get("unsubsribe"));
        }
        subscribe.add(subButton);
    }

    public void setSettingsKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton key = new KeyboardButton();

        key.setText("Сменить город " + buttonIcons.get("city"));

        keyboardFirstRow.add(key);
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public void broadcast() {
        broadcast.forEach(id -> sendMsg(id, buttonIcons.get("broadcast") + getWeatherCurrent(subscribes.get(id))));
    }

    @Override
    public String getBotUsername() {
        return "weather_uni_bot";
    }

}
