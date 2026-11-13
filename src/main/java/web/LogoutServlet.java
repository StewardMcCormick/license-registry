package web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.SessionUtil;

import java.io.IOException;

import static web.util.View.redirect;

@WebServlet("/logout")
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SessionUtil.logout(request);
        redirect(request, response, "/login");
    }
}
