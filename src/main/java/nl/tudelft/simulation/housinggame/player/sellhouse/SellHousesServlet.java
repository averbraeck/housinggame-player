package nl.tudelft.simulation.housinggame.player.sellhouse;

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

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.MovingreasonRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.house.HouseAccordion;
import nl.tudelft.simulation.housinggame.player.readbudget.BudgetAccordion;
import nl.tudelft.simulation.housinggame.player.readnews.NewsAccordion;

@WebServlet("/sell-house")
public class SellHousesServlet extends HttpServlet
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
        HouseAccordion.makeHousePicklist(data, true);
        makeAffordableText(data);
        makeHouseValue(data);
        makeSellingReasons(data);
        response.sendRedirect("jsp/player/sell-house.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    public static void makeAffordableText(final PlayerData data)
    {
        StringBuilder s = new StringBuilder();
        var houseGroup = data.getHouseGroup();
        var prr = data.getPlayerRound();
        int estSellingProfit = houseGroup.getMarketValue().intValue() - prr.getMortgageLeftStart();
        int spendableOnHouse = prr.getMaximumMortgage() + prr.getSpendableIncome() + estSellingProfit;
        s.append("              You cannot select a house whose price is higher than the maximum mortgage ("
                + data.k(prr.getMaximumMortgage()) + ")\n");
        if (prr.getSpendableIncome() >= 0)
            s.append("              + your savings (" + data.k(prr.getSpendableIncome()) + ")\n");
        else
            s.append("              - your debt (" + data.k(-prr.getSpendableIncome()) + ")\n");
        if (estSellingProfit >= 0)
            s.append("              + profit when selling your house (" + data.k(estSellingProfit) + " est.)\n");
        else
            s.append("              - loss when selling your house (" + data.k(-estSellingProfit) + " est.)\n");
        s.append("              = " + data.k(spendableOnHouse) + "\n");
        data.putContentHtml("house/affordable", s.toString());
    }

    public static void makeHouseValue(final PlayerData data)
    {
        var houseGroup = data.getHouseGroup();
        data.putContentHtml("house/house-value", String.valueOf(houseGroup.getMarketValue().intValue() / 1000));
    }

    public static void makeSellingReasons(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        StringBuilder s = new StringBuilder();
        List<MovingreasonRecord> reasonList = dslContext.selectFrom(Tables.MOVINGREASON)
                .where(Tables.MOVINGREASON.GAMEVERSION_ID.eq(data.getGameVersion().getId())).fetch()
                .sortAsc(Tables.MOVINGREASON.SEQUENCE_NUMBER);
        s.append("<option value=\"NONE\" class=\"reason-none\"></option>\n");
        for (var reason : reasonList)
        {
            String classString = reason.getIsOther().intValue() == 0 ? "reason-value" : "reason-other";
            s.append("<option value=\"" + reason.getId() + "\" class=\"" + classString + "\">" + reason.getReasonText()
                    + "</option>\n");
        }
        data.putContentHtml("house/selling-reasons", s.toString());
    }

}
