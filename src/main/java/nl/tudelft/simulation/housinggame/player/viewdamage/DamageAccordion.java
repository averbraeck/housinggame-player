package nl.tudelft.simulation.housinggame.player.viewdamage;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

/**
 * DamageAccordion.java.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DamageAccordion
{

    public static void makeDamageAccordion(final PlayerData data)
    {
        StringBuilder s = new StringBuilder();
        if (data.getPlayerRound().getFinalHousegroupId() == null)
        {
            s.append("            <div class=\"hg-header1\">No house</div>\n");
            s.append("            <div class=\"hg-box-grey\">\n");
            s.append("              Since you do not own a house, and you are not part of a community, there is no damage.\n");
            s.append("            </div>\n");
        }
        else
        {
            DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
            var prr = data.getPlayerRound();
            var grr = data.getGroupRound();
            int pCommProt = prr.getPluvialBaseProtection() + prr.getPluvialCommunityDelta();
            int fCommProt = prr.getFluvialBaseProtection() + prr.getFluvialCommunityDelta();
            int pHouseProt = pCommProt + prr.getPluvialHouseDelta();
            int fHouseProt = fCommProt + prr.getFluvialHouseDelta();
            int pDice = grr.getPluvialFloodIntensity();
            int fDice = grr.getFluvialFloodIntensity();

            // @formatter:off

            // Rain flood info

            s.append("            <div class=\"hg-header1\">Rain flood info</div>\n");
            s.append("            <div class=\"hg-box-grey\">\n");
            s.append("              Community rain protection: " + pCommProt + "<br/>\n");
            s.append("              House rain protection: " + pHouseProt + "<br/>\n");
            s.append("              Rain flood intensity: " + pDice + "<br/>\n");
            s.append("              Community damage due to rain: " + Math.max(0, pDice - pCommProt) + "<br/>\n");
            s.append("              House damage due to rain: " + Math.max(0, pDice - pHouseProt) + "<br/>\n");
            s.append("            </div>\n");

            // River flood info

            s.append("            <div class=\"hg-header1\">River flood info</div>\n");
            s.append("            <div class=\"hg-box-grey\">\n");
            s.append("              Community river protection: " + fCommProt + "<br/>\n");
            s.append("              House river protection: " + fHouseProt + "<br/>\n");
            s.append("              River flood intensity: " + fDice + "<br/>\n");
            s.append("              Community damage due to river: " + Math.max(0, fDice - fCommProt) + "<br/>\n");
            s.append("              House damage due to river: " + Math.max(0, fDice - fHouseProt) + "<br/>\n");
            s.append("            </div>\n");

            // Costs and Penalties

            s.append("            <div class=\"hg-header1\">Costs and penalties</div>\n");
            s.append("            <div class=\"hg-box-grey\">\n");
            s.append("              Costs for rain damage repairs: " + prr.getCostPluvialDamage() + "<br/>\n");
            s.append("              Costs for river damage repairs: " + prr.getCostFluvialDamage() + "<br/>\n");
            s.append("              Satisfaction penalty: " +
                    (prr.getSatisfactionPluvialPenalty() + prr.getSatisfactionFluvialPenalty()) + "<br/>\n");
            s.append("            </div>\n");

            // Flood history

            s.append("            <div class=\"hg-header1\">Flood history</div>\n");
            s.append("            <table class=\"hg-table\">\n");
            s.append("              <thead>\n");
            s.append("                <tr>\n");
            s.append("                  <th>Round</th>\n");
            s.append("                  <th>Owned</th>\n");
            s.append("                  <th colspan=\"2\">Flood level</th>\n");
            s.append("                  <th colspan=\"2\">Comm. prot.</th>\n");
            s.append("                  <th colspan=\"2\">House prot.</th>\n");
            s.append("                </tr>\n");
            s.append("                <tr>\n");
            s.append("                  <th>Nr.</th>\n");
            s.append("                  <th>House</th>\n");
            s.append("                  <th>Rain</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                  <th>Rain</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                  <th>Rain</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                </tr>\n");
            s.append("              </thead>\n");
            s.append("              <tbody>\n");
            for (int round = 1; round <= data.getPlayerRoundNumber(); round++)
            {
                s.append("                <tr>\n");
                s.append("                  <td>" + round + "</td>\n");
                GrouproundRecord g = data.getGroupRoundList().get(round);
                PlayerroundRecord p = dslContext.selectFrom(Tables.PLAYERROUND)
                        .where(Tables.PLAYERROUND.GROUPROUND_ID.eq(g.getId())
                        .and(Tables.PLAYERROUND.PLAYER_ID.eq(prr.getPlayerId()))).fetchOne();
                if (p.getFinalHousegroupId() == null)
                {
                    s.append("<td>--</td>\n");
                    s.append("<td>" + g.getPluvialFloodIntensity() + "</td>\n");
                    s.append("<td>" + g.getFluvialFloodIntensity() + "</td>\n");
                    s.append("<td>--</td><td>--</td><td>--</td><td>--</td>\n");
                }
                else
                {
                    var h = SqlUtils.readRecordFromId(data, Tables.HOUSEGROUP, p.getFinalHousegroupId());
                    s.append("<td>" + h.getCode() + "</td>\n");
                    s.append("<td>" + g.getPluvialFloodIntensity() + "</td>\n");
                    s.append("<td>" + g.getFluvialFloodIntensity() + "</td>\n");
                    int pcp = p.getPluvialBaseProtection() + p.getPluvialCommunityDelta();
                    int fcp = p.getFluvialBaseProtection() + p.getFluvialCommunityDelta();
                    int php = pcp + p.getPluvialHouseDelta();
                    int fhp = pcp + p.getFluvialHouseDelta();
                    s.append("<td>" + pcp + "</td>\n");
                    s.append("<td>" + fcp + "</td>\n");
                    s.append("<td>" + php + "</td>\n");
                    s.append("<td>" + fhp + "</td>\n");
                }
                s.append("                </tr>\n");
            }
            s.append("              </tbody>\n");
            s.append("            </table>\n");

            // Damage history

            s.append("            <div class=\"hg-header1\">Damage history</div>\n");
            s.append("            <table class=\"hg-table\">\n");
            s.append("              <thead>\n");
            s.append("                <tr>\n");
            s.append("                  <th>Round</th>\n");
            s.append("                  <th>Owned</th>\n");
            s.append("                  <th colspan=\"2\">Sat. penalty</th>\n");
            s.append("                  <th colspan=\"2\">Repair costs</th>\n");
            s.append("                </tr>\n");
            s.append("                <tr>\n");
            s.append("                  <th>Nr.</th>\n");
            s.append("                  <th>House</th>\n");
            s.append("                  <th>Rain</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                  <th>Rain</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                </tr>\n");
            s.append("              </thead>\n");
            s.append("              <tbody>\n");
            for (int round = 1; round <= data.getPlayerRoundNumber(); round++)
            {
                s.append("                <tr>\n");
                s.append("                  <td>" + round + "</td>\n");
                GrouproundRecord g = data.getGroupRoundList().get(round);
                PlayerroundRecord p = dslContext.selectFrom(Tables.PLAYERROUND)
                        .where(Tables.PLAYERROUND.GROUPROUND_ID.eq(g.getId())
                        .and(Tables.PLAYERROUND.PLAYER_ID.eq(prr.getPlayerId()))).fetchOne();
                if (p.getFinalHousegroupId() == null)
                {
                    s.append("<td>--</td>\n");
                    s.append("<td>--</td><td>--</td><td>--</td><td>--</td>\n");
                }
                else
                {
                    var h = SqlUtils.readRecordFromId(data, Tables.HOUSEGROUP, p.getFinalHousegroupId());
                    s.append("<td>" + h.getCode() + "</td>\n");
                    s.append("<td>" + p.getSatisfactionPluvialPenalty() + "</td>\n");
                    s.append("<td>" + p.getSatisfactionFluvialPenalty() + "</td>\n");
                    s.append("<td>" + data.k(p.getCostPluvialDamage()) + "</td>\n");
                    s.append("<td>" + data.k(p.getCostFluvialDamage()) + "</td>\n");
                }
                s.append("                </tr>\n");
            }
            s.append("              </tbody>\n");
            s.append("            </table>\n");

            // @formatter:on
        }

        data.getContentHtml().put("panel/damage", s.toString());
    }

}
