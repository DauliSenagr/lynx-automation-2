package utils.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class SupportUtils {

    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static String generateRandomUsername(int length) {
        String chars = "abcdefghijk"
                + "lmnopqrstuvwxyz";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public static String generateTicketName() {
        Random random = new Random();
        StringBuilder word = new StringBuilder();

        // Generate the first 4 letters
        for (int i = 0; i < 4; i++) {
            char letter = (char) ('A' + random.nextInt(26));
            word.append(letter);
        }
        word.append("TKT");

        return word.toString();
    }

    public static String generateRandomMobileNumber() {
        long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(number);
    }

    public static String generateSessionName(String initials) {
        StringBuilder builder = new StringBuilder();
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return builder.append(initials).append("_").append(localDateTime.format(formatter)).toString();
    }

    public static String getCurrentDate(String dateFormat) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateFormat);
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static String epochTimeFormat(String dateFormat, String date) throws ParseException {
        //Timestamp in seconds, remove '/1000' from milliseconds.
        return String.valueOf(new SimpleDateFormat(dateFormat).parse(date).getTime());
    }

    public static String getCurrentDateTime(String formatPattern, LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        return localDateTime.format(formatter);
    }

    public static String addTimeUnitsToCurrentDateTime(String formatPattern, int sec, int min, int hour, int day, int month) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        LocalDateTime currentTime;
        currentTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime newTime = currentTime.plusSeconds(sec)
                .plusMinutes(min)
                .plusHours(hour)
                .plusDays(day)
                .plusMonths(month);
        return newTime.format(formatter);
    }

    public static String currentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }


    public static String utcTimeFormat(String istTime) {
        ZonedDateTime istDateTime = ZonedDateTime.parse(istTime + "+05:30");
        ZonedDateTime utcDateTime = istDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(UTC_FORMAT);
        return utcDateTime.format(formatter);
    }

    public static String getFutureDate(int daysToAdd, String dateFormat) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysToAdd);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return futureDate.format(formatter);
    }

    public static String getFutureDateAndTime(String formatPattern, int daysToAdd) {
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(daysToAdd);
        return getCurrentDateTime(formatPattern, futureDateTime);
    }

    public static String getFutureDateTimeWithMinutes(String formatPattern, int secondsToAdd) {
        LocalDateTime futureDateTime = LocalDateTime.now().plusSeconds(secondsToAdd);
        return getCurrentDateTime(formatPattern, futureDateTime);
    }

    public static long getPastDate(int days) {
        // Current DateTime
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastDateTime = now.minusDays(days);
        // Convert to Epoch Milliseconds
        return pastDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

}














