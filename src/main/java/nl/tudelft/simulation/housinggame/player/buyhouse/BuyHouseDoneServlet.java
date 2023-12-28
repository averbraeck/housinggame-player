package nl.tudelft.simulation.housinggame.player.buyhouse;

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
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

@WebServlet("/buy-house-done")
public class BuyHouseDoneServlet extends HttpServlet
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

        // player decided which house to buy with BUY HOUSE and has entered the price on the buy-house screen
        if (nextScreen.equals("buy-house-wait"))
        {
            // handle the entered buy-house data: Parameter house[e.g., "24" = N07], Parameter price[e.g., 105]
            String houseGroupIdStr = request.getParameter("house");
            String priceStr = request.getParameter("price");
            if (!makeHouseTransaction(data, houseGroupIdStr, priceStr))
            {
                response.sendRedirect("/housinggame-player/buy-house");
                return;
            }
            data.getPlayerRound().setPlayerState(PlayerState.BUY_HOUSE_WAIT.toString());
            data.getPlayerRound().store();
            response.sendRedirect("/housinggame-player/buy-house-wait");
            return;
        }

        // if the player did not click 'view house' and enters the read-news-done servlet, something is wrong
        System.err.println("Player app called buy-house-done servlet, but NextScreen button is " + nextScreen);
        response.sendRedirect("/housinggame-player/login");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    private static boolean makeHouseTransaction(final PlayerData data, final String houseGroupIdStr, final String priceStr)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);

        if (houseGroupIdStr.contains("NONE"))
        {
            System.err.println("No house was selected");
            return false;
        }

        int houseGroupId;
        try
        {
            houseGroupId = Integer.valueOf(houseGroupIdStr);
        }
        catch (Exception e)
        {
            System.err.println("Could not translate houseGroupId " + houseGroupIdStr + " into a number");
            return false;
        }

        HousegroupRecord houseGroup = SqlUtils.readRecordFromId(data, Tables.HOUSEGROUP, houseGroupId);

        if (houseGroup == null)
        {
            System.err.println("Could not locate house with id " + houseGroupId);
            return false;
        }

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
        transaction.setTransactionStatus(TransactionStatus.UNAPPROVED_BUY);
        transaction.setHousegroupId(houseGroup.getId());
        transaction.setPlayerroundId(data.getPlayerRound().getId());
        transaction.setGrouproundId(data.getGroupRound().getId());
        transaction.store();

        PlayerroundRecord playerRound = data.getPlayerRound();
        playerRound.setActiveTransactionId(transaction.getId());
        playerRound.store();

        return true;
    }

}
