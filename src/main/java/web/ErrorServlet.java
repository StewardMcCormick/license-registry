package web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/error")
public class ErrorServlet extends BaseServlet {

    private static final Logger LOGGER = Logger.getLogger(ErrorServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String containerMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int status = statusCode != null ? statusCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String message = exception != null
                ? exception.getClass().getSimpleName() + ": " + exception.getMessage()
                : containerMessage;

        LOGGER.log(Level.SEVERE, "Ошибка " + status + " на " + requestUri
                + (message != null && !message.isBlank() ? ": " + message : ""), exception);

        response.setStatus(status);
        request.setAttribute("status", status);
        request.setAttribute("message", message);
        render(request, response, "error.jsp");
    }
}
