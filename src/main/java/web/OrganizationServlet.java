package web;

import exception.NotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Organization;
import model.OrganizationMember;
import util.ParamUtil;
import util.PathUtil;
import web.controller.LicenseController;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static web.util.View.*;

@WebServlet("/org/*")
public class OrganizationServlet extends BaseServlet {

    private LicenseController licenseController;

    @Override
    public void init() throws ServletException {
        super.init();
        licenseController = new LicenseController(services());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] segments = PathUtil.segments(req.getPathInfo());
        UUID orgId = PathUtil.segmentAsUuid(segments, 0);
        if (orgId == null) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Организация не найдена");
            return;
        }

        switch (PathUtil.segment(segments, 1)) {
            case "licenses" -> licenseController.doGet(req, resp, segments, orgId);
            case null -> showOrganization(req, resp, orgId);
            default -> renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Страница не найдена");
        }
    }

    private void showOrganization(HttpServletRequest req, HttpServletResponse resp, UUID orgId) throws ServletException, IOException {
        try {
            Organization organization = services().getOrganizationService().getById(orgId);
            List<OrganizationMember> members = services().getOrganizationService().getMembers(orgId);

            req.setAttribute("organization", organization);
            req.setAttribute("members", members);

            render(req, resp, "org/settings.jsp");
        } catch (NotFoundException e) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Организация не найдена");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] segments = PathUtil.segments(req.getPathInfo());
        UUID orgId = PathUtil.segmentAsUuid(segments, 0);
        if (orgId == null) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Организация не найдена");
            return;
        }

        switch (PathUtil.segment(segments, 1)) {
            case "licenses" -> licenseController.doPost(req, resp, segments, orgId);
            case null -> handleOrganizationAction(req, resp, segments, orgId);
            default -> renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Страница не найдена");
        }
    }

    private void handleOrganizationAction(HttpServletRequest req, HttpServletResponse resp, String[] segments, UUID orgId) throws IOException, ServletException {
        String action = PathUtil.segment(segments, 1);

        switch (action == null ? "" : action) {
            case "reissue-code" -> services().getOrganizationService().reissueJoinCode(orgId); // TODO: rewrite on AJAX
            case "promote", "lower" -> {
                UUID userId = ParamUtil.parseUuid(req.getParameter("userId"));
                if (userId == null) {
                    renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Пользователь не найден");
                    return;
                }

                handleUserRoleRequest(action, orgId, userId);               // TODO: rewrite on AJAX
            }
            default -> {
                renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Страница не найдена");
                return;
            }
        }

        redirect(req, resp, "/org/" + orgId);
    }

    private void handleUserRoleRequest(String action, UUID orgId, UUID userId) {
        if (action.equals("promote")) {
            services().getOrganizationService().promoteToAdmin(orgId, userId);
        } else {
            services().getOrganizationService().lowerToManager(orgId, userId);
        }
    }
}
