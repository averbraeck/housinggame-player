package nl.tudelft.simulation.housinggame.player.viewdamage;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.CumulativeNewsEffects;
import nl.tudelft.simulation.housinggame.common.FluvialPluvial;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HouseRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

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
            // TODO: Make sure that data in database is calculated correctly
            // int pCommProt = prr.getPluvialBaseProtection() + prr.getPluvialCommunityDelta();
            // int fCommProt = prr.getFluvialBaseProtection() + prr.getFluvialCommunityDelta();
            // int pHouseProt = pCommProt + prr.getPluvialHouseDelta();
            // int fHouseProt = fCommProt + prr.getFluvialHouseDelta();

            // replacement code https://github.com/averbraeck/housinggame-player/issues/45
            var houseGroup = data.getHouseGroup();
            HouseRecord house = PlayerUtils.readRecordFromId(data, Tables.HOUSE, houseGroup.getHouseId());
            var cumulativeNewsEffects = CumulativeNewsEffects.readCumulativeNewsEffects(data.getDataSource(),
                    data.getScenario(), data.getPlayerRoundNumber());
            int fCommBaseProt = houseGroup.getFluvialBaseProtection();
            int pCommBaseProt = houseGroup.getPluvialBaseProtection();
            int fCommDelta = cumulativeNewsEffects.get(house.getCommunityId()).getFluvialProtectionDelta();
            int pCommDelta = cumulativeNewsEffects.get(house.getCommunityId()).getPluvialProtectionDelta();
            var fpRecord = FluvialPluvial.measureProtectionTillRound(data, data.getPlayerRoundNumber(), houseGroup);
            int fHouseDelta = fpRecord.fluvial();
            int pHouseDelta = fpRecord.pluvial();
            int fCommProt = fCommBaseProt + fCommDelta;
            int pCommProt = pCommBaseProt + pCommDelta;
            int fHouseProt = fCommProt + fHouseDelta;
            int pHouseProt = pCommProt + pHouseDelta;
            // end replacement code https://github.com/averbraeck/housinggame-player/issues/45

            int pDice = grr.getPluvialFloodIntensity();
            int fDice = grr.getFluvialFloodIntensity();

            // @formatter:off

            // River flood info

            s.append("            <div class=\"hg-header1\">River flood info</div>\n");
            s.append("            <div class=\"hg-box-grey\">\n");
            s.append("              <b>Flood level: " + fDice + "</b><br/>\n");
            s.append("              <b>Total protection: " + fHouseProt + "</b><br/>\n");
            s.append("              - Community protection: " + fCommProt + "<br/>\n");
            s.append("              - House measures: +" + fHouseDelta + "<br/>\n");
            s.append("              <br/>\n");
            s.append("              <b>Damage and penalties</b><br/>\n");
            String fnf = (fHouseProt - fDice) > 0 ? " (No flood)" : " (Flood)";
            s.append("              Total protection - flood level: " + (fHouseProt - fDice) + fnf + "<br/>\n");
            s.append("              Community damage: " + Math.max(0, fDice - fCommProt) + "<br/>\n");
            s.append("              House damage: " + Math.max(0, fDice - fHouseProt) + "<br/>\n");
            s.append("              Costs for damage repairs: " + data.k(prr.getCostFluvialDamage()) + "<br/>\n");
            s.append("              Satisfaction penalty: " + prr.getSatisfactionFluvialPenalty() + "<br/>\n");
            s.append("            </div>\n");

            // Rain flood info

            s.append("            <div class=\"hg-header1\">Rain flood info</div>\n");
            s.append("            <div class=\"hg-box-grey\">\n");
            s.append("              <b>Flood level: " + pDice + "</b><br/>\n");
            s.append("              <b>Total protection: " + pHouseProt + "</b><br/>\n");
            s.append("              - Community protection: " + pCommProt + "<br/>\n");
            s.append("              - House measures: +" + pHouseDelta + "<br/>\n");
            s.append("              <br/>\n");
            s.append("              <b>Damage and penalties</b><br/>\n");
            fnf = (pHouseProt - pDice) > 0 ? " (No flood)" : " (Flood)";
            s.append("              Total protection - flood level: " + (pHouseProt - pDice) + fnf + "<br/>\n");
            s.append("              Community damage: " + Math.max(0, pDice - pCommProt) + "<br/>\n");
            s.append("              House damage: " + Math.max(0, fDice - pHouseProt) + "<br/>\n");
            s.append("              Costs for damage repairs: " + data.k(prr.getCostPluvialDamage()) + "<br/>\n");
            s.append("              Satisfaction penalty: " + prr.getSatisfactionPluvialPenalty() + "<br/>\n");
            s.append("            </div>\n");

            // Flood history

            s.append("            <div class=\"hg-header1\">Flood history</div>\n");
            s.append("            <table class=\"hg-table\">\n");
            s.append("              <thead>\n");
            s.append("                <tr>\n");
            s.append("                  <th>Round</th>\n");
            s.append("                  <th>Owned</th>\n");
            s.append("                  <th colspan=\"2\">Flood<br/>level</th>\n");
            s.append("                  <th colspan=\"2\">Community<br/>protection</th>\n");
            s.append("                  <th colspan=\"2\">House<br/>protection</th>\n");
            s.append("                </tr>\n");
            s.append("                <tr>\n");
            s.append("                  <th>Nr.</th>\n");
            s.append("                  <th>House</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                  <th>Rain</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                  <th>Rain</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                  <th>Rain</th>\n");
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
                    s.append("<td>" + g.getFluvialFloodIntensity() + "</td>\n");
                    s.append("<td>" + g.getPluvialFloodIntensity() + "</td>\n");
                    s.append("<td>--</td><td>--</td><td>--</td><td>--</td>\n");
                }
                else
                {
                    var rHouseGroup = PlayerUtils.readRecordFromId(data, Tables.HOUSEGROUP, p.getFinalHousegroupId());
                    s.append("<td>" + rHouseGroup.getCode() + "</td>\n");
                    s.append("<td>" + g.getFluvialFloodIntensity() + "</td>\n");
                    s.append("<td>" + g.getPluvialFloodIntensity() + "</td>\n");

                    HouseRecord rHouse = PlayerUtils.readRecordFromId(data, Tables.HOUSE, rHouseGroup.getHouseId());
                    var rCumulativeNewsEffects = CumulativeNewsEffects.readCumulativeNewsEffects(data.getDataSource(),
                            data.getScenario(), round);
                    int rfCommBaseProt = rHouseGroup.getFluvialBaseProtection();
                    int rpCommBaseProt = rHouseGroup.getPluvialBaseProtection();
                    int rfCommDelta = rCumulativeNewsEffects.get(rHouse.getCommunityId()).getFluvialProtectionDelta();
                    int rpCommDelta = rCumulativeNewsEffects.get(rHouse.getCommunityId()).getPluvialProtectionDelta();
                    var rfpRecord = FluvialPluvial.measureProtectionTillRound(data, round, rHouseGroup);
                    int rfHouseDelta = rfpRecord.fluvial();
                    int rpHouseDelta = rfpRecord.pluvial();
                    int rfCommProt = rfCommBaseProt + rfCommDelta;
                    int rpCommProt = rpCommBaseProt + rpCommDelta;

                    s.append("<td>" + rfCommProt + "</td>\n");
                    s.append("<td>" + rpCommProt + "</td>\n");
                    s.append("<td>+" + rfHouseDelta + "</td>\n");
                    s.append("<td>+" + rpHouseDelta + "</td>\n");
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
            s.append("                  <th colspan=\"2\">Satisfaction penalty</th>\n");
            s.append("                  <th colspan=\"2\">Repair costs</th>\n");
            s.append("                </tr>\n");
            s.append("                <tr>\n");
            s.append("                  <th>Nr.</th>\n");
            s.append("                  <th>House</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                  <th>Rain</th>\n");
            s.append("                  <th>River</th>\n");
            s.append("                  <th>Rain</th>\n");
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
                    var h = PlayerUtils.readRecordFromId(data, Tables.HOUSEGROUP, p.getFinalHousegroupId());
                    s.append("<td>" + h.getCode() + "</td>\n");
                    s.append("<td>" + p.getSatisfactionFluvialPenalty() + "</td>\n");
                    s.append("<td>" + p.getSatisfactionPluvialPenalty() + "</td>\n");
                    s.append("<td>" + data.k(p.getCostFluvialDamage()) + "</td>\n");
                    s.append("<td>" + data.k(p.getCostPluvialDamage()) + "</td>\n");
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
