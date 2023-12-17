<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Budget Overview</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>

    <div class="hg-title">
      Let's start round ${playerData.getPlayerRoundNumber() }
    </div>
    
    <div class="panel-group pmd-accordion" id="hg-accordion" role="tablist" aria-multiselectable="true" >
       <jsp:include page="accordion1.jsp">
         <jsp:param name="open" value="in" />
       </jsp:include>
    </div>
    
    <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="read-news" />
        <input type="submit" value="READ NEWS" class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
    
  </div>
  
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'read-budget'},
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
