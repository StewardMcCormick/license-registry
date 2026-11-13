package web;

import exception.NotFoundException;
import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.License;
import util.CurrentUser;
import util.ParamUtil;
import util.PathUtil;

import java.io.IOException;
import java.util.UUID;

@WebServlet("/licenses/*")
public class LicenseItemServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);
        String[] segments = PathUtil.segments(request.getPathInfo());
        UUID licenseId = PathUtil.segmentAsUuid(segments, 0);
        String action = PathUtil.segment(segments, 1);

        if (licenseId == null) {
            renderError(request, response, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
            return;
        }

        try {
            License license = services().getLicenseService().getById(licenseId);
            if (!belongsToCurrentOrg(currentUser, license.getOrgId())) {
                renderError(request, response, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
                return;
            }

            if ("edit".equals(action)) {
                request.setAttribute("license", license);
                request.setAttribute("types", services().getLicenseTypeService().getAll());
                request.setAttribute("selectedTypes", services().getLicenseService().getTypes(licenseId));
                request.setAttribute("selectedTags", services().getLicenseService().getTags(licenseId));
                render(request, response, "licenses/form.jsp");
            } else {
                request.setAttribute("license", license);
                request.setAttribute("types", services().getLicenseService().getTypes(licenseId));
                request.setAttribute("tags", services().getLicenseService().getTags(licenseId));
                render(request, response, "licenses/detail.jsp");
            }
        } catch (NotFoundException e) {
            renderError(request, response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);
        String[] segments = PathUtil.segments(request.getPathInfo());
        UUID licenseId = PathUtil.segmentAsUuid(segments, 0);
        String action = PathUtil.segment(segments, 1);

        if (licenseId == null) {
            renderError(request, response, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
            return;
        }

        try {
            License license = services().getLicenseService().getById(licenseId);
            if (!belongsToCurrentOrg(currentUser, license.getOrgId())) {
                renderError(request, response, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
                return;
            }

            if ("edit".equals(action)) {
                services().getLicenseService().update(
                        licenseId,
                        request.getParameter("title"),
                        ParamUtil.parseDate(request.getParameter("issuedAt")),
                        ParamUtil.parseDate(request.getParameter("expiresAt")),
                        request.getParameter("notes"),
                        ParamUtil.parseUuidList(request.getParameterValues("typeIds")),
                        ParamUtil.parseCommaSeparated(request.getParameter("tags"))
                );
            } else if ("cancel".equals(action)) {
                services().getLicenseService().cancel(licenseId);
            } else if ("restore".equals(action)) {
                services().getLicenseService().restore(licenseId);
            } else if ("delete".equals(action)) {
                services().getLicenseService().delete(licenseId);
                redirect(request, response, "/licenses");
                return;
            }
            redirect(request, response, "/licenses/" + licenseId);
        } catch (ValidationException | NotFoundException e) {
            renderError(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
