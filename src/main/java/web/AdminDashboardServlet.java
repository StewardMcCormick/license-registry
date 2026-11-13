package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin")
public class AdminDashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("stats", services().getStatsService().getGlobalStats());
        request.setAttribute("organizations", services().getStatsService().getOrganizationSummaries());
        render(request, response, "admin/dashboard.jsp");
    }
}
