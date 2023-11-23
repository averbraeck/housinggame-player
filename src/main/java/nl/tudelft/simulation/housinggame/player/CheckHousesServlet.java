package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HouseRecord;

@WebServlet("/check-houses")
public class CheckHousesServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = SessionUtils.getData(session);
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        data.getContentHtml().clear();
        ContentUtils.makeBudgetAccordion(data);
        ContentUtils.makeNewsAccordion(data);

        if (ContentUtils.makeHousesAccordion(data))
            response.sendRedirect("jsp/player/check-houses.jsp");
        else
        {
            // TODO: make error screen instead
            response.sendRedirect("jsp/player/login.jsp");
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
