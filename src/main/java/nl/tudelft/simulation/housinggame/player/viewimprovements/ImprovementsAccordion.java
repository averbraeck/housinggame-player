package nl.tudelft.simulation.housinggame.player.viewimprovements;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasureRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasuretypeRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.WelfaretypeRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

/**
 * ImprovementsAccordion.java.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ImprovementsAccordion
{

    public static void makeImprovementsAccordion(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<MeasuretypeRecord> measureTypeList = dslContext.selectFrom(Tables.MEASURETYPE)
                .where(Tables.MEASURETYPE.GAMEVERSION_ID.eq(data.getGameVersion().getId())).fetch();
        List<MeasureRecord> measureList = dslContext.selectFrom(Tables.MEASURE)
                .where(Tables.MEASURE.HOUSEGROUP_ID.eq(data.getPlayerRound().getFinalHousegroupId())).fetch();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("<p><b>Please select your improvements:</b></p>\n");
        s.append("<p>Your spendable income is: " + data.k(data.getPlayerRound().getSpendableIncome()) + "</p>\n");
        s.append("<form id=\"improvements-form\">\n");
        for (MeasuretypeRecord measureType : measureTypeList)
        {
            boolean bought = false;
            for (MeasureRecord measure : measureList)
            {
                if (measure.getMeasuretypeId().equals(measureType.getId()))
                    bought = true;
            }
            String readonly = bought ? "readonly" : "";
            String checked = bought ? "checked" : "";
            s.append("  <div class=\"checkbox pmd-default-theme\">\n");
            s.append("    <label class=\"pmd-checkbox pmd-checkbox-ripple-effect\">\n");
            s.append("      <input type=\"checkbox\" name=\"measure-" + measureType.getId() + "\" id=\"measure-"
                    + measureType.getId() + "\" value=\"" + measureType.getId() + "\" " + checked + " " + readonly + " />\n");
            s.append("      <span>" + measureType.getShortAlias() + ", costs: " + data.k(measureType.getPrice())
                    + ", satisfaction: " + measureType.getSatisfactionDelta() + "</span>\n");
            s.append("    </label>\n");
            s.append("  </div>\n");
        }
        s.append("<br/><p>Please select if you want to buy extra satisfaction:</p>\n");
        WelfaretypeRecord wft = SqlUtils.readRecordFromId(data, Tables.WELFARETYPE, data.getPlayer().getWelfaretypeId());
        s.append("  <div class=\"form-group\">\n");
        s.append("    <label for=\"regular1\" class=\"control-label\">");
        s.append("Costs per satisfaction point : " + data.k(wft.getSatisfactionCostPerPoint()));
        s.append("</label>\n");
        s.append("     <select class=\"form-control\" id=\"selected-points\">\n");
        s.append("       <option value=\"0\">no extra satisfaction points</option>\n");
        for (int i = 1; i <= 10; i++)
            s.append("       <option value=\"" + i + "\">" + i + "x - cost = " + data.k(i * wft.getSatisfactionCostPerPoint())
                    + "</option>\n");
        s.append("     </select>\n");
        s.append("  </div>\n");
        s.append("</form>\n");
        s.append("            </div>\n");
        data.getContentHtml().put("house/improvements", s.toString());
    }

    public static void makeBoughtImprovementsAccordion(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<MeasureRecord> measureList = dslContext.selectFrom(Tables.MEASURE)
                .where(Tables.MEASURE.HOUSEGROUP_ID.eq(data.getPlayerRound().getFinalHousegroupId())).fetch();
        int satBought = data.getPlayerRound().getSatisfactionBought();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        if (satBought == 0 && measureList.size() == 0)
            s.append("<p>You did not buy any improvements in this round</p>\n");
        else
        {
            int totCost = 0;
            int totSat = 0;
            if (measureList.size() > 0)
            {
                s.append("<p>You bought the following improvements:<br/>\n");
                for (var measure : measureList)
                {
                    if (measure.getRoundNumber().equals(data.getPlayerRoundNumber()))
                    {
                        var measureType = SqlUtils.readRecordFromId(data, Tables.MEASURETYPE, measure.getMeasuretypeId());
                        s.append(
                                " - " + measureType.getShortAlias() + ", costs: " + data.k(measureType.getPrice()) +
                                ", satisfaction: " + measureType.getSatisfactionDelta() + "<br/>\n");
                        totCost += measureType.getPrice();
                        totSat += measureType.getSatisfactionDelta();
                    }
                }
                s.append("</p>\n");
            }
            if (satBought > 0)
            {
                s.append("<p>You bought extra satisfaction:<br/>\n");
                s.append(" - nr of points: " + satBought + ", costs: "
                        + data.k(data.getPlayerRound().getCostSatisfactionBought()) + "</p>\n");
                totCost += data.getPlayerRound().getCostSatisfactionBought();
                totSat += satBought;
            }
            s.append("<p>Total spend on improvements: " + data.k(totCost) + "<br/>\n");
            s.append("Total satisfaction delta: " + totSat + "</p>\n");

        }
        s.append("            </div>\n");
        data.getContentHtml().put("panel/improvements", s.toString());
    }

}
