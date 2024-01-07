package nl.tudelft.simulation.housinggame.player.buyhouse;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.gson.JsonObject;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasureRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

@WebServlet("/check-buy-house")
public class CheckBuyHouseServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        // reload the round with the latest state
        if (!data.readDynamicData())
        {
            data.errorNoRedirect(response, "No house data provided for check-buy-house servlet");
            return;
        }

        String houseGroupIdStr = request.getParameter("houseGroupId");
        if (houseGroupIdStr == null)
        {
            data.errorNoRedirect(response, "No house data provided for check-buy-house servlet");
            return;
        }

        if (houseGroupIdStr.contains("NONE"))
        {
            JsonObject json = new JsonObject();
            json.addProperty("housePriceInput", "  <div id=\"house-price-input-div\"></div>\n");
            json.addProperty("houseBuyFeedback", "      <div id=\"house-buy-feedback-div\"></div>\n");
            json.addProperty("housePrice", "");
            json.addProperty("error", "");
            response.setContentType("text/plain");
            response.getWriter().write(json.toString());
            return;
        }

        int houseGroupId;
        try
        {
            houseGroupId = Integer.valueOf(houseGroupIdStr);
        }
        catch (Exception e)
        {
            data.errorNoRedirect(response, "Could not translate houseGroupId " + houseGroupIdStr + " into a number");
            return;
        }

        HousegroupRecord houseGroup = SqlUtils.readRecordFromId(data, Tables.HOUSEGROUP, houseGroupId);

        if (houseGroup == null)
        {
            data.errorNoRedirect(response, "Could not locate house with id " + houseGroupId);
            return;
        }

        String housePriceInput = makeHousePriceInput(houseGroup);
        String houseBuyFeedback = makeHouseBuyFeedback(data, houseGroup);

        JsonObject json = new JsonObject();
        json.addProperty("housePriceInput", housePriceInput);
        json.addProperty("houseBuyFeedback", houseBuyFeedback);
        json.addProperty("housePrice", String.valueOf(houseGroup.getMarketValue() / 1000));
        json.addProperty("error", "");

        response.setContentType("text/plain");
        response.getWriter().write(json.toString());
    }

    private String makeHousePriceInput(final HousegroupRecord houseGroup)
    {
        StringBuilder s = new StringBuilder();
        String priceInputId = "\"house-price-input\"";
        String houseValue = "\"" + (houseGroup.getMarketValue() / 1000) + "\"";
        s.append("  <div id=\"house-price-input-div\">\n");
        s.append("  <label for=" + priceInputId + " id=" + "\"house-price-label\""
                + " class=\"house-price-label\">House price (in k)*</label>\n");
        s.append("  <input type=\"number\" id=" + priceInputId + " name=" + priceInputId + " value=" + houseValue + " class="
                + priceInputId + " oninput=\"changePrice();\">\n");
        s.append("</div>\n");
        return s.toString();
    }

    private String makeHouseBuyFeedback(final PlayerData data, final HousegroupRecord houseGroup)
    {
        StringBuilder s = new StringBuilder();
        int mortgage = Math.min(data.getPlayerRound().getMaximumMortgage(), houseGroup.getMarketValue());

        s.append("      <div id=\"house-buy-feedback-div\">\n");
        s.append("        <div class=\"house-details\" id=\"house-details\">\n");
        s.append("          <div class=\"hg-house-row\">\n");
        s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">euro</i></div>\n");
        s.append("            <div class=\"hg-house-text\">\n");
        s.append("              Price: " + data.k(houseGroup.getMarketValue()) + "<br>Mortgage payment per round will be: "
                + data.k(mortgage * data.getMortgagePercentage() / 100) + "\n");
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
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<MeasureRecord> measureList =
                dslContext.selectFrom(Tables.MEASURE).where(Tables.MEASURE.HOUSEGROUP_ID.eq(houseGroup.getId())).fetch();
        int count = 0;
        for (var measure : measureList)
        {
            if (measure.getConsumedInRound() == null || measure.getConsumedInRound().intValue() == 0)
            {
                var measureType = SqlUtils.readRecordFromId(data, Tables.MEASURETYPE, measure.getMeasuretypeId());
                s.append(" - " + measureType.getShortAlias() + ", costs: " + data.k(measureType.getPrice()) + ", satisfaction: "
                        + measureType.getSatisfactionDelta() + "<br/>\n");
                count++;
            }
        }
        if (count == 0)
            s.append(" - None\n");
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
            s.append("<p class=\"hg-box-red\">The rating of the house is below your preferred rating. " + "You will lose: "
                    + "house rating (" + hr + ") - preferred rating (" + phr + ") = " + (hr - phr)
                    + " house satisfaction points.</p>\n");
        else
            s.append("<p class=\"hg-box-green\">The rating of the house is above your preferred rating. " + "You will gain: "
                    + "house rating (" + hr + ") - preferred rating (" + phr + ") = " + (hr - phr)
                    + " house satisfaction points.</p>\n");
        s.append("<p>If you found your preferred house, put your pawn on the map.</p>\n");

        s.append("        </div>\n");
        s.append("      </div>\n");
        return s.toString();
    }
}
