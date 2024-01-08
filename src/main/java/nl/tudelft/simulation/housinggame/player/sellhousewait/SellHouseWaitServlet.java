package nl.tudelft.simulation.housinggame.player.sellhousewait;

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

@WebServlet("/sell-house-wait")
public class SellHouseWaitServlet extends HttpServlet
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

        response.sendRedirect("jsp/player/sell-house-wait.jsp");
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
            s.append("<p>You have opted to sell your house " + houseGroup.getCode() + "<br/>\n");
            s.append("You bought the house for " + data.k(houseGroup.getLastSoldPrice()) + "<br/>\n");
            s.append("Its market value is currently " + data.k(houseGroup.getMarketValue()) + "<br/>\n");
            s.append("The entered sales price was " + data.k(transaction.getPrice()) + "<br/>\n");
            s.append("<p>\n");
            s.append("If the facilitator approves your selling request, you can continue.\n");
            s.append("Otherwise, you can go back to the sell house screen to select another option.\n");
            s.append("</p>\n");
        }
        s.append("            </div>\n");
        data.getContentHtml().put("panel/house", s.toString());
        data.getContentHtml().put("panel/house/title", "3. Your house choice");
    }

}
