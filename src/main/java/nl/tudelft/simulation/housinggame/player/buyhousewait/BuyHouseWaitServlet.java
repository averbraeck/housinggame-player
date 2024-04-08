package nl.tudelft.simulation.housinggame.player.buyhousewait;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;
import nl.tudelft.simulation.housinggame.player.readbudget.BudgetAccordion;
import nl.tudelft.simulation.housinggame.player.readnews.NewsAccordion;

@WebServlet("/buy-house-wait")
public class BuyHouseWaitServlet extends HttpServlet
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

        data.getContentHtml().clear();
        BudgetAccordion.makeBudgetAccordion(data);
        NewsAccordion.makeNewsAccordion(data);
        makeHouseWaitConfirmationAccordion(data);

        response.sendRedirect("jsp/player/buy-house-wait.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    private static void makeHouseWaitConfirmationAccordion(final PlayerData data)
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
            s.append("<div class=\"hg-header1\">Your mortgage</div>\n");
            s.append("<p>You opted for house " + houseGroup.getCode() + "<br/>\n");
            s.append("The price you plan to pay is " + data.k(transaction.getPrice()) + ".<br/>\n");
            s.append("Your maximum mortgage is " + data.k(data.getPlayerRound().getMaximumMortgage()) + ".<br/>\n");
            int mortgage = Math.min(transaction.getPrice(), data.getPlayerRound().getMaximumMortgage());
            s.append("The mortgage for this house is " + data.k(mortgage) + ".<br/>\n");
            int savingsUsed = Math.max(transaction.getPrice() - data.getPlayerRound().getMaximumMortgage(), 0);
            savingsUsed = Math.min(savingsUsed, playerRound.getSpendableIncome());
            s.append("Savings used to buy the house are " + data.k(savingsUsed) + ".<br/>\n");

            s.append("<div class=\"hg-header1\">Mortgage payment</div>\n");
            s.append("Mortgage payment per round will be  " + data.k(mortgage * data.getMortgagePercentage() / 100)
                    + ".<br/></p>\n");

            s.append("<div class=\"hg-header1\">Choice implications</div>\n");
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
        data.getContentHtml().put("panel/house", s.toString());
        data.getContentHtml().put("panel/house/title", "3. Your house choice");
    }

}
