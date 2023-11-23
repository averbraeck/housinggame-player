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
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;

/**
 * AdvanceStateServlet is called when the OK button at the bottom of the player scren is pressed, and the player wants to
 * advances to a next state.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
@WebServlet("/advance-state")
public class AdvanceStateServlet extends HttpServlet
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
        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
        RoundState roundState = RoundState.valueOf(data.getGroupRound().getRoundState());

        String okButton = "";
        if (request.getParameter("okButton") != null)
            okButton = request.getParameter("okButton");

        if (okButton.equals("welcome-wait"))
        {
            if (!playerState.equals(PlayerState.LOGIN))
                System.err.println("playerFinish = 'welcome-wait', but player state is '" + playerState + "'");
            if (roundState.nr < RoundState.NEW_ROUND.nr)
                System.err.println("playerFinish = 'welcome-wait', but group state is '" + roundState + "'");
            if (data.getPlayerRoundNumber() != 0)
            {
                data.setError("jsp = 'welcome-wait', but player round is " + data.getPlayerRoundNumber()
                + ", and player state is '" + playerState + "'");
                response.sendRedirect("/housinggame-player/error");
            }
            if (data.getGroupRoundNumber() == 0)
            {
                data.setError("jsp = 'welcome-wait', but group round is " + data.getGroupRoundNumber());
                response.sendRedirect("/housinggame-player/error");
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

        if (okButton.equals("read-budget"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.READ_NEWS.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/read-news");
            return;
        }

        if (okButton.equals("read-news"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.CHECK_HOUSES.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/check-houses");
            return;
        }

        if (okButton.equals("check-houses"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.BOUGHT_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/bought-house");
            return;
        }

        if (okButton.equals("bought-house"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_TAXES.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/view-taxes");
            return;
        }

        if (okButton.equals("view-taxes"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.IMPROVEMENTS.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/improvements");
            return;
        }

        if (okButton.equals("improvements"))
        {
            // TODO handle the entered improvements, create measure records
            data.getPlayerRound().setPlayerState(PlayerState.ANSWER_SURVEY.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/answer-survey");
            return;
        }

        if (okButton.equals("improvements"))
        {
            // TODO handle the entered survey answers, create answer records
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_DAMAGE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/view-damage");
            return;
        }

        if (okButton.equals("view-damage"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.SUMMARY.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/summary");
            return;
        }

        if (okButton.equals("summary"))
        {
            // advance to next round
            int roundNr = data.getPlayerRoundNumber();
            if (data.getGroupRoundList().size() <= roundNr + 1)
            {
                data.setError("jsp = 'summary' -> 'new-round', but group round is too low: " + data.getGroupRoundNumber());
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

        response.sendRedirect("/housinggame-player/login");
    }

}
