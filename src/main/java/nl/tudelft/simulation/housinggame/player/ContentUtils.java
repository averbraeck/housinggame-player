package nl.tudelft.simulation.housinggame.player;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.HouseGroupStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasureRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasuretypeRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerRecord;
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

    public static void makeHousePicklist(final PlayerData data, final boolean sell)
    {
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
                    if (HouseGroupStatus.isAvailable(houseGroup.getStatus()))
                    {
                        houseGroupMap.put(houseGroup.getCode(), houseGroup);
                        s.append("<option value=\"" + houseGroup.getCode() + "\">" + houseGroup.getCode() + ", rating: "
                                + houseGroup.getRating() + ", value: " + data.k(houseGroup.getMarketValue()) + "</option>\n");
                    }
                    else if (sell)
                    {
                        houseGroupMap.put(houseGroup.getCode(), houseGroup);
                        PlayerRecord player = SqlUtils.readRecordFromId(data, Tables.PLAYER, houseGroup.getOwnerId());
                        s.append("<option value=\"" + houseGroup.getCode() + "\">" + houseGroup.getCode() + ", rating: "
                                + houseGroup.getRating() + ", value: " + data.k(houseGroup.getMarketValue()) + " [owned by "
                                + player.getCode() + "]</option>\n");
                    }

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
                int mortgage = Math.min(data.getPlayerRound().getMaximumMortgage(), houseGroup.getMarketValue());
                s.append("        <div class=\"house-details\" id=\"house-details-" + houseGroup.getCode()
                        + "\" style=\"display: none;\">\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">euro</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              Price: " + data.k(houseGroup.getMarketValue())
                        + "<br>Mortgage payment per round will be: " + data.k(mortgage * data.getMortgagePercentage() / 100)
                        + "\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">star</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              House Rating: " + houseGroup.getRating() + "<br>Your preferred house rating: "
                        + data.getPlayerRound().getPreferredHouseRating() + "\n");
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

                s.append("<br /><p>\n");
                s.append("Measures implemented:<br/> \n");
                s.append("- None\n"); // TODO: iterate over measures
                s.append("<br /></p>\n");

                if (data.getMaxMortgagePlusSavings() >= houseGroup.getMarketValue())
                {
                    s.append("<p class=\"hg-box-green\">\n");
                    s.append("Great! Your maximum mortgage plus savings are enough to buy this house.\n");
                    s.append("</p>\n");
                }
                else
                {
                    s.append("<p class=\"hg-box-red\">\n");
                    s.append("Oops, your maximum mortgage plus savings are NOT enough to afford this house.\n");
                    s.append("</p>\n");
                }

                int phr = data.getPlayerRound().getPreferredHouseRating();
                int hr = houseGroup.getRating();
                if (hr == phr)
                    s.append("<p class=\"hg-box-yellow\">The rating of the house equals your preferred rating. "
                            + "You will not get extra satisfaction points.</p>\n");
                else if (hr < phr)
                    s.append("<p class=\"hg-box-red\">The rating of the house is below your preferred rating. "
                            + "You will lose: " + "house rating (" + hr + ") - preferred rating (" + phr + ") = " + (hr - phr)
                            + " house satisfaction points.</p>\n");
                else
                    s.append("<p class=\"hg-box-green\">The rating of the house is above your preferred rating. "
                            + "You will gain: " + "house rating (" + hr + ") - preferred rating (" + phr + ") = " + (hr - phr)
                            + " house satisfaction points.</p>\n");
                s.append("<p>If you found your preferred house, put your pawn on the map.</p>\n");

                s.append("        </div>\n\n");
            }
            data.getContentHtml().put("house/details", s.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void makeBuyHouseAccordion(final PlayerData data)
    {
        makeHousePicklist(data, false);
    }

    public static void makeHouseWaitConfirmationAccordion(final PlayerData data)
    {
        PlayerroundRecord playerRound = data.getPlayerRound();
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        if (playerRound.getActiveTransactionId() == null)
        {
            s.append("<p class=\"hg-box-red\">\n");
            s.append("You have not been allocated a house in this round or an earlier round.\n");
            s.append("Without a house, you cannot fully participate in the game, since you cannot \n");
            s.append("experience the effect of flooding, nor buy measures to improve your house.\n");
            s.append("Get a house allocation in the next round!\n");
            s.append("</div>\n");
        }
        else
        {
            HousetransactionRecord transaction =
                    SqlUtils.readRecordFromId(data, Tables.HOUSETRANSACTION, playerRound.getActiveTransactionId());
            HousegroupRecord houseGroup = SqlUtils.readRecordFromId(data, Tables.HOUSEGROUP, transaction.getHousegroupId());
            s.append("<p>You have opted for house " + houseGroup.getCode() + "<br/>\n");
            s.append("The price you plan to pay is " + data.k(transaction.getPrice()) + ".<br/>\n");
            s.append("Your maximum mortgage is " + data.k(data.getPlayerRound().getMaximumMortgage()) + ".<br/>\n");
            int mortgage = Math.min(transaction.getPrice(), data.getPlayerRound().getMaximumMortgage());
            s.append("The mortgage for this house is " + data.k(mortgage) + ".<br/>\n");
            int savingsUsed = Math.max(transaction.getPrice() - data.getPlayerRound().getMaximumMortgage(), 0);
            savingsUsed = Math.min(savingsUsed, playerRound.getSpendableIncome());
            s.append("Savings used to buy the house are " + data.k(savingsUsed) + ".<br/>\n");
            s.append("Mortgage payment per round will be  " + data.k(mortgage * data.getMortgagePercentage() / 100)
                    + ".<br/></p>\n");

            if (data.getMaxMortgagePlusSavings() >= houseGroup.getMarketValue())
            {
                s.append("<p class=\"hg-box-green\">\n");
                s.append("Great! Your maximum mortgage plus savings are enough to buy this house.\n");
                s.append("</p>\n");
            }
            else
            {
                s.append("<p class=\"hg-box-red\">\n");
                s.append("Actually, you do not have enough available income for this house,\n");
                s.append("but the facilitator can grant an exception if no cheaper houses are available.\n");
                s.append("You will go into debt as a result.\n");
                s.append("</p>\n");
            }

            int phr = data.getPlayerRound().getPreferredHouseRating();
            int hr = houseGroup.getRating();
            if (hr == phr)
                s.append("<p class=\"hg-box-yellow\">The rating of the house equals your preferred rating. "
                        + "You will not get extra satisfaction points.</p>\n");
            else if (hr < phr)
                s.append("<p class=\"hg-box-red\">The rating of the house is below your preferred rating. " + "You will lose: "
                        + "house rating (" + hr + ") - preferred rating (" + phr + ") = " + (hr - phr)
                        + " house satisfaction points.</p>\n");
            else
                s.append("<p class=\"hg-box-green\">The rating of the house is above your preferred rating. "
                        + "You will gain: " + "house rating (" + hr + ") - preferred rating (" + phr + ") = " + (hr - phr)
                        + " house satisfaction points.</p>\n");

            s.append("<p>\n");
            s.append("If the facilitator approves your buying request, you can move in.\n");
            s.append("Otherwise, you will go back to the view house screen to select another house.\n");
            s.append("</p>\n");
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

    public static void makeSellHouseAccordion(final PlayerData data)
    {
        makeHousePicklist(data, true);

        // extra information to sell the house (price, reason for selling)
    }

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
