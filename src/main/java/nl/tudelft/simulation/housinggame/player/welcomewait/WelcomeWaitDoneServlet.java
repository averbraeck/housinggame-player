package nl.tudelft.simulation.housinggame.player.welcomewait;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.GroupState;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

@WebServlet("/welcome-wait-done")
public class WelcomeWaitDoneServlet extends HttpServlet
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

        // player clicked START GAME on welcome-wait screen to advance to read-budget
        if (nextScreen.equals("start-game"))
        {
            // reload with the latest state
            data.readDynamicData();
            PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
            GroupState groupState = GroupState.valueOf(data.getGroupRound().getGroupState());

            if (!playerState.equals(PlayerState.LOGIN))
                System.err.println("jsp = 'welcome-wait', but player state is '" + playerState + "'");
            if (groupState.nr < GroupState.NEW_ROUND.nr)
                System.err.println("jsp = 'welcome-wait', but group state is '" + groupState + "'");
            if (data.getPlayerRoundNumber() != 0)
            {
                data.setError("jsp = 'welcome-wait', but player round is " + data.getPlayerRoundNumber()
                        + ", and player state is '" + playerState + "'");
                response.sendRedirect("/housinggame-player/error");
                return;
            }
            if (data.getGroupRoundNumber() == 0)
            {
                data.setError("jsp = 'welcome-wait', but group round is " + data.getGroupRoundNumber());
                response.sendRedirect("/housinggame-player/error");
                return;
            }

            // advance to round 1
            GrouproundRecord groupRound1 = data.getGroupRoundList().get(1);
            PlayerroundRecord prr = SqlUtils.makePlayerRound(data, groupRound1);
            prr.setPlayerState(PlayerState.READ_BUDGET.toString());
            prr.store();
            data.readDynamicData();
            response.sendRedirect("/housinggame-player/read-budget");
            return;
        }

        // if the player did not click 'start-game' and enters the welcome-wait-done servlet, something is wrong
        System.err.println("Player app called welcome-wait-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
