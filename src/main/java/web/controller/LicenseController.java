package web.controller;

import config.ServiceRegistry;
import exception.NotFoundException;
import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.License;
import model.LicenseStatus;
import model.LicenseType;
import model.Tag;
import util.ParamUtil;
import util.PathUtil;
import util.CurrentUser;
import util.SessionUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static web.util.View.render;
import static web.util.View.renderError;
import static web.util.View.redirect;


public class LicenseController {

    private final ServiceRegistry services;

    public LicenseController(ServiceRegistry services) {
        this.services = services;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp, String[] segments, UUID orgId) throws ServletException, IOException {
        String action = PathUtil.segment(segments, 2);

        switch (action) {
            case null -> showLicense(req, resp, orgId);
            case "new" -> showNewLicensePage(req, resp, orgId);
            case "types" -> showTypes(req, resp, orgId);
            default -> handleLicenseInfoRequest(req, resp, segments, orgId);
        }
    }

    private void showLicense(HttpServletRequest req, HttpServletResponse resp, UUID orgId) throws ServletException, IOException {
        LicenseStatus status = parseStatus(req.getParameter("status"));
        UUID typeId = ParamUtil.parseUuid(req.getParameter("typeId"));
        UUID tagId = ParamUtil.parseUuid(req.getParameter("tagId"));
        String query = req.getParameter("q");

        List<License> licenses = services.getLicenseService().search(orgId, status, typeId, tagId, query);
        List<LicenseType> types = services.getLicenseTypeService().getAll();
        List<Tag> tags = services.getTagService().getByOrgId(orgId);

        req.setAttribute("orgId", orgId);
        req.setAttribute("licenses", licenses);
        req.setAttribute("types", types);
        req.setAttribute("tags", tags);
        render(req, resp, "licenses/list.jsp");
    }

    private void showNewLicensePage(HttpServletRequest req, HttpServletResponse resp, UUID orgId) throws ServletException, IOException {
        req.setAttribute("orgId", orgId);
        req.setAttribute("types", services.getLicenseTypeService().getAll());
        render(req, resp, "licenses/form.jsp");
    }

    private void showTypes(HttpServletRequest req, HttpServletResponse resp, UUID orgId) throws ServletException, IOException {
        req.setAttribute("orgId", orgId);
        req.setAttribute("types", services.getLicenseTypeService().getAll());
        render(req, resp, "licenses/types.jsp");
    }

    private LicenseStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LicenseStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void handleLicenseInfoRequest(HttpServletRequest req, HttpServletResponse resp, String[] segments, UUID orgId) throws ServletException, IOException {
        UUID licenseId = PathUtil.segmentAsUuid(segments, 2);
        String action = PathUtil.segment(segments, 3);

        if (licenseId == null) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
            return;
        }

        try {
            License license = services.getLicenseService().getById(licenseId);
            req.setAttribute("orgId", orgId);

            if ("edit".equals(action)) {
                req.setAttribute("license", license);
                req.setAttribute("types", services.getLicenseTypeService().getAll());
                req.setAttribute("selectedTypes", services.getLicenseService().getTypes(licenseId));
                req.setAttribute("selectedTags", services.getLicenseService().getTags(licenseId));
                render(req, resp, "licenses/form.jsp");
            } else {
                req.setAttribute("license", license);
                req.setAttribute("types", services.getLicenseService().getTypes(licenseId));
                req.setAttribute("tags", services.getLicenseService().getTags(licenseId));
                render(req, resp, "licenses/detail.jsp");
            }
        } catch (NotFoundException e) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp, String[] segments, UUID orgId) throws ServletException, IOException {
        CurrentUser currentUser = SessionUtil.getCurrentUser(req);

        switch (PathUtil.segment(segments, 2)) {
            case "new" -> createLicense(req, resp, orgId);
            case "types" -> createType(req, resp, orgId);
            case null -> renderError(req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод не поддерживается");
            default -> handleLicenseAction(req, resp, segments, orgId, currentUser);
        }
    }

    private void createLicense(HttpServletRequest req, HttpServletResponse resp, UUID orgId) throws ServletException, IOException {
        try {
            License license = services.getLicenseService().create(
                    orgId,
                    req.getParameter("title"),
                    ParamUtil.parseDate(req.getParameter("issuedAt")),
                    ParamUtil.parseDate(req.getParameter("expiresAt")),
                    req.getParameter("notes"),
                    ParamUtil.parseUuidList(req.getParameterValues("typeIds")),
                    ParamUtil.parseCommaSeparated(req.getParameter("tags"))
            );
            redirect(req, resp, "/org/" + orgId + "/licenses/" + license.getId());
        } catch (ValidationException e) {
            req.setAttribute("orgId", orgId);
            req.setAttribute("error", e.getMessage());
            req.setAttribute("types", services.getLicenseTypeService().getAll());
            render(req, resp, "licenses/form.jsp");
        }
    }

    private void createType(HttpServletRequest req, HttpServletResponse resp, UUID orgId) throws ServletException, IOException {
        try {
            services.getLicenseTypeService().create(
                    req.getParameter("code"),
                    req.getParameter("name")
            );
            redirect(req, resp, "/org/" + orgId + "/licenses/types");
        } catch (ValidationException e) {
            req.setAttribute("orgId", orgId);
            req.setAttribute("error", e.getMessage());
            req.setAttribute("types", services.getLicenseTypeService().getAll());
            render(req, resp, "licenses/types.jsp");
        }
    }

    private void handleLicenseAction(HttpServletRequest req, HttpServletResponse resp, String[] segments, UUID orgId, CurrentUser currentUser)
            throws ServletException, IOException {
        UUID licenseId = PathUtil.segmentAsUuid(segments, 2);
        String action = PathUtil.segment(segments, 3);

        try {
            License license = services.getLicenseService().getById(licenseId);
            if (!belongsToCurrentOrg(currentUser, license.getOrgId()) || !orgId.equals(license.getOrgId())) {
                renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
                return;
            }

            switch (action == null ? "" : action) {
                case "edit" -> updateLicense(req, licenseId);
                case "cancel" -> services.getLicenseService().cancel(licenseId);
                case "restore" -> services.getLicenseService().restore(licenseId);
                case "delete" -> {
                    services.getLicenseService().delete(licenseId);
                    redirect(req, resp, "/org/" + orgId + "/licenses");
                    return;
                }
                default -> renderError(req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод не поддерживается");
            }

            redirect(req, resp, "/org/" + orgId + "/licenses/" + licenseId); // TODO: rewrite on AJAX
        } catch (ValidationException e) {
            renderError(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
        }
    }

    private void updateLicense(HttpServletRequest req, UUID licenseId) {
        services.getLicenseService().update(
                licenseId,
                req.getParameter("title"),
                ParamUtil.parseDate(req.getParameter("issuedAt")),
                ParamUtil.parseDate(req.getParameter("expiresAt")),
                req.getParameter("notes"),
                ParamUtil.parseUuidList(req.getParameterValues("typeIds")),
                ParamUtil.parseCommaSeparated(req.getParameter("tags"))
        );
    }

    private boolean belongsToCurrentOrg(CurrentUser user, UUID orgId) {
        return user.getMemberRoleMap().containsKey(orgId);
    }
}
