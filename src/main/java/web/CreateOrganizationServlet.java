package web;

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

@WebServlet("/create-organization")
public class CreateOrganizationServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);
        String name = request.getParameter("name");

        try {
            Organization organization = services().getOrganizationService()
                    .createOrganization(name, currentUser.getUserId());
            currentUser.addMemberRoleMap(organization.getId(), MemberRole.ADMIN);
            redirect(request, response, "/org/" + organization.getId() + "/licenses");
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            render(request, response, "home/onboarding.jsp");
        }
    }
}
