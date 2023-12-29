package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.RoundState;

/**
 * ValidStates checks whether the combination of PlayerState and GroupState is valid, and whether the nextScreen button can be
 * activated.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class ValidStates
{

    /** */
    private ValidStates()
    {
        // static class
    }

    /**
     * Check if the player is in a valid state compared to the group, and redirect to error page (and return false) if this is
     * not the case.
     * @param data PlayerData; data on the player
     * @param response HttpServletResponse; to be used for redirect to error page
     * @return boolean; whether the player state is valid or not
     * @throws IOException on redirect error
     * @throws ServletException on redirect error
     */
    public static boolean isValidState(final PlayerData data, final HttpServletResponse response)
            throws IOException, ServletException
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
            return false;
        }

        if (roundState == null)
        {
            data.setError("GroupRound for player " + data.getPlayerCode() + " is in illegal state: "
                    + data.getGroupRound().getRoundState() + "<br>Log out and log in again when problem has been corrected.");
            response.sendRedirect("/housinggame-player/error");
            return false;
        }

        if (data.getPlayerRoundNumber() > data.getGroupRoundNumber())
        {
            data.setError("Player " + data.getPlayerCode() + " is in Round " + data.getPlayerRoundNumber()
                    + ", but group is only in Round " + data.getGroupRoundNumber()
                    + "<br>Log out and log in again when group has advanced to the same round.");
            response.sendRedirect("/housinggame-player/error");
            return false;
        }

        // if the player is in the previous round, but beyond buying a house, the player can still continue
        if (data.getGroupRoundNumber() - data.getPlayerRoundNumber() == 1 && playerState.ge(PlayerState.BOUGHT_HOUSE))
            return true;
        if (data.getGroupRoundNumber() - data.getPlayerRoundNumber() == 1 && playerState.lt(PlayerState.BOUGHT_HOUSE))
        {
            data.setError("Player " + data.getPlayerCode() + " is one round behind the group, and too far behind to catch up.");
            response.sendRedirect("/housinggame-player/error");
            return false;
        }
        if (data.getGroupRoundNumber() - data.getPlayerRoundNumber() > 1)
        {
            data.setError("Player " + data.getPlayerCode()
                    + " is more than one round behind the group, and too far behind to catch up.");
            response.sendRedirect("/housinggame-player/error");
            return false;
        }

        // check if the player is in the same round and in a valid combination
        if (data.getPlayerRoundNumber() == data.getGroupRoundNumber())
        {
            if (roundState.nr > playerState.nr)
                return true;
            if (playerState.eq(PlayerState.BOUGHT_HOUSE) && roundState.ge(RoundState.ALLOW_BUYING))
                return true;
            if (playerState.eq(PlayerState.STAYED_HOUSE) && roundState.ge(RoundState.ALLOW_BUYING))
                return true;
            if (playerState.eq(PlayerState.SURVEY_COMPLETED) && roundState.ge(RoundState.SHOW_SURVEY))
                return true;
        }

        data.setError("Player " + data.getPlayerCode() + " is in State " + playerState + ", but group is only in State "
                + roundState + "<br>Log out and log in again when group has advanced to the same state.");
        response.sendRedirect("/housinggame-player/error");
        return false;
    }


}
