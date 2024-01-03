package nl.tudelft.simulation.housinggame.player.viewdamage;

import nl.tudelft.simulation.housinggame.player.PlayerData;

/**
 * DamageAccordion.java.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DamageAccordion
{

    public static void makeDamageAccordion(final PlayerData data)
    {
        StringBuilder s = new StringBuilder();
        // @formatter:off
        s.append("            <div class=\"hg-header1\">Pluvial damage</div>\n");
        s.append("            <div class=\"hg-box-grey\">\n");
        s.append("              xxx <br/>\n");
        s.append("            </div>\n");

        s.append("            <div class=\"hg-header1\">Fluvial damage</div>\n");
        s.append("            <div class=\"hg-box-grey\">\n");
        s.append("              xxx <br/>\n");
        s.append("            </div>\n");

        // @formatter:on
        data.getContentHtml().put("damage/content", s.toString());
    }

}
