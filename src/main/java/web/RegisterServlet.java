package web;

import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.CurrentUser;
import util.SessionUtil;

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

    @Override // TODO: rewrite to AJAX
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String lastName = request.getParameter("lastName");
        String firstName = request.getParameter("firstName");
        String middleName = request.getParameter("middleName");

        try {
            User user = services().getAuthService().register(email, password, lastName, firstName, middleName);
            SessionUtil.login(request, new CurrentUser(user.getId(), user.getEmail(),
                    user.getLastName() + " " + user.getFirstName(), user.getGlobalRole()));
            redirect(request, response, "/");
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            render(request, response, "auth/register.jsp");
        }
    }
}
