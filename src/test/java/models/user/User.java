package models.user;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Dauli Sengar
 * @since 26th May 2022
 * Represents a User entity.
 */
@Getter
@Setter
public class User {
    private String username, password, email, mobile, crypt;

    /**
     * Constructs a new User instance with the provided details.
     *
     * @param username The username of the user
     * @param password The password of the user
     * @param email    The email address of the user
     * @param mobile   The mobile number of the user
     * @param crypt    The encrypted data associated with the user
     */
    public User(String username, String password, String email, String mobile, String crypt) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.mobile = mobile;
        this.crypt = crypt;
    }

    public User() {

    }
}
