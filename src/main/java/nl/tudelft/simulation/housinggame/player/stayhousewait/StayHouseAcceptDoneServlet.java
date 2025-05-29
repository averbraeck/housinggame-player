package nl.tudelft.simulation.housinggame.player.stayhousewait;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.TransactionStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

@WebServlet("/stay-house-accept-done")
public class StayHouseAcceptDoneServlet extends HttpServlet
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

        // player clicked MOVE IN on the stay-house-wait screen
        if (nextScreen.equals("accept-stay"))
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
                if (transaction.getTransactionStatus().equals(TransactionStatus.APPROVED_STAY))
                {
                    prr.setActiveTransactionId(null);
                    data.newPlayerState(prr, PlayerState.STAYED_HOUSE, "");
                    response.sendRedirect("/housinggame-player/stayed-house");
                    return;
                }
                else
                {
                    System.err.println(
                            "Player clicked accept-stay, but transaction status is " + transaction.getTransactionStatus());
                }
            }
        }

        // if the player did not click 'view house' and enters the read-news-done servlet, something is wrong
        System.err.println("Player app called stay-house-accept-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
