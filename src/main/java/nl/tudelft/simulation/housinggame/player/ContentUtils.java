package nl.tudelft.simulation.housinggame.player;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.HouseGroupStatus;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasuretypeRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.NewsitemRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.QuestionRecord;
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
        int startSavings = Math.max(data.getPrevPlayerRound().getSpendableIncome(), 0);
        int startDebt = -Math.min(data.getPrevPlayerRound().getSpendableIncome(), 0);
        StringBuilder s = new StringBuilder();
        // @formatter:off
        s.append("            <div class=\"hg-header1\">Your mortgage</div>\n");
        s.append("            <div style=\"background-color:#fafaf0;\">\n");
        s.append("              Maximum mortgage: " + data.k(data.getPlayerRound().getMaximumMortgage()) + " <br/>\n");
        s.append("              Current mortgage: " + data.k(data.getPlayerRound().getMortgageLeftEnd()) + " <br/>\n");
        s.append("            </div>\n");

        s.append("            <div class=\"hg-header1\">House expectations</div>\n");
        s.append("            <div style=\"background-color:#fafaf0;\">\n");
        s.append("              Preferred house rating: " + data.getPlayerRound().getPreferredHouseRating() + " <br/>\n");
        s.append("            </div>\n");

        s.append("            <div class=\"hg-header1\">Satisfaction costs</div>\n");
        s.append("            <div style=\"background-color:#fafaf0;\">\n");
        s.append("              Satisfaction increase per point: " + data.k(welfareType.getSatisfactionCostPerPoint()) + " <br/>\n");
        s.append("            </div>\n");

        s.append("            <div class=\"hg-header1\">Spendable income</div>\n");
        s.append("            <div style=\"display: flex; flex-direction: row; justify-content: flex-start; column-gap: 10px; " +
                "background-color:#fafaf0;\">\n");
        s.append("              <div>\n");
        s.append("                  Start savings / debt <br/>\n");
        s.append("                  Round income <br/>\n");
        s.append("                  Round living costs <br />\n");
        s.append("                  Profit sold house <br />\n");
        s.append("                  Spent savings to buy house <br />\n");
        if (PlayerState.valueOf(data.getPlayerRound().getPlayerState()).nr >= PlayerState.BOUGHT_HOUSE.nr)
        {
            s.append("                  Actual mortgage payment <br />\n");
            s.append("                  Actual taxes <br />\n");
        }
        else
        {
            s.append("                  Expected mortgage payment<br />\n");
            s.append("                  Expected taxes <br />\n");
        }
        s.append("                  Personal improvements <br />\n");
        s.append("                  House improvements <br />\n");
        s.append("                  House damage <br />\n");
        s.append("                  <br />\n");
        s.append("              </div>\n");

        s.append("              <div>\n");
        if (startSavings > 0)
            s.append("                + " + data.k(startSavings) + " <br/>\n");
        else if (startDebt > 0)
            s.append("                - " + data.k(startDebt) + " <br/>\n");
        else
            s.append("                +0 <br/>\n");
        s.append("                + " + data.k(data.getPlayerRound().getRoundIncome()) + " <br/>\n");
        s.append("                - " + data.k(data.getPlayerRound().getLivingCosts()) + " <br />\n");
        s.append("                + " + data.k(data.getPlayerRound().getProfitSoldHouse()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getSpentSavingsForBuyingHouse()) + " <br />\n");
        if (PlayerState.valueOf(data.getPlayerRound().getPlayerState()).nr >= PlayerState.BOUGHT_HOUSE.nr)
        {
            s.append("                - " + data.k(data.getPlayerRound().getMortgagePayment()) + " <br />\n");
            s.append("                - " + data.k(data.getPlayerRound().getCostTaxes()) + " <br />\n");
        }
        else
        {
            s.append("                - " + data.k(data.getExpectedMortgage()) + " <br />\n");
            s.append("                - " + data.k(data.getExpectedTaxes()) + " <br />\n");
        }
        s.append("                - " + data.k(data.getPlayerRound().getCostSatisfactionBought()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getCostMeasuresBought()) + " <br />\n");
        s.append("                - " + data.k(data.getPlayerRound().getCostPluvialDamage()
                                             + data.getPlayerRound().getCostFluvialDamage()) + " <br />\n");
        s.append("                = " + data.k(data.getPlayerRound().getSpendableIncome()) + " \n");
        s.append("              </div>\n");
        s.append("            </div>\n");

        s.append("            <div class=\"hg-header1\">Satisfaction points</div>\n");
        s.append("            <div style=\"display: flex; flex-direction: row; justify-content: flex-start; column-gap: 10px; "
                + "background-color:#fafaf0;\">\n");
        s.append("              <div>\n");
        s.append("                  Personal satisfaction<br/>\n");
        s.append("                  House satisfaction<br />\n");
        s.append("                  Penalties<br />\n");
        s.append("                  <br />\n");
        s.append("              </div>\n");
        s.append("              <div>\n");
        int psat = data.getPlayerRound().getPersonalSatisfaction();
        int hsat = data.getHouseSatisfaction();
        int penalty = data.getPlayerRound().getSatisfactionFluvialPenalty() + data.getPlayerRound().getSatisfactionPluvialPenalty();
        s.append("                + " + (psat + penalty) + " <br/>\n");
        s.append("                + " + hsat + " <br />\n");
        s.append("                - " + penalty + " <br />\n");
        s.append("                = " + (psat + hsat) + " \n");
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

    public static boolean makeBuyHouseAccordion(final PlayerData data)
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
            List<HousegroupRecord> houseGroupList = dslContext.selectFrom(Tables.HOUSEGROUP)
                    .where(Tables.HOUSEGROUP.GROUP_ID.eq(data.getGroup().getId())).fetch();
            SortedMap<String, HousegroupRecord> houseGroupMap = new TreeMap<>();

            // fill the house names
            StringBuilder s = new StringBuilder();
            s.append("<option value=\"NONE\"></option>\n");
            for (var houseGroup : houseGroupList)
            {
                if (HouseGroupStatus.isAvailableOrOccupied(houseGroup.getStatus()))
                {
                    houseGroupMap.put(houseGroup.getCode(), houseGroup);
                    s.append("<option value=\"" + houseGroup.getCode() + "\">" + houseGroup.getCode() + "</option>\n");
                }
            }
            data.getContentHtml().put("house/options", s.toString());

            // Fill the prices list
            // <label for="house-price">House price (in k)*</label>
            // <input type="number" id="house-price" name="house-price">

            s = new StringBuilder();
            for (HousegroupRecord houseGroup : houseGroupMap.values())
            {
                String priceLabelId = "\"house-price-label-" + houseGroup.getCode() + "\"";
                String priceInputId = "\"house-price-input-" + houseGroup.getCode() + "\"";
                String houseValue = "\"" + (houseGroup.getMarketValue() / 1000) + "\"";
                s.append("<label for=" + priceInputId + " id=" + priceLabelId
                        + " class=\"house-price-label\" style=\"display: none;\">House price (in k)*</label>\n");
                s.append("<input type=\"number\" id=" + priceInputId + " name=" + priceInputId + " value=" + houseValue
                        + " class=\"house-price-input\" style=\"display: none;\">\n");
            }
            data.putContentHtml("house/prices", s.toString());

            // fill the house details
            s = new StringBuilder();
            for (HousegroupRecord houseGroup : houseGroupMap.values())
            {
                s.append("        <div class=\"house-details\" id=\"house-details-" + houseGroup.getCode()
                        + "\" style=\"display: none;\">\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">euro</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              Price: " + data.k(houseGroup.getMarketValue())
                        + "<br>Yearly Mortgage (payment per round): "
                        + data.k(houseGroup.getMarketValue() * data.getMortgagePercentage() / 100) + "\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">star</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              House Rating: " + houseGroup.getRating()
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
                    s.append(houseGroup.getPluvialBaseProtection() + houseGroup.getPluvialHouseProtection());
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
                    s.append(houseGroup.getFluvialBaseProtection() + houseGroup.getFluvialHouseProtection());
                s.append("<br>Amount of protection from river flooding\n");
                s.append("            </div>\n");
                s.append("          </div>\n");

                s.append("<br />\n");
                s.append("Measures implemented:<br/> \n");
                s.append("- None\n"); // TODO: iterate over measures
                s.append("<br /><br />\n");

                if (data.getMaxMortgagePlusSavings() >= houseGroup.getMarketValue())
                    s.append("Great! Your available income is enough for this house.\n");
                else
                    s.append("Oops, you do not have enough available income for this house.\n");

                int phr = data.getPlayerRound().getPreferredHouseRating();
                int hr = houseGroup.getRating();
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

    public static void makeHouseWaitConfirmationAccordion(final PlayerData data)
    {
        PlayerroundRecord playerRound = data.getPlayerRound();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        if (playerRound.getActiveTransactionId() == null)
        {
            s.append("You have not been allocated a house in this round or an earlier round.\n");
            s.append("Without a house, you cannot fully participate in the game, since you cannot \n");
            s.append("experience the effect of flooding, nor buy measures to improve your house.\n");
            s.append("Get a house allocation in the next round!\n");
        }
        else
        {
            HousetransactionRecord transaction =
                    SqlUtils.readRecordFromId(data, Tables.HOUSETRANSACTION, playerRound.getActiveTransactionId());
            HousegroupRecord houseGroup = SqlUtils.readRecordFromId(data, Tables.HOUSEGROUP, transaction.getHousegroupId());
            s.append("You have opted for house " + houseGroup.getCode() + "<br/>\n");
            s.append("The price you plan to pay is " + data.k(transaction.getPrice()) + ".<br/>\n");
            s.append("Your maximum mortgage is " + data.k(data.getPlayerRound().getMaximumMortgage()) + ".<br/>\n");
            int savingsUsed = Math.max(transaction.getPrice() - data.getPlayerRound().getMaximumMortgage(), 0);
            s.append("Savings used to buy the house are " + data.k(savingsUsed) + ".<br/>\n");
            if (data.getMaxMortgagePlusSavings() >= transaction.getPrice())
            {
                s.append("Your maximum mortgage and savings are enough for this house.\n");
            }
            else
            {
                s.append("Actually, you do not have enough available income for this house,\n");
                s.append("but the facilitator can grant an exceptio if no cheaper houses are available.\n");
            }

            int phr = data.getPlayerRound().getPreferredHouseRating();
            int hr = houseGroup.getRating();
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
        }
        s.append("            </div>\n");
        data.getContentHtml().put("house/wait-confirmation", s.toString());
    }

    public static void makeHouseConfirmationAccordion(final PlayerData data)
    {
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("You live in house " + data.getHouse().getCode() + "<br/>\n");
        if (data.getPlayerRound().getHousePriceBought() != null)
        {
            s.append("You have bought a house in this round.<br/>\n");
            s.append("The price you paid is " + data.k(data.getPlayerRound().getHousePriceBought()) + ".<br/>\n");
            s.append("The left mortgage is " + data.k(data.getPlayerRound().getMortgageLeftEnd()) + ".<br/>\n");
            s.append("Your maximum mortgage is " + data.k(data.getPlayerRound().getMaximumMortgage()) + ".<br/>\n");
            s.append("Savings used to buy the house are " + data.k(data.getPlayerRound().getSpentSavingsForBuyingHouse())
                    + ".<br/>\n");
            s.append("Your preferred house rating is " + data.getPlayerRound().getPreferredHouseRating() + ".<br/>\n");
            s.append("The rating of the house is " + data.getHouse().getRating() + ".<br/>\n");
            s.append("Satisfaction change: " + (data.getHouse().getRating() - data.getPlayerRound().getPreferredHouseRating())
                    + " points.<br/>\n");
        }
        else
        {
            s.append("You did not change houses in this round.<br/>\n");
        }
        s.append("            </div>\n");
        data.getContentHtml().put("house/confirmation", s.toString());
    }

    public static void makeImprovementsAccordion(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<MeasuretypeRecord> measureTypeList = dslContext.selectFrom(Tables.MEASURETYPE)
                .where(Tables.MEASURETYPE.GAMEVERSION_ID.eq(data.getGameVersion().getId())).fetch();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("<p>Please select your improvements:</p>\n");
        for (MeasuretypeRecord measureType : measureTypeList)
        {
            s.append("[ ] " + measureType.getShortAlias() + ", costs: " + data.k(measureType.getPrice()) + "<br/>\n");
        }
        s.append("<br/><p>Please select if you want to buy extra satisfaction:</p>\n");
        WelfaretypeRecord wft = SqlUtils.readRecordFromId(data, Tables.WELFARETYPE, data.getPlayer().getWelfaretypeId());
        s.append("[ ] " + " costs per point: " + data.k(wft.getSatisfactionCostPerPoint()) + "<br/>\n");
        s.append("            </div>\n");
        data.getContentHtml().put("house/improvements", s.toString());
    }

    public static void makeBoughtImprovementsAccordion(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<MeasuretypeRecord> measureTypeList = dslContext.selectFrom(Tables.MEASURETYPE)
                .where(Tables.MEASURETYPE.GAMEVERSION_ID.eq(data.getGameVersion().getId())).fetch();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("<p>You bought the following improvements:</p>\n");
        for (MeasuretypeRecord measureType : measureTypeList)
        {
            s.append("[ ] " + measureType.getShortAlias() + ", costs: " + data.k(measureType.getPrice()) + "<br/>\n");
        }
        s.append("<br/><p>You bought extra satisfaction:</p>\n");
        WelfaretypeRecord wft = SqlUtils.readRecordFromId(data, Tables.WELFARETYPE, data.getPlayer().getWelfaretypeId());
        s.append("[ ] " + " costs per point: " + data.k(wft.getSatisfactionCostPerPoint()) + "<br/>\n");
        s.append("            </div>\n");
        data.getContentHtml().put("house/bought-improvements", s.toString());
    }

    public static void makeSurveyAccordion(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<QuestionRecord> questionList = dslContext.selectFrom(Tables.QUESTION)
                .where(Tables.QUESTION.SCENARIO_ID.eq(data.getScenario().getId())).fetch();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("<p>Please answer the following questions:</p>\n");
        for (QuestionRecord question : questionList)
        {
            s.append("Question " + question.getQuestionNumber() + ".<br/>" + question.getDescription() + "<br/>\n");
            s.append("<br/>\n");
        }
        s.append("<br/>\n");
        s.append("            </div>\n");
        data.getContentHtml().put("house/survey", s.toString());
    }

}
