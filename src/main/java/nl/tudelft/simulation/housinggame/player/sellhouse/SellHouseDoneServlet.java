package nl.tudelft.simulation.housinggame.player.sellhouse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.TransactionStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;

@WebServlet("/sell-house-done")
public class SellHouseDoneServlet extends HttpServlet
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
        System.out.println("nextScreen = " + nextScreen);

        if (nextScreen.equals("stay-house-wait"))
        {
            // player decided to stay in their current house
            makeStayHouseTransaction(data);
            data.getPlayerRound().setMovingreasonId(null);
            data.getPlayerRound().setMovingReasonOther("");
            data.getPlayerRound().setPlayerState(PlayerState.STAY_HOUSE_WAIT.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/stay-house-wait");
            return;
        }

        else if (nextScreen.equals("sell-house-wait"))
        {
            // handle the house selling by making a transaction
            String priceStr = request.getParameter("sell-price");
            String reasonStr = request.getParameter("sell-reason");
            String otherStr = request.getParameter("reason-other");
            if (!makeSellHouseTransaction(data, priceStr))
            {
                response.sendRedirect("/housinggame-player/sell-house");
                return;
            }
            data.getPlayerRound().setMovingreasonId(Integer.valueOf(reasonStr));
            data.getPlayerRound().setMovingReasonOther(otherStr);
            data.getPlayerRound().setPlayerState(PlayerState.SELL_HOUSE_WAIT.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/sell-house-wait");
            return;
        }

        // if the player did not click 'sell house' or 'stay' and enters the sell-house-done servlet, something is wrong
        System.err.println("Player app called sell-house-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    private static boolean makeSellHouseTransaction(final PlayerData data, final String priceStr)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        int price;
        try
        {
            price = 1000 * Integer.valueOf(priceStr);
        }
        catch (Exception e)
        {
            System.err.println("Could not translate house price " + priceStr + " into a number");
            return false;
        }

        // make HouseTransaction record
        HousetransactionRecord transaction = dslContext.newRecord(Tables.HOUSETRANSACTION);
        transaction.setPrice(price);
        transaction.setComment("");
        transaction.setTransactionStatus(TransactionStatus.UNAPPROVED_SELL);
        transaction.setHousegroupId(data.getHouseGroup().getId());
        transaction.setPlayerroundId(data.getPlayerRound().getId());
        transaction.setGrouproundId(data.getGroupRound().getId());
        transaction.store();

        PlayerroundRecord playerRound = data.getPlayerRound();
        playerRound.setActiveTransactionId(transaction.getId());
        playerRound.store();

        return true;
    }

    private static boolean makeStayHouseTransaction(final PlayerData data)
    {
        // make HouseTransaction record
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        HousetransactionRecord transaction = dslContext.newRecord(Tables.HOUSETRANSACTION);
        transaction.setPrice(0);
        transaction.setComment("");
        transaction.setTransactionStatus(TransactionStatus.UNAPPROVED_STAY);
        transaction.setHousegroupId(data.getHouseGroup().getId());
        transaction.setPlayerroundId(data.getPlayerRound().getId());
        transaction.setGrouproundId(data.getGroupRound().getId());
        transaction.store();

        PlayerroundRecord playerRound = data.getPlayerRound();
        playerRound.setActiveTransactionId(transaction.getId());
        playerRound.store();

        return true;
    }

}
