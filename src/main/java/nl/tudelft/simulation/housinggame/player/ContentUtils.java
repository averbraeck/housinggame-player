package nl.tudelft.simulation.housinggame.player;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HouseRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.NewsitemRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.WelfaretypeRecord;

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
        WelfaretypeRecord welfareType =
                SqlUtils.readRecordFromId(data, Tables.WELFARETYPE, data.getPlayer().getWelfaretypeId());
        StringBuilder s = new StringBuilder();
        // @formatter:off
        s.append("            <div style=\"display: flex; flex-direction: row; justify-content: space-between;\">\n");
        s.append("              <div>\n");
        s.append("                Annual income: " + data.k(data.getPlayerRound().getRoundIncome()) + " <br/>\n");
        s.append("                Preferred house rating: " + data.getPlayerRound().getPreferredHouseRating() + " <br/>\n");
        s.append("                Max mortgage: " + data.k(data.getPlayerRound().getMaximumMortgage()) + " <br/>\n");
        s.append("                Current mortgage: " + data.k(data.getPlayerRound().getMortgageLeftEnd()) + " <br/>\n");
        s.append("              </div>\n");
        s.append("              <div>\n");
        s.append("                Annual living costs: " + data.k(data.getPlayerRound().getLivingCosts()) + " <br/>\n");
        s.append("                Satisfaction increase: " + data.k(welfareType.getSatisfactionCostPerPoint()) + " <br/>\n");
        s.append("                Savings: " + data.k(data.getPlayerRound().getStartSavings()) + " <br/>\n");
        s.append("                Debt: " + data.k(data.getPlayerRound().getStartDebt()) + " <br />\n");
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
        {
            s.append("                  actual mortgage <br />\n");
            s.append("                  actual taxes <br />\n");
        }
        else
        {
            s.append("                  expected mortgage <br />\n");
            s.append("                  expected taxes <br />\n");
        }
        s.append("                  improvements <br />\n");
        s.append("                  house damage <br />\n");
        s.append("                  <br />\n");
        s.append("              </div>\n");
        s.append("              <div>\n");
        s.append("                + " + data.k(data.getPlayerRound().getRoundIncome()) + " <br/>\n");
        s.append("                + " + data.k(data.getPlayerRound().getStartSavings()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getLivingCosts()) + " <br />\n");
        s.append("                - " + data.k(data.getExpectedMortgage()) + " <br />\n");
        s.append("                - " + data.k(data.getExpectedTaxes()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getCostMeasuresBought()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getFluvialDamage()
                                             + data.getPlayerRound().getFluvialDamage()) + " <br />\n");
        s.append("                = " + data.k(data.getPlayerRound().getCurrentSpendableIncome()) + " \n");
        s.append("              </div>\n");
        s.append("            </div>\n");
        // @formatter:on
        data.getContentHtml().put("panel/budget", s.toString());
    }

    public static void makeNewsAccordion(final PlayerData data)
    {
        // get the news record(s) for the current round
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<NewsitemRecord> newsList = dslContext.selectFrom(Tables.NEWSITEM)
                .where(Tables.NEWSITEM.ROUND_NUMBER.eq(data.getPlayerRoundNumber())).fetch();
        int nr = 1;
        for (NewsitemRecord news : newsList)
        {
            data.getContentHtml().put("news/name/" + nr, news.getName());
            data.getContentHtml().put("news/summary/" + nr, news.getSummary());
            data.getContentHtml().put("news/content/" + nr, news.getContent());
            nr++;
        }
    }

    public static boolean makeHousesAccordion(final PlayerData data)
    {
        // fill the options list

        /*-
           <option value="NONE"></option>
           <option value="D01">D01</option>
           <option value="N04">N04</option>
         */

        // loop through the houses that are valid for this round
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        try
        {
            Result<org.jooq.Record> resultList =
                    dslContext.fetch("SELECT house.id FROM house INNER JOIN community ON house.community_id=community.id "
                            + "WHERE community.gameversion_id=3;");
            SortedMap<String, HouseRecord> houseMap = new TreeMap<>();

            // fill the house names
            StringBuilder s = new StringBuilder();
            s.append("<option value=\"NONE\"></option>\n");
            for (org.jooq.Record record : resultList)
            {
                int id = Integer.valueOf(record.get(0).toString());
                HouseRecord house = SqlUtils.readRecordFromId(data, Tables.HOUSE, id);
                if (house.getAvailableRound() == data.getPlayerRoundNumber())
                {
                    houseMap.put(house.getCode(), house);
                    s.append("<option value=\"" + house.getCode() + "\">" + house.getCode() + "</option>\n");
                }
            }
            data.getContentHtml().put("house/options", s.toString());

            // fill the house details
            s = new StringBuilder();
            for (HouseRecord house : houseMap.values())
            {
                s.append("        <div class=\"house-details\" id=\"house-details-" + house.getCode()
                        + "\" style=\"display: none;\">\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">euro</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              Price: " + data.k(house.getPrice()) + "<br>Yearly Mortgage (payment per round): "
                        + data.k(house.getPrice() / 10) + "\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">star</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              House Rating: " + house.getRating()
                        + "<br>Your satisfaction will be affected by this\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">thunderstorm</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              Pluvial protection: ");
                if (data.getScenario().getInformationAmount() < 1)
                    s.append("?");
                else
                    s.append(house.getInitialPluvialProtection());
                s.append("<br>Amount of protection from rain flooding\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">houseboat</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              Fluvial protection: ");
                if (data.getScenario().getInformationAmount() < 1)
                    s.append("?");
                else
                    s.append(house.getInitialFluvialProtection());
                s.append("<br>Amount of protection from river flooding\n");
                s.append("            </div>\n");
                s.append("          </div>\n");

                s.append("<br />\n");
                s.append("Measures implemented: \n");
                s.append("- None\n"); // TODO: iterate over measures
                s.append("<br /><br />\n");

                if (data.getMaxMortgagePlusSavings() >= house.getPrice())
                    s.append("Great! Your available income is enough for this house.\n");
                else
                    s.append("Oops, you do not have enough available income for this house.\n");

                int phr = data.getPlayerRound().getPreferredHouseRating();
                int hr = house.getRating();
                if (hr == phr)
                    s.append("<br /><br />The rating of the house equals your preferred rating. "
                            + "You will not get extra satisfaction points.\n");
                else if (hr < phr)
                    s.append("<br /><br />The rating of the house is below your preferred rating. " + "You will lose: "
                            + "house rating (" + hr + ") - preferred rating (" + phr + ") = " + (hr - phr)
                            + " house satisfaction points.\n");
                else
                    s.append("<br /><br />The rating of the house is above your preferred rating. " + "You will gain: "
                            + "house rating (" + hr + ") - preferred rating (" + phr + ") = " + (hr - phr)
                            + " house satisfaction points.\n");
                s.append("<br /><br />If you found your preferred house, put your pawn on the map.\n");

                s.append("        </div>\n\n");
            }
            data.getContentHtml().put("house/details", s.toString());
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static void makeHouseConfirmationAccordion(final PlayerData data)
    {
        StringBuilder s = new StringBuilder();
        // @formatter:off
        s.append("            <div>\n");
        if (data.getHouse() == null)
        {
            s.append("You have not been allocated a house in this round or an earlier round.\n");
            s.append("Without a house, you cannot fully participate in the game, since you cannot \n");
            s.append("experience the effect of flooding, nor buy measures to improve your house.\n");
            s.append("Get a house allocation in the next round!\n");
        }
        else
        {
            s.append("You live in house " + data.getHouse().getCode() + "<br/>\n");
            if (data.getPlayerRound().getHousePriceBought() != null)
            {
                s.append("The house was bought in this round.<br/>\n");
                s.append("The price you paid was " +
                    data.k(data.getPlayerRound().getHousePriceBought()) + ".<br/>\n");
                s.append("The left mortgage is " +
                    data.k(data.getPlayerRound().getMortgageLeftEnd()) + ".<br/>\n");
                s.append("Your maximum mortgage is " +
                    data.k(data.getPlayerRound().getMaximumMortgage()) + ".<br/>\n");
                s.append("Savings used to buy the house are " +
                    data.k(data.getPlayerRound().getSpentSavingsForBuyingHouse()) + ".<br/>\n");
                s.append("Your preferred house rating is " +
                    data.getPlayerRound().getPreferredHouseRating() + ".<br/>\n");
                s.append("The rating of the house is " +
                    data.getHouse().getRating() + ".<br/>\n");
            }
            else
            {
                s.append("You did not change houses in this round.<br/>\n");
            }
        }
        s.append("            </div>\n");
        // @formatter:on
        data.getContentHtml().put("house/confirmation", s.toString());
    }
}
