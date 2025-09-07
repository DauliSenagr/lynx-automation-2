package utils.users;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.adda52.logging.Logging;
import models.user.User;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * @author Dauli Sengar
 * @since 28th May 2022
 */

public class UserFactory implements Logging {
    User user;
    String username, password, email, mobile, crypt;
    Workbook workbook;

    protected final static ArrayList<User> users = new ArrayList<>();


    public synchronized void createUserInstance() {
        User user = users.get(0);
        UserManager.setUser(user);
        users.remove(user);
        getLogger().info("Created instance for user: " + user.getUsername() + " at: " + new Timestamp(System.currentTimeMillis()));
    }

    public void initializeJsonUserPool() {
        getLogger().info("Initializing user pool.");
        try (FileReader fileReader = new FileReader(Constants.getUserDataSheetPath())) {
            JsonArray userData = JsonParser.parseReader(fileReader).getAsJsonArray();
            for (JsonElement userDatum : userData) {
                JsonObject userObject = userDatum.getAsJsonObject();
                User user = createUserFromJsonObject(userObject);
                users.add(user);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading user data file.", e);
        }
    }


    private User createUserFromJsonObject(JsonObject userObject) {
        String username = userObject.get("userName").getAsString();
        String password = userObject.get("password").getAsString();
        String email = userObject.get("email").getAsString();
        String mobile = userObject.get("mobile").getAsString();
        String crypt = userObject.get("crypt").getAsString(); // or use getAsInt() or other appropriate methods if 'crypt' is of different data type
        return new User(username, password, email, mobile, crypt);
    }


    public static synchronized void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public static void printAvailableUsers() {
        for (User value : users) {
            System.out.println(value.getUsername());
        }
    }

}
