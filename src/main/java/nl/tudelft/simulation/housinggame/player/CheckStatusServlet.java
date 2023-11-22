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

@WebServlet("/check-status")
public class CheckStatusServlet extends HttpServlet
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

        // reload with the latest state
        data.readDynamicData();

        String okButton = "";
        if (request.getParameter("okButton") != null)
            okButton = request.getParameter("okButton");

        // are we in the same round as the group?
        if (data.getPlayerRoundNumber() != data.getGroupRoundNumber())
        {
            data.setError("data.getPlayerRoundNumber(" + data.getPlayerRoundNumber() + ") != data.getGroupRoundNumber("
                    + data.getGroupRoundNumber() + ")");
            response.sendRedirect("/housinggame-player/error");
            return;
        }

        if (okButton.equals("welcome-wait"))
        {
            if (!data.getPlayerRound().getPlayerState().equals(PlayerState.INIT.toString())
                    && !data.getPlayerRound().getPlayerState().equals(PlayerState.LOGIN.toString()))
                System.err.println(
                        "playerFinish = 'welcome-wait', but player state is '" + data.getPlayerRound().getPlayerState() + "'");
            if (RoundState.lt(data.getGroupRound().getRoundState(), RoundState.NEW_ROUND.toString()))
                System.err.println(
                        "playerFinish = 'welcome-wait', but group state is '" + data.getGroupRound().getRoundState() + "'");
            data.getPlayerRound().setPlayerState(PlayerState.READ_BUDGET.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/read-budget");
        }

        response.sendRedirect("/housinggame-player/login");
    }

}
