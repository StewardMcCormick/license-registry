package web;

import exception.NotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Organization;
import model.OrganizationMember;
import util.ParamUtil;
import util.PathUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/org/*")
public class OrganizationServlet extends BaseServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		UUID orgId = PathUtil.segmentAsUuid(PathUtil.segments(request.getPathInfo()), 0);
		if (orgId == null) {
			renderError(request, response, HttpServletResponse.SC_NOT_FOUND, "Организация не найдена");
			return;
		}

		try {
			Organization organization = services().getOrganizationService().getById(orgId);
			List<OrganizationMember> members = services().getOrganizationService().getMembers(orgId);
			request.setAttribute("organization", organization);
			request.setAttribute("members", members);
			render(request, response, "org/settings.jsp");
		} catch (NotFoundException e) {
			renderError(request, response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String[] segments = PathUtil.segments(request.getPathInfo());
		UUID orgId = PathUtil.segmentAsUuid(segments, 0);
		String action = PathUtil.segment(segments, 1);

		if (orgId == null) {
			renderError(request, response, HttpServletResponse.SC_NOT_FOUND, "Организация не найдена");
			return;
		}

		UUID userId = ParamUtil.parseUuid(request.getParameter("userId"));
		if (userId == null) {
			return;
		}

		if ("reissue-code".equals(action)) {
			services().getOrganizationService().reissueJoinCode(orgId);
		} else if ("promote".equals(action)) {
			services().getOrganizationService().promoteToAdmin(orgId, userId);
		} else if ("lower".equals(action)) {
			services().getOrganizationService().lowerToManager(orgId, userId);
		}

		redirect(request, response, "/org/" + orgId);
	}
}
