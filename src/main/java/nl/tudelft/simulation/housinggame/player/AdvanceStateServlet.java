package nl.tudelft.simulation.housinggame.player;

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

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        // the ok button indicates the INTENTION of the player, not the screen it originates from.
        String nextScreen = "";
        if (request.getParameter("okButton") != null)
            nextScreen = request.getParameter("okButton");
        if (request.getParameter("nextScreen") != null)
            nextScreen = request.getParameter("nextScreen");

        System.out.println("advance-state called with value " + nextScreen);

        // player clicked STAY on the sell-house screen
        if (nextScreen.equals("stay"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.STAY_HOUSE_WAIT.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/stay-house-wait");
            return;
        }

        // player decided to sell the house with SELL HOUSE and has entered price and reason for moving
        if (nextScreen.equals("sell-house"))
        {
            // TODO handle the entered sell-house data
            data.getPlayerRound().setPlayerState(PlayerState.SELL_HOUSE_WAIT.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/sell-house-wait");
            return;
        }

        // player clicked ENJOY STAY on the stay-house-wait screen
        if (nextScreen.equals("enjoy-stay"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.STAYED_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/sell-house-stay");
            return;
        }

        // player clicked REJECT STAY on the stay-house-wait screen
        if (nextScreen.equals("reject-stay"))
        {
            // TODO undo the entered stay-house data
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_SELL_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/sell-house");
            return;
        }

        // player clicked MOVE OUT on the sell-house-wait screen
        if (nextScreen.equals("move-out"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_BUY_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/buy-house");
            return;
        }

        // player clicked REJECT SELL on the sell-house-wait screen
        if (nextScreen.equals("reject-sell"))
        {
            // TODO undo the entered sell-house data
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_SELL_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/sell-house");
            return;
        }

        // the player clicked the NEW ROUND button in the view-summary screen
        if (nextScreen.equals("new-round"))
        {
            // advance to next round (if allowed)
            int roundNr = data.getPlayerRoundNumber();
            if (roundNr >= data.getScenario().getHighestRoundNumber())
                return;
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

        System.err.println("Did not recognize nextScreen value " + nextScreen);

        response.sendRedirect("/housinggame-player/login");
    }

}
