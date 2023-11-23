package nl.tudelft.simulation.housinggame.player;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.NewsitemRecord;

/**
 * ContentUtils.java.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ContentUtils
{

    /** */
    private ContentUtils()
    {
        // utility class
    }

    /*-
            <div style="display: flex; flex-direction: row; justify-content: space-between;">
              <div>
                Annual income: ${playerData.k(playerData.getPlayerRound().getIncomePerRound()) } <br/>
                Preferred house rating: ${playerData.getPlayerRound().getPreferredHouseRating() } <br/>
                Max mortgage: ${playerData.k(playerData.getPlayerRound().getMaximumMortgage()) } <br/>
                Current mortgage: ${playerData.k(playerData.getPlayerRound().getMortgage()) } <br/>
              </div>
              <div>
                Annual living costs: ${playerData.k(playerData.getPlayerRound().getLivingCosts()) } <br/>
                Satisfaction increase: ${playerData.k(playerData.getPlayerRound().getSatisfactionCostPerPoint()) } <br/>
                Savings: ${playerData.k(playerData.getPlayerRound().getSavings()) } <br/>
                Debt: ${playerData.k(playerData.getPlayerRound().getDebt()) } <br />
              </div>
            </div>
            <br />
            <div style="display: flex; flex-direction: row; justify-content: flex-start; column-gap: 10px;">
              <div>
                Spendable income =
              </div>
              <div>
                  annual income <br/>
                  savings <br />
                  annual living costs <br />
                  <br />
              </div>
              <div>
                + ${playerData.k(playerData.getPlayerRound().getIncomePerRound()) } <br/>
                + ${playerData.k(playerData.getPlayerRound().getSavings()) } <br />
                - ${playerData.k(playerData.getPlayerRound().getLivingCosts()) } <br />
                = ${playerData.k(playerData.getPlayerRound().getSpendableIncome()) }
              </div>
            </div>
     */
    public static void makeBudgetAccordion(final PlayerData data)
    {
        StringBuilder s = new StringBuilder();
        // @formatter:off
        s.append("            <div style=\"display: flex; flex-direction: row; justify-content: space-between;\">\n");
        s.append("              <div>\n");
        s.append("                Annual income: " + data.k(data.getPlayerRound().getIncomePerRound().intValue()) + " <br/>\n");
        s.append("                Preferred house rating: " + data.getPlayerRound().getPreferredHouseRating() + " <br/>\n");
        s.append("                Max mortgage: " + data.k(data.getPlayerRound().getMaximumMortgage().intValue()) + " <br/>\n");
        s.append("                Current mortgage: " + data.k(data.getPlayerRound().getMortgage().intValue()) + " <br/>\n");
        s.append("              </div>\n");
        s.append("              <div>\n");
        s.append("                Annual living costs: " + data.k(data.getPlayerRound().getLivingCosts().intValue()) + " <br/>\n");
        s.append("                Satisfaction increase: " + data.k(data.getPlayerRound().getSatisfactionCostPerPoint().intValue()) + " <br/>\n");
        s.append("                Savings: " + data.k(data.getPlayerRound().getSavings().intValue()) + " <br/>\n");
        s.append("                Debt: " + data.k(data.getPlayerRound().getDebt().intValue()) + " <br />\n");
        s.append("              </div>\n");
        s.append("            </div>\n");
        s.append("            <br />\n");
        s.append("            <div style=\"display: flex; flex-direction: row; justify-content: flex-start; column-gap: 10px;\">\n");
        s.append("              <div>\n");
        s.append("                Spendable income =\n");
        s.append("              </div>\n");
        s.append("              <div>\n");
        s.append("                  round income <br/>\n");
        s.append("                  savings <br />\n");
        s.append("                  annual living costs <br />\n");
        if (PlayerState.valueOf(data.getPlayerRound().getPlayerState()).nr >= PlayerState.BOUGHT_HOUSE.nr)
            s.append("                  actual mortgage <br />\n");
        else
            s.append("                  expected mortgage <br />\n");
        if (PlayerState.valueOf(data.getPlayerRound().getPlayerState()).nr >= PlayerState.VIEW_TAXES.nr)
            s.append("                  actual taxes <br />\n");
        else
            s.append("                  expected taxes <br />\n");
        s.append("                  improvements <br />\n");
        s.append("                  house damage <br />\n");
        s.append("                  <br />\n");
        s.append("              </div>\n");
        s.append("              <div>\n");
        s.append("                + " + data.k(data.getPlayerRound().getIncomePerRound().intValue()) + " <br/>\n");
        s.append("                + " + data.k(data.getPlayerRound().getSavings().intValue()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getLivingCosts().intValue()) + " <br />\n");
        s.append("                - " + data.k(data.getExpectedMortgage()) + " <br />\n");
        s.append("                - " + data.k(data.getExpectedTaxes()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getCostMeasureBought().intValue()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getFluvialDamage().intValue()
                                             + data.getPlayerRound().getFluvialDamage().intValue()) + " <br />\n");
        s.append("                = " + data.k(data.getPlayerRound().getSpendableIncome()) + " \n");
        s.append("              </div>\n");
        s.append("            </div>\n");
        // @formatter:on
        data.getContentHtml().put("panel/budget", s.toString());
        System.out.println("SAVED BUDGET");
    }

    public static void makeNewsAccordion(final PlayerData data)
    {
        // get the news record(s) for the current round
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<NewsitemRecord> newsList =
                dslContext.selectFrom(Tables.NEWSITEM).where(Tables.NEWSITEM.ROUND_ID.eq(data.getRound().getId())).fetch();
        int nr = 1;
        for (NewsitemRecord news : newsList)
        {
            data.getContentHtml().put("news/name/" + nr, news.getName());
            data.getContentHtml().put("news/summary/" + nr, news.getSummary());
            data.getContentHtml().put("news/content/" + nr, news.getContent());
            nr++;
        }
    }
}
