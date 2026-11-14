package web;

import exception.NotFoundException;
import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.ParamUtil;
import util.PathUtil;

import java.io.IOException;
import java.util.UUID;

import static web.util.View.redirect;
import static web.util.View.renderError;

@WebServlet("/admin/*")
public class AdminActionServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = PathUtil.segment(PathUtil.segments(request.getPathInfo()), 0);

        try {
            switch (action == null ? "" : action) {
                case "grant-superadmin" -> {
                    UUID userId = requireUuid(request, response, "userId", "Пользователь не найден");
                    if (userId == null) {
                        return;
                    }
                    services().getAdminService().grantSuperadmin(userId);
                }
                case "revoke-superadmin" -> {
                    UUID userId = requireUuid(request, response, "userId", "Пользователь не найден");
                    if (userId == null) {
                        return;
                    }
                    services().getAdminService().revokeSuperadmin(userId);
                }
                case "reissue-code" -> {
                    UUID orgId = requireUuid(request, response, "orgId", "Организация не найдена");
                    if (orgId == null) {
                        return;
                    }
                    services().getOrganizationService().reissueJoinCode(orgId);
                }
                default -> {
                    renderError(request, response, HttpServletResponse.SC_NOT_FOUND, "Страница не найдена");
                    return;
                }
            }
        } catch (NotFoundException e) {
            renderError(request, response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        } catch (ValidationException e) {
            renderError(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        redirect(request, response, "/admin");
    }

    private UUID requireUuid(HttpServletRequest request, HttpServletResponse response,
                             String parameter, String message) throws ServletException, IOException {
        UUID id = ParamUtil.parseUuid(request.getParameter(parameter));
        if (id == null) {
            renderError(request, response, HttpServletResponse.SC_NOT_FOUND, message);
        }
        return id;
    }
}
