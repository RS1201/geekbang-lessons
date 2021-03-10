package org.geektimes.projects.user.web.controller;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.DatabaseUserService;
import org.geektimes.projects.user.service.UserServiceImpl;
import org.geektimes.web.mvc.controller.PageController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/register")
public class RegisterAController implements PageController {


    @Resource(name = "bean/UserService")
    private UserServiceImpl userService;


    @GET
    @POST
    @Path("/add")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {

        String inputEmail = request.getParameter("inputEmail");
        String inputPassword = request.getParameter("inputPassword");
        String name = request.getParameter("name");
        String phoneNum = request.getParameter("phoneNum");
        System.out.println(inputEmail);
        System.out.println(inputPassword);

        User user = new User();
        user.setName(name);
        user.setPassword(inputPassword);
        user.setEmail(inputEmail);
        user.setPhoneNumber(phoneNum);

//        userService.register(user);

        DatabaseUserService databaseUserService = new DatabaseUserService();
        databaseUserService.register(user);
        return "success.jsp";
    }
}
