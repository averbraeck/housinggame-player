package nl.tudelft.simulation.housinggame.player.boughthouse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.house.HouseAccordion;
import nl.tudelft.simulation.housinggame.player.readbudget.BudgetAccordion;
import nl.tudelft.simulation.housinggame.player.readnews.NewsAccordion;

@WebServlet("/bought-house")
public class BoughtHouseServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        data.getContentHtml().clear();
        BudgetAccordion.makeBudgetAccordion(data);
        NewsAccordion.makeNewsAccordion(data);
        HouseAccordion.makeHouseConfirmationAccordion(data);

        response.sendRedirect("jsp/player/bought-house.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
