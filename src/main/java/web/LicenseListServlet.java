package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.License;
import model.LicenseStatus;
import model.LicenseType;
import model.Tag;
import util.CurrentUser;
import util.ParamUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/licenses")
public class LicenseListServlet extends BaseServlet {

    @Override // TODO: rewrite on AJAX
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CurrentUser currentUser = currentUser(request);

        LicenseStatus status = parseStatus(request.getParameter("status"));
        UUID typeId = ParamUtil.parseUuid(request.getParameter("typeId"));
        UUID tagId = ParamUtil.parseUuid(request.getParameter("tagId"));
        String query = request.getParameter("q");

        List<License> licenses = services().getLicenseService()
                .search(currentUser.getOrgId(), status, typeId, tagId, query);
        List<LicenseType> types = services().getLicenseTypeService().getAll();
        List<Tag> tags = services().getTagService().getByOrgId(currentUser.getOrgId());

        request.setAttribute("licenses", licenses);
        request.setAttribute("types", types);
        request.setAttribute("tags", tags);
        render(request, response, "licenses/list.jsp");
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
