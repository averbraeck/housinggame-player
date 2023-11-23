package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.RoundState;

/**
 * PlayerStateUtils.java.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class PlayerStateUtils
{

    /** */
    private PlayerStateUtils()
    {
        // utility class
    }

    public static boolean checkOkButton(final PlayerData data, final String jsp)
    {
        data.setError("");
        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
        RoundState roundState = RoundState.valueOf(data.getGroupRound().getRoundState());

        System.out.println("checkOkButton for player " + data.getPlayerCode() + ", jsp = " + jsp);

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
            if (!playerState.equals(PlayerState.LOGIN) || data.getPlayerRoundNumber() != 0)
            {
                data.setError("jsp = 'welcome-wait', but player round is " + data.getPlayerRoundNumber()
                        + ", and player state is '" + playerState + "'");
                return false;
            }
            if (data.getGroupRound() == null)
            {
                data.setError("jsp = 'welcome-wait', but GroupRound has not yet been created");
                return false;
            }
            if (data.getGroupRound() == null)
            {
                data.setError("jsp = 'welcome-wait', but GroupRound has not yet been created");
                return false;
            }
            if (roundState.nr < RoundState.NEW_ROUND.nr)
            {
                data.setError("jsp = 'welcome-wait', but GroupRound state is " + roundState);
                return false;
            }

            // advance player to round 1 and state to READ_BUDGET
            data.getPlayerRound().setPlayerState(PlayerState.READ_BUDGET.toString());
            data.getPlayerRound().store();
            return true;
        }

        // for all other states, the same 'ok' rule holds
        if (data.getGroupRoundNumber() > data.getPlayerRoundNumber())
            return true;
        if (roundState.nr > playerState.nr)
            return true;
        // summary screen in last round: OK should not be true there
        if (data.getPlayerRoundNumber() == data.getScenario().getHighestRoundNumber().intValue() && jsp.equals("summary"))
            return false;

        return false;
    }

    public static void redirect(final PlayerData data, final HttpServletResponse response) throws IOException, ServletException
    {
        data.setError("");
        data.readDynamicData();
        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
        RoundState roundState = RoundState.valueOf(data.getGroupRound().getRoundState());
        if (playerState == null)
        {
            data.setError("Player " + data.getPlayerCode() + " is in illegal state: " + data.getPlayerRound().getPlayerState()
                    + "<br>Log out and log in again when problem has been corrected.");
            response.sendRedirect("/housinggame-player/error");
            return;
        }
        if (data.getPlayerRoundNumber() > data.getGroupRoundNumber())
        {
            data.setError("Player " + data.getPlayerCode() + " is in Round " + data.getPlayerRoundNumber()
                    + ", but group is only in Round " + data.getGroupRoundNumber()
                    + "<br>Log out and log in again when group has advanced to the same round.");
            response.sendRedirect("/housinggame-player/error");
            return;
        }
        if (data.getPlayerRoundNumber() == data.getGroupRoundNumber() && playerState.nr > roundState.nr)
        {
            data.setError("Player " + data.getPlayerCode() + " is in State " + playerState + ", but group is only in State "
                    + roundState + "<br>Log out and log in again when group has advanced to the same state.");
            response.sendRedirect("/housinggame-player/error");
            return;
        }

        switch (playerState)
        {
            case LOGIN:
                response.sendRedirect("/housinggame-player/welcome-wait");
                break;

            case READ_BUDGET:
                response.sendRedirect("/housinggame-player/read-budget");
                break;

            case READ_NEWS:
                response.sendRedirect("/housinggame-player/read-news");
                break;

            case CHECK_HOUSES:
                response.sendRedirect("/housinggame-player/check-houses");
                break;

            case BOUGHT_HOUSE:
                response.sendRedirect("/housinggame-player/bought-house");
                break;

            case VIEW_TAXES:
                response.sendRedirect("/housinggame-player/view-taxes");
                break;

            case IMPROVEMENTS:
                response.sendRedirect("/housinggame-player/improvements");
                break;

            case ANSWER_SURVEY:
                response.sendRedirect("/housinggame-player/answer-survey");
                break;

            case VIEW_DAMAGE:
                response.sendRedirect("/housinggame-player/view-damage");
                break;

            case SUMMARY:
                response.sendRedirect("/housinggame-player/summary");
                break;

            default:
                throw new IllegalArgumentException(
                        "Unexpected value for PlayerState: " + data.getPlayerRound().getPlayerState());
        }

    }

}
