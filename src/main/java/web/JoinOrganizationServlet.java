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

@WebServlet("/onboarding/join-organization")
public class JoinOrganizationServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);
        String joinCode = request.getParameter("joinCode");

        try {
            Organization organization = services().getOrganizationService()
                    .joinOrganization(joinCode, currentUser.getUserId());
            currentUser.setOrgId(organization.getId());
            currentUser.setMemberRole(MemberRole.MANAGER);
            redirect(request, response, "/licenses");
        } catch (ValidationException | NotFoundException e) {
            request.setAttribute("error", e.getMessage());
            render(request, response, "onboarding/index.jsp");
        }
    }
}
