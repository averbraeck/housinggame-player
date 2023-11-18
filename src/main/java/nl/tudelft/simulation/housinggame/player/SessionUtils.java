package nl.tudelft.simulation.housinggame.player;

import javax.servlet.http.HttpSession;

public final class SessionUtils
{

    private SessionUtils()
    {
        // utility class
    }

    public static PlayerData getData(final HttpSession session)
    {
        PlayerData data = (PlayerData) session.getAttribute("playerData");
        return data;
    }
}
