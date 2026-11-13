package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.CurrentUser;

import java.io.IOException;

import static web.util.View.redirect;
import static web.util.View.render;

@WebServlet("/")
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);
        if (currentUser == null) {
            redirect(request, response, "/login");
            return;
        }

        request.setAttribute("currentUser", currentUser);
        render(request, response, "home/index.jsp");
    }
}
