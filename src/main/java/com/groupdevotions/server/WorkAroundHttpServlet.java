package com.groupdevotions.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class WorkAroundHttpServlet extends HttpServlet
{
    protected static final Logger logger = Logger
            .getLogger(WorkAroundHttpServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {

        if (!req.getRequestURI().startsWith("/.well-known"))
        {
            resp.sendError(404);
            return;
        }

        logger.info("comodo file served up");
        resp.setContentType("text/plain");
        resp.getOutputStream().print("fill me in");
    }
}
