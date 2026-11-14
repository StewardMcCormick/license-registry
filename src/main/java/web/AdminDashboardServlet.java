package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static web.util.View.render;

@WebServlet("/admin")
public class AdminDashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("stats", services().getAdminService().getGlobalStats());
        request.setAttribute("organizations", services().getAdminService().getOrganizationSummaries());
        render(request, response, "admin/dashboard.jsp");
    }
}
