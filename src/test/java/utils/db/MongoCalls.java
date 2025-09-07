package utils.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.adda52.logging.Logging;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import com.adda52.utils.database.mongo.MongoDataBasePool;

public class MongoCalls extends MongoDataBasePool implements Logging {

    public String getResponsibleGamingOtp(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName= Constants.getMongoDbPrefix()+"user_details";
        String collectionName = "user_otp_verification";
        String transactionToken = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("userId", userId)).first();
        if (document != null) {
            transactionToken = document.getString("transactionToken");
            getLogger().info("Transaction Token: " + transactionToken);
        } else {
            getLogger().error("RG OTP: Document not found");
        }
        return transactionToken;
    }

    public String getResponsibleGamingGameVariant(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "table_limits";
        String pnm = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pnm = document.getString("pnm");
            getLogger().info("Game Variant: " + pnm);
        } else {
            getLogger().error("RG Game Variant: Document not found");
        }
        return pnm;
    }

    public String getResponsibleGamingTournamentBuyInLimitOptionText(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "tournament_buy_in_limits";
        String pvl = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pvl = document.getString("pvl");
            getLogger().info("buy in limit option text: " + pvl);
        } else {
            getLogger().error("RG tournament buy in limit option: Document not found");
        }
        return pvl;
    }

    public String getResponsibleGamingSngBuyInLimitOptionText(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "sng_buy_in_limits";
        String pvl = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pvl = document.getString("pvl");
            getLogger().info("buy in limit option text: " + pvl);
        } else {
            getLogger().error("RG tournament buy in limit option: Document not found");
        }
        return pvl;
    }

    public void expireResponsibleGamingLM(String username, long pastEpochDate) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "table_limits";
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Fetch the document to update
        Document document = collection.find(Filters.eq("uid", userId)).sort(Sorts.descending("lm")).first();
        if (document != null) {
            // Perform the update
            collection.updateOne(
                    Filters.eq("uid", userId),
                    Updates.set("lm", pastEpochDate)               // Set the lm field to the past date
            );
            getLogger().info("Updated lm to past date: " + pastEpochDate);
        } else {
            getLogger().error("Document not found for userId: " + userId);
        }
    }









    public String getResponsibleGamingDepositLimitValueText(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "deposit_limit";
        String pvl = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).sort(Sorts.descending("lm"))// Assuming you have a timestamp field
                .first();
        if (document != null) {
            pvl = document.getString("pvl").trim();
            getLogger().info("deposit Limit  existing value: " + pvl);
        } else {
            getLogger().error("RG tournament deposit limit existing value : Document not found");
        }
        return pvl;
    }

    public String getResponsibleGamingDepositDailyLimitCountValueText(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "deposit_limit";
        String pvl = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).sort(Sorts.descending("lm"))// Assuming you have a timestamp field
                .first();
        if (document != null) {
            pvl = document.getString("pvl").trim();
            getLogger().info("deposit daily limit count Limit  existing value: " + pvl);
        } else {
            getLogger().error("RG tournament deposit limit daily count value : Document not found");
        }
        return pvl;
    }

    public String getResponsibleGamingDepositWeeklyLimitCountValueText(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "deposit_limit";
        String pvl = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).sort(Sorts.descending("lm"))// Assuming you have a timestamp field
                .first();
        if (document != null) {
            pvl = document.getString("pvl").trim();
            getLogger().info("deposit weekly limit count Limit  existing value: " + pvl);
        } else {
            getLogger().error("RG tournament deposit limit weekly count value : Document not found");
        }
        return pvl;
    }

    public String getResponsibleGamingDepositDailyLimitExistingValueText(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "deposit_limit";
        String pvl = "";
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).sort(Sorts.descending("lm")).skip(1)// Assuming you have a timestamp field
                .first();
        if (document != null) {
            pvl = document.getString("pvl").trim();
            getLogger().info("deposit daily limit existing amount value: " + pvl);
        } else {
            getLogger().error("RG tournament deposit limit daily existing amount value : Document not found");
        }
        return pvl;
    }

    public String getResponsibleGamingDepositWeeklyLimitExistingValueText(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "deposit_limit";
        String pvl = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).sort(Sorts.descending("lm")).skip(1)// Assuming you have a timestamp field
                .first();
        if (document != null) {
            pvl = document.getString("pvl").trim();
            getLogger().info("deposit weekly limit existing amount value: " + pvl);
        } else {
            getLogger().error("RG tournament deposit limit weekly existing amount value : Document not found");
        }
        return pvl;
    }

    public String getResponsibleGamingDepositLimitTabName(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "deposit_limit";
        String tabName = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).sort(Sorts.descending("lm")) // Assuming you have a timestamp field
                .first();
        if (document != null) {
            tabName = document.getString("tab").trim();
            getLogger().info("deposit Limit tab name: " + tabName);
        } else {
            getLogger().error("RG tournament deposit limit tab name : Document not found");
        }
        return tabName;
    }

    public String getResponsibleGamingSelfExclusionSelectedPeriodText(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "self_exclusions";
        String pvl = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pvl = document.getString("pvl");
            getLogger().info("self exclusion selected period text: " + pvl);
        } else {
            getLogger().error("RG self exclusion selected period text: Document not found");
        }
        return pvl;
    }

    public String getResponsibleGamingTournamentBuyInLimitOptionPropValue(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "tournament_buy_in_limits";
        String pnm = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pnm = document.getString("pnm");
            getLogger().info("buy in limit option prop value: " + pnm);
        } else {
            getLogger().error("RG tournament buy in limit option prop value : Document not found");
        }
        return pnm;
    }

    public String getResponsibleGamingSngBuyInLimitOptionPropValue(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "sng_buy_in_limits";
        String pnm = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pnm = document.getString("pnm");
            getLogger().info("buy in limit option prop value: " + pnm);
        } else {
            getLogger().error("RG tournament buy in limit option prop value : Document not found");
        }
        return pnm;
    }

    public String getResponsibleGamingSelfExclusionPeriodPropValue(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "self_exclusions";
        String pnm = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pnm = document.getString("pnm");
            getLogger().info("self exclusion prop value: " + pnm);
        } else {
            getLogger().error("RG self exclusion prop value : Document not found");
        }
        return pnm;
    }

    public String getResponsibleGamingHoldemGameVariant(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "table_limits";
        String pnm = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pnm = document.getString("pnm").toUpperCase();
            getLogger().info("Game Variant: " + pnm);
        } else {
            getLogger().error("RG Game Variant: Document not found");
        }
        return pnm;
    }

    public String getResponsibleGamingBlindLimitDontPlay(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix()+"responsible_gaming";
        String collectionName = "table_limits";
        String pvl = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pvl = document.getString("pvl");
            getLogger().info("blind limit " + pvl);
        } else {
            getLogger().error("RG blind limit: Document not found");
        }
        return pvl;
    }

    public String getResponsibleGamingBlindLimit(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"responsible_gaming";
        String collectionName = "table_limits";
        String pvl = "";
        String expectedBlindLimit = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            pvl = document.getString("pvl");
            expectedBlindLimit = StringUtils.substringAfter(pvl, "Blinds").trim();
            getLogger().info("Blind Limit : " + expectedBlindLimit);
        } else {
            getLogger().error("RG Cash Blind Limit: Document not found");
        }
        return expectedBlindLimit;
    }


    public String getLoginOtpMessage(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"user_details";
        String collectionName = "sms_history";
        String message = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).sort(Sorts.descending("createdAt")).first();
        if (document != null) {
            message = document.getString("msg");
            getLogger().info("OTP SMS: " + message);
        } else {
            getLogger().error("LOGIN OTP SMS: Document not found");
        }

        return message;
    }

    public String getResponsibleGamingDurationLimit(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"responsible_gaming";
        String collectionName = "table_limits";
        String duration = "";

        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            duration = document.getString("duration");
            getLogger().info("Duration Limit: " + duration);
        } else {
            getLogger().error("RG Duration Limit: Document not found");
        }
        return duration;
    }

    public boolean deleteResponsibleGamingOtp(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"user_details";
        String collectionName = "user_otp_verification";
        boolean isDeleted = false;
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Find the document to delete
        Document document = collection.find(Filters.eq("userId", userId)).first();
        if (document != null) {
            // Delete the document
            DeleteResult result = collection.deleteOne(Filters.eq("userId", userId));
            isDeleted = result.getDeletedCount() > 0;
            getLogger().info("Document deleted: " + isDeleted);
        } else {
            getLogger().error("RG OTP: Document not found");
        }
        MongoDataBasePool.closeConnection();
        return isDeleted;
    }

    public void deleteResponsibleGamingCashTableLimits(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName =  Constants.getMongoDbPrefix() +"responsible_gaming";
        String collectionName = "table_limits";
        boolean isDeleted = false;
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Find the document to delete
        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            // Delete the document
            DeleteResult result = collection.deleteOne(Filters.eq("uid", userId));
            isDeleted = result.getDeletedCount() > 0;
            getLogger().info("Document deleted: " + isDeleted);
        } else {
            getLogger().error("cash table limits: Document not found");
        }
    }

    public void deleteResponsibleGamingTournamentTableLimits(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"responsible_gaming";
        String collectionName = "tournament_buy_in_limits";
        boolean isDeleted = false;
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Find the document to delete
        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            // Delete the document
            DeleteResult result = collection.deleteOne(Filters.eq("uid", userId));
            isDeleted = result.getDeletedCount() > 0;
            getLogger().info("Document deleted: " + isDeleted);
        } else {
            getLogger().error("tournament table limits: Document not found");
        }
    }

    public boolean deleteResponsibleGamingDepositLimit(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"responsible_gaming";
        String collectionName = "deposit_limit";
        boolean isDeleted = false;
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Find the document to delete
        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            // Delete the document
            DeleteResult result = collection.deleteOne(Filters.eq("uid", userId));
            isDeleted = result.getDeletedCount() > 0;
            getLogger().info("Document deleted: " + isDeleted);
        } else {
            getLogger().error("deposit limit: Document not found");
        }
        return isDeleted;
    }

    public boolean deleteResponsibleGamingAllDepositLimitEntery(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"responsible_gaming";
        String collectionName = "deposit_limit";
        boolean isDeleted = false;
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Delete all documents for the user
        DeleteResult result = collection.deleteMany(Filters.eq("uid", userId));
        // Check if any documents were deleted
        isDeleted = result.getDeletedCount() > 0;
        if (isDeleted) {
            getLogger().info("Documents deleted: " + result.getDeletedCount());
        } else {
            getLogger().error("deposit limit: No documents found to delete");
        }
        return isDeleted;
    }

    public boolean deleteResponsibleGamingSngTableLimits(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"responsible_gaming";
        String collectionName = "sng_buy_in_limits";
        boolean isDeleted = false;
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Find the document to delete
        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            // Delete the document
            DeleteResult result = collection.deleteOne(Filters.eq("uid", userId));
            isDeleted = result.getDeletedCount() > 0;
            getLogger().info("Document deleted: " + isDeleted);
        } else {
            getLogger().error("sng table limits: Document not found");
        }
        return isDeleted;
    }

    public boolean deleteResponsibleGamingSelfExclusionTableLimits(String username) {
        int userId = Integer.parseInt(new MySqlCalls().getUserID(username));
        String databaseName = Constants.getMongoDbPrefix() +"responsible_gaming";
        String collectionName = "self_exclusions";
        boolean isDeleted = false;
        MongoDatabase database = MongoDataBasePool.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Find the document to delete
        Document document = collection.find(Filters.eq("uid", userId)).first();
        if (document != null) {
            // Delete the document
            DeleteResult result = collection.deleteOne(Filters.eq("uid", userId));
            isDeleted = result.getDeletedCount() > 0;
            getLogger().info("Document deleted: " + isDeleted);
        } else {
            getLogger().error("self exclusion option limit: Document not found");
        }
        return isDeleted;
    }


}
