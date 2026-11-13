package web;

import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static web.util.View.redirect;
import static web.util.View.render;

@WebServlet("/licenses/types")
public class LicenseTypeListServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("types", services().getLicenseTypeService().getAll());
        render(request, response, "licenses/types.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            services().getLicenseTypeService().create(
                    request.getParameter("code"),
                    request.getParameter("name")
            );
            redirect(request, response, "/licenses/types");
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("types", services().getLicenseTypeService().getAll());
            render(request, response, "licenses/types.jsp");
        }
    }
}
