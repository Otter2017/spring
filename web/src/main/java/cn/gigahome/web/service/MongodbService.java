//package cn.gigahome.web.service;
//
//import cn.gigahome.web.entity.User;
//import com.mongodb.client.result.DeleteResult;
//import com.mongodb.client.result.UpdateResult;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.List;
//
//@Service
//public class MongodbService {
//    private MongoTemplate mongoTemplate;
//
//    private static final String USER_COLLECTION_NAME = "systemUser";
//
//    @Autowired
//    public void setMongoTemplate(MongoTemplate mongoTemplate) {
//        this.mongoTemplate = mongoTemplate;
//    }
//
//    public void addUser(User user) {
//        mongoTemplate.save(user, USER_COLLECTION_NAME);
//    }
//
//    public User getUser(String userID) {
//        Query query = new Query(Criteria.where("userID").is(userID));
//        return mongoTemplate.findOne(query, User.class, USER_COLLECTION_NAME);
//    }
//
//    public List<User> listUser() {
//        Query query = new Query(Criteria.where("isDelete").is(false).and("isActive").is(true));
//        return mongoTemplate.find(query, User.class, USER_COLLECTION_NAME);
//    }
//
//    public boolean addPrivilege(String userID, String privilegeName) {
//        Query query = new Query(Criteria.where("userID").is(userID).and("isDelete").is(false));
//        if (mongoTemplate.exists(query, User.class, USER_COLLECTION_NAME)) {
//            User user = getUser(userID);
//            HashMap<String, Boolean> privileges = user.getOperatePrivileges();
//            privileges.put(privilegeName, true);
//            mongoTemplate.findAndReplace(query, user, USER_COLLECTION_NAME);
//            return true;
//        }
//        return false;
//    }
//
//    public boolean updateUser(User user) {
//        Query query = new Query(Criteria.where("userID").is(user.getUserID()));
//        Update update = new Update().set("userName", user.getUserName())
//                .set("password", user.getPassword())
//                .set("operatePrivileges", user.getOperatePrivileges())
//                .set("departments", user.getDepartments());
//        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class, USER_COLLECTION_NAME);
//        return updateResult.getModifiedCount() > 0;
//    }
//
//    public boolean logicalDelete(String userID) {
//        Query query = new Query(Criteria.where("userID").is(userID));
//        Update update = new Update().set("isDelete", true);
//        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class, USER_COLLECTION_NAME);
//        return updateResult.getModifiedCount() > 0;
//    }
//
//    public boolean deleteUser(String userID) {
//        Query query = new Query(Criteria.where("userID").is(userID));
//        DeleteResult deleteResult = mongoTemplate.remove(query, User.class, USER_COLLECTION_NAME);
//        return deleteResult.getDeletedCount() > 0;
//    }
//}
