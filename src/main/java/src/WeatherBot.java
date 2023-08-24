package src;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherBot extends TelegramLongPollingBot {
    private static final String TOKEN = "5791093743:AAG6VBTsduS3itEUVzz_9s_nVlBogcY5ZgM";
    private static final String API_KEY = "caceda6ea68d92735ca56f94c619ece2";

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new WeatherBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        getUserInfo(message);
        if (message.hasText()) {
            String text = message.getText();
            String city = getCity(text);
            if (city != null) {
                sendMsg(message, "<b>Այս պահին ջերմաստիճանը &#128205;" + city + "ում:" + getWeather(city) + "\n </b>");
                sendMsg(message, "<b>Температура в этот момент в&#128205;" + city  + getWeather(city) + "\n </b>");
                sendMsg(message, "<b>The temperature at this moment at &#128205;" + city + "is:" + getWeather(city) + "\n </b>");
            } else {
                sendMsg(message, "<b> &#128205; Խնդրում եմ գրեք բնակավայրի անունը :\n</b>");
                sendMsg(message, "<b> &#128205; Пожалуйста, напишите название места :\n</b>");
                sendMsg(message, "<b> &#128205; Please write the name of the place :\n</b>");
            }
        }
    }



    private void getUserInfo(Message message) {
        User user = message.getFrom();
        String lastName = user.getLastName();
        String userName = user.getUserName();
        String firstName = user.getFirstName();
        long userId = user.getId();
        String languageCode = user.getLanguageCode();
        long chatId = message.getChatId();
        String chatTitle = message.getChat().getTitle();
        System.out.println("City " + message.getText());
        System.out.println("First name: " + firstName);
        System.out.println("Last name: " + lastName);
        System.out.println("Username: " + userName);
        System.out.println("User ID: " + userId);
        System.out.println("Language code: " + languageCode);
        System.out.println("Chat ID: " + chatId);
        System.out.println("Chat title: " + chatTitle);
    }

    public String getWeather(String city) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric")
                    .build();
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            JSONObject Jobject = new JSONObject(jsonData);
            JSONObject main = Jobject.getJSONObject("main");
            double temp = main.getDouble("temp");
            double temp_min = main.getDouble("temp_min");
            double temp_max = main.getDouble("temp_max");
            if (temp_min > 0 && temp_min < 5) {
                return temp + "°C\n"
                        + "Մինիմում Ջերմաստիճան: &#127780;" + temp_min + "°C:\n"
                        + "Մաքսիմում Ջերմաստիճան: &#127780;" + temp_max + "°C:\n";
            }
            if (temp_min < 0) {
                return temp + "°C\n"
                        + "Մինիմում Ջերմաստիճան: &#10052;" + temp_min + "°C:\n"
                        + "Մաքսիմում Ջերմաստիճան: &#10052;" + temp_max + "°C:\n";
            } else if (temp_min > 5) {
                return temp + "°C\n"
                        + "Մինիմում Ջերմաստիճան: &#127774;" + temp_min + "°C:\n"
                        + "Մաքսիմում Ջերմաստիճան: &#127774;" + temp_max + "°C:\n";
            }
            return temp + "°C\n"
                    + "Մինիմում Ջերմաստիճան: &#127774;" + temp_min + "°C:\n"
                    + "Մաքսիմում Ջերմաստիճան:&#127774;" + temp_max + "°C:\n";
        } catch (Exception e) {
            return "Անհասանաելի է, որովհետև բնակավայրի անունը սխալ է։";
        }
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getCity(String text) {
        Pattern cityPattern = Pattern.compile("(?<=\\s|^)\\p{L}+(?:\\s+\\p{L}+)*(?=\\s|$)");
        Matcher cityMatcher = cityPattern.matcher(text);
        if (cityMatcher.find()) {
            return cityMatcher.group();
        } else {
            return null;
        }
    }

    public String getBotUsername() {
        return "weather_arm_bot";
    }

    public String getBotToken() {
        return TOKEN;
    }
}

