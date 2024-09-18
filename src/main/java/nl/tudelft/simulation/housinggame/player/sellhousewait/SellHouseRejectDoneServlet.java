package nl.tudelft.simulation.housinggame.player.sellhousewait;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.TransactionStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

@WebServlet("/sell-house-reject-done")
public class SellHouseRejectDoneServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null || request.getParameter("nextScreen") == null)
        {
            System.err.println("data == null or nextScreen == null");
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        // the next screen button indicates the INTENTION of the player, not the screen it originates from.
        String nextScreen = request.getParameter("nextScreen");

        // player clicked REJECT SELL on the sell-house-wait screen
        if (nextScreen.equals("reject-sell"))
        {
            // reload the round with the latest state
            data.readDynamicData();

            PlayerroundRecord prr = data.getPlayerRound();
            Integer transactionId = prr.getActiveTransactionId();
            if (transactionId == null)
            {
                System.err.println("No transaction found");
            }
            else
            {
                HousetransactionRecord transaction = PlayerUtils.readRecordFromId(data, Tables.HOUSETRANSACTION, transactionId);
                if (transaction.getTransactionStatus().equals(TransactionStatus.REJECTED_SELL))
                {
                    prr.setActiveTransactionId(null);
                    data.newPlayerState(prr, PlayerState.VIEW_SELL_HOUSE, "Transaction=REJECTED_SELL");
                    response.sendRedirect("/housinggame-player/sell-house");
                    return;
                }
                else
                {
                    System.err.println(
                            "Player clicked reject-sell, but transaction status is " + transaction.getTransactionStatus());
                }
            }
        }

        // if the player did not click 'view house' and enters the read-news-done servlet, something is wrong
        System.err.println("Player app called sell-house-reject-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
