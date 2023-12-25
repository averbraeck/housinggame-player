package nl.tudelft.simulation.housinggame.player.readnews;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.player.PlayerData;

@WebServlet("/read-news-done")
public class ReadNewsDoneServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null || request.getParameter("nextScreen") == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        // the next screen button indicates the INTENTION of the player, not the screen it originates from.
        String nextScreen = request.getParameter("nextScreen");

        // player clicked VIEW HOUSES on the read-news screen, and we are in round 1 (buy house)
        if (nextScreen.equals("view-buy-house") && data.getPlayerRoundNumber() == 1)
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_BUY_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/buy-house");
            return;
        }

        // player clicked VIEW HOUSES on the read-news screen, and we are in round 2 or up (sell/stay house)
        if (nextScreen.equals("view-sell-house") && data.getPlayerRoundNumber() > 1)
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_SELL_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/sell-house");
            return;
        }

        // if the player did not click 'view house' and enters the read-news-done servlet, something is wrong
        System.err.println("Player app called read-news-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
