<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Survey</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>

    <h1 style="text-align: center; color: blue;">Answer survey questions</h1>
    
    <div class="panel-group pmd-accordion" id="hg-accordion" role="tablist" aria-multiselectable="true" > 
      <jsp:include page="accordion1.jsp"></jsp:include>
      <jsp:include page="accordion2.jsp"></jsp:include>
      <jsp:include page="accordion3.jsp"></jsp:include>
      <jsp:include page="accordion4.jsp"></jsp:include>
      <jsp:include page="accordion5.jsp">
        <jsp:param name="open" value="in" />
      </jsp:include>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading6">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse6" aria-expanded="false" 
              aria-controls="collapse6" data-expandable="false">
              6. Answer survey
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse6" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="heading6">
          <div class="panel-body">
            <p>
              ${playerData.getContentHtml("house/survey") }
            </p>
          </div>
        </div>
      </div>

    </div>

    
   <form action="/housinggame-player/answer-survey-done" method="post">
      <div class="hg-button">
        <input type="hidden" name="nextScreen" value="view-damage" />
        <input type="submit" value='STORE SURVEY' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
    
  </div>
  
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'answer-survey'},
        function(data, status) {
          if (data == "OK") {
            $("#hg-submit").removeAttr("disabled");
          } else {
            setTimeout(check, 5000);
          }
        });
    }
  </script>
  
</body>
</html>
