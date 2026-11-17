package web.organization.contoller;

import config.ServiceRegistry;
import exception.NotFoundException;
import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.license.License;
import model.license.LicenseStatus;
import model.license.type.LicenseType;
import model.organization.MemberRole;
import model.tag.Tag;
import util.auth.CurrentUser;
import util.web.ParamUtil;
import util.web.PathUtil;
import util.auth.SessionUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static web.util.View.redirect;
import static web.util.View.render;
import static web.util.View.renderError;


public class LicenseController {

    private final ServiceRegistry services;

    public LicenseController(ServiceRegistry services) {
        this.services = services;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp, String[] segments, UUID orgId)
            throws ServletException, IOException {
        String action = PathUtil.segment(segments, 2);

        switch (action) {
            case null -> showLicenseList(req, resp, orgId);
            case "new" -> showNewLicensePage(req, resp, orgId);
            case "types" -> showTypes(req, resp, orgId);
            default -> handleLicenseInfoRequest(req, resp, segments, orgId);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp, String[] segments, UUID orgId)
            throws ServletException, IOException {
        CurrentUser currentUser = SessionUtil.getCurrentUser(req);

        switch (PathUtil.segment(segments, 2)) {
            case "new" -> createLicense(req, resp, orgId);
            case "types" -> createType(req, resp, orgId, currentUser);
            case null -> renderError(req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "Метод не поддерживается");
            default -> handleLicenseAction(req, resp, segments, orgId, currentUser);
        }
    }

    private void showLicenseList(HttpServletRequest req, HttpServletResponse resp, UUID orgId)
            throws ServletException, IOException {
        LicenseStatus status = parseStatus(req.getParameter("status"));
        UUID typeId = ParamUtil.parseUuid(req.getParameter("typeId"));
        List<UUID> tagIds = ParamUtil.parseUuidList(req.getParameterValues("tagIds"));
        String query = req.getParameter("q");

        List<License> licenses = services.getLicenseService().search(orgId, status, typeId, tagIds, query);
        List<LicenseType> types = services.getLicenseTypeService().getAll(orgId);
        List<Tag> tags = services.getTagService().getByOrgId(orgId);

        exposeOrgContext(req, orgId);
        req.setAttribute("licenses", licenses);
        req.setAttribute("types", types);
        req.setAttribute("tags", tags);
        req.setAttribute("selectedTagIds", tagIds);

        String acceptHeader = req.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            sendJsonLicensesList(resp, licenses);
            return;
        }

        sendJspLicensesList(req, resp);
    }

    private void sendJspLicensesList(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        render(req, resp, "licenses/list.jsp");
    }

    private void sendJsonLicensesList(HttpServletResponse resp, List<License> licenses) throws IOException {
        resp.setContentType("application/json");
        services.getObjectMapper().writeValue(resp.getWriter(), licenses);
    }

    private void showNewLicensePage(HttpServletRequest req, HttpServletResponse resp, UUID orgId)
            throws ServletException, IOException {
        exposeOrgContext(req, orgId);
        req.setAttribute("types", services.getLicenseTypeService().getAll(orgId));
        render(req, resp, "licenses/new.jsp");
    }

    private void showTypes(HttpServletRequest req, HttpServletResponse resp, UUID orgId)
            throws ServletException, IOException {
        exposeOrgContext(req, orgId);
        req.setAttribute("types", services.getLicenseTypeService().getAll(orgId));
        render(req, resp, "licenses/types.jsp");
    }

    private void handleLicenseInfoRequest(HttpServletRequest req, HttpServletResponse resp,
                                          String[] segments, UUID orgId) throws ServletException, IOException {
        UUID licenseId = PathUtil.segmentAsUuid(segments, 2);
        String action = PathUtil.segment(segments, 3);

        if (licenseId == null) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
            return;
        }

        try {
            License license = services.getLicenseService().getById(licenseId);
            if (!orgId.equals(license.getOrgId())) {
                renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
                return;
            }

            exposeOrgContext(req, orgId);
            req.setAttribute("license", license);

            if (action == null) {
                req.setAttribute("types", services.getLicenseService().getTypes(licenseId));
                req.setAttribute("tags", services.getLicenseService().getTags(licenseId));
                render(req, resp, "licenses/detail.jsp");
            } else if ("edit".equals(action)) {
                showEditLicensePage(req, resp, license);
            } else {
                renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Страница не найдена");
            }
        } catch (NotFoundException e) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    private void showEditLicensePage(HttpServletRequest req, HttpServletResponse resp, License license)
            throws ServletException, IOException {
        UUID orgId = license.getOrgId();
        req.setAttribute("types", services.getLicenseTypeService().getAll(orgId));

        req.setAttribute("selectedTypeIds", services.getLicenseService().getTypes(license.getId()).stream()
                .map(LicenseType::getId)
                .toList());

        List<Tag> selectedTags = services.getLicenseService().getTags(license.getId());

        req.setAttribute("tagsValue", selectedTags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", ")));

        render(req, resp, "licenses/edit.jsp");
    }

    private void createLicense(HttpServletRequest req, HttpServletResponse resp, UUID orgId)
            throws ServletException, IOException {
        String title = req.getParameter("title");
        LocalDate issuedAt = ParamUtil.parseDate(req.getParameter("issuedAt"));
        LocalDate expiresAt = ParamUtil.parseDate(req.getParameter("expiresAt"));
        String notes = req.getParameter("notes");
        List<UUID> typeIds = ParamUtil.parseUuidList(req.getParameterValues("typeIds"));
        List<String> tags = ParamUtil.parseCommaSeparated(req.getParameter("tags"));

        try {
            License license = services.getLicenseService().create(
                    orgId,
                    title,
                    issuedAt,
                    expiresAt,
                    notes,
                    typeIds,
                    tags
            );
            redirect(req, resp, "/org/" + orgId + "/licenses/" + license.getId());
        } catch (ValidationException e) {
            exposeOrgContext(req, orgId);
            req.setAttribute("error", e.getMessage());
            req.setAttribute("types", services.getLicenseTypeService().getAll(orgId));
            req.setAttribute("formTitle", title);
            req.setAttribute("formIssuedAt", issuedAt);
            req.setAttribute("formExpiresAt", expiresAt);
            req.setAttribute("formNotes", notes);
            req.setAttribute("formTags", req.getParameter("tags"));
            req.setAttribute("formTypeIds", typeIds);
            render(req, resp, "licenses/new.jsp");
        }
    }

    private void createType(HttpServletRequest req, HttpServletResponse resp, UUID orgId, CurrentUser currentUser)
            throws ServletException, IOException {
        if (!isOrgAdmin(currentUser, orgId)) {
            renderError(req, resp, HttpServletResponse.SC_FORBIDDEN,
                    "Доступно только администратору организации");
            return;
        }
        try {
            services.getLicenseTypeService().create(
                    orgId,
                    req.getParameter("code"),
                    req.getParameter("name")
            );
            redirect(req, resp, "/org/" + orgId + "/licenses/types");
        } catch (ValidationException e) {
            exposeOrgContext(req, orgId);
            req.setAttribute("error", e.getMessage());
            req.setAttribute("types", services.getLicenseTypeService().getAll(orgId));
            render(req, resp, "licenses/types.jsp");
        }
    }

    private void handleLicenseAction(HttpServletRequest req, HttpServletResponse resp, String[] segments,
                                     UUID orgId, CurrentUser currentUser)
            throws ServletException, IOException {
        UUID licenseId = PathUtil.segmentAsUuid(segments, 2);
        String action = PathUtil.segment(segments, 3);

        if (licenseId == null) {
            renderError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Лицензия не найдена");
            return;
        }

        try {
            License license = services.getLicenseService().getById(licenseId);
            if (!orgId.equals(license.getOrgId()) || !canManage(currentUser, orgId)) {
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
                default -> {
                    renderError(req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                            "Метод не поддерживается");
                    return;
                }
            }

            redirect(req, resp, "/org/" + orgId + "/licenses/" + licenseId);
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

    private void exposeOrgContext(HttpServletRequest req, UUID orgId) {
        req.setAttribute("orgId", orgId);
        CurrentUser currentUser = SessionUtil.getCurrentUser(req);
        String orgRole = null;
        if (currentUser != null && !currentUser.isSuperadmin()) {
            orgRole = services.getOrganizationService()
                    .findMemberRole(orgId, currentUser.userId())
                    .map(MemberRole::name)
                    .orElse(null);
        }
        req.setAttribute("orgRole", orgRole);
    }

    private boolean canManage(CurrentUser user, UUID orgId) {
        if (user == null) {
            return false;
        }
        if (user.isSuperadmin()) {
            return true;
        }
        return services.getOrganizationService().findMemberRole(orgId, user.userId()).isPresent();
    }

    private boolean isOrgAdmin(CurrentUser user, UUID orgId) {
        if (user == null) {
            return false;
        }
        if (user.isSuperadmin()) {
            return true;
        }
        return services.getOrganizationService().findMemberRole(orgId, user.userId())
                .orElse(null) == MemberRole.ADMIN;
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
}
