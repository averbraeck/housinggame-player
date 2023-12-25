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

        // the ok button indicates the INTENTION of the player, not the screen it originates from.
        String nextScreen = "";
        if (request.getParameter("okButton") != null)
            nextScreen = request.getParameter("okButton");
        if (request.getParameter("nextScreen") != null)
            nextScreen = request.getParameter("nextScreen");

        System.out.println("advance-state called with value " + nextScreen);

        // player clicked START GAME on welcome-wait screen to advance to read-budget
        if (nextScreen.equals("start-game"))
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

        // player decided which house to buy with BUY HOUSE and has entered the price on the buy-house screen
        if (nextScreen.equals("buy-house-wait"))
        {
            // handle the entered buy-house data: Parameter house[e.g., N07], Parameter price[e.g., 105]
            String house = request.getParameter("house");
            String price = request.getParameter("price");
            if (!SqlUtils.makeHouseTransaction(data, house, price))
            {
                response.sendRedirect("/housinggame-player/buy-house");
                return;
            }
            data.getPlayerRound().setPlayerState(PlayerState.BUY_HOUSE_WAIT.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/buy-house-wait");
            return;
        }

        // player clicked MOVE IN on the buy-house-wait screen
        if (nextScreen.equals("move-in"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.BOUGHT_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/bought-house");
            return;
        }

        // player clicked REJECT BUY on the buy-house-wait screen
        if (nextScreen.equals("reject-buy"))
        {
            // TODO undo the entered buy-house data
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_BUY_HOUSE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/buy-house");
            return;
        }

        // the player clicked the VIEW IMPROVEMENTS button in the view-taxes screen
        if (nextScreen.equals("view-improvements"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_IMPROVEMENTS.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/view-improvements");
            return;
        }

        // the player clicked the ANSWER SURVEY button in the buy-improvements screen
        if (nextScreen.equals("answer-survey"))
        {
            // TODO handle the entered improvements, create measure records
            data.getPlayerRound().setPlayerState(PlayerState.ANSWER_SURVEY.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/answer-survey");
            return;
        }

        // the player clicked the WAIT FOR THE DICE button in the answer-survey screen
        if (nextScreen.equals("wait-for-dice"))
        {
            // TODO handle the entered survey answers, create answer records
            data.getPlayerRound().setPlayerState(PlayerState.SURVEY_COMPLETED.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/survey-completed");
            return;
        }

        // the player clicked the VIEW DAMAGE button in the survey-completed screen
        if (nextScreen.equals("view-damage"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_DAMAGE.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/view-damage");
            return;
        }

        // the player clicked the VIEW SUMMARY button in the view-damage screen
        if (nextScreen.equals("view-summary"))
        {
            data.getPlayerRound().setPlayerState(PlayerState.VIEW_SUMMARY.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/view-summary");
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
