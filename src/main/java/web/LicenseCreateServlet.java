package web;

import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.License;
import util.CurrentUser;
import util.ParamUtil;

import java.io.IOException;

@WebServlet("/licenses/new")
public class LicenseCreateServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("types", services().getLicenseTypeService().getAll());
        render(request, response, "licenses/form.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);
        try {
            License license = services().getLicenseService().create(
                    currentUser.getOrgId(),
                    request.getParameter("title"),
                    ParamUtil.parseDate(request.getParameter("issuedAt")),
                    ParamUtil.parseDate(request.getParameter("expiresAt")),
                    request.getParameter("notes"),
                    ParamUtil.parseUuidList(request.getParameterValues("typeIds")),
                    ParamUtil.parseCommaSeparated(request.getParameter("tags"))
            );
            redirect(request, response, "/licenses/" + license.getId());
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("types", services().getLicenseTypeService().getAll());
            render(request, response, "licenses/form.jsp");
        }
    }
}
