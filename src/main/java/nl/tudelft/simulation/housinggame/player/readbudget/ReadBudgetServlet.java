package nl.tudelft.simulation.housinggame.player.readbudget;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SessionUtils;

@WebServlet("/read-budget")
public class ReadBudgetServlet extends HttpServlet
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

        BudgetAccordion.makeBudgetAccordion(data);

        response.sendRedirect("jsp/player/read-budget.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
