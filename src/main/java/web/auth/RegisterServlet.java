package web.auth;

import web.common.BaseServlet;
import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.user.User;
import util.auth.CurrentUser;
import util.auth.SessionUtil;

import java.io.IOException;

import static web.util.View.redirect;
import static web.util.View.render;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        render(request, response, "auth/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String lastName = request.getParameter("lastName");
        String firstName = request.getParameter("firstName");
        String middleName = request.getParameter("middleName");

        try {
            User user = services().getAuthService().register(email, password, lastName, firstName, middleName);
            SessionUtil.login(request, CurrentUser.of(user));
            redirect(request, response, "/");
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("lastName", lastName);
            request.setAttribute("firstName", firstName);
            request.setAttribute("middleName", middleName);
            render(request, response, "auth/register.jsp");
        }
    }
}
