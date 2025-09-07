package utils.db;

import com.adda52.logging.Logging;
import com.adda52.utils.database.sql.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PSQLCalls implements Logging {

    public Map<String, String> getLoginSessionDetail(String username) {
        Map<String, String> data = new HashMap<>();
        String userId = new MySqlCalls().getUserID(username);
        String query = "Select * from user_login_session_history where user_id = " + userId + " order by id desc limit 1";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
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
            getLogger().error("Unable to get user session data", e);
        }

        return data;
    }

    public long getLastLoginSessionId(String username) {
        long id = 0L;
        String userId = new MySqlCalls().getUserID(username);
        String query = "Select * from user_login_session_history where user_id = " + userId + " order by id desc limit 1";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getLong("id");
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get last user session id", e);

        }
        return id;
    }

    public int getRoomNameGroupID(String roomName, String gameVariant) {
        int groupID = 0;
        String gameVariant1 = switch (gameVariant) {
            case "Hold'em" -> "HOLDEM";
            case "PL Omaha" -> "OMAHA";
            case "PLO 5" -> "OMAHA_5";
            case "PLO 6" -> "OMAHA_6";
            default -> "";
        };
        String query = "SELECT group_id FROM public.ring_group_master WHERE group_name ILIKE ? AND ring_variant ILIKE ?";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            {
                preparedStatement.setString(1, roomName);
                preparedStatement.setString(2, gameVariant1);
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                groupID = resultSet.getInt("group_id");
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get group id", e);
        }
        return groupID;
    }


    public List<Integer> getShowDirectTableTrueList() {
        List<Integer> groupID = new ArrayList<>();
        String query = "SELECT group_id FROM public.ring_group_master WHERE show_direct_table = true";
        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            for (int i = 0; resultSet.next(); i++) {
                groupID.add(resultSet.getInt("group_id"));
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get group id list", e);
        }
        return groupID;
    }

    public Boolean getShowDirectTableStatus(String roomName, String gameVariant) {
        boolean showDirectTableStatus = false;
        int groupID = getRoomNameGroupID(roomName, gameVariant);
        String query = "SELECT show_direct_table FROM public.ring_group_master WHERE group_id = ?";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            {
                preparedStatement.setInt(1, groupID);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    showDirectTableStatus = resultSet.getBoolean("show_direct_table");
                }
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get show direct table status", e);
        }
        return showDirectTableStatus;
    }


    public void setShowDirectTableStatus(int groupID, boolean status) {
        String query = "UPDATE public.ring_group_master SET show_direct_table = ? WHERE group_id = ?";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            {
                preparedStatement.setBoolean(1, status);
                preparedStatement.setInt(2, groupID);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            getLogger().error("Unable to set show direct table status", e);
        }
    }
    public int getUserGamePlaySummary(String username){
        int user_id = 0;
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));

        String query = "Select * from public.user_game_summary_status where user_id = " + userId + " order by user_id desc limit 1";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user_id = resultSet.getInt("user_id");
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get user gameplay summary", e);
        }
        return user_id;
    }

    public void insertIntoUserGameSummaryStatus(String username){
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));

        String query = "insert into public.user_game_summary_status (user_id,cash_player,tourney_player,created_at,updated_at) values ("+userId+",false,true,NOW(),NOW())";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(query)) {
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to insert into user game summary status table", e);
        }
    }

    public void deleteFromUserGameSummaryStatus(String username){
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));

        String query = "delete from public.user_game_summary_status where user_id = "+userId;

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(query)) {
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to delete from user game summary status table", e);
        }
    }

    public String getUsersVisibilityStatusOfCashGameVariantWidget(String username){
        String visibilityStatus=null;

        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));

        String query = "Select * from public.widget_user_mapping where user_id = "+userId+" and widget_id = 17";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                visibilityStatus = resultSet.getString("visibility_status");
            }
        } catch (SQLException e) {
            getLogger().error("Unable to get the visibility status of the widget", e);
        }
        return visibilityStatus;
    }

    public void updateSequenceOfCashGameVariantWidget(String username,int sequence){
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));

        String query = "update public.widget_user_mapping set sequence = "+sequence+" where user_id = "+userId+" and widget_id = 17";

        try (Connection connection = DatabaseManager.getDataSource(DataBaseConnections.PSQL.getResource()).getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(query)) {
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Unable to update the sequence of cash game variant widget", e);
        }
    }

}
