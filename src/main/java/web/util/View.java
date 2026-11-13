package web.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class View {

    private static final Logger LOGGER = Logger.getLogger(View.class.getName());


    public static void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(request, response);
    }

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    public static void renderError(HttpServletRequest request, HttpServletResponse response, int status, String message)
            throws ServletException, IOException {
        LOGGER.log(Level.WARNING, "Ошибка " + status + " на " + request.getMethod() + " "
                + request.getRequestURI() + ": " + message);
        response.setStatus(status);
        request.setAttribute("status", status);
        request.setAttribute("message", message);
        render(request, response, "error.jsp");
    }
}
