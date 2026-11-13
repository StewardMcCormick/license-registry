package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.ParamUtil;
import util.PathUtil;

import java.io.IOException;
import java.util.UUID;

import static web.util.View.redirect;

@WebServlet("/admin/*")
public class AdminActionServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = PathUtil.segment(PathUtil.segments(request.getPathInfo()), 0);
        UUID userId = ParamUtil.parseUuid(request.getParameter("userId"));

        if (userId != null && "grant-superadmin".equals(action)) {
            services().getStatsService().grantSuperadmin(userId);
        } else if (userId != null && "revoke-superadmin".equals(action)) {
            services().getStatsService().revokeSuperadmin(userId);
        }

        redirect(request, response, "/admin");
    }
}
