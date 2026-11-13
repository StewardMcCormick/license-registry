package web;

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

import static web.util.View.render;

@WebServlet("/organizations")
public class OrganizationsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);

        List<Organization> organizations = services().getOrganizationService()
                .getByUserId(currentUser.getUserId());
        List<OrganizationMember> memberships = services().getOrganizationService()
                .getMembershipsByUserId(currentUser.getUserId());

        Map<UUID, String> rolesByOrgId = new HashMap<>();
        for (OrganizationMember membership : memberships) {
            rolesByOrgId.put(membership.getOrgId(), membership.getMemberRole().name());
        }

        request.setAttribute("organizations", organizations);
        request.setAttribute("rolesByOrgId", rolesByOrgId);
        render(request, response, "home/organizations.jsp");
    }
}
