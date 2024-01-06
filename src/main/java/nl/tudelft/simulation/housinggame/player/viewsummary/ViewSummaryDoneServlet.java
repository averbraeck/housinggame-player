package nl.tudelft.simulation.housinggame.player.viewsummary;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

@WebServlet("/view-summary-done")
public class ViewSummaryDoneServlet extends HttpServlet
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

        // player clicked VIEW SUMMARY on the view-damage screen
        if (nextScreen.equals("next-round"))
        {
            // check if groupRound has advanced to one above the current player round
            data.readDynamicData(); // update groupRoundList
            int roundNr = data.getPlayerRoundNumber();
            if (roundNr >= data.getScenario().getHighestRoundNumber())
                return;
            if (data.getGroupRoundList().size() <= roundNr + 1)
            {
                data.setError(
                        "jsp = 'summary' -> 'new-round', but group round is too low: " + data.getHighestGroupRoundNumber());
                response.sendRedirect("/housinggame-player/error");
            }
            GrouproundRecord groupRoundN = data.getGroupRoundList().get(roundNr + 1);
            PlayerroundRecord prr = SqlUtils.makePlayerRound(data, groupRoundN);
            prr.setPlayerState(PlayerState.READ_BUDGET.toString());
            prr.store();
            data.readDynamicData();
            response.sendRedirect("/housinggame-player/read-budget");
            return;
        }

        // if the player did not click 'VIEW SUMMARY' and enters the view-damage-done servlet, something is wrong
        System.err.println("Player app called view-damage-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
