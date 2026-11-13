package web;

import exception.NotFoundException;
import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberRole;
import model.Organization;
import util.CurrentUser;

import java.io.IOException;

import static web.util.View.redirect;
import static web.util.View.render;

@WebServlet("/join-organization")
public class JoinOrganizationServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);
        String joinCode = request.getParameter("joinCode");

        try {
            Organization organization = services().getOrganizationService()
                    .joinOrganization(joinCode, currentUser.getUserId());
            currentUser.addMemberRoleMap(organization.getId(), MemberRole.MANAGER);
            redirect(request, response, "/org/" + organization.getId() + "/licenses");
        } catch (ValidationException | NotFoundException e) {
            request.setAttribute("error", e.getMessage());
            render(request, response, "home/onboarding.jsp");
        }
    }
}
