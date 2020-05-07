package cn.gigahome.web;

import cn.gigahome.web.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);

//    private MongodbService mongodbService;
//
//    @Autowired
//    public void setMongodbService(MongodbService mongodbService) {
//        this.mongodbService = mongodbService;
//    }
//
//    @PostMapping("/user")
//    public boolean addUser() {
//        Region region = new Region();
//        region.setRegionID(4403);
//        region.setProvinceName("广东省");
//        region.setCityName("深圳市");
//        User user = new User();
//        user.setUserID(UUID.randomUUID().toString());
//        user.setUserName("admin");
//        user.setPassword("admin");
//        user.setActive(true);
//        user.setDelete(false);
//        user.setRegion(region);
//        HashMap<String, Boolean> privileges = new HashMap<>();
//        privileges.put("addUser", true);
//        privileges.put("updateUser", true);
//        privileges.put("deleteUser", false);
//        privileges.put("deleteUsers", false);
//        user.setOperatePrivileges(privileges);
//
//        mongodbService.addUser(user);
//        return true;
//    }
//
//    @GetMapping("/user/{userID}")
//    public User getUser(@PathVariable("userID") String userID) {
//        return mongodbService.getUser(userID);
//    }
//
//    @GetMapping("/users")
//    public List<User> listUser() {
//        return mongodbService.listUser();
//    }
//
//    @PostMapping("/user/privilege")
//    public boolean addPrivilege(@RequestParam("userID") String userID, @RequestParam("privilegeName") String privilegeName) {
//        return mongodbService.addPrivilege(userID, privilegeName);
//    }

    @PostMapping("/user/login")
    public boolean userLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody User data) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    logger.info("{} = {}", cookie.getName(), cookie.getValue());
                }
            }
            String userName = data.getUserName();
            String password = data.getPassword();
            Cookie newCookie = new Cookie("user-token", userName);
            response.addCookie(newCookie);
            logger.info("Login : {}  - {}", userName, password);
            if ("admin".equals(userName)) {
                return true;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return false;
    }
}
