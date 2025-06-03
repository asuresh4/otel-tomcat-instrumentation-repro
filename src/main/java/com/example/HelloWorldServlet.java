package com.example;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/hello")
public class HelloWorldServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the "error" parameter to trigger different exceptions
        String errorType = request.getParameter("error");

		Span span = Span.current();
        // RuntimeException: Handled by Tomcat
       	if ("runtime".equals(errorType)) {
           	throw new RuntimeException("This is a runtime exception!");
       	}
       	
        // Custom Exception: Handled internally by the application
        if ("custom".equals(errorType)) {
            try {
                throw new CustomException("This is a custom handled exception!");
            } catch (CustomException e) {
                // Handle the custom exception internally
                response.setContentType("text/plain");
                response.setStatus(500);
                response.getWriter().println("Error: " + e.getMessage());
                return;
            }
        }
       	
        // Custom Exception: Handled internally by the application
        if ("custom-updated-span".equals(errorType)) {
            try {
                throw new CustomException("This is a custom handled exception with manual instrumentation!");
            } catch (CustomException e) {
                // Handle the custom exception internally
                response.setContentType("text/plain");
                response.setStatus(500);
                response.getWriter().println("Error: " + e.getMessage());
                
	            // Record exception if caught
	            if (span != null) {
	                span.recordException(e);
	                // span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR);
	            }
                return;
            }
        }

        // Normal response when no error is triggered
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        response.getWriter().println("Hello, World!");
    }
}

class CustomException extends Exception {
    public CustomException(String message) {
        super(message);
    }
}
