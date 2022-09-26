package bbcdevelopment.addon.bbcaddon.utils.misc;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TextUtils {


    private static final ArrayList<String> encryptor = new ArrayList<>();

    public static String getName() {
        return (Modules.get().get(NameProtect.class)).getName(mc.getSession().getUsername());
    }

    public static String getServer() {
        return Utils.getWorldName();
    }

    public static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        return formatter.format(date);
    }

    public static void setEncrypt(String url) {
        try {
            URL link = new URL(url);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(link.openStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                encryptor.add(line);
            }
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }

    public static String encrypt(String text) {
        StringBuilder message = new StringBuilder();
        char[] d = encryptor.get(0).toCharArray();
        char[] e = encryptor.get(1).toCharArray();

        for (char c : text.toCharArray()) {
            for (int i = 0; i < d.length; i++) {
                if (c == d[i]) message.append(e[i]);
            }
        }

        return message.toString();
    }

    public static String decrypt(String text, Decryptor decryptor) {
        StringBuilder message = new StringBuilder();
        char[] d = (decryptor.equals(Decryptor.BBCrypt) ? encryptor.get(0).toCharArray() : encryptor.get(2).toCharArray());
        char[] e = (decryptor.equals(Decryptor.BBCrypt) ? encryptor.get(1).toCharArray() : encryptor.get(3).toCharArray());

        for (char c : text.toCharArray()) {
            for (int i = 0; i < e.length; i++) {
                if (c == e[i]) message.append(d[i]);
            }
        }

        return message.toString();
    }

    public static String setColor(Color c1, Color c2) {
        return "<" + toString(getColor(c1)) + ":" + toString(getColor(c2)) + ">";
    }

    public static Formatting fromString(String color) {
        return switch (color) {
            case "1" -> Formatting.RED;
            case "2" -> Formatting.AQUA;
            case "3" -> Formatting.BLUE;
            case "4" -> Formatting.DARK_BLUE;
            case "5" -> Formatting.DARK_GREEN;
            case "6" -> Formatting.DARK_AQUA;
            case "7" -> Formatting.DARK_RED;
            case "8" -> Formatting.DARK_PURPLE;
            case "9" -> Formatting.GOLD;
            case "10" -> Formatting.GRAY;
            case "11" -> Formatting.BLACK;
            case "12" -> Formatting.GREEN;
            case "13" -> Formatting.WHITE;
            case "14" -> Formatting.DARK_GRAY;
            case "15" -> Formatting.LIGHT_PURPLE;
            case "16" -> Formatting.YELLOW;
            default -> throw new IllegalStateException("Unexpected value: " + color);
        };
    }

    private static String toString(Formatting formatting) {
        return switch (formatting) {
            case RED -> "1";
            case AQUA -> "2";
            case BLUE -> "3";
            case DARK_BLUE -> "4";
            case DARK_GREEN -> "5";
            case DARK_AQUA -> "6";
            case DARK_RED -> "7";
            case DARK_PURPLE -> "8";
            case GOLD -> "9";
            case GRAY -> "10";
            case BLACK -> "11";
            case GREEN -> "12";
            case WHITE -> "13";
            case DARK_GRAY -> "14";
            case LIGHT_PURPLE -> "15";
            case YELLOW -> "16";
            case OBFUSCATED -> "17";
            case BOLD -> "18";
            case STRIKETHROUGH -> "19";
            case UNDERLINE -> "20";
            case ITALIC -> "21";
            case RESET -> "22";
        };
    }

    public static Formatting getColor(Color color) {
        return switch (color) {
            case Red -> Formatting.RED;
            case Aqua -> Formatting.AQUA;
            case Blue -> Formatting.BLUE;
            case DarkBlue -> Formatting.DARK_BLUE;
            case DarkGreen -> Formatting.DARK_GREEN;
            case DarkAqua -> Formatting.DARK_AQUA;
            case DarkRed -> Formatting.DARK_RED;
            case DarkPurple -> Formatting.DARK_PURPLE;
            case Gold -> Formatting.GOLD;
            case Gray -> Formatting.GRAY;
            case Black -> Formatting.BLACK;
            case Green -> Formatting.GREEN;
            case White -> Formatting.WHITE;
            case DarkGray -> Formatting.DARK_GRAY;
            case LightPurple -> Formatting.LIGHT_PURPLE;
            case Yellow -> Formatting.YELLOW;
        };
    }

    public enum Color {
        Black, DarkBlue, DarkGreen, DarkAqua, DarkRed, DarkPurple, Gold, Gray, DarkGray, Blue, Green, Aqua, Red, LightPurple, Yellow, White
    }

    public enum Decryptor {
        BBCrypt, Proxima
    }
}
