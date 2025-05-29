package nl.tudelft.simulation.housinggame.player.welcomewait;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nl.tudelft.simulation.housinggame.common.GroupState;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

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
            GroupState groupState =
                    GroupState.valueOf(data.getGroupRoundList().get(data.getHighestGroupRoundNumber()).getGroupState());

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
            if (data.getHighestGroupRoundNumber() == 0)
            {
                data.setError("jsp = 'welcome-wait', but group round is " + data.getHighestGroupRoundNumber());
                response.sendRedirect("/housinggame-player/error");
                return;
            }

            // advance to round 1
            GrouproundRecord groupRound1 = data.getGroupRoundList().get(1);
            PlayerroundRecord prr = PlayerUtils.makePlayerRound(data, groupRound1);
            prr.store();
            data.newPlayerState(prr, PlayerState.READ_BUDGET, "Round=1");
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
