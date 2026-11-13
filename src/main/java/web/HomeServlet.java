package web;

import exception.RepositoryException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Organization;
import model.OrganizationMember;
import util.CurrentUser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static web.util.View.*;

@WebServlet("/")
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);
        if (currentUser == null) {
            redirect(request, response, "/login");
            return;
        }

        try {
            populateHomeAttributes(request, currentUser);
            render(request, response, "home/index.jsp");
        } catch (RepositoryException e) {
            renderError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    void populateHomeAttributes(HttpServletRequest request, CurrentUser currentUser) {
        List<Organization> userOrganizations = services().getOrganizationService().getByUserId(currentUser.getUserId());
        List<OrganizationMember> memberships = services().getOrganizationService().getMembershipsByUserId(currentUser.getUserId());

        Map<UUID, String> rolesByOrgId = new HashMap<>();
        for (OrganizationMember membership : memberships) {
            rolesByOrgId.put(membership.getOrgId(), membership.getMemberRole().name());
        }

        request.setAttribute("currentUser", currentUser);
        request.setAttribute("organizations", userOrganizations);
        request.setAttribute("rolesByOrgId", rolesByOrgId);
    }
}
