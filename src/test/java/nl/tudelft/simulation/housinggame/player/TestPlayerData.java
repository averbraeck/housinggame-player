package nl.tudelft.simulation.housinggame.player;

/**
 * TestPlayerData.java.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestPlayerData
{

    public static void main(final String[] args)
    {
        PlayerData data = new PlayerData();
        report(data, 12000);
        report(data, 120);
        report(data, 12345);
        report(data, 120000);
        report(data, 123456);
        report(data, -12000);
        report(data, -120);
        report(data, -12345);
        report(data, -120000);
        report(data, -123456);
    }

    protected static void report(final PlayerData data, final int n)
    {
        System.out.println(n + "   " + data.k(n) + "   " + data.kdig(n));
    }
}
