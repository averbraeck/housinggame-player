package nl.tudelft.simulation.housinggame.player.viewdamage;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nl.tudelft.simulation.housinggame.common.CalcHouseGroup;
import nl.tudelft.simulation.housinggame.common.CumulativeNewsEffects;
import nl.tudelft.simulation.housinggame.common.SqlUtils;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.house.HouseAccordion;
import nl.tudelft.simulation.housinggame.player.readbudget.BudgetAccordion;
import nl.tudelft.simulation.housinggame.player.readnews.NewsAccordion;
import nl.tudelft.simulation.housinggame.player.viewimprovements.ImprovementsAccordion;
import nl.tudelft.simulation.housinggame.player.viewtaxes.TaxAccordion;

@WebServlet("/view-damage")
public class ViewDamageServlet extends HttpServlet
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

        // (re)calculate damage for this player and this player's house.
        var cumulativeNewsEffects = CumulativeNewsEffects.readCumulativeNewsEffects(data.getDataSource(), data.getScenario(),
                data.getPlayerRoundNumber());
        var groupRound = SqlUtils.readRecordFromId(data, Tables.GROUPROUND, data.getPlayerRound().getGrouproundId());
        CalcHouseGroup.calcFloodHousePlayer(data, data.getHouseGroup(), data.getPlayerRoundNumber(), cumulativeNewsEffects,
                groupRound.getPluvialFloodIntensity(), groupRound.getFluvialFloodIntensity());

        data.getContentHtml().clear();
        BudgetAccordion.makeBudgetAccordion(data);
        NewsAccordion.makeNewsAccordion(data);
        HouseAccordion.makeHouseConfirmationAccordion(data);
        TaxAccordion.makeTaxAccordion(data);
        ImprovementsAccordion.makeBoughtImprovementsAccordion(data);
        DamageAccordion.makeDamageAccordion(data);

        response.sendRedirect("jsp/player/view-damage.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
