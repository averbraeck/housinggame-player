package nl.tudelft.simulation.housinggame.player;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.GroupState;

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
     */
    public static boolean isValidState(final PlayerData data)
    {
        data.setError("");
        data.readDynamicData();
        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
        GroupState groupState = GroupState.valueOf(data.getGroupRound().getGroupState());

        if (playerState == null)
        {
            data.setError("Player " + data.getPlayerCode() + " is in illegal state: " + data.getPlayerRound().getPlayerState()
                    + "<br>Log out and log in again when problem has been corrected.");
            return false;
        }

        if (groupState == null)
        {
            data.setError("GroupRound for player " + data.getPlayerCode() + " is in illegal state: "
                    + data.getGroupRound().getGroupState() + "<br>Log out and log in again when problem has been corrected.");
            return false;
        }

        if (data.getPlayerRoundNumber() > data.getGroupRoundNumber())
        {
            data.setError("Player " + data.getPlayerCode() + " is in Round " + data.getPlayerRoundNumber()
                    + ", but group is only in Round " + data.getGroupRoundNumber()
                    + "<br>Log out and log in again when group has advanced to the same round.");
            return false;
        }

        // if the player is in the previous round, but beyond buying a house, the player can still continue
        if (data.getGroupRoundNumber() - data.getPlayerRoundNumber() == 1 && playerState.ge(PlayerState.BOUGHT_HOUSE))
            return true;
        if (data.getGroupRoundNumber() - data.getPlayerRoundNumber() == 1 && playerState.lt(PlayerState.BOUGHT_HOUSE))
        {
            data.setError("Player " + data.getPlayerCode() + " is one round behind the group, and too far behind to catch up.");
            return false;
        }
        if (data.getGroupRoundNumber() - data.getPlayerRoundNumber() > 1)
        {
            data.setError("Player " + data.getPlayerCode()
                    + " is more than one round behind the group, and too far behind to catch up.");
            return false;
        }

        // check if the player is in the same round and in a valid combination
        if (data.getPlayerRoundNumber() == data.getGroupRoundNumber())
        {
            if (groupState.nr > playerState.nr)
                return true;
            if (playerState.eq(PlayerState.BOUGHT_HOUSE) && groupState.ge(GroupState.ALLOW_BUYING))
                return true;
            if (playerState.eq(PlayerState.STAYED_HOUSE) && groupState.ge(GroupState.ALLOW_BUYING))
                return true;
            if (playerState.eq(PlayerState.SURVEY_COMPLETED) && groupState.ge(GroupState.SHOW_SURVEY))
                return true;
        }

        data.setError("Player " + data.getPlayerCode() + " is in State " + playerState + ", but group is only in State "
                + groupState + "<br>Log out and log in again when group has advanced to the same state.");
        return false;
    }

    public static boolean checkNextScreenButton(final PlayerData data, final String jsp)
    {
        if (!isValidState(data))
            return false;

        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
        GroupState groupState = GroupState.valueOf(data.getGroupRound().getGroupState());

        if (jsp.equals("welcome-wait"))
        {
            if (data.getGroupRound() == null)
            {
                data.setError("jsp = 'welcome-wait', but GroupRound has not yet been created");
                return false;
            }
            if (data.getGroupRoundNumber() <= 1 && groupState.lt(GroupState.NEW_ROUND))
            {
                data.setError("jsp = 'welcome-wait', but GroupRound state is " + groupState + ", groupround = "
                        + data.getGroupRoundNumber());
                return false;
            }
            return true;
        }

        // for all other states, the same 'ok' rule holds
        if (data.getGroupRoundNumber() > data.getPlayerRoundNumber())
            return true;
        if (groupState.nr > playerState.nr)
            return true;
        if (playerState.eq(PlayerState.BUY_HOUSE_WAIT) && groupState.ge(GroupState.ALLOW_BUYING))
            return true;
        if (playerState.eq(PlayerState.STAY_HOUSE_WAIT) && groupState.ge(GroupState.ALLOW_BUYING))
            return true;
        if (playerState.eq(PlayerState.ANSWER_SURVEY) && groupState.ge(GroupState.SHOW_SURVEY))
            return true;
        // summary screen in last round: OK should not be true there
        if (data.getPlayerRoundNumber() == data.getScenario().getHighestRoundNumber() && jsp.equals("summary"))
            return false;

        data.setError("jsp = " + jsp + ", groupState = " + groupState + ", playerState = " + playerState
                + "<br>This is an incompatible combination. Please ask the facilitator for help.");
        return false;
    }
}
