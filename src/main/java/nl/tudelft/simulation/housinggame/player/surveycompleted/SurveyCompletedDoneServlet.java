package nl.tudelft.simulation.housinggame.player.surveycompleted;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.player.PlayerData;

@WebServlet("/survey-completed-done")
public class SurveyCompletedDoneServlet extends HttpServlet
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

        // player clicked VIEW DAMAGE on the survey-completed screen
        if (nextScreen.equals("survey-completed"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_DAMAGE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/view-damage");
            return;
        }

        // if the player did not click 'VIEW DAMAGE' and enters the survey-completed-done servlet, something is wrong
        System.err.println("Player app called survey-completed-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
