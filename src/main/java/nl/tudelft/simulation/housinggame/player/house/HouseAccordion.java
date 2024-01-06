package nl.tudelft.simulation.housinggame.player.house;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.HouseGroupStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

/**
 * BuyHouseAccordion.java.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class HouseAccordion
{

    public static void makeHousePicklist(final PlayerData data, final boolean includeOccupied)
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
                        s.append("<option value=\"" + houseGroup.getId() + "\">" + houseGroup.getCode() + ", rating: "
                                + houseGroup.getRating() + ", value: " + data.k(houseGroup.getMarketValue()) + "</option>\n");
                    }
                    else if (includeOccupied)
                    {
                        houseGroupMap.put(houseGroup.getCode(), houseGroup);
                        PlayerRecord player = SqlUtils.readRecordFromId(data, Tables.PLAYER, houseGroup.getOwnerId());
                        s.append("<option value=\"" + houseGroup.getId() + "\">" + houseGroup.getCode() + ", rating: "
                                + houseGroup.getRating() + ", value: " + data.k(houseGroup.getMarketValue()) + " [owned by "
                                + player.getCode() + "]</option>\n");
                    }

                }
            }
            data.getContentHtml().put("house/options", s.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Fill the prices list. <pre> &lt;label for="house-price"&gt;House price (in k)*&lt;/label&gt; &lt;input type="number"
     * id="house-price" name="house-price"&gt; </pre>
     */
    public static void makeHousePriceList(final PlayerData data, final boolean includeOccupied)
    {
        // loop through the houses that are valid for this round
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        try
        {
            List<HousegroupRecord> houseGroupList = dslContext.selectFrom(Tables.HOUSEGROUP)
                    .where(Tables.HOUSEGROUP.GROUP_ID.eq(data.getGroup().getId())).fetch();

            // fill the house names
            StringBuilder s = new StringBuilder();
            s.append("<option value=\"NONE\"></option>\n");
            for (var houseGroup : houseGroupList)
            {
                if (HouseGroupStatus.isAvailable(houseGroup.getStatus()) || includeOccupied)
                {
                    String priceLabelId = "\"house-price-label-" + houseGroup.getCode() + "\"";
                    String priceInputId = "\"house-price-input-" + houseGroup.getCode() + "\"";
                    String houseValue = "\"" + (houseGroup.getMarketValue() / 1000) + "\"";
                    s.append("<label for=" + priceInputId + " id=" + priceLabelId
                            + " class=\"house-price-label\" style=\"display: none;\">House price (in k)*</label>\n");
                    s.append("<input type=\"number\" id=" + priceInputId + " name=" + priceInputId + " value=" + houseValue
                            + " class=\"house-price-input\" style=\"display: none;\">\n");
                }
            }
            data.putContentHtml("house/prices", s.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
     * fill the house details
     */
    public static void makeHouseDetails(final PlayerData data, final boolean includeOccupied)
    {
        // loop through the houses that are valid for this round
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        try
        {
            List<HousegroupRecord> houseGroupList = dslContext.selectFrom(Tables.HOUSEGROUP)
                    .where(Tables.HOUSEGROUP.GROUP_ID.eq(data.getGroup().getId())).fetch();

            // fill the house names
            StringBuilder s = new StringBuilder();
            for (var houseGroup : houseGroupList)
            {
                if (HouseGroupStatus.isAvailable(houseGroup.getStatus()) || includeOccupied)
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
                    s.append(
                            "            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">thunderstorm</i></div>\n");
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
                    s.append(
                            "            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">houseboat</i></div>\n");
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
                                + "You will lose: " + "house rating (" + hr + ") - preferred rating (" + phr + ") = "
                                + (hr - phr) + " house satisfaction points.</p>\n");
                    else
                        s.append("<p class=\"hg-box-green\">The rating of the house is above your preferred rating. "
                                + "You will gain: " + "house rating (" + hr + ") - preferred rating (" + phr + ") = "
                                + (hr - phr) + " house satisfaction points.</p>\n");
                    s.append("<p>If you found your preferred house, put your pawn on the map.</p>\n");

                    s.append("        </div>\n\n");
                }
                data.getContentHtml().put("house/details", s.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
            s.append("The left mortgage is " + data.k(data.getPlayerRound().getMortgageLeftEnd()) + ".<br/>\n");
            s.append("Your maximum mortgage is " + data.k(data.getPlayerRound().getMaximumMortgage()) + ".<br/>\n");
        }
        s.append("            </div>\n");
        data.getContentHtml().put("panel/house", s.toString());
        data.getContentHtml().put("panel/house/title", "3. Your house confirmation");
    }

}
