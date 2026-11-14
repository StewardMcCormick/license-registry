package web;

import exception.NotFoundException;
import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberRole;
import model.Organization;
import model.OrganizationMember;
import util.CurrentUser;
import util.ParamUtil;
import util.PathUtil;
import web.controller.LicenseController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static web.util.View.redirect;
import static web.util.View.render;
import static web.util.View.renderError;

@WebServlet({"/org", "/org/*"})
public class OrganizationServlet extends BaseServlet {

    private LicenseController licenseController;

    @Override
    public void init() throws ServletException {
        super.init();
        licenseController = new LicenseController(services());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String[] segments = PathUtil.segments(req.getPathInfo());
        if (isOrgRoot(segments)) {
            showUserOrganizations(req, resp);
            return;
        }

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String[] segments = PathUtil.segments(req.getPathInfo());
        if (isOrgRoot(segments)) {
            createOrganization(req, resp);
            return;
        }

        UUID orgId = PathUtil.segmentAsUuid(segments, 0);
        if (orgId == null) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Организация не найдена");
            return;
        }

        switch (PathUtil.segment(segments, 1)) {
            case "licenses" -> licenseController.doPost(req, resp, segments, orgId);
            case null -> renderError(req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "Метод не поддерживается");
            default -> handleOrganizationAction(req, resp, PathUtil.segment(segments, 1), orgId);
        }
    }

    private boolean isOrgRoot(String[] segments) {
        return segments.length == 0 || (segments.length == 1 && segments[0].isBlank());
    }

    private void showUserOrganizations(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(req);

        List<Organization> organizations = services().getOrganizationService()
                .getByUserId(currentUser.getUserId());
        List<OrganizationMember> memberships = services().getOrganizationService()
                .getMembershipsByUserId(currentUser.getUserId());

        Map<UUID, String> rolesByOrgId = new HashMap<>();
        for (OrganizationMember membership : memberships) {
            rolesByOrgId.put(membership.getOrgId(), membership.getMemberRole().name());
        }

        req.setAttribute("organizations", organizations);
        req.setAttribute("rolesByOrgId", rolesByOrgId);
        render(req, resp, "home/org-list.jsp");
    }

    private void showOrganization(HttpServletRequest req, HttpServletResponse resp, UUID orgId)
            throws ServletException, IOException {
        try {
            Organization organization = services().getOrganizationService().getById(orgId);
            List<OrganizationMember> members = services().getOrganizationService().getMembers(orgId);

            req.setAttribute("orgId", orgId);
            req.setAttribute("orgRole", resolveOrgRole(req, orgId));
            req.setAttribute("organization", organization);
            req.setAttribute("members", members);

            render(req, resp, "org/settings.jsp");
        } catch (NotFoundException e) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Организация не найдена");
        }
    }

    private void createOrganization(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(req);

        try {
            Organization organization = services().getOrganizationService()
                    .createOrganization(req.getParameter("name"), currentUser.getUserId());
            redirect(req, resp, "/org/" + organization.getId() + "/licenses");
        } catch (ValidationException e) {
            req.setAttribute("error", e.getMessage());
            render(req, resp, "home/onboarding.jsp");
        }
    }

    private void handleOrganizationAction(HttpServletRequest req, HttpServletResponse resp,
                                          String action, UUID orgId) throws IOException, ServletException {
        try {
            switch (action == null ? "" : action) {
                case "reissue-code" -> services().getOrganizationService().reissueJoinCode(orgId);
                case "promote", "lower" -> {
                    UUID userId = ParamUtil.parseUuid(req.getParameter("userId"));
                    if (userId == null) {
                        renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Пользователь не найден");
                        return;
                    }
                    handleUserRoleRequest(action, orgId, userId);
                }
                default -> {
                    renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Страница не найдена");
                    return;
                }
            }
        } catch (NotFoundException e) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        } catch (ValidationException e) {
            renderError(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        redirect(req, resp, "/org/" + orgId);
    }

    private void handleUserRoleRequest(String action, UUID orgId, UUID userId) {
        if ("promote".equals(action)) {
            services().getOrganizationService().promoteToAdmin(orgId, userId);
        } else {
            services().getOrganizationService().lowerToManager(orgId, userId);
        }
    }

    private String resolveOrgRole(HttpServletRequest req, UUID orgId) {
        CurrentUser currentUser = currentUser(req);
        if (currentUser == null || currentUser.isSuperadmin()) {
            return null;
        }
        return services().getOrganizationService()
                .findMemberRole(orgId, currentUser.getUserId())
                .map(MemberRole::name)
                .orElse(null);
    }
}
