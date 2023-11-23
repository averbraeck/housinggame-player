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

        System.out.println("checkOkButton for player " + data.getPlayerCode() + ", jsp = " + jsp);

        if (jsp.equals("welcome-wait"))
        {
            if (!data.getPlayerRound().getPlayerState().equals(PlayerState.LOGIN.toString()))
            {
                data.setError("jsp = 'welcome-wait', but player state is '" + data.getPlayerRound().getPlayerState() + "'");
                return false;
            }
            if (data.getGroupRound() == null)
            {
                data.setError("jsp = 'welcome-wait', but GroupRound has not yet been created");
                return false;
            }
            if (RoundState.lt(data.getGroupRound().getRoundState(), RoundState.NEW_ROUND.toString()))
            {
                data.setError("jsp = 'welcome-wait', but group state is '" + data.getGroupRound().getRoundState() + "'");
                return false;
            }
            if (data.getGroupRoundNumber() == 0)
            {
                data.setError("jsp = 'welcome-wait', but GroupRoundNumber = 0");
                return false;
            }
            if (data.getGroupRoundNumber() > 1)
            {
                data.setError("jsp = 'welcome-wait', but GroupRoundNumber > 1");
                return false;
            }
            data.getPlayerRound().setPlayerState(PlayerState.READ_BUDGET.toString());
            data.getPlayerRound().store();
            return true;
        }

        // are we in the same round as the group?
        if (data.getPlayerRoundNumber() != data.getGroupRoundNumber())
        {
            data.setError("data.getPlayerRoundNumber(" + data.getPlayerRoundNumber() + ") != data.getGroupRoundNumber("
                    + data.getGroupRoundNumber() + ")");
            return false;
        }

        return false;
    }

    public static void redirect(final PlayerData data, final HttpServletResponse response) throws IOException, ServletException
    {
        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
        if (playerState == null)
            throw new IllegalArgumentException("Unexpected value for PlayerState: " + data.getPlayerRound().getPlayerState());
        switch (playerState)
        {
            case LOGIN:
                if (data.getPlayerRoundNumber() != 0)
                {
                    data.setError("Player " + data.getPlayerCode() + " is in LOGIN state, but not in round 0");
                    response.sendRedirect("/housinggame-player/error");
                }
                else
                    response.sendRedirect("/housinggame-player/welcome-wait");
                break;

            case READ_BUDGET:
                response.sendRedirect("/housinggame-player/read-budget");
                break;

            case READ_NEWS:
                response.sendRedirect("/housinggame-player/read-news");
                break;

            case CHECK_HOUSES:
                response.sendRedirect("/housinggame-player/check-house");

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
