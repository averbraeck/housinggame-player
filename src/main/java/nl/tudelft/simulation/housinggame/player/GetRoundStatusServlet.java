package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.RoundState;

@WebServlet("/get-round-status")
public class GetRoundStatusServlet extends HttpServlet
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

        // reload the round with the latest state
        data.readDynamicData();

        // return OK if the button for the current screen can be enabled, an empty string otherwise
        String jsp = request.getParameter("jsp");
        boolean ok = jsp == null ? false : checkNextScreenButton(data, jsp);

        if (!ok && data.getError().length() > 0)
            System.err.println("checkNextScreenButton for player " + data.getPlayerCode() + ": " + data.getError());

        response.setContentType("text/plain");
        response.getWriter().write(ok ? "OK" : "");
    }

    public static boolean checkNextScreenButton(final PlayerData data, final String jsp)
    {
        data.setError("");
        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
        RoundState roundState = RoundState.valueOf(data.getGroupRound().getRoundState());

        if (data.getGroupRoundNumber() < data.getPlayerRoundNumber())
        {
            data.setError("jsp = " + jsp + ", but group round " + data.getGroupRoundNumber() + " is less than player round "
                    + data.getPlayerRoundNumber() + ", and player state is '" + playerState + "'");
            return false;
        }
        if (data.getGroupRoundNumber() == data.getPlayerRoundNumber() && playerState.nr > roundState.nr)
        {
            data.setError("jsp = " + jsp + ", but player state " + playerState + " is larger than round state " + roundState
                    + ", and player round nr is " + data.getPlayerRoundNumber());
            return false;
        }

        if (jsp.equals("welcome-wait"))
        {
            if (data.getGroupRound() == null)
            {
                data.setError("jsp = 'welcome-wait', but GroupRound has not yet been created");
                return false;
            }
            if (data.getGroupRoundNumber() <= 1 && roundState.lt(RoundState.NEW_ROUND))
            {
                data.setError("jsp = 'welcome-wait', but GroupRound state is " + roundState + ", groupround = "
                        + data.getGroupRoundNumber());
                return false;
            }
            return true;
        }

        // for all other states, the same 'ok' rule holds
        if (data.getGroupRoundNumber() > data.getPlayerRoundNumber())
            return true;
        if (roundState.nr > playerState.nr)
            return true;
        // summary screen in last round: OK should not be true there
        if (data.getPlayerRoundNumber() == data.getScenario().getHighestRoundNumber() && jsp.equals("summary"))
            return false;

        return false;
    }

}
