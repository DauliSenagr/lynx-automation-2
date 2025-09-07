package utils.db;

import com.adda52.controllers.ExecutionController;
import com.adda52.logging.Logging;
import com.adda52.utils.database.sql.DatabaseManager;
import utils.csv.CsvUtils;
import utils.keys.KeyReferences;
import utils.support.SupportUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MySqlCalls implements Logging {

    public String getUserID(String username) {
        String userId = "";
        String query = "Select user_id from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user where user_name ='" + username + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            userId = resultSet.getString("user_id");
        } catch (SQLException e) {
            getLogger().error("Unable to get user id for user: " + username, e);
        }
        return userId;
    }

    public String getBlockReferenceId(String username) {
        String userId = getUserID(username);
        String id = "";
        String query = "Select id from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_block where user_id = ? order by id desc limit 1";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            id = resultSet.getString("id");
        } catch (SQLException e) {
            getLogger().error("Unable to get block reference id for user: " + username, e);
        }
        return id;
    }

    public String getLoginOtp(String username) {
        String otp = "";
        String query = "Select activation_code from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user where user_name ='" + username + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            otp = resultSet.getString("activation_code");
        } catch (Exception e) {
            getLogger().error("Unable to get OTP for user: " + username, e);
        }
        return otp;
    }

    public void updateUserAccountBalance(String username, String vip, String tb, String ib, String freeRoll) {
        String userID = getUserID(username);

        if (!userID.equalsIgnoreCase("")) {
            updateGameAccount(userID, "VIP", vip, username);
            updateGameAccount(userID, "VIP_IB", ib, username);
            updateGameAccount(userID, "VIP_TB", tb, username);
            updateGameAccount(userID, "Freeroll", freeRoll, username);
        }
    }

    public void updateLockedBonus(String username, String Lb) {
        String userID = getUserID(username);
        if (!userID.equalsIgnoreCase("")) {
            updateGameAccount(userID, "PAYOFF_BONUS", Lb, username);

        }
    }

    public String getUsername(String mobile) {
        String prefix = DatabaseManager.dataBasePrefix;
        String username = "";
        String query = "SELECT u.user_name FROM " + prefix + "cardplay.cp_user u JOIN " + prefix + "cardplay.cp_user_profile up ON u.user_id = up.user_id WHERE up.mobile = " + mobile;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            username = resultSet.getString("user_name");
        } catch (SQLException e) {
            getLogger().error("Unable to get Username", e);
        }
        return username;
    }

    private void updateGameAccount(String userID, String chipType, String chips, String username) {
        String checkQuery = "SELECT COUNT(chip_type) FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts WHERE user_id = ? AND chip_type = ?";
        String updateQuery = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts SET chips = ? WHERE user_id = ? AND chip_type = ?";
        String insertQuery = "INSERT INTO " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts (user_id, chip_type, chips, chips_in_play, version) VALUES (?, ?, ?, 0, 1)";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

            checkStatement.setString(1, userID);
            checkStatement.setString(2, chipType);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            getLogger().info(count + " rows found for " + chipType + " for user: " + username);

            if (count > 0) {
                updateStatement.setString(1, chips);
                updateStatement.setString(2, userID);
                updateStatement.setString(3, chipType);
                updateStatement.executeUpdate();
                getLogger().info("Updated " + chipType + " balance for user: " + username + " to " + chips);
            } else if (count == 0) {
                insertStatement.setString(1, userID);
                insertStatement.setString(2, chipType);
                insertStatement.setString(3, chips);
                insertStatement.executeUpdate();
                getLogger().info("Inserted new row for " + chipType + " chips for user: " + username);
            }
        } catch (SQLException e) {
            getLogger().error("Unable to update " + chipType + " chips for the user: " + username, e);
        }
    }


    public void updateRabbitBalance(String username, String amount) {
        String userId = getUserID(username);
        if (!userId.equalsIgnoreCase("")) {
            // Updating VIP chips
            String rabbitStatusQuery = "Select count(chip_type) from " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts where user_id = " + userId + " and chip_type = 'RABBIT'";
            try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(rabbitStatusQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt("count(chip_type)");
                getLogger().info(count + " rows found for RABBIT for user: " + username);
                // If row for RABBIT already exists in game account, update the value. If it does not exist insert a new row with required values.
                if (count > 0) {
                    String updateRabbitQuery = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts set chips = " + amount + " where user_id = " + userId + " and chip_type = 'RABBIT'";
                    preparedStatement.executeUpdate(updateRabbitQuery);
                    getLogger().info("Updated RABBIT balance for user:" + username + " to " + amount);
                } else if (count == 0) {
                    getLogger().info("Row for RABBIT chips does not exist for user: " + username + ". Inserting a new row");
                    String insertRabbitQuery = "Insert into " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts (user_id, chip_type, chips, chips_in_play, version) values (" + userId + ", 'RABBIT', " + amount + ", 0, 1)";
                    preparedStatement.executeUpdate(insertRabbitQuery);
                    getLogger().info("Updated RABBIT balance for user:" + username + " to " + amount);
                }
            } catch (SQLException e) {
                getLogger().error("Unable to update RABBIT chips for the user: " + username, e);
            }
        }
    }

    public String getSignUpOtp(String mobile) {
        String otp = "";
        String query = "Select transaction_token from " + DatabaseManager.dataBasePrefix + "cardplay.cp_mobile_verification_pages where mobile = '" + mobile + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            getLogger().info("Getting sing up OTP for user mobile: " + mobile);
            otp = resultSet.getString("transaction_token");
            getLogger().info("OTP for mobile number " + mobile + " is: " + otp);
        } catch (Exception e) {
            getLogger().error("Unable to get OTP for mobile: " + mobile, e);
        }
        return otp;

    }

    public void setCrowns(String username, String crowns) {
        String userId = getUserID(username);
        int count;
        String crownsStatusQuery = "Select count(total_crowns) from " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(crownsStatusQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            count = resultSet.getInt("count(total_crowns)");

            if (count > 0) {
                String updateCrownsQuery = "Update " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty set total_crowns = " + crowns + " where user_id = " + userId;
                preparedStatement.executeUpdate(updateCrownsQuery);
                getLogger().info("Updated CROWN balance for user:" + username + " to " + crowns);
            } else {
                String insertCrownsQuery = "Insert into " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty (user_id, total_crowns, current_loyalty_points, unused_loyalty_points, level_id, last_modified) values (" + userId + ", " + crowns + ", 0, 0, 1, CURRENT_TIMESTAMP)";
                preparedStatement.executeUpdate(insertCrownsQuery);
                getLogger().info("Updated CROWN balance for user:" + username + " to " + crowns);

            }
        } catch (SQLException e) {
            getLogger().error("Unable to fetch crowns details for user: " + username, e);
        }
    }

    public String getIbBalance(String username) {
        String totalIbBalance;
        double ib = 0;
        double promo = 0;
        String userId = getUserID(username);
        String queryToGetIbFromUserAccount = "Select * from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_account where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryToGetIbFromUserAccount);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            promo = resultSet.getDouble("promo_chips");
        } catch (SQLException e) {
            getLogger().error("Unable to get data from cp_user_account", e);
        }
        String queryToGetIbFromGameAccount = "Select chips from " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts where chip_type = 'VIP_IB' and user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryToGetIbFromGameAccount);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            ib = resultSet.getDouble("chips");
        } catch (SQLException e) {
            getLogger().error("Unable to fetch VIP_IB from username: " + username, e);
        }
        totalIbBalance = String.valueOf(ib + promo);
        return totalIbBalance;
    }

    public String getTbBalance(String username) {
        String totalTbBalance;
        double tb = 0;
        double tb_chips = 0;
        String userId = getUserID(username);
        String queryToGetIbFromUserAccount = "Select * from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_account where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryToGetIbFromUserAccount);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            tb_chips = resultSet.getDouble("tb_chips");
        } catch (SQLException e) {
            getLogger().error("Unable to get data from cp_user_account", e);
        }
        String queryToGetTbFromGameAccount = "Select chips from " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts where chip_type = 'VIP_TB' and user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryToGetTbFromGameAccount);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            tb = resultSet.getDouble("chips");
        } catch (SQLException e) {
            getLogger().error("Unable to fetch VIP_TB from username: " + username, e);
        }
        totalTbBalance = String.valueOf(tb + tb_chips);
        return totalTbBalance;
    }

    public String getVipBalance(String username) {
        String totalVipBalance;
        double vip = 0;
        double deposit = 0;
        double real_chips = 0;
        String userId = getUserID(username);
        String queryToGetVipFromUserAccount = "Select * from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_account where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryToGetVipFromUserAccount);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            deposit = resultSet.getDouble("deposite");
            real_chips = resultSet.getDouble("real_chips");
        } catch (SQLException e) {
            getLogger().error("Unable to get data from cp_user_account", e);
        }

        String queryToGetVipFromGameAccount = "Select chips from " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts where chip_type = 'VIP' and user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryToGetVipFromGameAccount);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            vip = resultSet.getDouble("chips");
        } catch (SQLException e) {
            getLogger().error("Unable to fetch VIP from username: " + username, e);
        }
        totalVipBalance = String.valueOf(vip + deposit + real_chips);
        return totalVipBalance;
    }

    public double getRabbitBalance(String username) {
        double chipTypeRabbitAmt = 0.00;
        String query = "SELECT chips FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts WHERE chip_type='RABBIT' AND user_id = (SELECT user_id FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user WHERE user_name= '" + username + "')";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            chipTypeRabbitAmt = Double.parseDouble(resultSet.getString("chips"));
        } catch (Exception e) {
            getLogger().error("Unable to get RABBIT Amount for user: " + username, e);
        }
        return chipTypeRabbitAmt;
    }

    public double getBBBalance(String username) {
        double bb = 0.00;
        String query = "SELECT chips FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts WHERE chip_type='PAYOFF_BONUS' AND user_id = (SELECT user_id FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user WHERE user_name= '" + username + "')";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            bb = Double.parseDouble(resultSet.getString("chips"));
        } catch (Exception e) {
            getLogger().error("Unable to get BB Amount for user: " + username, e);
        }
        return bb;
    }

    public void blockUser(String username, int blockType) {
        String userId = getUserID(username);
        long startTime = System.currentTimeMillis() / 1000L;
        long endTime = startTime + 345600L;
        if (!userId.equalsIgnoreCase("")) {
            // Updating VIP chips
            String userBlockStatus = "Select count(user_id) from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_block where user_id = " + userId;
            try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(userBlockStatus);
                 ResultSet blockedResultSet = preparedStatement.executeQuery()) {
                blockedResultSet.next();
                int count = blockedResultSet.getInt("count(user_id)");
                getLogger().info(count + " rows found for blocked user: " + username);
                // If row for blocked user already exists in db, update the value. If it does not exist insert a new row with required values.
                if (count > 0) {
                    String blockQuery = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_block set block_type = " + blockType + ", status = 1, start_time = " + startTime + ", end_time = " + endTime + ", block_time = " + startTime;
                    preparedStatement.executeUpdate(blockQuery);
                    getLogger().info("Blocked user: " + username);
                } else if (count == 0) {
                    String insertBlockQuery = "INSERT INTO " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_block (user_id, block_type, status, start_time, end_time, block_time, unblock_time, dispute_time, block_admin_name, unblock_admin_name, reason, unblock_remarks, remarks, dispute_remark, user_action, dispute_raised, approved_by, linux_modified_on, linux_added_on) values (" + userId + ", " + blockType + ", 1, " + startTime + ", " + endTime + ", " + startTime + ", 0, 0, 'mohit', '', 'Chip Dumping', '', 'You are blocked bro', '', 0,0,'', " + startTime + ", " + startTime + ")";
                    preparedStatement.executeUpdate(insertBlockQuery);
                    getLogger().info("Blocked insert user: " + username);
                }

                if (blockType == 1) {
                    String blockType1Query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set status = 'temp blocked' where user_id = " + userId;
                    preparedStatement.executeUpdate(blockType1Query);
                    getLogger().info("Blocked user: " + username + " temporarily");
                } else if (blockType == 2) {
                    String blockType2Query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set status = 'fixed blocked' where user_id = " + userId;
                    preparedStatement.executeUpdate(blockType2Query);
                } else if (blockType == 3) {
                    String blockType3Query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set status = 'blocked' where user_id = " + userId;
                    preparedStatement.executeUpdate(blockType3Query);
                }
            } catch (SQLException e) {
                getLogger().error("Unable to block user: " + username, e);
            }
        }
    }

    public int getDisputeStatus(String username) {
        int status = 404;
        String userId = getUserID(username);
        String query = "Select dispute_raised from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_block where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            status = resultSet.getInt("dispute_raised");
        } catch (Exception e) {
            getLogger().error("Unable to get dispute status", e);
        }
        return status;
    }

    public void unblockUser(String username) {
        String userID = getUserID(username);
        getLogger().info("Unblocking user: " + username);
        String delQuery = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_block WHERE user_id = " + userID;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(delQuery)) {
            preparedStatement.executeUpdate();
            String updateQuery = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set status = 'active' where user_id = " + userID;
            preparedStatement.executeUpdate(updateQuery);
            getLogger().info("Unblocked user: " + username);
        } catch (SQLException e) {
            getLogger().error("Unable to unblock user: " + username, e);
        }

    }

    public void resetUserPassword(String password, String username) {
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set password = '" + password + "' where user_name ='" + username + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
            getLogger().info("Password reset for user: " + username);
        } catch (SQLException e) {
            getLogger().info("Unable to reset password for user: " + username, e);
        }

    }

    public String getUserEncryptedPass(String username) {
        String query = "Select password from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user where user_name = '" + username + "'";
        String password = "";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            password = resultSet.getString("password");
        } catch (Exception e) {
            getLogger().error("Unable to get user password", e);
        }
        return password;
    }

    public HashMap<String, String> getUserPreferences(String username) {
        HashMap<String, String> preferences = new HashMap<>();
        String userID = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_preferences WHERE user_id = " + userID;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            preferences.put("auto_post_BB", resultSet.getString(String.valueOf(KeyReferences.PREFERENCES.auto_post_BB)));
            preferences.put("auto_muck", resultSet.getString(String.valueOf(KeyReferences.PREFERENCES.auto_muck)));
            preferences.put("show_hand_strength", resultSet.getString(String.valueOf(KeyReferences.PREFERENCES.show_hand_strength)));
            preferences.put("is_config_view", resultSet.getString(String.valueOf(KeyReferences.PREFERENCES.is_config_view)));
            preferences.put("sound_mute", resultSet.getString(String.valueOf(KeyReferences.PREFERENCES.sound_mute)));
            preferences.put("vibrate_mode", resultSet.getString(String.valueOf(KeyReferences.PREFERENCES.vibrate_mode)));
        } catch (SQLException e) {
            getLogger().error("Unable to get user preferences");
        }
        return preferences;
    }

    public void setBoosterBonus(String username, String amount) {
        String userId = getUserID(username);
        if (!userId.equalsIgnoreCase("")) {
            // Updating VIP chips
            String bbStatusQuery = "Select count(chip_type) from " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts where user_id = " + userId + " and chip_type = 'PAYOFF_BONUS'";
            try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(bbStatusQuery);
                 ResultSet resultSetBoosterBonus = preparedStatement.executeQuery()) {
                resultSetBoosterBonus.next();
                int count = resultSetBoosterBonus.getInt("count(chip_type)");
                getLogger().info(count + " rows found for Booster Bonus for user: " + username);
                // If row for Booster Bonus already exists in game account, update the value. If it does not exist insert a new row with required values.
                if (count > 0) {
                    String updateBbQuery = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts set chips = " + amount + " where user_id = " + userId + " and chip_type = 'PAYOFF_BONUS'";
                    preparedStatement.executeUpdate(updateBbQuery);
                    getLogger().info("Updated Booster Bonus balance for user:" + username + " to " + amount);
                } else if (count == 0) {
                    getLogger().info("Row for Booster Bonus chips does not exist for user: " + username + ". Inserting a new row");
                    String insertBbQuery = "Insert into " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts (user_id, chip_type, chips, chips_in_play, version) values (" + userId + ", 'PAYOFF_BONUS', " + amount + ", 0, 1)";
                    preparedStatement.executeUpdate(insertBbQuery);
                    getLogger().info("Updated Booster Bonus balance for user:" + username + " to " + amount);
                }
            } catch (SQLException e) {
                getLogger().error("Unable to update Booster Bonus chips for the user: " + username, e);
            }
        }
    }

    public void clearFavoriteTableMapping(String username) {
        String userId = getUserID(username);
        String updateQuery = "Delete from " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_fav_game_mapping where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.executeUpdate();
            getLogger().info("Cleared favorite table mapping for user: " + username);
        } catch (Exception e) {
            getLogger().error("Unable to clear favorite table mapping.", e);
        }
    }

    public String getUserFavGameMappingStatus(String username, String configId) {
        String userId = getUserID(username);
        String query = "SELECT status FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_fav_game_mapping where game_config_id= " + configId + " AND user_id= " + userId;
        String favStatus = "";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            favStatus = resultSet.getString("status");
        } catch (Exception e) {
            getLogger().error("Unable to get Favourite Status", e);
        }
        ExecutionController.pauseExecution(3);
        return favStatus;
    }

    public int getCountOfActiveFavoriteTables(String username) {
        String userId = getUserID(username);
        String query = "SELECT count(status) FROM `" + DatabaseManager.dataBasePrefix + "cardplay_poker`.`cp_user_fav_game_mapping` where status='ACTIVE' AND user_id=" + userId;
        int favStatusCount = 0;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            favStatusCount = Integer.parseInt(resultSet.getString("count(status)"));
        } catch (Exception e) {
            getLogger().error("Unable to get favourite Status", e);
        }
        return favStatusCount;
    }

    public void updateSettings(String property, String value, String username) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_settings set property_value = " + value + " where user_id = " + userId + " and property_name = '" + property + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
            getLogger().info("Updated setting: " + property + " for user: " + username + " to " + value);
        } catch (SQLException e) {
            getLogger().error("Unable to update setting: " + property + " for user: " + username);
        }
    }

    public HashMap<String, String> getUserSettings(String username) {
        HashMap<String, String> preferences = new HashMap<>();
        String userID = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_settings WHERE user_id=" + userID;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                preferences.put(resultSet.getString("property_name"), resultSet.getString("property_value"));
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get user Settings value");
        }
        return preferences;
    }

    public void deleteUserNotes(String username) {
        String userId = getUserID(username);
        if (!userId.equalsIgnoreCase("")) {
            String query = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_notes WHERE user_id=" + userId;
            try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                getLogger().info(" Row not found because it is Already Deleted form the `" + DatabaseManager.dataBasePrefix + "cardplay`.`cp_game_accounts` for the User: " + username);
            }
        }
    }

    public HashMap<String, String> getUserNotes(String username, String opponentUsername) {
        HashMap<String, String> preferences = new HashMap<>();
        String userID = getUserID(username);
        String opponentUserId = getUserID(opponentUsername);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_notes WHERE user_id = " + userID + " AND " + opponentUserId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            preferences.put("user_id", resultSet.getString(String.valueOf(KeyReferences.NOTES.user_id)));
            preferences.put("opponent_id", resultSet.getString(String.valueOf(KeyReferences.NOTES.opponent_id)));
            preferences.put("note", resultSet.getString(String.valueOf(KeyReferences.NOTES.note)));
            preferences.put("is_active", resultSet.getString(String.valueOf(KeyReferences.NOTES.is_active)));
            preferences.put("color", resultSet.getString(String.valueOf(KeyReferences.NOTES.color)));
        } catch (SQLException e) {
            getLogger().error("Unable to get user NOTES");
        }
        return preferences;
    }

    public int getCountOfNotes(String username, String opponentUsername) {
        String userId = getUserID(username);
        String opponentUserId = getUserID(opponentUsername);
        String query = "SELECT count(*) FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_notes WHERE user_id = " + userId + " AND " + opponentUserId;
        int notesCount = 0;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            notesCount = Integer.parseInt(resultSet.getString("count(*)"));
        } catch (Exception e) {
            getLogger().error("Unable to get Notes", e);
        }
        return notesCount;
    }

    public void clearPrivateTablesForUser(String username) {
        String toEmail = getUserID(username);
        String query = "Select game_config_id from " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_table_invite WHERE to_email = " + toEmail;
        ArrayList<String> data = new ArrayList<>();
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                data.add(resultSet.getString("game_config_id"));
            }


            if (!data.isEmpty()) {
                for (String configId : data) {
                    getLogger().info("Marking " + configId + " as bad");
                    String updateQuery = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_game_config SET STATUS = 'buddy.bad' WHERE id = " + configId;
                    preparedStatement.executeUpdate(updateQuery);
                }
                getLogger().info("Deleting entry for " + toEmail + " from " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_table_invite");
                String deleteQuery = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_table_invite WHERE to_email = " + toEmail;
                preparedStatement.executeUpdate(deleteQuery);
            } else {
                getLogger().info("No private tables found for the user");
            }
        } catch (Exception e) {
            getLogger().error("Unable to get data for private tables", e);
        }

    }

    public String getPrivateTablePIN(String username) {
        String toEmail = getUserID(username);
        String query = "SELECT game_config_id FROM  " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_table_invite WHERE to_email= " + toEmail;
        String PIN = "";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            String configId = resultSet.getString("game_config_id");
            String query2 = " SELECT prop_value FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_tournament_prop WHERE prop_name= 'PNR' AND game_config_id= " + configId;
            ResultSet resultSet1 = preparedStatement.executeQuery(query2);
            resultSet1.next();
            PIN = "PIN : " + resultSet1.getString("prop_value");
            getLogger().info(PIN);
        } catch (Exception var7) {
            getLogger().error("Unable to fetch PIN", var7);
        }

        return PIN;
    }

    public String getExpiryDateOfPrivateTable(String username) {
        String toEmail = getUserID(username);
        String query = " SELECT game_config_id FROM  " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_table_invite WHERE to_email= " + toEmail;
        String expiryDate = "";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            String configId = resultSet.getString("game_config_id");
            String query2 = "SELECT end_date FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_game_config WHERE id= " + configId;
            ResultSet resultSet2 = preparedStatement.executeQuery(query2);
            getLogger().info("Fetching expiry date for " + configId);
            resultSet2.next();
            expiryDate = resultSet2.getString("end_date");
            getLogger().info(expiryDate);
        } catch (Exception e) {
            getLogger().error("Unable to fetch expiry date ", e);
        }
        return expiryDate;
    }

    public void updateExpiryDateOfPrivateTable(String username) {
        String toEmail = getUserID(username);
        String query = "Select game_config_id from " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_table_invite WHERE to_email = " + toEmail;

        ArrayList<String> data = new ArrayList<>();

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                data.add(resultSet.getString("game_config_id"));
            }


            if (!data.isEmpty()) {
                for (String configId : data) {
                    getLogger().info("Updating expiry date of " + configId);
                    String updateQuery = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_game_config SET end_date = '2022-10-01 00:00:45' WHERE id = " + configId;
                    preparedStatement.executeUpdate(updateQuery);
                }
            } else {
                getLogger().info("No private tables found for the user");
            }
        } catch (Exception e) {
            getLogger().error("Unable to get data for private tables", e);
        }
    }

    public void setLoyaltyLevel(String username, int levelId) {
        String userId = getUserID(username);
        String selectQuery = "SELECT COUNT(*) FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty WHERE user_id = ?";
        String updateQuery = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty SET level_id = ? WHERE user_id = ?";
        String insertQuery = "INSERT INTO " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty (user_id, total_loyalty_points, current_loyalty_points, unused_loyalty_points, level_id, grace, last_modified, pgp_m, pgp_w) " +
                "VALUES (?, 0, 0, 0, ?, -1, CURRENT_TIMESTAMP, 0.000, 0.000)";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

            selectStatement.setString(1, userId);
            ResultSet resultSet = selectStatement.executeQuery();
            resultSet.next();

            int count = resultSet.getInt(1);

            if (count == 0) {
                insertStatement.setString(1, userId);
                insertStatement.setInt(2, levelId);
                insertStatement.executeUpdate();
                getLogger().info("Entry in loyalty table does not exist. Inserted new row.");
            } else {
                updateStatement.setInt(1, levelId);
                updateStatement.setString(2, userId);
                updateStatement.executeUpdate();
                getLogger().info("Loyalty level updated to " + levelId + " for user " + username);
            }

        } catch (SQLException e) {
            getLogger().error("Unable to set loyalty level for the user: " + username, e);
        }
    }

    public String getLevelId(String username) {
        String userId = getUserID(username);
        String levelId = " ";
        String query = "SELECT level_id FROM  " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty where user_id = '" + userId + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            levelId = resultSet.getString("level_id");
        } catch (SQLException e) {
            getLogger().error("Unable to get level id for user: " + username, e);
        }
        return levelId;
    }

    public String getClubLevelName(String username) {
        String levelId = getLevelId(username);
        String clubName = " ";
        String query = "SELECT level FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_level WHERE id = '" + levelId + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            clubName = resultSet.getString("level");
        } catch (SQLException e) {
            getLogger().error("Unable to get level name  for user: " + username, e);
        }
        return clubName;
    }


    public void setLoginAttempt(String username, int count) {
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set attempt = " + count + " where user_name = '" + username + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
            getLogger().info("Login attempt count set to: " + count + " for user: " + username);
        } catch (SQLException e) {
            getLogger().error("Unable to set login attempt count for user: " + username, e);
        }
    }

    public void updateEmailAddress(String username, String email, String status) {
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set email = '" + email + "', status = '" + status + "'where user_name = '" + username + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
            getLogger().info("Email for user " + username + " updated to: " + email);
        } catch (SQLException e) {
            getLogger().error("Unable to update email for user: " + username, e);
        }
    }

    public void setAutoBuyIn(String username, String status, String bbValue) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_settings set property_value = '" + status + "' where property_name = 'NG_AUTO_BUY_IN' and user_id = " + userId;
        String query2 = "Update " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_settings set property_value = '" + bbValue + "' where property_name = 'NG_AUTO_BUY_IN_BB' and user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
            preparedStatement.executeUpdate(query2);
            getLogger().info("Auto buy in option was reset for user: " + username);
        } catch (SQLException e) {
            getLogger().error("Unable to reset aut buy in for user: " + username, e);
        }
    }

    public void setStateBlockedStatus(String city, String state, int blockStatus) {
        String queryCount = "SELECT COUNT(city_id) FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_cities_access_list WHERE city_name = '" + city + "' AND city_state = '" + state + "'";
        String insertGeo = "INSERT INTO " + DatabaseManager.dataBasePrefix + "cardplay.cp_cities_access_list (city_name,city_state,access_status,rummy_access_status) VALUES ('" + city + "','" + state + "'," + blockStatus + "," + blockStatus + ")";
        String updateGeo = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_cities_access_list set access_status = " + blockStatus + ", rummy_access_status= " + blockStatus + " where city_name = '" + city + "' AND city_state = '" + state + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(queryCount); ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            int count = resultSet.getInt("Count(city_id)");
            getLogger().info(count);
            if (count == 0) {
                getLogger().info("State does not exists. Inserting..");
                preparedStatement.executeUpdate(insertGeo);
            } else {
                preparedStatement.executeUpdate(updateGeo);
                getLogger().info("State exists. Updating..");
            }
        } catch (SQLException e) {
            getLogger().error("Unable to setStateBlockStatus", e);
        }

    }

    public HashMap<Object, Object> getUserProfileInfo(String username) {
        HashMap<Object, Object> profileInfo = new HashMap<>();
        String userID = getUserID(username);
        String query = "Select * From " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile WHERE user_id =" + userID;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            profileInfo.put("mobile", resultSet.getString("mobile"));
            profileInfo.put("isapproved_PAN", Integer.parseInt(resultSet.getString("isapproved_PAN")));
            profileInfo.put("first_name", resultSet.getString("first_name"));
            profileInfo.put("is_mobile_verified", resultSet.getString("is_mobile_verified"));
        } catch (SQLException e) {
            getLogger().error("Unable to get user profile info ");
        }
        return profileInfo;
    }

    public HashMap<String, String> getUserInfo(String username) {
        HashMap<String, String> userInfo = new HashMap<>();
        String userID = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user WHERE user_id= " + userID;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            userInfo.put("user_name", resultSet.getString("user_name"));
            userInfo.put("email", resultSet.getString("email"));
            userInfo.put("last_login", resultSet.getString("last_login"));

        } catch (SQLException e) {
            getLogger().info("Unable to get user info");
        }
        return userInfo;
    }

    public int getOddEvenStatus(String mobile) {
        int num;
        int value = 0;
        String query = "Select user_id from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile where mobile = '" + mobile + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            num = Integer.parseInt(resultSet.getString("user_id"));
            value = value + num % 2;
        } catch (Exception e) {
            getLogger().error("Not able to fetch odd even status", e);
        }
        return value;
    }

    public void updateKycDataInApplication(String username, String isKycVerified, String isPanVerified, String isBankVerified, String isSelfieVerified) {
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection()) {
            String selectKycQuery = "SELECT user_id FROM dev_gauss_kyc.`application_users` WHERE app_user_id = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectKycQuery)) {
                String userId = getUserID(username);
                selectStatement.setString(1, userId);

                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int kycUserId = resultSet.getInt("user_id");

                        String updateKycQuery = "UPDATE dev_gauss_kyc.`application_users` SET `is_kyc_verified` = ?, `is_pan_verified` = ?, `is_bank_verified` = ?, `is_selfie_verified` = ?, `is_selfie_mandatory` = '1' WHERE `application_users`.`user_id` = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateKycQuery)) {
                            updateStatement.setString(1, isKycVerified);
                            updateStatement.setString(2, isPanVerified);
                            updateStatement.setString(3, isBankVerified);
                            updateStatement.setString(4, isSelfieVerified);
                            updateStatement.setInt(5, kycUserId);
                            updateStatement.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("Unable to update the data in the user status table: ", e);
        }
    }

    public void updateUserBankDetailsInUserBankInfo(String username, String isBankVerified, String isBankDefault, String isBankDeleted, String bankStatus, String bankLastFourDigit) {
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection()) {
            connection.setAutoCommit(false); // Start a transaction

            String userId = getUserID(username);
            int kycUserId = 0;

            String selectQuery = "SELECT au.user_id, ubi.count FROM dev_gauss_kyc.`application_users` au LEFT JOIN (SELECT user_id, COUNT(*) AS count FROM dev_gauss_kyc.`user_bank_info` GROUP BY user_id) ubi ON au.user_id = ubi.user_id WHERE au.app_user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        kycUserId = resultSet.getInt("user_id");
                        int count = resultSet.getInt("count");

                        if (count == 0) {
                            String insertQuery = "INSERT INTO dev_gauss_kyc.`user_bank_info` (`user_id`, `account_name`, `account_number`, `reason`, `account_type`, `ifsc_id`, `added_on`, `modified_on`, `is_verified`, `is_default`, `is_deleted`, `status`, `approved_mode`, `verification_level`, `is_idfy_failure`, `upload_by_admin`) VALUES (?, ?, ?, ?, NULL, 1364, ?, ?, '1', '1', '0', 'verified', 'server', 'doc_validate', '0', 0)";
                            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                                insertStatement.setInt(1, kycUserId);
                                insertStatement.setString(2, username);
                                insertStatement.setString(3, "61234567890");
                                insertStatement.setString(4, "Running Auto Script for testing");
                                insertStatement.setString(5, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                                insertStatement.setString(6, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                                insertStatement.executeUpdate();
                            }
                        } else {
                            getLogger().info("Bank detail Information already exists for the user " + username + " : UserId : " + kycUserId);
                            String updateQuery = "UPDATE dev_gauss_kyc.`user_bank_info` SET `is_verified` = ?, `is_default` = ?, `is_deleted` = ?, `status` = ? WHERE `user_bank_info`.`user_id` = ? AND account_number LIKE CONCAT('%', ?, '%')";
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                updateStatement.setString(1, isBankVerified);
                                updateStatement.setString(2, isBankDefault);
                                updateStatement.setString(3, isBankDeleted);
                                updateStatement.setString(4, bankStatus);
                                updateStatement.setInt(5, kycUserId);
                                updateStatement.setString(6, bankLastFourDigit);
                                updateStatement.executeUpdate();
                                getLogger().info("Bank detail Information updated successfully for the user " + username + " : UserId : " + kycUserId);
                            }
                        }
                    }
                }
            }

            connection.commit(); // Commit the transaction
        } catch (Exception e) {
            getLogger().error("Unable to update the Bank details for user: " + username, e);
        }
    }

    public String getAccountNumberInUserBankInfo(String username) {
        String userId = getUserID(username);
        String accNum = "";
        int kycUserId = 0;

        String selectQuery = "SELECT au.user_id, ubi.account_number FROM dev_gauss_kyc.`application_users` au LEFT JOIN dev_gauss_kyc.`user_bank_info` ubi ON au.user_id = ubi.user_id WHERE au.app_user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    kycUserId = resultSet.getInt("user_id");
                    accNum = resultSet.getString("account_number");
                    getLogger().info(" ---> Kyc User ID : " + kycUserId);
                }
            }
        } catch (Exception e) {
            getLogger().error("Unable to retrieve the bank detail for user: " + username, e);
        }

        return accNum;
    }

    public void updateUserPanDetailInPanInfoTable(String username) {
        String userId = getUserID(username);
        int count = 0;
        int kycUserId = 0;

        String selectQuery = "SELECT au.user_id, IFNULL((SELECT COUNT(*) FROM dev_gauss_kyc.`user_pan_info` WHERE user_id = au.user_id), 0) AS count FROM dev_gauss_kyc.`application_users` au WHERE au.app_user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    kycUserId = resultSet.getInt("user_id");
                    count = resultSet.getInt("count");
                    getLogger().info(" ---> Kyc User ID : " + kycUserId);
                    getLogger().info(" ---> count : " + count);

                    if (count == 0) {
                        String insertQuery = "INSERT INTO dev_gauss_kyc.`user_pan_info` (`user_id`, `pan_number`, `pan_name`, `pan_document`, `dob`, `added_on`, `modified_on`, `status`, `reason`, `is_deleted`, `approved_mode`, `verification_level`, `is_idfy_failure`) VALUES (?, 'COUPM8322J', ?, '1630247067062pickedMedia1.png', '1991-08-15', ?, ?, 'verified', 'Automation test script for testing', '0', 'server', 'doc_validate', '0')";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setInt(1, kycUserId);
                            insertStatement.setString(2, username + " Test");
                            insertStatement.setString(3, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                            insertStatement.setString(4, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                            insertStatement.executeUpdate();
                        }
                    } else {
                        getLogger().info("PAN detail Information already exists for the user " + username + " : UserId : " + kycUserId);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("Unable to update the PAN details for user: " + username, e);
        }
    }

    public void updateUserAddressDetailsInApplicationUserProfileTable(String username) {
        String userId = getUserID(username);
        int count = 0;
        int kycUserId = 0;

        String selectQuery = "SELECT au.user_id, IFNULL((SELECT COUNT(*) FROM dev_gauss_kyc.`application_users_profile` WHERE user_id = au.user_id), 0) AS count FROM dev_gauss_kyc.`application_users` au WHERE au.app_user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    kycUserId = resultSet.getInt("user_id");
                    count = resultSet.getInt("count");
                    getLogger().info(" ---> Kyc User ID : " + kycUserId);
                    getLogger().info(" ---> count : " + count);

                    if (count == 0) {
                        String insertQuery = "INSERT INTO dev_gauss_kyc.`application_users_profile` (`user_id`, `name`, `dob`, `address`, `district`, `pincode`, `state`, `street_address`, `doc_id`, `added_on`, `modified_on`) VALUES (?, ?, '1991-08-15', 'TESTING BAZAR Of PlayPoker webclient', 'GURUGRAM', '122016', 'HARYANA', 'DLF Road', 57, ?, ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setInt(1, kycUserId);
                            insertStatement.setString(2, username);
                            insertStatement.setString(3, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                            insertStatement.setString(4, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                            insertStatement.executeUpdate();
                        }
                    } else {
                        getLogger().info("Kyc Document Information already exists for the user " + username + " : UserId : " + kycUserId);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("Unable to update the Kyc Document details for user: " + username, e);
        }
    }

    public void updateUserKycDocInUserKycInfoTable(String username) {
        String userId = getUserID(username);
        int count = 0;
        int kycUserId = 0;

        String selectQuery = "SELECT au.user_id, IFNULL((SELECT COUNT(*) FROM dev_gauss_kyc.user_kyc_info WHERE user_id = au.user_id), 0) AS count FROM dev_gauss_kyc.application_users au WHERE au.app_user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    kycUserId = resultSet.getInt("user_id");
                    count = resultSet.getInt("count");
                    getLogger().info(" ---> Kyc User ID : " + kycUserId);
                    getLogger().info(" ---> count : " + count);

                    if (count == 0) {
                        String insertQuery = "INSERT INTO dev_gauss_kyc.user_kyc_info (`doc_id`, `user_id`, `name`, `doc_number`, `gender`, `dob`, `file_1`, `file_2`, `added_on`, `modified_on`, `status`, `is_deleted`, `is_idfy_failure`, `reason`, `approved_mode`, `verification_level`, `upload_by_admin`) VALUES (57, ?, ?, 'Z3227822', 'male', '1991-08-15', '1630878770147rWoIMTUAzioHRxk4eAlYqufn.jpg', '1630878771228AuAlnVeL7CI_gVuRa00gOLYD.jpg', ?, ?, 'verified', '0', '0', 'Running Auto Script for testing', 'server', 'doc_validate', '0')";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setInt(1, kycUserId);
                            insertStatement.setString(2, username);
                            insertStatement.setString(3, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                            insertStatement.setString(4, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                            insertStatement.executeUpdate();
                        }
                    } else {
                        getLogger().info("Kyc Document Information already exists for the user " + username + " : UserId : " + kycUserId);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("Unable to update the Kyc Document details for user: " + username, e);
        }
    }

    public void updateEmailAndMobileVerifiedFromUserProfileTable(String username, String isApprovedPan, String isEmailVerified, String isMobileVerified, String mobile) {
        String userId = getUserID(username);

        String selectQuery = "SELECT COUNT(*) AS count FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    getLogger().info(" ---> count : " + count);

                    if (count == 0) {
                        String insertQuery = "INSERT INTO " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile (`user_id`, `first_name`, `last_name`, `dob`, `gender`, `mobile`, `primary_wrong_number`, `alternate_number`, `alternate_wrong_number`, `isapproved_PAN`, `preferred_number`, `profile_pic`, `date_of_birth`, `call_from`, `call_to`, `compliance_tag`, `last_tagged_date`, `is_dnc`, `ivr_status`, `is_email_verified`, `is_mobile_verified`, `is_alt_mobile_verified`, `mobileCookiecheck`, `network_id`, `modified_on`, `linux_modified_on`) VALUES (?, ?, 'mk2', '1991', 'male', ?, '0', NULL, '0', '1', NULL, '', '', '', '', ?, '0', '0', '1', '1', '0', '0', '0', CURRENT_TIMESTAMP(), ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, userId);
                            insertStatement.setString(2, username);
                            insertStatement.setString(3, mobile);
                            insertStatement.setString(4, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                            insertStatement.setString(5, SupportUtils.epochTimeFormat("yyyy/MM/dd HH:mm:ss", SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss")));
                            insertStatement.executeUpdate();
                        }
                    } else {
                        String updateQuery = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay.`cp_user_profile` SET `first_name` = ?, `last_name` = 'mk2', `mobile` = ?, `isapproved_PAN` = ?, `is_email_verified` = ?, `is_mobile_verified` = ? WHERE `cp_user_profile`.user_id = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setString(1, username);
                            updateStatement.setString(2, mobile);
                            updateStatement.setString(3, isApprovedPan);
                            updateStatement.setString(4, isEmailVerified);
                            updateStatement.setString(5, isMobileVerified);
                            updateStatement.setString(6, userId);
                            updateStatement.executeUpdate();
                        }
                        getLogger().info("Updated the User Profile for the user: " + username);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("Unable to update the User Profile table for user: " + username, e);
        }
    }

    public void updateUserPurchaseSettingsTable(String username, String blockOrUnblock) {
        String userId = getUserID(username);
        String updateQuery = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_purchase_settings SET purchase_status = ?  WHERE `cp_user_purchase_settings`.`user_id` = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setString(1, blockOrUnblock);
            updateStatement.setString(2, userId);
            updateStatement.executeUpdate();
            getLogger().info("Updated successfully the Purchase Settings for the user: " + username);
        } catch (Exception e) {
            getLogger().error("Unable to update the cp_user_purchase_settings table form user and not allow yet to purchase :  " + username, e);
        }
    }

    public void resetSignUpRestriction(String username) {
        String prefix = DatabaseManager.dataBasePrefix;
        String userId = getUserID(username);
        String useQuery = "USE " + prefix + "cardplay";
        String queryUser = "UPDATE " + prefix + "cardplay.cp_user SET ip_address = '' WHERE user_id = " + userId;
        String querySource = "UPDATE " + prefix + "cardplay.cp_source SET special_tag = '' WHERE user_id = " + userId;

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryUser); Statement statement = connection.createStatement()) {
            statement.execute(useQuery);
            preparedStatement.executeUpdate();
            preparedStatement.executeUpdate(querySource);
            getLogger().error("Signup source reset success for username: " + username);
        } catch (Exception e) {
            getLogger().error("Unable to reset signup source", e);
        }
    }

    public Map<String, String> getCpSourceData(String username) {
        Map<String, String> data = new HashMap<>();
        String userId = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_source WHERE user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get source data from cp_source. ", e);
        }
        return data;

    }

    public void setEmailStatus(String username, String status) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile set is_email_verified = ? WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
            getLogger().error("Email status of user " + username + "set to " + status);
        } catch (Exception e) {
            getLogger().error("Unable to set email status. ", e);
        }
    }

    public void setMobileStatus(String username, String status) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile set is_mobile_verified = ? WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
            getLogger().error("Mobile number status of user " + username + "set to " + status);
        } catch (Exception e) {
            getLogger().error("Unable to set mobile status. ", e);
        }
    }


    public String getEmailOtp(String emailId) {
        String otp = "";
        String query = "Select transaction_token from " + DatabaseManager.dataBasePrefix + "cardplay.cp_email_verification_pages WHERE email  = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, emailId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            otp = resultSet.getString("transaction_token");
        } catch (SQLException e) {
            getLogger().error("Unable to get email OTP. ", e);
        }
        return otp;
    }

    public String getLastAddedBonusCodeId() {
        String bonusId = "";
        String query = "Select bonus_id from " + DatabaseManager.dataBasePrefix + "cardplay.cp_bonus_details ORDER BY detail_id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            bonusId = resultSet.getString("bonus_id");
        } catch (SQLException e) {
            getLogger().error("Unable to get bonus code ID", e);
        }
        return bonusId;
    }

    public String getLastAddedBonusCodeName() {
        String bonusName = "";
        String query = "Select bonus_code from " + DatabaseManager.dataBasePrefix + "cardplay.cp_bonus_code_list ORDER BY bonus_id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            bonusName = resultSet.getString("bonus_code");
        } catch (SQLException e) {
            getLogger().error("Unable to get bonus code name", e);
        }
        return bonusName;
    }

    public void deleteBonusCode(String bonusId) {
        String databaseName = DatabaseManager.dataBasePrefix + "cardplay";

        String useDatabaseQuery = "USE " + databaseName;

        String deleteQuery = "DELETE details, list " +
                "FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_bonus_details AS details " +
                "INNER JOIN " + DatabaseManager.dataBasePrefix + "cardplay.cp_bonus_code_list AS list " +
                "ON details.bonus_id = list.bonus_id " +
                "WHERE details.bonus_id = ?";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             Statement useDatabaseStatement = connection.createStatement();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

            useDatabaseStatement.execute(useDatabaseQuery);

            preparedStatement.setString(1, bonusId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to delete bonus code", e);
        }
    }

    public String getUserHoldAmount(String username) {
        String holdAmount = "";
        String userId = getUserID(username);
        String query = "SELECT chips from " + DatabaseManager.dataBasePrefix + "cardplay.cp_chip_accounts WHERE user_id = ? AND chip_type = 'HOLD_AMT'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            holdAmount = resultSet.getString("chips");
        } catch (SQLException e) {
            getLogger().error("Unable to get user's hold amount", e);
        }
        return holdAmount;
    }

    public void setHoldAmount(String username, String amount) {
        String userId = getUserID(username);
        String query = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay.cp_chip_accounts set chips = ? where user_id = ? and chip_type = 'HOLD_AMT'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, amount);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            getLogger().error("Unable to set user's hold amount", e);
        }
    }

    public HashMap<String, Double> getUserAccountInfo(String username) {
        HashMap<String, Double> accountInfo = new HashMap<>();
        String userID = getUserID(username);
        String query = "SELECT chip_type,chips FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts WHERE user_id= " + userID;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                try {
                    accountInfo.put(resultSet.getString("chip_type"), resultSet.getDouble("chips"));
                } catch (NumberFormatException e) {
                    getLogger().info("unable to fetch user account info");
                }
            }
        } catch (SQLException e) {
            getLogger().info("Unable to get game account info");
        }
        return accountInfo;
    }

    public String getKycUserId(String username) {
        String userId = getUserID(username);
        String query = "Select user_id from dev_gauss_kyc.application_users where app_user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getString("user_id");
        } catch (SQLException e) {
            getLogger().error("Unable to get kyc user id", e);
            return null;
        }
    }

    public void updateKycApplicationUsers(String username, String isKycVerified, String isPanVerified, String isBankVerified, String isSelfieVerified) {
        String query = "UPDATE dev_gauss_kyc.application_users SET " +
                "is_kyc_verified = ?," +
                "is_pan_verified = ?," +
                "is_bank_verified = ?," +
                "is_selfie_verified = ?," +
                "modified_on = CURRENT_TIMESTAMP() WHERE username = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, isKycVerified);
            preparedStatement.setString(2, isPanVerified);
            preparedStatement.setString(3, isBankVerified);
            preparedStatement.setString(4, isSelfieVerified);
            preparedStatement.setString(5, username);
            preparedStatement.executeUpdate();
            getLogger().info("Kyc application table updated for user: " + username);
        } catch (SQLException e) {
            getLogger().error("Unable to update kyc application users", e);
        }
    }

    public void updateUserBankInfo(String username, String isVerified, String isDefault, KycStatus status) {
        String kycUserId = getKycUserId(username);
        String query = "UPDATE dev_gauss_kyc.user_bank_info SET " +
                "modified_on= CURRENT_TIMESTAMP()," +
                "is_verified = ?," +
                "is_default = ?," +
                "STATUS = ?" +
                "WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, isVerified);
            preparedStatement.setString(2, isDefault);
            preparedStatement.setString(3, status.getStatus());
            preparedStatement.setString(4, kycUserId);
            preparedStatement.executeUpdate();
            getLogger().info("Bank Info updated for user: " + username);
        } catch (SQLException e) {
            getLogger().error("Unable to update Bank Info Table.", e);
        }
    }

    public void updateUserPanInfo(String username, KycStatus isVerified, KycStatus docStatus) {
        String kycUserId = getKycUserId(username);
        String query = "UPDATE dev_gauss_kyc.user_pan_info SET " +
                "modified_on= CURRENT_TIMESTAMP()," +
                "status = ?," +
                "verification_level = ? Where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, isVerified.getStatus());
            preparedStatement.setString(2, docStatus.getStatus());
            preparedStatement.setString(3, kycUserId);
            preparedStatement.executeUpdate();
            getLogger().info("Pan Info updated for user: " + username);
        } catch (SQLException e) {
            getLogger().error("Unable to update Pan Info Table.", e);
        }
    }

    public void updateUserKycInfo(String username, KycStatus isVerified, KycStatus docStatus) {
        String kycUserId = getKycUserId(username);
        String query = "UPDATE dev_gauss_kyc.user_kyc_info SET " +
                "modified_on= CURRENT_TIMESTAMP()," +
                "status = ?," +
                "verification_level = ? Where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, isVerified.getStatus());
            preparedStatement.setString(2, docStatus.getStatus());
            preparedStatement.setString(3, kycUserId);
            preparedStatement.executeUpdate();
            getLogger().info("KYC Info updated for user: " + username);
        } catch (SQLException e) {
            getLogger().error("Unable to KYC Pan Info Table.", e);
        }
    }

    public void clearAllTransactions(String username) {
        String prefix = DatabaseManager.dataBasePrefix;
        String userId = getUserID(username);

        String[] queries = {
                "DELETE FROM " + prefix + "cardplay.cp_purchase_transaction_history WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_transaction_history WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_user_premium_chips_transaction WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_pokerservice_crdr_txn WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_user_chip_txn_master WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_user_freeroll_txn WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_user_pw_txn WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_user_ticket_txn_master WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_user_txn_master WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_user_vip_txn WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.user_investment_ledger_txn WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_redeem WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_redeem_status WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.cp_user_insta_redeem_details WHERE user_id = " + userId,
                "DELETE FROM " + prefix + "cardplay.user_investment_ledger_status WHERE user_id = " + userId

        };

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(" ")) {
            for (String query : queries) {
                preparedStatement.addBatch(query);
            }
            preparedStatement.executeBatch();
            getLogger().info("Cleared all transactions for user: " + username);
        } catch (SQLException e) {
            getLogger().error("Unable to clear transactions for user " + username, e);
        }
    }

    public void updateUserLedgerStatus(String username, String totalInvestment, String totalTds, String totalRedeem) {
        String prefix = DatabaseManager.dataBasePrefix;
        String userId = getUserID(username);
        String queryCount = "SELECT COUNT(id) FROM " + prefix + "cardplay.user_investment_ledger_status WHERE user_id = " + userId;
        String queryUpdate = "Update " + prefix + "cardplay.user_investment_ledger_status set total_investment = ?, total_tds =?, total_redeem =?, updated_at = CURRENT_TIMESTAMP() where user_id = ?";
        String queryInsert = "INSERT INTO " + prefix + "cardplay.user_investment_ledger_status (user_id, total_investment, total_tds, total_redeem, VERSION, created_at, updated_at)" +
                "VALUES(?,?,?,?,0,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP())";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(queryCount);
            resultSet.next();
            int count = resultSet.getInt("count(id)");
            if (count == 1) {
                PreparedStatement preparedStatement = connection.prepareStatement(queryUpdate);
                preparedStatement.setString(1, totalInvestment);
                preparedStatement.setString(2, totalTds);
                preparedStatement.setString(3, totalRedeem);
                preparedStatement.setString(4, userId);
                preparedStatement.executeUpdate();
                getLogger().info("Ledger status updated for user: " + username);
            }
            if (count == 0) {
                PreparedStatement preparedStatement = connection.prepareStatement(queryInsert);
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, totalInvestment);
                preparedStatement.setString(3, totalTds);
                preparedStatement.setString(4, totalRedeem);
                preparedStatement.executeUpdate();
                getLogger().info("Ledger status inserted for user: " + username);
            }
            if (count > 1) {
                throw new RuntimeException("Ledger status data is corrupted for user: " + username + ". Instead of single entry, number of rows found: " + count);
            }
        } catch (SQLException e) {
            getLogger().error("Unable to update user's ledger status", e);
            throw new RuntimeException("Unable to update user's ledger status. Username: " + username, e);
        }
    }

    public void updateUserPurchaseStatus(String username, String totalDeposit) {
        String prefix = DatabaseManager.dataBasePrefix;
        String userId = getUserID(username);
        String queryCount = "SELECT COUNT(id) FROM " + prefix + "cardplay.cp_user_purchase_status WHERE user_id = " + userId;
        String queryUpdate = "UPDATE " + prefix + "cardplay.cp_user_purchase_status SET total_purchase_amount = ?, updated_at = UNIX_TIMESTAMP() WHERE user_id = ?";
        String queryInsert = "INSERT INTO " + prefix + "cardplay.cp_user_purchase_status (user_id, first_purchase_date, last_purchase_date, first_purchase_amount, last_purchase_amount, total_purchase_amount, total_purchase_count, created_at, updated_at)" +
                "VALUES(?, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), ?, ?, ?, 2, UNIX_TIMESTAMP(), UNIX_TIMESTAMP())";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(queryCount);
            resultSet.next();
            int count = resultSet.getInt("count(id)");
            if (count == 0) {
                PreparedStatement preparedStatement = connection.prepareStatement(queryInsert);
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, "500");
                preparedStatement.setString(3, "1000");
                preparedStatement.setString(4, totalDeposit);
                preparedStatement.executeUpdate();
                getLogger().info("purchase status updated for user: " + username);
            }
            if (count == 1) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(queryUpdate)) {
                    preparedStatement.setString(1, totalDeposit);
                    preparedStatement.setString(2, userId);
                    preparedStatement.executeUpdate();
                    getLogger().info("purchase status updated for user: " + username);
                }
            }
            if (count > 1) {
                throw new RuntimeException("purchase status data is corrupted for user: " + username + ". Instead of single entry, number of rows found: " + count);
            }
        } catch (SQLException e) {
            getLogger().error("Unable to update user's purchase status", e);
            throw new RuntimeException("Unable to update user's purchase status. Username: " + username, e);
        }
    }


    public void resetChipsInPlay(String username) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts set chips_in_play = 0 where user_id =?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to reset chips in play for user: " + username, e);
        }
    }

    public Map<String, String> getLastRedeemDetails(String username) {
        Map<String, String> data = new HashMap<>();
        String userId = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_redeem WHERE user_id = " + userId + " ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get redeem data from cp_redeem", e);
            throw new RuntimeException("Unable to get redeem data from cp_redeem.", e);
        }
        return data;
    }

    public String getLastRedeemId(String username) {
        String userId = getUserID(username);
        String query = "Select id from " + DatabaseManager.dataBasePrefix + "cardplay.cp_redeem where user_id = " + userId + " ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getString("id");

        } catch (SQLException e) {
            getLogger().error("Unable to get last redeem ID for user", e);
            throw new RuntimeException("Unable to get last redeem ID for user", e);
        }
    }

    public void setInstaRedeemStatus(String username, String status) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_redeem_config set is_insta_redeem=" + status + " where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to set insta redeem status for user", e);
            throw new RuntimeException("Unable to set insta redeem status for user", e);
        }
    }

    public void setDailyRedeemLimit(String username, String limit) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_redeem_config set daily_redeem_limit=" + limit + " where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to set insta redeem status for user", e);
            throw new RuntimeException("Unable to set insta redeem status for user", e);
        }
    }

    public Map<String, String> getUserLedgerDetails(String username) {
        Map<String, String> data = new HashMap<>();
        String userId = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.user_investment_ledger_status WHERE user_id = " + userId + " ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get redeem data from user_investment_ledger_status", e);
            throw new RuntimeException("Unable to get redeem data from user_investment_ledger_status.", e);
        }
        return data;
    }

    public void setRedeemBlockStatus(String username, String status) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_redeemption_block set status = ? where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to set redeem status");
        }
    }

    public Map<String, String> getControlRecordsDetails(String username) {
        Map<String, String> data = new HashMap<>();
        String userId = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.control_records WHERE relation = " + userId + " ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get data from control_records", e);
            throw new RuntimeException("Unable to get redeem data from data from control_records", e);
        }
        return data;
    }

    public void setSpecialTagsForUser(String username, String tag) {
        String userId = getUserID(username);
        String query = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay.cp_source set special_tag = ? WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tag);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to set special tag for the user.");
            throw new RuntimeException("Unable to set special tag for the user.", e);
        }
    }

    public void setUserAsADepositor(String username, String amount, String count) {
        String userId = getUserID(username);
        String query = "INSERT INTO " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_status (user_id, last_purchase_date, first_purchase_date , total_purchase_amount, \n" +
                "total_purchase_count, first_login_date, last_login_date, updated_at, created_at) VALUES ( " + userId + ", \n" +
                "CURRENT_TIME, CURRENT_TIME, 2000, 1, CURRENT_TIME, CURRENT_TIME, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()) ON DUPLICATE KEY UPDATE\n" +
                "last_purchase_date = CURRENT_TIME,\n" +
                "first_purchase_date = CURRENT_TIME,\n" +
                "total_purchase_amount = " + amount + ",\n" +
                "total_purchase_count = " + count + ",\n" +
                "first_login_date = CURRENT_TIME,\n" +
                "last_login_date = CURRENT_TIME,\n" +
                "updated_at = UNIX_TIMESTAMP(),\n" +
                "created_at = UNIX_TIMESTAMP()";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to set user as a depositor", e);
            throw new RuntimeException("Unable to set user as a depositor", e);
        }
    }

    public void setUserHoldAmount(String username, String amount) {
        String userId = getUserID(username);
        String query = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay.cp_chip_accounts set chips = ? where chip_type = 'HOLD_AMT' and user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, amount);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to set user hold amount", e);
        }
    }

    public void setInstaRedeemAmountLimit(String username, String amountLimit) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_redeem_config set insta_amount_limit = ? where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, amountLimit);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
            getLogger().info("Insta redeem limit set for user: " + username + " to " + amountLimit);
        } catch (SQLException e) {
            getLogger().error("Unable to set users insta redeem limit", e);
        }
    }

    public void setUserAsNonDepositor(String username) {
        String userId = getUserID(username);
        String query = "Delete from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_status where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to set user as non depositor", e);
        }
    }

    public double getLoyaltyPoints(String username) {
        double pgpPoints = 0.00;
        String userId = getUserID(username);
        String query = "Select pgp_m from " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            pgpPoints = resultSet.getDouble("pgp_m");
        } catch (SQLException e) {
            getLogger().error("Unable to get pgp points for the user, " + username, e);
        }
        return pgpPoints;
    }

    public void setPgpPoints(String username, String points) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_loyalty set pgp_m = ? where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, points);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
            getLogger().info("PGP points for user " + username + ", set to: " + points);

        } catch (SQLException e) {
            getLogger().error("Unable to set pgp points for the user, " + username, e);
        }
    }

    public String getReportIssueID(String username) {
        String query = "SELECT id FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_kenscio_email WHERE subject LIKE" + "'%" + username.toUpperCase() + "%'" + " ORDER BY id DESC LIMIT 1";
        String reportIssueId = "";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            reportIssueId = resultSet.getString("id");
        } catch (SQLException e) {
            getLogger().error("Unable to get report issue id for user: " + username, e);
        }
        return reportIssueId;

    }

    public String getReportIssueContent(String username) {
        String query = "SELECT content FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_kenscio_email WHERE subject LIKE" + "'%" + username.toUpperCase() + "%'" + " ORDER BY id DESC LIMIT 1";
        String content = "";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            content = resultSet.getString("content");
        } catch (SQLException e) {
            getLogger().error("Unable to get content for user: " + username, e);
        }
        return content;

    }

    public Map<String, String> getTicketDetails(String familyId) {
        Map<String, String> data = new HashMap<>();
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_ticket_details WHERE family_id = " + familyId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get ticket details from cp_ticket_details", e);
            throw new RuntimeException("Unable to get ticket details from cp_ticket_details", e);
        }
        return data;
    }

    public Map<String, String> getCpRafRefereeData(String username) {
        Map<String, String> data = new HashMap<>();
        String userId = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_raf_referee_details WHERE referee_user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get data from cp_user_enquiry. ", e);
        }
        return data;

    }

    public String getReferralCode(String username) {
        String userId = getUserID(username);
        String query = "Select referral_code from " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_raf_referral_details where user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getString("referral_code");

        } catch (SQLException e) {
            getLogger().error("Unable to get referral code for user", e);
            throw new RuntimeException("Unable to get referral code for user", e);
        }
    }


    public Map<String, String> getCpUserEnquiryData(String username) {
        Map<String, String> data = new HashMap<>();
        String userId = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_enquiry WHERE user_id = " + userId + " ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get data from cp_user_enquiry. ", e);
        }
        return data;

    }


    public void deleteTicketFamilyMasterQuery(String familyName) {
        String query = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.ticket_family_master WHERE family_name = '" + familyName + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            getLogger().info(" Row not found because it is Already Deleted form the `" + DatabaseManager.dataBasePrefix + "cardplay`.`ticket_family_master`");
        }

    }

    public void deleteCpTicketDetailsQuery(String tktName) {
        String query = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_ticket_details WHERE tkt_name = '" + tktName + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            getLogger().info(" Row not found because it is Already Deleted form the `" + DatabaseManager.dataBasePrefix + "cardplay`.`cp_ticket_details`");
        }

    }

    public void deleteLastCreatedTicketOffer(String offerName) {
        String query = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_discounted_tkt_offers WHERE offer_name = '" + offerName + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            getLogger().info(" Row not found because it is Already Deleted form the `" + DatabaseManager.dataBasePrefix + "cardplay`.`cp_discounted_tkt_offers`");
        }

    }

    public void updateAutoBuyInInactive(String username) {
        String userId = getUserID(username);
        String query = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_user_settings SET STATUS = 'INACTIVE' WHERE property_name = 'NG_AUTO_BUY_IN' AND user_id = " + userId;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to update setting for user: " + username);
        }

    }

    public String getAlternateMobileOtp(String alt_mobile) {
        String otp = "";
        String query = "Select transaction_token from " + DatabaseManager.dataBasePrefix + "cardplay.cp_mobile_verification_pages where mobile = '" + alt_mobile + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            getLogger().info("Getting sing up OTP for user mobile: {}", alt_mobile);
            otp = resultSet.getString("transaction_token");
            getLogger().info("OTP for mobile number {} is: {}", alt_mobile, otp);
        } catch (Exception e) {
            getLogger().error("Unable to get OTP for mobile: {}", alt_mobile, e);
        }
        return otp;

    }

    public void resetEmail(String username) {
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set email = ? where user_name = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, "");
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
            getLogger().info("Deleting email id for user: {}", username);
        } catch (SQLException e) {
            getLogger().info("Unable to delete email id for user: {}", username, e);
        }
    }

    public void resetExistingEmail(String emailId) {
        String query1 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set email = ? where email = ?";
        String query2 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_email_verification_pages set email = ? where email = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
            preparedStatement.setString(1, "");
            preparedStatement.setString(2, emailId);
            preparedStatement.executeUpdate();
            getLogger().info("Deleting email id for user:{}", emailId);
        } catch (SQLException e) {
            getLogger().info("Unable to delete email id for user: {}", emailId, e);
        }

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
            preparedStatement.setString(1, "");
            preparedStatement.setString(2, emailId);
            preparedStatement.executeUpdate();
            getLogger().info("Deleting email id for user: " + emailId);
        } catch (SQLException e) {
            getLogger().info("Unable to delete email id for user: " + emailId, e);
        }
    }

    public void setEmail(String username, String email) {
        String userId = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user set email = ? WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            getLogger().error("Unable to set email. ", e);
        }
    }

    public void setEmailAsUnverified(String username) {
        String userID = getUserID(username);
        String query1 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile set is_email_verified = ? where user_id= ?";
        String query2 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_email_verification_pages set verified = ? where user_id= ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
            preparedStatement.setString(1, "0");
            preparedStatement.setString(2, userID);
            preparedStatement.executeUpdate();
            getLogger().info("Setting email id as unverified for user: {}", username);
        } catch (SQLException e) {
            getLogger().info("Unable to set email as unverified for user: {}", username, e);
        }

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
            preparedStatement.setString(1, "0");
            preparedStatement.setString(2, userID);
            preparedStatement.executeUpdate();
            getLogger().info("Setting email id as unverified for user : {}", username);
        } catch (SQLException e) {
            getLogger().info("Unable to set email as unverified for user: {}", username, e);
        }
    }

    public void setExistingEmailAsUnverified(String emailId, String status) {
        String userId = getUserID(emailId);
        String query1 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile set is_email_verified = ? where user_id = ?";
        String query2 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_email_verification_pages set verified = ? where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
            getLogger().info("Setting email id as unverified");
        } catch (SQLException e) {
            getLogger().info("Unable to set email as unverified:", e);
        }

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
            getLogger().info("Setting email id as unverified:");
        } catch (SQLException e) {
            getLogger().info("Unable to set email as unverified:", e);
        }
    }

    public void resetAlternateMobileNumber(String username) {
        String userID = getUserID(username);
        String query1 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile set alternate_number= ?,is_alt_mobile_verified = ? where user_id= ?";
        String query2 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_mobile_verification_pages set mobile= ?,verified = ? where user_id= ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
            preparedStatement.setString(1, "");
            preparedStatement.setString(2, "0");
            preparedStatement.setString(3, userID);
            preparedStatement.executeUpdate();
            getLogger().info("Resetting alternate mobile number for user: {}", username);
        } catch (SQLException e) {
            getLogger().info("Unable to reset alternate mobile number for user: {}", username, e);
        }

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
            preparedStatement.setString(1, "");
            preparedStatement.setString(2, "0");
            preparedStatement.setString(3, userID);
            preparedStatement.executeUpdate();
            getLogger().info("Resetting alternate mobile number for user : {}", username);
        } catch (SQLException e) {
            getLogger().info("Unable to reset alternate mobile number for user: {}", username, e);
        }

    }

    public void setAlternateMobileNumber(String username, String altMobile) {
        String userID = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile set alternate_number= ?,is_alt_mobile_verified = ? where user_id= ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, altMobile);
            preparedStatement.setString(2, "1");
            preparedStatement.setString(3, userID);
            preparedStatement.executeUpdate();
            // getLogger().info("Setting alternate mobile number for user: " + username);
        } catch (SQLException e) {
            getLogger().info("Unable to set alternate mobile number for user: {}", username, e);
        }
    }

    public void resetExistingAlternateMobileNumber(String altMobile) {

        String query1 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile set alternate_number= ? where alternate_number= ?";
        String query2 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_mobile_verification_pages set mobile= ? where mobile= ?";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
            preparedStatement.setString(1, "");
            preparedStatement.setString(2, altMobile);
            preparedStatement.executeUpdate();
            getLogger().info("Resetting existing alternate mobile number: {}", altMobile);
        } catch (SQLException e) {
            getLogger().info("Unable to reset existing email alternate mobile number:{}", altMobile, e);
        }

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
            preparedStatement.setString(1, "");
            //preparedStatement.setString(2, "0");
            preparedStatement.setString(2, altMobile);
            preparedStatement.executeUpdate();
            getLogger().info("Resetting existing alternate mobile number : {}", altMobile);
        } catch (SQLException e) {
            getLogger().info("Unable to reset existing email alternate mobile number:{}", altMobile, e);
        }
    }


    public Object setMobileAsPreferred(String username) {
        String userID = getUserID(username);
        String query = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile set preferred_number= ? where user_id= ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, userID);
            preparedStatement.executeUpdate();
            getLogger().info("Setting Mobile as Preferred for user: {}", username);
        } catch (SQLException e) {
            getLogger().info("Unable to set mobile as preferred for user: {}", username, e);
        }
        return null;
    }

    public void resetGender(String username) {

        String userID = getUserID(username);
        String kycUserId = getKycUserId(username);
        String query1 = "Update " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile Set gender= ? where user_id = ?";
        String query2 = "UPDATE dev_gauss_kyc.user_kyc_info SET gender = ? WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement1 = connection.prepareStatement(query1)) {
            preparedStatement1.setNull(1, Types.VARCHAR);
            preparedStatement1.setString(2, userID);
            preparedStatement1.executeUpdate();
            getLogger().info("Resetting gender for user: {}", username);
        } catch (SQLException e) {
            getLogger().info("Unable to reset gender for user: {}", username, e);
        }

        try (Connection connection2 = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement2 = connection2.prepareStatement(query2)) {
            preparedStatement2.setNull(1, Types.VARCHAR);
            preparedStatement2.setString(2, kycUserId);
            preparedStatement2.executeUpdate();
            getLogger().info("Resetting gender for user : {} ", username);
        } catch (SQLException e) {
            getLogger().info("Unable to reset gender in user_kyc_info for user: {}", username, e);
        }
    }


    public void setGender(String username) {
        String userID = getUserID(username);
        String kycUserId = getKycUserId(username);
        String gender = "female";

        String query1 = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile SET gender = ? WHERE user_id = ?";
        String query2 = "UPDATE dev_gauss_kyc.user_kyc_info SET gender = ? WHERE user_id = ?";

        try (Connection connection1 = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement1 = connection1.prepareStatement(query1)) {
            preparedStatement1.setString(1, gender);
            preparedStatement1.setString(2, userID);
            preparedStatement1.executeUpdate();
        } catch (SQLException e) {
            getLogger().info("Unable to set gender in cp_user_profile for user: {}", username, e);
        }

        try (Connection connection2 = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement2 = connection2.prepareStatement(query2)) {
            preparedStatement2.setString(1, gender);
            preparedStatement2.setString(2, kycUserId);
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            getLogger().info("Unable to set gender in user_kyc_info for user: {}", username, e);
        }
    }

    public void updateUserKycInfoUsingUsername(String username, String status, String verificationLevel) {
        String kycUserId = getKycUserId(username);
        String query = "UPDATE dev_gauss_kyc.user_kyc_info SET " +
                "modified_on= CURRENT_TIMESTAMP()," +
                "status = ?," +
                "verification_level = ? Where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, verificationLevel);
            preparedStatement.setString(3, kycUserId);
            preparedStatement.executeUpdate();
            getLogger().info("KYC Info updated for user: {} ", username);
        } catch (SQLException e) {
            getLogger().error("Unable to KYC Pan Info Table.", e);
        }
    }


    public String getPanDocumentID(String username) {
        String panDocumentID = "";
        String kycUserId = getKycUserId(username);
        String query = "SELECT pan_number FROM dev_gauss_kyc.user_pan_info WHERE user_id = '" + kycUserId + "'";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            getLogger().info("Getting Pan Document ID for user: {}", username);
            panDocumentID = resultSet.getString("pan_number");
            getLogger().info("Pan Document ID of the user: {} in database is: {}", username, panDocumentID);
        } catch (Exception e) {
            getLogger().error("Unable to get Pan Document ID for user: {}", username, e);
        }
        return panDocumentID;
    }


    public String getPanDocumentStatus(String username) {
        String panDocumentStatus = "";
        String kycUserId = getKycUserId(username);
        String query = "SELECT status FROM dev_gauss_kyc.user_pan_info WHERE user_id = '" + kycUserId + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            getLogger().info("Getting Pan Document Status for user: {}", username);
            panDocumentStatus = resultSet.getString("status");
            getLogger().info("Pan Document Status of the user: {} in database is: {}", username, panDocumentStatus);
        } catch (Exception e) {
            getLogger().error("Unable to get Pan Document Status for user: {}", username, e);
        }
        return panDocumentStatus;
    }


    public String getKycDocumentType(String username) {
        String kycDocumentType = "";
        String kycUserId = getKycUserId(username);
        String query = "SELECT disp_name " +
                "FROM dev_gauss_kyc.document_type t1 " +
                "JOIN dev_gauss_kyc.user_kyc_info t2 " +
                "ON t1.id = t2.doc_id " +
                "WHERE user_id = '" + kycUserId + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            getLogger().info("Getting KYC Document Type for user: {}", username);
            kycDocumentType = resultSet.getString("disp_name");
            getLogger().info("KYC Document Type of the user: {} in database is: {}", username, kycDocumentType);
        } catch (Exception e) {
            getLogger().error("Unable to get KYC Document Type for user: {}", username, e);
        }
        return kycDocumentType;
    }

    public String getKycDocumentId(String username) {
        String kycDocumentID = "";
        String kycUserId = getKycUserId(username);
        String query = "SELECT doc_number FROM dev_gauss_kyc.user_kyc_info WHERE user_id = '" + kycUserId + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            getLogger().info("Getting KYC Document ID for user: {} ", username);
            kycDocumentID = resultSet.getString("doc_number");
            getLogger().info("KYC Document ID of the user : {} in database is: {}", username, kycDocumentID);
        } catch (Exception e) {
            getLogger().error("Unable to get KYC Document ID for user: {}", username, e);
        }
        return kycDocumentID;
    }

    public String getKycDocumentStatus(String username) {
        String kycDocumentStatus = "";
        String kycUserId = getKycUserId(username);
        String query = "SELECT status FROM dev_gauss_kyc.user_kyc_info WHERE user_id = '" + kycUserId + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            getLogger().info("Getting KYC Document Status for user: {}", username);
            kycDocumentStatus = resultSet.getString("status");
            getLogger().info("KYC Document Status of the user: {} in database is: {}", username, kycDocumentStatus);
        } catch (Exception e) {
            getLogger().error("Unable to get KYC Document Status for user: {}", username, e);
        }
        return kycDocumentStatus;
    }


    public void resetUserPanInfo(String username) {
        String kycUserId = getKycUserId(username);
        String query = "DELETE FROM dev_gauss_kyc.user_pan_info WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, kycUserId);
            preparedStatement.executeUpdate();
            getLogger().info("Resetting PAN details for user: {}", username);
        } catch (SQLException e) {
            getLogger().error("Unable to reset PAN details for user: {}", username, e);
        }
    }

    public void resetUserKycInfo(String username) {
        String kycUserId = getKycUserId(username);
        String query = "DELETE FROM dev_gauss_kyc.user_kyc_info WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, kycUserId);
            preparedStatement.executeUpdate();
            getLogger().info("Resetting KYC details for user: {}", username);
        } catch (SQLException e) {
            getLogger().error("Unable to reset KYC details for user: {}", username, e);
        }
    }

    public void setAccountName(String username) {
        String kycUserId = getKycUserId(username);
        String accountName = "Mr. AYUSH  YADAV";
        String query = "UPDATE dev_gauss_kyc.user_pan_info set pan_name = ? where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountName);
            preparedStatement.setString(2, kycUserId);
            preparedStatement.executeUpdate();
            getLogger().info("Setting account name for user: {}", username);
        } catch (SQLException e) {
            getLogger().error("Unable to set account name for user: {}", username, e);
        }
    }

    public void resetBankDetails(String username) {
        String kycUserId = getKycUserId(username);
        String accountNumber = "20159346314";
        String query1 = "UPDATE dev_gauss_kyc.user_bank_info SET is_verified = 0, is_default = 0 WHERE account_number = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
            preparedStatement.setString(1, accountNumber);
            preparedStatement.executeUpdate();
            getLogger().info("Resetting Bank details status for account number : {}", accountNumber);
        } catch (SQLException e) {
            getLogger().error("Unable to reset Bank details status for account number : {}", accountNumber, e);
        }

        String query2 = "UPDATE dev_gauss_kyc.application_users SET is_bank_verified = 0 WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
            preparedStatement.setString(1, kycUserId);
            preparedStatement.executeUpdate();
            getLogger().info("Resetting verified Bank details status for user: {}", username);
        } catch (SQLException e) {
            getLogger().error("Unable to reset verified Bank details status for user: {}", username, e);
        }

        String query3 = "DELETE FROM dev_gauss_kyc.user_bank_info WHERE account_number = ? and account_type = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query3)) {
            preparedStatement.setString(1, accountNumber);
            preparedStatement.setString(2, "Bank");
            preparedStatement.executeUpdate();
            getLogger().info("Deleting bank info data for user : {} with account number : {}", username, accountNumber);
        } catch (SQLException e) {
            getLogger().error("Unable to delete bank info data for user : {} with account number : {}", username, accountNumber, e);
        }

    }


    public void removeExcessBankAccountDetails(String username) {
        String kycUserId = getKycUserId(username);
        String query = "DELETE FROM dev_gauss_kyc.user_bank_info" +
                " WHERE user_bank_id In (" +
                "SELECT user_bank_id " +
                "From ( " +
                "Select user_bank_id " +
                "FROM dev_gauss_kyc.user_bank_info " +
                "WHERE user_id = ? AND account_type = ? " +
                "ORDER BY added_on DESC " +
                "LIMIT 3, 1000 " +
                ") as subquery " +
                ")";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, kycUserId);
            preparedStatement.setString(2, "Bank");
            preparedStatement.executeUpdate();
            getLogger().info("Deleting more than 3 bank details data for user : {} ", username);
        } catch (SQLException e) {
            getLogger().error("Unable to delete more than 3 bank details data for user : {}", username, e);
        }
    }


    public String getGameConfigIdUsingRoomName(String roomName, String gameVariant) {
        String gameConfigId = "";
        if (gameVariant.equals("Hold'em")) {
            gameVariant = "NL";
        } else {
            gameVariant = "PLO";
        }
        String query = "SELECT id FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_game_config WHERE remarks = ? AND betting_rule = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, roomName);
            preparedStatement.setString(2, gameVariant);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            gameConfigId = resultSet.getString("id");
        } catch (SQLException e) {
            getLogger().error("Unable to get game config id for room name: " + roomName, e);
        }
        return gameConfigId;
    }

    public HashMap<String, String> getGameInfoDataFromGameConfig(String roomName, String gameVariant) {
        String gameConfigId = getGameConfigIdUsingRoomName(roomName, gameVariant);
        String query = "SELECT buyin_low, buyin_high, small_blind, big_blind, remarks FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_game_config WHERE id = ?";
        HashMap<String, String> gameInfo1 = new HashMap<>();
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, gameConfigId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                gameInfo1.put("buyin_low", resultSet.getString("buyin_low"));
                gameInfo1.put("buyin_high", resultSet.getString("buyin_high"));
                gameInfo1.put("small_blind", resultSet.getString("small_blind"));
                gameInfo1.put("big_blind", resultSet.getString("big_blind"));
                gameInfo1.put("remarks", resultSet.getString("remarks"));
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get game info data for room name: " + roomName, e);
        }

        return gameInfo1;
    }

    public HashMap<String, String> getGameInfoDataFromTournamentProp(String roomName, String gameVariant) {
        String gameConfigId = getGameConfigIdUsingRoomName(roomName, gameVariant);
        String query = "SELECT " +
                "MAX(CASE WHEN prop_name = 'TURN_TIME' THEN prop_value END) AS TURN_TIME, " +
                "MAX(CASE WHEN prop_name = 'TIME_BANK' THEN prop_value END) AS TIME_BANK, " +
                "MAX(CASE WHEN prop_name = 'TIME_BANK_REFRESH_RATE' THEN prop_value END) AS TIME_BANK_REFRESH_RATE, " +
                "MAX(CASE WHEN prop_name = 'BUYINCHECK_TIME' THEN prop_value END) AS ANTI_BANKING, " +
                "MAX(CASE WHEN prop_name = 'MULTIRUN_ALLOWED' THEN prop_value END) AS MULTIRUN_ALLOWED " +
                "FROM " + DatabaseManager.dataBasePrefix + "cardplay_poker.cp_tournament_prop " +
                "WHERE game_config_id = ? " +
                "AND prop_name IN ('TURN_TIME', 'TIME_BANK', 'TIME_BANK_REFRESH_RATE', 'BUYINCHECK_TIME', 'MULTIRUN_ALLOWED')";
        HashMap<String, String> gameInfo2 = new HashMap<>();
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, gameConfigId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                gameInfo2.put("TURN_TIME", resultSet.getString("TURN_TIME"));
                gameInfo2.put("TIME_BANK", resultSet.getString("TIME_BANK"));
                gameInfo2.put("TIME_BANK_REFRESH_RATE", resultSet.getString("TIME_BANK_REFRESH_RATE"));
                gameInfo2.put("ANTI_BANKING", resultSet.getString("ANTI_BANKING"));
                gameInfo2.put("MULTIRUN_ALLOWED", resultSet.getString("MULTIRUN_ALLOWED"));
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get game info data for room name: " + roomName, e);
        }
        return gameInfo2;
    }

    public String getEmailId(String username) {
        String emailID = "";
        String query = "Select email from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user where user_name = '" + username + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            //getLogger().info("Getting Email ID for user: " + username);
            emailID = resultSet.getString("email");
            //getLogger().info("Email Id of the user: " + username + " in database is: " + emailID);
        } catch (Exception e) {
            getLogger().error("Unable to get email id for user: {}", username, e);
        }
        return emailID;
    }


    public String getMobileNumber(String username) {
        String mobileNumber = "";
        String userID = getUserID(username);
        String query = "Select mobile from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile where user_id = '" + userID + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            //getLogger().info("Getting Mobile Number for user: " + username);
            mobileNumber = resultSet.getString("mobile");
            //getLogger().info("Mobile Number of the user: " + username + " in database is: " + mobileNumber);
        } catch (Exception e) {
            getLogger().error("Unable to get mobile number for user: {}", username, e);
        }
        return mobileNumber;
    }

    public String getAltMobileNumber(String username) {
        String altMobileNumber = "";
        String userID = getUserID(username);
        String query = "Select alternate_number from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile where user_id = '" + userID + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            //getLogger().info("Getting Alternate Number for user: " + username);
            altMobileNumber = resultSet.getString("alternate_number");
            //getLogger().info("Alternate Number of the user: " + username + " in database is: " + altMobileNumber);
        } catch (Exception e) {
            getLogger().error("Unable to get alternate number for user: {}", username, e);
        }
        return altMobileNumber;
    }

    public String getGender(String username) {
        String gender = "";
        String userID = getUserID(username);
        String query = "Select gender from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_profile where user_id = '" + userID + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            //getLogger().info("Getting gender for user: " + username);
            gender = resultSet.getString("gender");
            //getLogger().info("Gender of the user: " + username + " in database is: " + gender);
        } catch (Exception e) {
            getLogger().error("Unable to get gender for user: {}", username, e);
        }
        return gender;
    }


    public String getName(String username) {
        String name = "";
        String kycUserId = getKycUserId(username);
        String query = "SELECT pan_name FROM dev_gauss_kyc.user_pan_info WHERE user_id = '" + kycUserId + "'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            // getLogger().info("Getting name for user: " + username);
            name = resultSet.getString("pan_name");
            // getLogger().info("Name of the user: " + username + " in database is: " + name);
        } catch (Exception e) {
            getLogger().error("Unable to get name for user: {}", username, e);
        }
        return name;
    }

    public void updateVipHoldForUser(String username, String amount) {
        String userId = getUserID(username);
        String query = "UPDATE " + DatabaseManager.dataBasePrefix + "cardplay.cp_game_accounts set chips = ? where user_id = ? and chip_type = 'VIP_HOLD'";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, amount);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
            getLogger().info("Updated VIP_HOLD for user :" + username);
        } catch (Exception e) {
            getLogger().error("Unable to update VIP_HOLD for user", e);
        }
    }


    public void clearUserPurchaseStatus(String username) {
        String userId = getUserID(username);
        String query = "Delete from " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_purchase_status where user_id = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to delete user purchase status", e);
        }
    }

    public void addBankDetailsInUserBankInfo(String username) {
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.KYC.getResource()).getConnection()) {
            connection.setAutoCommit(false); // Start a transaction

            String userId = getKycUserId(username);

            String queryCount = "SELECT COUNT(user_bank_id) FROM dev_gauss_kyc.user_bank_info WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryCount)) {
                preparedStatement.setString(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);

                        if (count == 0) {
                            String insertQuery = "INSERT INTO dev_gauss_kyc.`user_bank_info` (`user_id`, `account_name`, `account_number`, `reason`, `account_type`, `ifsc_id`, `added_on`, `modified_on`, `is_verified`, `is_default`, `is_deleted`, `status`, `approved_mode`, `verification_level`, `is_idfy_failure`, `upload_by_admin`) VALUES (?, ?, ?, ?, 'BANK', 26, ?, ?, '1', '1', '0', 'verified', 'server', 'doc_bank_validate', '0', 0)";
                            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                                insertStatement.setInt(1, Integer.parseInt(userId));
                                insertStatement.setString(2, username);
                                insertStatement.setString(3, "4522383942");
                                insertStatement.setString(4, "For Test Automation ");
                                insertStatement.setString(5, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                                insertStatement.setString(6, SupportUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss"));
                                insertStatement.executeUpdate();
                            }
                        } else if (count == 1) {
                            getLogger().info("Bank details already exists for user: " + username);
                        } else if (count == 2) {
                            String deleteQuery = "DELETE FROM dev_gauss_kyc.user_bank_info WHERE user_id = ? ORDER BY added_on DESC LIMIT 1";
                            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                                deleteStatement.setString(1, userId);
                                deleteStatement.executeUpdate();
                                getLogger().info("Deleted the last added bank detail for user: " + username);
                            }
                        }
                    }
                }
            }
            connection.commit(); // Commit the transaction
        } catch (Exception e) {
            getLogger().error("Unable to add Bank details for user: " + username, e);
        }

    }

    public void fetchUserIds(List<String> usernames, String csvFilePath) throws SQLException {
        // Generate the username list without extra quotes
        String usernameList = usernames.stream()
                .map(username -> "'" + username.trim() + "'")
                .collect(Collectors.joining(", "));

        // Construct the SQL query
        String query = "SELECT user_id FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user WHERE user_name IN (" + usernameList + ")";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Define column name mapping
            Map<String, String> columnMapping = new HashMap<>();
            columnMapping.put("user_id", "userIds");

            // Write results to CSV file
            CsvUtils.writeToCSV(resultSet, csvFilePath, columnMapping);

            while (resultSet.next()) {
                int id = resultSet.getInt("user_id");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUserSegment(String segmentName) throws SQLException {
        String delQuery = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay_segmentation.segment_details WHERE segment_name = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(delQuery)) {
            preparedStatement.setString(1, segmentName);
            getLogger().info("Executing query: " + preparedStatement);
            preparedStatement.executeUpdate();
            getLogger().info("Deleted segment: " + segmentName);
        } catch (SQLException e) {
            getLogger().error("Unable to delete the segment: " + segmentName, e);
        }
    }

    public String getLastSTWBonusCreditId(String username, String chipType, String narration, String amount) {
        String userId = getUserID(username);
        String bonus_credit_id = "";
        String query = "SELECT*FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_bonus_credit_details WHERE user_id = " + userId + " AND chip_type = '" + chipType + "' AND narration = '" + narration + "' AND amount = '" + amount + "' ORDER BY bonus_credit_id  DESC LIMIT 1;";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            bonus_credit_id = resultSet.getString("bonus_credit_id");
        } catch (SQLException e) {
            getLogger().error("Unable to get already stored bonus credit id  for user: " + username, e);
        }
        return bonus_credit_id;
    }

    public String getLastSTWBoosterBonusCreditId(String username, String narration, String amount) {
        String userId = getUserID(username);
        String booster_credit_id = "";
        String query = "SELECT*FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_booster_bonus_credit_details WHERE user_id = " + userId + " AND narration = '" + narration + "' AND amount = '" + amount + "' ORDER BY booster_credit_id  DESC LIMIT 1;";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            booster_credit_id = resultSet.getString("booster_credit_id");
        } catch (SQLException e) {
            getLogger().error("Unable to get already stored booster bonus credit id  for user: " + username, e);
        }
        return booster_credit_id;
    }

    public String getLastSTWTicketCreditId(String username, String ticketName, String narration) {
        String userId = getUserID(username);
        String query = "SELECT*FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_ticket_txn_master WHERE user_id = " + userId + " AND ticket_name = '" + ticketName + "' AND narration = '" + narration + "' ORDER BY ticket_txn_id  DESC LIMIT 1;";
        String ticket_txn_id = null;
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            ticket_txn_id = (resultSet.getString("ticket_txn_id"));
        } catch (SQLException e) {
            getLogger().error("Unable to get already stored ticket txn id  for user: " + username, e);
        }
        return ticket_txn_id;
    }

    public Map<String, String> getBonusCreditDetails(String username, String chipType, String narration, String amount) {
        Map<String, String> data = new HashMap<>();
        String userId = new MySqlCalls().getUserID(username);
        String query = "SELECT*FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_bonus_credit_details WHERE user_id = " + userId + " AND chip_type = '" + chipType + "' AND narration = '" + narration + "' AND amount = '" + amount + "' ORDER BY bonus_credit_id  DESC LIMIT 1;";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }

        } catch (SQLException e) {
            getLogger().error("Unable to get bonus credit details", e);
        }

        return data;
    }

    public void deleteSpinTheWheelData(String stwName) {
        String delQuery = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_stw_master WHERE Name = ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(delQuery)) {
            preparedStatement.setString(1, stwName);
            preparedStatement.executeUpdate();
            getLogger().info("Deleted spin the wheel data for : " + stwName);
        } catch (SQLException e) {
            getLogger().error("Unable to delete spin the wheel data for user: " + stwName, e);
        }
    }

    public Map<String, String> getBoosterBonusCreditDetails(String username, String narration, String amount) {
        Map<String, String> data = new HashMap<>();
        String userId = new MySqlCalls().getUserID(username);
        String query = "SELECT*FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_booster_bonus_credit_details WHERE user_id = " + userId + " AND narration = '" + narration + "' AND amount = '" + amount + "' ORDER BY booster_credit_id  DESC LIMIT 1;";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }

        } catch (SQLException e) {
            getLogger().error("Unable to get  booster bonus credit details", e);
        }

        return data;

    }

    public Map<String, String> getTicketTxnDetails(String username, String ticketName, String narration) {
        Map<String, String> data = new HashMap<>();
        String userId = new MySqlCalls().getUserID(username);
        String query = "SELECT*FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_ticket_txn_master WHERE user_id = " + userId + " AND ticket_name = '" + ticketName + "' AND narration = '" + narration + "' ORDER BY ticket_txn_id  DESC LIMIT 1;";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            if (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    data.put(columnName, columnValue);
                }
            }

        } catch (SQLException e) {
            getLogger().error("Unable to get ticket txn details", e);
        }

        return data;
    }

    public void deleteTicketDetails(String ticketName, String ticketCreationName) throws SQLException {
        String delQuery1 = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_ticket_txn_master WHERE ticket_name = ?";
        getLogger().info(" query1: " + delQuery1);
        String delQuery2 = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_ticket_details WHERE tkt_name = ?";
        getLogger().info(" query2: " + delQuery2);
        String delQuery3 = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_user_ticket_expiry_details WHERE ticket_name = ?";
        getLogger().info(" query3: " + delQuery3);
        String delQuery4 = "DELETE FROM " + DatabaseManager.dataBasePrefix + "cardplay.ticket_family_master WHERE family_name = ?";
        getLogger().info(" query4: " + delQuery4);
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement1 = connection.prepareStatement(delQuery1);
             PreparedStatement preparedStatement2 = connection.prepareStatement(delQuery2);
             PreparedStatement preparedStatement3 = connection.prepareStatement(delQuery3);
             PreparedStatement preparedStatement4 = connection.prepareStatement(delQuery4)) {
            preparedStatement1.setString(1, ticketName);
            preparedStatement2.setString(1, ticketName);
            preparedStatement3.setString(1, ticketName);
            preparedStatement4.setString(1, ticketCreationName);
            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            preparedStatement3.executeUpdate();
            preparedStatement4.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to delete ticket details for ticket: " + ticketName, e);
        }
    }

    public String getRecentSTWCreatedId(String stwName) {
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_stw_master Where name = '" + stwName + "' ORDER BY id DESC LIMIT 1";
        String recentSTWId = "";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            recentSTWId = resultSet.getString("id");
        } catch (SQLException e) {
            getLogger().error("Unable to get stw id", e);
        }
        return recentSTWId;
    }

    public String getRecentSTWOptionId(String username, int recentSTWCreatedId) {
        String userId = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_stw_result Where user_id = " + userId + " AND stw_master_id = " + recentSTWCreatedId + " ORDER BY id DESC LIMIT 1";
        String recentSTWOptionId = "";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            recentSTWOptionId = resultSet.getString("stw_options_id");
        } catch (SQLException e) {
            getLogger().error("Unable to get stw option id", e);
        }
        return recentSTWOptionId;
    }

    public String getRecentSTWSequenceId(String username, int recentSTWCreatedId) {
        String userId = getUserID(username);
        String query = "SELECT * FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_stw_result Where user_id = " + userId + " AND stw_master_id = " + recentSTWCreatedId + " ORDER BY id DESC LIMIT 1";
        String recentSTWOptionId = "";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            recentSTWOptionId = resultSet.getString("sequence_id");
        } catch (SQLException e) {
            getLogger().error("Unable to get stw option id", e);
        }
        return recentSTWOptionId;
    }

    public String getSTWLabelText(int stwId, int sequenceId) {
        String labelText = "";
        String query = "SELECT label FROM " + DatabaseManager.dataBasePrefix + "cardplay.cp_stw_options WHERE stw_id = " + stwId + " AND sequence_id = " + sequenceId + " ";
        getLogger().info("Query: " + query);
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.APPLICATION.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                labelText = resultSet.getString("label");
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get stw option text", e);
        }
        return labelText;
    }


}