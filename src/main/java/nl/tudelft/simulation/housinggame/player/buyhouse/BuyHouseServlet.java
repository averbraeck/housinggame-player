package nl.tudelft.simulation.housinggame.player.buyhouse;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.HouseGroupStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.readbudget.BudgetAccordion;
import nl.tudelft.simulation.housinggame.player.readnews.NewsAccordion;

@WebServlet("/buy-house")
public class BuyHouseServlet extends HttpServlet
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
        if (!makeHousePicklist(data, response))
            return;
        response.sendRedirect("jsp/player/buy-house.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    private static boolean makeHousePicklist(final PlayerData data, final HttpServletResponse response) throws IOException
    {
        // loop through the houses that are valid for this round and available
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
                }
            }
            data.getContentHtml().put("house/options", s.toString());
            return true;
        }
        catch (Exception e)
        {
            data.errorRedirect(response, "Error during building of buy-house options list: " + e.getMessage());
            return false;
        }
    }

}
