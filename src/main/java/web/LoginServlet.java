package web;

import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.OrganizationMember;
import model.User;
import util.CurrentUser;
import util.SessionUtil;

import java.io.IOException;
import java.util.List;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        render(request, response, "auth/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User user = services().getAuthService().authenticate(email, password);
            CurrentUser currentUser = new CurrentUser(user.getId(), user.getEmail(),
                    user.getLastName() + " " + user.getFirstName(), user.getGlobalRole());

            List<OrganizationMember> memberships = services().getOrganizationService()
                    .getMembershipsByUserId(user.getId());
            if (!memberships.isEmpty()) {
                OrganizationMember membership = memberships.get(0);
                currentUser.setOrgId(membership.getOrgId());
                currentUser.setMemberRole(membership.getMemberRole());
            }

            SessionUtil.login(request, currentUser);
            redirect(request, response, currentUser.getOrgId() == null ? "/onboarding" : "/licenses");
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            render(request, response, "auth/login.jsp");
        }
    }
}
