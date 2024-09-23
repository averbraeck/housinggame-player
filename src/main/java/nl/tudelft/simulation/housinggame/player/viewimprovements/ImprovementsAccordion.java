package nl.tudelft.simulation.housinggame.player.viewimprovements;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.MeasureTypeList;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousemeasureRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasurecategoryRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasuretypeRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.WelfaretypeRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

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
        List<MeasureTypeList> measureTypeList = MeasureTypeList.getMeasureListRecords(data, data.getScenario());
        List<HousemeasureRecord> measureList = dslContext.selectFrom(Tables.HOUSEMEASURE)
                .where(Tables.HOUSEMEASURE.HOUSEGROUP_ID.eq(data.getPlayerRound().getFinalHousegroupId())).fetch();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("<p><b>Please select your improvements:</b></p>\n");
        s.append("<p>Your spendable income is: " + data.k(data.getPlayerRound().getSpendableIncome()) + "</p>\n");
        s.append("<form id=\"improvements-form\">\n");
        for (MeasureTypeList mtl : measureTypeList)
        {
            MeasurecategoryRecord measureCategory = mtl.measureCategory();
            s.append(" <p><b>" + measureCategory.getName() + "</b>\n");
            s.append(" <i>" + measureCategory.getDescription() + "</i>\n");
            for (MeasuretypeRecord measureType : mtl.measureTypeList())
            {
                boolean bought = false;
                for (HousemeasureRecord measure : measureList)
                {
                    if (measure.getMeasuretypeId().equals(measureType.getId()))
                        bought = true;
                }
                s.append("  <div class=\"checkbox pmd-default-theme\">\n");
                s.append("    <label class=\"pmd-checkbox pmd-checkbox-ripple-effect\">\n");
                if (bought)
                {
                    // https://stackoverflow.com/questions/155291/can-html-checkboxes-be-set-to-readonly
                    s.append("      <input type=\"hidden\" name=\"measure-" + measureType.getId() + "\" id=\"measure-"
                            + measureType.getId() + "\" value=\"" + measureType.getId() + "\" />\n");
                    s.append("      <input type=\"checkbox\" name=\"measure-" + measureType.getId() + "_dummy\" id=\"measure-"
                            + measureType.getId() + "_dummy\" value=\"" + measureType.getId()
                            + "\" checked=\"checked\" disabled=\"disabled\" />\n");
                }
                else
                {
                    s.append("      <input type=\"checkbox\" name=\"measure-" + measureType.getId() + "\" id=\"measure-"
                            + measureType.getId() + "\" value=\"" + measureType.getId() + "\" />\n");
                }
                s.append("      <span>" + measureType.getShortAlias() + ", costs: " + data.k(data.getMeasurePrice(measureType))
                        + ", satisfaction: " + data.getSatisfactionDeltaIfBought(measureType) + "</span>\n");
                s.append("    </label>\n");
                s.append("  </div>\n");
            }
            s.append(" </p>\n");
        }
        s.append("<br/><p>Please select if you want to buy extra satisfaction:</p>\n");
        WelfaretypeRecord wft = PlayerUtils.readRecordFromId(data, Tables.WELFARETYPE, data.getPlayer().getWelfaretypeId());
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
        List<HousemeasureRecord> measureList = dslContext.selectFrom(Tables.HOUSEMEASURE)
                .where(Tables.HOUSEMEASURE.HOUSEGROUP_ID.eq(data.getPlayerRound().getFinalHousegroupId())).fetch();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        if (measureList.size() == 0)
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
                    if (measure.getBoughtInRound().equals(data.getPlayerRoundNumber()))
                    {
                        var measureType = PlayerUtils.readRecordFromId(data, Tables.MEASURETYPE, measure.getMeasuretypeId());
                        s.append(" - " + measureType.getShortAlias() + ", costs: " + data.k(data.getMeasurePrice(measureType))
                                + ", satisfaction: " + data.getSatisfactionDeltaIfBought(measureType) + "<br/>\n");
                        totCost += data.getMeasurePrice(measureType);
                        totSat += data.getSatisfactionDeltaIfBought(measureType);
                    }
                }
                s.append("</p>\n");
            }
            s.append("<p>Total spend on improvements: " + data.k(totCost) + "<br/>\n");
            s.append("Total satisfaction delta: " + totSat + "</p>\n");

        }
        s.append("            </div>\n");
        data.getContentHtml().put("panel/improvements", s.toString());
    }

}
