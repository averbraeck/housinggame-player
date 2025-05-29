package nl.tudelft.simulation.housinggame.player.answersurvey;

import java.io.IOException;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nl.tudelft.simulation.housinggame.common.GroupState;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.QuestionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.QuestionscoreRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;

@WebServlet("/answer-survey-done")
public class AnswerSurveyDoneServlet extends HttpServlet
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

        try
        {
            // the next screen button indicates the INTENTION of the player, not the screen it originates from.
            String nextScreen = request.getParameter("nextScreen");

            // player clicked STORE SURVEY on the answer-survey screen
            if (nextScreen.equals("survey-completed"))
            {
                // reload the round with the latest state
                data.readDynamicData();
                PlayerroundRecord prr = data.getPlayerRound();

                DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
                List<QuestionRecord> questionList =
                        dslContext.selectFrom(Tables.QUESTION).where(Tables.QUESTION.SCENARIO_ID.eq(data.getScenario().getId()))
                                .fetch().sortAsc(Tables.QUESTION.QUESTION_NUMBER);
                for (var question : questionList)
                {
                    String answer = request.getParameter("id-" + question.getId());
                    if (answer == null)
                    {
                        data.errorRedirect(response,
                                "Player app answer-survey-done servlet, but could not retrieve parameter: id-"
                                        + question.getId());
                        return;
                    }
                    QuestionscoreRecord qsr = dslContext.newRecord(Tables.QUESTIONSCORE);
                    qsr.setAnswer(answer);
                    qsr.setPlayerroundId(prr.getId());
                    qsr.setQuestionId(question.getId());
                    if (data.getHighestGroupRoundNumber() > data.getPlayerRoundNumber()
                            || GroupState.valueOf(data.getGroupRound().getGroupState()).ge(GroupState.SURVEY_COMPLETED))
                        qsr.setLateAnswer((byte) 1);
                    else
                        qsr.setLateAnswer((byte) 0);

                    switch (question.getType())
                    {
                        case "SELECT":
                        {
                            // TODO: check input for validity
                            qsr.store();
                            break;
                        }
                        case "STRING":
                        {
                            // TODO: check input for validity
                            qsr.store();
                            break;
                        }
                        case "INTEGER":
                        {
                            // TODO: check input for validity
                            qsr.store();
                            break;
                        }
                        case "TEXT":
                        {
                            // TODO: check input for validity
                            qsr.store();
                            break;
                        }
                        default:
                        {
                            data.errorRedirect(response, "Unexpected value in survey question: " + question.getType());
                            return;
                        }
                    }
                }

                data.newPlayerState(prr, PlayerState.SURVEY_COMPLETED, "");
                response.sendRedirect("/housinggame-player/survey-completed");
                return;
            }

            // if the player did not click 'VIEW DAMAGE' and enters the survey-completed-done servlet, something is wrong
            System.err.println("Player app called survey-completed-done servlet, but NextScreen button is " + nextScreen);
            response.sendRedirect("/housinggame-player/login");

        }
        catch (Exception e)
        {
            data.errorRedirect(response,
                    "Player app called view-improvements-done servlet, but error occurred: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
