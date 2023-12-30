package nl.tudelft.simulation.housinggame.player.viewtaxes;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.player.PlayerData;

@WebServlet("/view-taxes-done")
public class ViewTaxesDoneServlet extends HttpServlet
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

        // player clicked VIEW IMPROVEMENTS on the view-taxes screen
        if (nextScreen.equals("view-improvements"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_IMPROVEMENTS.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/view-improvements");
            return;
        }

        // if the player did not click 'view taxes' and enters the read-news-done servlet, something is wrong
        System.err.println("Player app called view-taxes-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
